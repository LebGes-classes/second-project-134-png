package storages;

import products.Product;
import staff.Accountable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class Point {//для хранилищ будет использоваться одно множество ID-шек
    protected boolean parsed = false;//информация о товарах в точках хранится в отдельных
    //файлах, поэтому это парсится отдельно, переменная отслеживает была ли эта информация спарсена

    protected int maxAmount;//макc кол-во товаров, который может вмещать пункт
    protected int currentAmount = 0;//чтобы отслеживать количество товаров в пункте
    protected int pointId;
    protected City city;
    protected String street;

    protected ArrayList<Cell> goods;

    protected Accountable accountable;
    //конструкторы напрямую будут использоваться только в начале программы когда буферизуются все данные
    protected Point(int pointId, int maxAmount, String city, String street){
        this.pointId = pointId;
        this.maxAmount = maxAmount;
        this.goods = new ArrayList<>(maxAmount);
        this.city = City.getByTitle(city);
        this.street = street;

        for(int i = 0; i < maxAmount; i++){
            goods.add(new Cell());
        }

    }

    public boolean removeProduct(Product prod){
        if(currentAmount == 0){
            System.out.println("Пункт пуст");
            return false;
        }

        int id = prod.getId();

        int indexInGoods = findProduct(id);
        if(indexInGoods == -1){
            System.out.println("Пункт не хранит продукт с таким id");
            return false;
        }

        goods.remove(indexInGoods);
        goods.add(new Cell());
        currentAmount--;
        return true;
    }
    public void putProduct(Product product){
        if(currentAmount == maxAmount) {
            System.out.println("Пункт заполнен");
            return;
        }
        goods.get(currentAmount).putProduct(product);
        currentAmount++;
    }
    public void showProducts(){
        if(currentAmount == 0){
            System.out.println("В точке сейчас нет товаров");
            return;
        }

        for(int i = 0; i < currentAmount; i++){
            System.out.println((i + 1)+ ". " + goods.get(i).getProduct());
        }
    }
    public int findProduct(int id){//возвращает индекс продукта в массиве goods
        for(int i = 0; i < goods.size(); i++){

            if(goods.get(i).getProductId() == id)
                return i;
        }

        return -1;//если не найдено ничего
    }
    public Product getProductInLastCell(){
        return goods.get(currentAmount-1).getProduct();
    }

    public void setAccountable(Accountable newAccountable){
        if(accountable != null) {
            System.out.println("У точки уже есть ответственный");
            return;
        }

        accountable = newAccountable;
    }
    public void removeAccountable(){
        if(accountable == null){
            System.out.println("На данный момент у точки нет ответственного");
        }
        accountable = null;
    }
    public Accountable getAccountable(){
        return accountable;
    }

    public boolean isParsed(){
        return parsed;
    }
    public void setParsed(){
        parsed = true;
    }

    public boolean isEmpty(){
        if(currentAmount == 0)
            return true;
        return false;
    }
    public boolean isOverfilled(){
        if(currentAmount == maxAmount)
            return true;
        return false;
    }
    public int getFreeSpace(){
        return maxAmount - currentAmount;
    }
    public int getGoodsAmount(){
        return currentAmount;
    }

    public String getCityTitle(){
        return city.toString();
    }

    public List<Product> getAvailableProductList(){
        List<Product> res = goods
                .stream()
                .filter(cell->{
                    return !cell.isEmpty();
                })
                .map(cell->cell.getProduct())
                .sorted((prod1, prod2)->{
                    return Integer.valueOf((prod1.getId())).compareTo(prod2.getId());
                })
                .collect(Collectors.toUnmodifiableList());

        return res;
    }
    public int getPointId(){
        return pointId;
    }
    public String getStreet(){
        return street;
    }

    @Override
    public String toString(){
        return "ID: " + pointId + "\nГород: " + city + "\nУлица: " + street;
    }

}
