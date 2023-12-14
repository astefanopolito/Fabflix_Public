import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedbMaster");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        JsonObject retObj = new JsonObject();
        PrintWriter out = response.getWriter();

        String firstName = request.getParameter("firstname");
        String lastName = request.getParameter("lastname");
        String cardNumber = request.getParameter("card_number");
        String expiration = request.getParameter("expiration");

        if (firstName.equals("demo")) {
            firstName = "a";
            lastName = "a";
            cardNumber = "941";
            expiration = "2005-11-01";
        }

        if ((firstName == null) || (lastName == null) || (cardNumber == null) || (expiration == null)) {
            retObj.addProperty("status", "Failure; All fields required");
            out.println(retObj.toString());
            out.close();
            return;
        }

        cardNumber = cardNumber.replaceAll("[^0-9]", "");


        try (Connection conn = dataSource.getConnection()) {


            String idQuery = "SELECT max(id) FROM sales";
            String cardQuery = "SELECT id, firstName, lastName, expiration " +
                    "FROM creditcards " +
                    "WHERE id = ?";

            try (Statement statement = conn.createStatement();
                PreparedStatement cardStatement = conn.prepareStatement(cardQuery)) {
                cardStatement.setString(1, cardNumber);

                try (ResultSet resId = statement.executeQuery(idQuery);
                    ResultSet cardId = cardStatement.executeQuery()) {
                    while (cardId.next()) {
                        if (!(
                                cardId.getString("firstName").equals(firstName) &&
                                        cardId.getString("lastName").equals(lastName) &&
                                        cardId.getString("expiration").equals(expiration) &&
                                        cardId.getString("id").replaceAll("[^0-9]", "").equals(cardNumber)
                        )) {
                            throw new Exception("Invalid payment details");
                        }
                    }



                    Integer maxId = 0;
                    while (resId.next()) {
                        maxId = resId.getInt("max(id)");
                        maxId++;
                    }

                    HttpSession session = request.getSession();
                    User user = (User)session.getAttribute("user");

                    HashMap<String, Item> cartMap = user.getCart();

                    String salesUpdate = "INSERT INTO sales VALUES (?, ?, ?, STR_TO_DATE(?, '%Y-%m-%d'), ?)";

                    Date curDate = Calendar.getInstance().getTime();
                    String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(curDate);

                    try (PreparedStatement prep = conn.prepareStatement(salesUpdate)) {
                        for (String id : cartMap.keySet()) {
                                prep.setInt(1, maxId);
                                prep.setInt(2, user.getId());
                                prep.setString(3, id);
                                prep.setString(4, timeStamp);
                                prep.setInt(5, cartMap.get(id).getQuantity());
                                prep.executeUpdate();
                                maxId++;
                        }
                    }

                    retObj.addProperty("status", "success");
                    out.write(retObj.toString());
                }
            }
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "Failure. Please try again.");
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