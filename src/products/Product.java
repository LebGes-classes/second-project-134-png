package products;

public class Product {
    private int prId;
    private String title;
    private String brand;
    private int price;
    private ProductType type;


    public Product(int prId, String title, String brand, int price, String type){
        this.prId = prId;
        this.title = title;
        this.brand = brand;
        this.price = price;
        this.type = ProductType.valueOf(type);
    }

    public int getId(){
        return prId;
    }
    public String getTitle(){
        return title;
    }

    public int getPrice(){
        return price;
    }
    public void setPrice(int newPrice){
        price = newPrice;
    }
    public String getBrand(){
        return brand;
    }
    public String getType(){
        return type.toString();
    }

    @Override
    public String toString(){
        return "ID: " + prId + "\nНазвание: " + title + "\nБренд: " + brand + "\nТип товара: " + type + "\nprice: " + price;
    }

}
