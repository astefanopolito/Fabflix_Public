import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

// Declaring a WebServlet called SessionServlet, which maps to url "/session"
@WebServlet(name = "LogoutServlet", urlPatterns = "/api/logout")
public class LogoutServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Get a instance of current session on the request
        HttpSession session = request.getSession(true);
        session.removeAttribute("user");

        JsonObject retObj = new JsonObject();
        retObj.addProperty("status", "success");

        out.print(retObj.toString());

        out.close();


    }
}
