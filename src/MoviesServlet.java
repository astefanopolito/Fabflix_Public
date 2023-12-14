import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        PrintWriter out = response.getWriter();

        request.getServletContext().log("Attempting connection with JDBC");

        try (Connection conn = dataSource.getConnection()) {
            //VALIDATE DATA

            Map<String, String[]> newParams = request.getParameterMap();

            HttpSession session = request.getSession();

            Integer pageNum = (Integer)session.getAttribute("pageNum");

            if (pageNum == null) {
                pageNum = 1;
            }

            if (newParams.containsKey("sort")) {
                session.setAttribute("sort", newParams.get("sort")[0]);
            }

            if (newParams.containsKey("length")) {
                session.setAttribute("length", newParams.get("length")[0]);
            }

            if (newParams.containsKey("sort_type")) {
                session.setAttribute("sort_type", newParams.get("sort_type")[0]);
            }

            if (newParams.containsKey("switch")) {
                if (newParams.get("switch")[0].equals("prev")) {
                    if (pageNum != 1) {
                        pageNum -= 1;
                    }
                }
                else {
                    pageNum += 1;
                }
                session.setAttribute("pageNum", pageNum);
            }

            if (!newParams.containsKey("search_type") && !newParams.containsKey("genre_id") && !newParams.containsKey("title_start")) {
                Map<String, String[]> prevParams = (Map<String, String[]>)session.getAttribute("prevParams");
                if (prevParams == null) {
                    session.setAttribute("prevParams", newParams);
                }
                else {
                    newParams = prevParams;
                }
            }
            else {
                session.setAttribute("prevParams", newParams);
            }

            String fulltext = newParams.getOrDefault("fulltext", new String[] {""})[0];
            String fulltextQuery = "";
            String[] fulltextTokens = null;
            if (!fulltext.equals("")) {
                fulltextTokens = fulltext.split(" ");
                if (fulltextTokens != null) {
                    for (int i = 0; i < fulltextTokens.length; i++) {
                        fulltextTokens[i] = "+" + fulltextTokens[i] + "*";
                    }
                }
                fulltextQuery = String.join(" ", fulltextTokens);
            }

            String search_type = newParams.getOrDefault("search_type", new String[] {""})[0];
            String title = newParams.getOrDefault("title", new String[] {""})[0];
            String year = newParams.getOrDefault("year", new String[] {""})[0];
            String director = newParams.getOrDefault("director", new String[] {""})[0];
            String star_name = newParams.getOrDefault("star_name", new String[] {""})[0];
            String genre_id = newParams.getOrDefault("genre_id", new String[] {""})[0];
            String title_start = newParams.getOrDefault("title_start", new String[] {""})[0];
            String sort_type = (String)session.getAttribute("sort_type");
            String sort = (String)session.getAttribute("sort");
            String length = (String)session.getAttribute("length");

            String query;
            boolean search = false;
            boolean genreSearch = false;
            boolean titleSearch = false;


            if (genre_id != "") {
                genreSearch = true;
                query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
                        "FROM movies AS m " +
                        "JOIN ratings AS r ON m.id = r.movieId " +
                        "JOIN genres_in_movies AS gm ON gm.movieId = m.id " +
                        "JOIN genres AS g ON g.id = gm.genreId " +
                        "WHERE g.id = ?";
            }
            else if (title_start != ""){
                titleSearch = true;
                query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
                        "FROM movies AS m " +
                        "JOIN ratings AS r ON m.id = r.movieId ";
                if (title_start.equals("*")) {
                    query += "WHERE m.title REGEXP '^[^a-zA-Z0-9]'";
                }
                else {
                    query += "WHERE m.title LIKE ?";
                }
            }
            else if (!search_type.equals("")){
                search = true;
                if (search_type.equals("advanced")) {
                    query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
                            "FROM movies AS m " +
                            "JOIN ratings AS r ON m.id = r.movieId " +
                            "JOIN stars_in_movies AS sm on sm.movieId = m.id  " +
                            "JOIN stars AS s on s.id = sm.starId  " +
                            "WHERE m.title LIKE ? " +
                            "AND m.director LIKE ? " +
                            "AND s.name LIKE ? ";

                }
                else {
                    query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
                            "FROM movies AS m " +
                            "JOIN ratings AS r ON m.id = r.movieId " +
                            "JOIN stars_in_movies AS sm on sm.movieId = m.id  " +
                            "JOIN stars AS s on s.id = sm.starId  " +
                            "WHERE MATCH(m.title) AGAINST (? IN BOOLEAN MODE) ";

                }

                if (!(year.equals(""))) {
                    query += " AND m.year = ?";
                }


            }
            else {
                query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
                        "FROM movies AS m " +
                        "JOIN ratings AS r ON m.id = r.movieId " +
                        "JOIN stars_in_movies AS sm on sm.movieId = m.id  " +
                        "JOIN stars AS s on s.id = sm.starId  ";

            }

            Map<String, String> sortMap = Map.of("m.title", "r.rating", "r.rating", "m.title");
            if (sort != null) {
                if (sort_type == null) {
                    sort_type = "asc";
                }
                query += " ORDER BY " + sort + " " + sort_type + ", " + sortMap.get(sort);
            }

            if (length == null) {
                length = "20";
            }
            query += " LIMIT " + length + " OFFSET " + ((pageNum - 1) * Integer.parseInt(length));

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                int index = 1;

                if (search) {
                    if (search_type.equals("advanced")) {
                        statement.setString(index++, "%" + title + "%");
                        statement.setString(index++, "%" + director + "%");
                        statement.setString(index++, "%" + star_name + "%");
                        if (!(year.equals(""))) {
                            statement.setString(index++, year);
                        }

                    }
                    else {
                        statement.setString(index, fulltextQuery);
                    }

                }
                else if (genreSearch) {
                    statement.setString(index++, genre_id);
                }
                else if (titleSearch) {
                    statement.setString(index++, title_start + "%");
                }


                request.getServletContext().log("Executing initial query");
                try (ResultSet rs = statement.executeQuery()) {
                    // Perform the query

                    String genresQuery = "SELECT g.name, g.id FROM genres as g " +
                            "JOIN genres_in_movies AS gm ON g.id = gm.genreId " +
                            "WHERE gm.movieId = ? " +
                            "ORDER BY g.name ASC " +
                            "LIMIT 3";


                    String starsQuery = "SELECT s.id, s.name FROM stars_in_movies AS sm " +
                            "JOIN stars AS s ON s.id = sm.starId " +
                            "WHERE sm.starId IN (" +
                            "SELECT s.id FROM stars AS s " +
                            "JOIN stars_in_movies AS sm ON sm.starId = s.id " +
                            "WHERE sm.movieId = ? " +
                            "ORDER BY s.name" +
                            ") " +
                            "GROUP BY sm.starId " +
                            "ORDER BY COUNT(sm.movieId) DESC " +
                            "LIMIT 3";

                    try (PreparedStatement genresStatement = conn.prepareStatement(genresQuery);
                        PreparedStatement starsStatement = conn.prepareStatement(starsQuery)) {


                        JsonArray jsonArray = new JsonArray();

                        // Iterate through each row of rs
                        while (rs.next()) {
                            String movie_id = rs.getString("id");
                            String movie_title = rs.getString("title");
                            String movie_year = rs.getString("year");
                            String movie_director = rs.getString("director");
                            String movie_rating = rs.getString("rating");

                            //Query for genres
                            genresStatement.setString(1, movie_id);
                            starsStatement.setString(1, movie_id);

                            request.getServletContext().log("Executing genre and star queries");

                            try (ResultSet genresRs = genresStatement.executeQuery();
                                ResultSet starsRs = starsStatement.executeQuery()) {
                                request.getServletContext().log("Executed");
                                JsonArray movie_genres = new JsonArray();
                                while (genresRs.next()) {
                                    JsonObject newObject = new JsonObject();
                                    newObject.addProperty("genre_id", genresRs.getString("g.id"));
                                    newObject.addProperty("genre_name", genresRs.getString("g.name"));
                                    movie_genres.add(newObject);
                                }

                                //Stars query

                                JsonArray movie_stars = new JsonArray();
                                while (starsRs.next()) {
                                    JsonObject newObject = new JsonObject();
                                    newObject.addProperty("star_id", starsRs.getString("s.id"));
                                    newObject.addProperty("star_name", starsRs.getString("s.name"));
                                    movie_stars.add(newObject);
                                }

                                // Create a JsonObject based on the data we retrieve from rs
                                JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("movie_title", movie_title);
                                jsonObject.addProperty("movie_id", movie_id);
                                jsonObject.addProperty("movie_year", movie_year);
                                jsonObject.addProperty("movie_director", movie_director);
                                jsonObject.addProperty("movie_rating", movie_rating);
                                jsonObject.add("movie_genres", movie_genres);
                                jsonObject.add("movie_stars", movie_stars);


                                jsonArray.add(jsonObject);

                            }
                        }
                        // Log to localhost log
                        request.getServletContext().log("getting " + jsonArray.size() + " results");

                        // Write JSON string to output
                        out.write(jsonArray.toString());
                        // Set response status to 200 (OK)
                        response.setStatus(200);
                    }
                }
            }
        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            System.out.println(e);

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
