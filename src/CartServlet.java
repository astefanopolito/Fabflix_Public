import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
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
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")

public class CartServlet extends HttpServlet {
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

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HashMap<String, Item> cartMap = user.getCart();

        JsonObject retObj = new JsonObject();

        JsonArray items = new JsonArray();

        Double total = 0.0;

        for (String id : cartMap.keySet()) {
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("id", cartMap.get(id).getId());
            itemObj.addProperty("title", cartMap.get(id).getTitle());
            itemObj.addProperty("quantity", cartMap.get(id).getQuantity());
            itemObj.addProperty("price", cartMap.get(id).getPrice());
            items.add(itemObj);
            total += (cartMap.get(id).getPrice() * cartMap.get(id).getQuantity());
        }

        retObj.add("items", items);
        retObj.addProperty("total", total);


        out.print(retObj.toString());

        out.close();

    }
}