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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

// Declaring a WebServlet called SessionServlet, which maps to url "/session"
@WebServlet(name = "EditCartServlet", urlPatterns = "/api/editcart")
public class EditCartServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String status;
        String id = request.getParameter("id");
        String action = request.getParameter("action");

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Get a instance of current session on the request
        HttpSession session = request.getSession(true);
        User user = (User)session.getAttribute("user");

        HashMap<String, Item> cartMap = user.getCart();


        try (Connection conn = dataSource.getConnection()) {

            if (action.equals("add")) {
                if (cartMap.containsKey(id)) {
                    cartMap.get(id).increment();
                }
                else {
                    String itemQuery = "SELECT m.title, m.price " +
                            "FROM movies AS m " +
                            "WHERE m.id = ?";


                    try (PreparedStatement getItems = conn.prepareStatement(itemQuery)) {
                        getItems.setString(1, id);
                        try (ResultSet rs = getItems.executeQuery()) {
                            while (rs.next()) {
                                Item newItem = new Item(id, rs.getString("title"), 1, rs.getDouble("price"));
                                cartMap.put(id, newItem);
                            }
                        }
                    }
                }
                status = "success";
            }
            else if (action.equals("remove")) {
                if (cartMap.containsKey(id)) {
                    if (cartMap.get(id).getQuantity() == 1) {
                        cartMap.remove(id);
                    }
                    else {
                        cartMap.get(id).decrement();
                    }
                    status = "success";
                }
                else {
                    status = "Item not in cart";
                }
            }
            else {
                status = "Invalid Parameter";
            }

            JsonObject retObj = new JsonObject();
            retObj.addProperty("status", status);

            out.print(retObj.toString());

            out.close();

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
