import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource
            String ratingsQuery = "SELECT m.id, m.title, m.year, m.director, r.rating FROM movies AS m " +
                    "JOIN ratings AS r ON r.movieId = m.id " +
                    "WHERE m.id = ?";

            // Construct a query with parameter represented by "?"
            String genresQuery = "SELECT g.name, g.id FROM genres AS g " +
                    "JOIN genres_in_movies AS gm ON g.id = gm.genreId " +
                    "JOIN movies AS m ON m.id = gm.movieId " +
                    "WHERE m.id = ? " +
                    "ORDER BY g.name ASC";

            String starsQuery = "SELECT s.id, s.name FROM stars_in_movies AS sm " +
                    "JOIN stars AS s ON s.id = sm.starId " +
                    "WHERE sm.starId IN (" +
                    "SELECT s.id FROM stars AS s " +
                    "JOIN stars_in_movies AS sm ON sm.starId = s.id " +
                    "WHERE sm.movieId = ? " +
                    "ORDER BY s.name" +
                    ") " +
                    "GROUP BY sm.starId " +
                    "ORDER BY COUNT(sm.movieId) DESC";

            // Declare our statement
            try (PreparedStatement ratingsStatement = conn.prepareStatement(ratingsQuery);
            PreparedStatement genresStatement = conn.prepareStatement(genresQuery);
            PreparedStatement starsStatement = conn.prepareStatement(starsQuery)) {
                // Set the parameter represented by "?" in the query to the id we get from url,
                // num 1 indicates the first "?" in the query
                genresStatement.setString(1, id);
                starsStatement.setString(1, id);
                ratingsStatement.setString(1, id);

                // Perform the query
                try (ResultSet genresRs = genresStatement.executeQuery();
                ResultSet starsRs = starsStatement.executeQuery();
                ResultSet ratingsRs = ratingsStatement.executeQuery()) {

                    JsonArray jsonArray = new JsonArray();

                    JsonArray movie_genres =  new JsonArray();
                    while (genresRs.next()) {
                        JsonObject newObject = new JsonObject();
                        newObject.addProperty("genre_name", genresRs.getString("g.name"));
                        newObject.addProperty("genre_id", genresRs.getString("g.id"));
                        movie_genres.add(newObject);
                    }

                    JsonArray movie_stars =  new JsonArray();
                    while (starsRs.next()) {
                        JsonObject newObject = new JsonObject();
                        newObject.addProperty("star_name", starsRs.getString("s.name"));
                        newObject.addProperty("star_id", starsRs.getString("s.id"));
                        movie_stars.add(newObject);
                    }

                    while (ratingsRs.next()) {

                        String movie_title = ratingsRs.getString("m.title");
                        String movie_year = ratingsRs.getString("m.year");
                        String movie_director = ratingsRs.getString("m.director");
                        String movie_rating = ratingsRs.getString("r.rating");


                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("movie_title", movie_title);
                        jsonObject.addProperty("movie_year", movie_year);
                        jsonObject.addProperty("movie_director", movie_director);
                        jsonObject.add("movie_genres", movie_genres);
                        jsonObject.add("movie_stars", movie_stars);
                        jsonObject.addProperty("movie_rating", movie_rating);

                        jsonArray.add(jsonObject);
                    }
                    // Write JSON string to output
                    out.write(jsonArray.toString());
                    // Set response status to 200 (OK)
                    response.setStatus(200);
                }
            }


        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
