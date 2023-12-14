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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

@WebServlet(name = "OrdersServlet", urlPatterns = "/api/orders")

public class OrdersServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession();

        User user = (User)session.getAttribute("user");
        Integer userId = user.getId();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JsonObject retObj = new JsonObject();


        try (Connection conn = dataSource.getConnection()) {
            String orderQuery = "SELECT id, movieId, saleDate, quantity " +
                    "FROM sales " +
                    "WHERE customerId = ? " +
                    "ORDER BY id DESC";

            try (PreparedStatement orderStatement = conn.prepareStatement(orderQuery)) {
                orderStatement.setInt(1, userId);

                try (ResultSet rs = orderStatement.executeQuery()) {
                    JsonArray orders = new JsonArray();

                    while (rs.next()) {
                        JsonObject order = new JsonObject();
                        order.addProperty("sale_id", rs.getString("id"));
                        order.addProperty("movie_id", rs.getString("movieId"));
                        order.addProperty("date", rs.getString("saleDate"));
                        order.addProperty("quantity", rs.getString("quantity"));
                        orders.add(order);
                    }

                    retObj.add("items", orders);
                    out.print(retObj.toString());
                }
            }
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", e.getMessage());
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