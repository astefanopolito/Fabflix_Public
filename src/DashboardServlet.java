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
@WebServlet(name = "DashboardServlet", urlPatterns = "/api/dashboard")
public class DashboardServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;


    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbMaster");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http GET
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");    // Response mime type
        JsonObject returnObj = new JsonObject();

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement()){

            String rType = request.getParameter("type");

            if (rType.equals("create_star")) {
                String star_name = request.getParameter("name");
                String yearString = request.getParameter("year");
                Integer year = null;
                if (!yearString.equals("")) {
                    year = Integer.parseInt(yearString);
                }

                if (star_name.equals("")) {
                    returnObj.addProperty("status", "failure");
                    returnObj.addProperty("message", "Star Name Required");
                    out.write(returnObj.toString());
                    out.close();
                    statement.close();
                    return;
                }

                String idQuery = "SELECT max(id) FROM stars";

                try (ResultSet resId = statement.executeQuery(idQuery)) {
                    //getting max int
                    Integer maxId = 0;
                    while (resId.next()) {
                        maxId = Integer.parseInt(resId.getString("max(id)").substring(2));
                    }
                    maxId++;
                    String newId = "nm" + maxId;

                    String starUpdate = "INSERT INTO stars VALUES (?, ?, ?)";

                    try (PreparedStatement prepStatement = conn.prepareStatement(starUpdate)) {
                        prepStatement.setString(1, newId);
                        prepStatement.setString(2, star_name);
                        prepStatement.setObject(3, year);

                        prepStatement.executeUpdate();

                        returnObj.addProperty("status", "success");
                        returnObj.addProperty("message", "successfully added star " + newId);
                        out.write(returnObj.toString());
                    }
                }
            }
            else {
                String title = request.getParameter("title");
                String yearString = request.getParameter("year");
                String director = request.getParameter("director");
                String star_name = request.getParameter("star_name");
                String genre = request.getParameter("genre");

                if (title.equals("") || yearString.equals("") ||
                        director.equals("") || star_name.equals("") || genre.equals("")) {
                    returnObj.addProperty("status", "failure");
                    returnObj.addProperty("message", "Missing Parameters");
                    out.write(returnObj.toString());
                    out.close();
                    return;
                }

                Integer year = Integer.parseInt(yearString);

                String callQuery = "{CALL add_movie(?, ?, ?, ?, ?)}";
                try (CallableStatement callable = conn.prepareCall(callQuery)) {
                    callable.setString(1, title);
                    callable.setObject(2, year);
                    callable.setString(3, director);
                    callable.setString(4, star_name);
                    callable.setString(5, genre);

                    try (ResultSet rs = callable.executeQuery()) {
                        while (rs.next()) {
                            returnObj.addProperty("status", "success");
                            returnObj.addProperty("message", rs.getString("message"));
                        }
                        out.write(returnObj.toString());
                    }
                }
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