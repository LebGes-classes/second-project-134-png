package character;

import products.Product;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerData {

    private int id;
    private HashMap<Integer, Product> cart;//корзина

    private HashMap<Integer, Product> ownedProds;
    private String fullName;

    public static CustomerData makeNewCustomer(int id, String fullName){
        CustomerData newAccount = new CustomerData(id, fullName);

        return newAccount;
    }
    public CustomerData(int id, String fullName){
        this.id = id;
        this.fullName = fullName;
        cart = new HashMap<>();
        ownedProds = new HashMap<>();

    }

    public void addProductToCart(Product product){

        cart.put(product.getId(), product);
    }
    public void addProductAsOwned(Product product){

        ownedProds.put(product.getId(), product);
    }
    public int getCustomerId(){
        return id;
    }
    public String getFullName(){
        return fullName;
    }

    public void removeFromCart(int prodId){
        cart.remove(prodId);
    }
    public boolean isCartEmpty(){
        return cart.isEmpty();
    }
    public List<Product> getCart(){
        return cart.values().stream().collect(Collectors.toUnmodifiableList());
    }
    public boolean isProductInCart(int prodId){

        if(cart.get(prodId) == null)
            return false;

        return true;
    }

    public boolean isOwnedGoodsListEmpty(){
        return ownedProds.isEmpty();
    }
    public List<Product> getOwnedGoodsList(){
        return ownedProds.values().stream().collect(Collectors.toUnmodifiableList());
    }
    public void removeFromOwned(int prodId){

        if(ownedProds.remove(prodId) == null)
            throw new NullPointerException("Продукт с таким id покупатель и не покупал)))");


    }
    public boolean isProductOwned(int prodId){
        if(ownedProds.get(prodId) == null)
            return false;

        return true;
    }

}
