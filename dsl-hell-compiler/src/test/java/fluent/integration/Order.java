package fluent.integration;

public class Order {

    private final String orderId;
    private final Side side;
    private final int quantity;
    private final String ric;
    private final double price;

    public Order(String orderId, Side side, int quantity, String ric, double price) {
        this.orderId = orderId;
        this.side = side;
        this.quantity = quantity;
        this.ric = ric;
        this.price = price;
    }

    public String getOrderId() {
        return orderId;
    }

    public Side getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getRic() {
        return ric;
    }

    public double getPrice() {
        return price;
    }

    public enum Side { BUY }

}
