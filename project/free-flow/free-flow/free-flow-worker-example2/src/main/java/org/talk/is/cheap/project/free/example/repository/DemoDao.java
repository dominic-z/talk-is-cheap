package org.talk.is.cheap.project.free.example.repository;

import org.springframework.stereotype.Repository;

@Repository
public class DemoDao {

    private int quantity = 0;

    public void reset(int i){
        this.quantity = i;
    }

    public void addQuantity(int i) {
        this.quantity = this.quantity + i;
    }

    public int getQuantity() {
        return quantity;
    }
}
