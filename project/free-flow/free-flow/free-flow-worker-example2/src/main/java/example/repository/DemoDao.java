package example.repository;

import org.springframework.stereotype.Repository;

@Repository
public class DemoDao {

    private int quantity = 0;

    public void addQuantity(int i) {
        this.quantity = this.quantity + i;
    }

    public int getQuantity() {
        return quantity;
    }
}
