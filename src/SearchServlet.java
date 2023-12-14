import javax.naming.InitialContext;
        import javax.naming.NamingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
        import jakarta.servlet.annotation.WebServlet;
        import jakarta.servlet.http.HttpServlet;
        import jakarta.servlet.http.HttpServletRequest;
        import jakarta.servlet.http.HttpServletResponse;
        import javax.sql.DataSource;
import javax.xml.transform.Result;
import java.io.IOException;
        import java.io.PrintWriter;
        import java.sql.Connection;
        import java.sql.ResultSet;
        import java.sql.Statement;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;


    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");    // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection dbCon = dataSource.getConnection()){
            JsonObject returnObject = new JsonObject();

            try (Statement statement = dbCon.createStatement()) {

                String query = "SELECT * FROM genres " +
                        "ORDER BY genres.name ASC";

                try (ResultSet rs = statement.executeQuery(query)) {
                    JsonArray newArray = new JsonArray();

                    while (rs.next()) {
                        JsonObject newObject = new JsonObject();
                        newObject.addProperty("genre_id", rs.getString("genres.id"));
                        newObject.addProperty("genre_name", rs.getString("genres.name"));
                        newArray.add(newObject);
                    }

                    statement.close();
                    dbCon.close();

                    returnObject.addProperty("alphanumerics", "*0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
                    returnObject.add("genres", newArray);

                    out.write(returnObject.toString());
                }
            }
        } catch (Exception e) {
            request.getServletContext().log("Error: ", e);
            out.write(new JsonObject().toString());
            return;
        } finally {
            out.close();
        }
    }
}