public class Item {
    private final String movieId;
    private final String movieTitle;
    private Integer quantity;
    private final Double price;

    public Item(String movieId, String movieTitle, Integer quantity, Double price) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.quantity = quantity;
        this.price = price;
    }

    public String getId() {
        return this.movieId;
    }

    public String getTitle() {
        return this.movieTitle;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public Double getPrice() {
        return this.price;
    }

    public void increment() {
        this.quantity += 1;
    }

    public void decrement() {
        if (this.quantity > 0) {
            this.quantity -= 1;
        }
    }
}
