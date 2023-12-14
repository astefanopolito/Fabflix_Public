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
import java.sql.*;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

// Declaring a WebServlet called FormServlet, which maps to url "/form"
@WebServlet(name = "DashdataServlet", urlPatterns = "/api/dashdata")
public class DashdataServlet extends HttpServlet {

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
        JsonArray returnArray = new JsonArray();

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement()){

            String metaQuery = "show tables";

            try (ResultSet rs = statement.executeQuery(metaQuery)) {
                while (rs.next()) {
                    try (Statement innerStatement = conn.createStatement()) {
                        JsonObject newObj = new JsonObject();
                        JsonArray contents = new JsonArray();

                        newObj.addProperty("table", rs.getString("Tables_in_moviedb"));

                        String newQuery = "describe " + rs.getString("Tables_in_moviedb");

                        try (ResultSet data = innerStatement.executeQuery(newQuery)) {
                            while (data.next()) {
                                JsonObject contentObj = new JsonObject();
                                String Field = data.getString("Field");
                                String Type = data.getString("Type");
                                contentObj.addProperty("field", Field);
                                contentObj.addProperty("type", Type);
                                contents.add(contentObj);
                            }
                            newObj.add("contents", contents);
                            returnArray.add(newObj);
                        }
                    }
                }
                out.write(returnArray.toString());
            }


        } catch (Exception e) {
            /*
             * After you deploy the WAR file through tomcat manager webpage,
             *   there's no console to see the print messages.
             * Tomcat append all the print messages to the file: tomcat_directory/logs/catalina.out
             *
             * To view the last n lines (for example, 100 lines) of messages you can use:
             *   tail -100 catalina.out
             * This can help you debug your program after deploying it on AWS.
             */
            request.getServletContext().log("Error: ", e);

            // Output Error Massage to html
            out.write(new JsonObject().toString());
            return;
        }
        out.close();
    }
}