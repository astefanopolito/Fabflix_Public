import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet("/hero-suggestion")
public class HeroSuggestion extends HttpServlet {
    /*
     * populate the Super hero hash map.
     * Key is hero ID. Value is hero name.
     */

    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()){
            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String query = request.getParameter("query");

            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            String titleQuery = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE) LIMIT 10";

            PreparedStatement statement = conn.prepareStatement(titleQuery);

            String fulltextQuery = "";
            String[] fulltextTokens = null;
            if (!query.equals("")) {
                fulltextTokens = query.split(" ");
                if (fulltextTokens != null) {
                    for (int i = 0; i < fulltextTokens.length; i++) {
                        fulltextTokens[i] = "+" + fulltextTokens[i] + "*";
                    }
                }
                fulltextQuery = String.join(" ", fulltextTokens);
            }

            statement.setString(1, fulltextQuery);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                jsonArray.add(generateJsonObject(movieId, movieTitle));
            }

            // search on superheroes and add the results to JSON Array
            // this example only does a substring match
            // TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars

            out.write(jsonArray.toString());
        } catch (Exception e) {
            System.out.println(e);
            response.sendError(500, e.getMessage());
        }
        finally {
            out.close();
        }
    }

    /*
     * Generate the JSON Object from hero to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "heroID": 11 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieId, String title) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", title);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movieId);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }


}