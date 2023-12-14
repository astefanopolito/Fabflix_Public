import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

// Declaring a WebServlet called SessionServlet, which maps to url "/session"
@WebServlet(name = "SessionServlet", urlPatterns = "/api/session")
public class SessionServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        // Get a instance of current session on the request
        HttpSession session = request.getSession(true);

        JsonObject retObj = new JsonObject();

        String username = null;

        try {
            username = (String)session.getAttribute("user");
        } catch(Exception e) {
            username = "null";

        }

        retObj.addProperty("username", username);

        out.print(retObj.toString());

        out.close();


    }
}
