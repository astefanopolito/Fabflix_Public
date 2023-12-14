import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/api/employeelogin")
public class EmployeeLoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        PrintWriter out = response.getWriter();

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            JsonObject failCaptchaObject = new JsonObject();
            failCaptchaObject.addProperty("status", "fail");
            request.getServletContext().log("Login failed");
            failCaptchaObject.addProperty("message", "reCaptcha Test Failure");

            out.write(failCaptchaObject.toString());
            return;
        }


        try (Connection conn = dataSource.getConnection()) {
            JsonObject responseJsonObject = new JsonObject();

            String authQuery = "SELECT * FROM employees WHERE employees.email=?";

            try (PreparedStatement authStatement = conn.prepareStatement(authQuery)) {
                authStatement.setString(1, username);
                try (ResultSet rs = authStatement.executeQuery()) {
                    boolean uPass = false;
                    boolean pPass = false;

                    if (rs.next()) {
                        uPass = true;
                        String realPassword = rs.getString("password");
                        Integer id = rs.getInt("id");

                        // set this user into the session
                        if (new StrongPasswordEncryptor().checkPassword(password, realPassword)) {
                            pPass = true;
                            request.getSession().setAttribute("user", new User(id, username));
                            request.getSession().setAttribute("employee", new User(id, username));

                            responseJsonObject.addProperty("status", "success");
                            responseJsonObject.addProperty("message", "success");
                        }

                    }
                    if (!uPass || !pPass) {
                        responseJsonObject.addProperty("status", "fail");
                        request.getServletContext().log("Login failed");
                        if (!uPass) {
                            responseJsonObject.addProperty("message", "employee " + username + " doesn't exist");
                        }
                        else {
                            responseJsonObject.addProperty("message", "incorrect password");
                        }
                    }
                    out.write(responseJsonObject.toString());
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

    }
}