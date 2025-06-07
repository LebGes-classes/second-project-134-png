package storages;

import products.*;

public class Cell {
    private int prId;
    private Product product;

    public boolean putProduct(Product product){//true если положили, false если ячейка занята
        if(isEmpty()) {
            this.product = product;
            prId = product.getId();
            return true;
        }

        return false;
    }
    public Product getProduct(){
        return product;
    }
    public int getProductId(){
        return prId;
    }
    public boolean isEmpty(){
        if(product == null)
            return true;

        return false;
    }
}
