import java.util.HashMap;
/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final Integer id;
    private final String username;

    private HashMap<String, Item> cart;

    public User(Integer id, String username) {
        this.id = id;
        this.username = username;
        this.cart = new HashMap<String, Item>();
    }

    public Integer getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public HashMap<String, Item> getCart() {
        return this.cart;
    }
}