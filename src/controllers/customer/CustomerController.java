package controllers.customer;

import character.CustomerData;
import controllers.common.ProductController;
import controllers.common.TransactionController;
import files.parsers.FileCustomerParser;
import files.savers.FileCustomerSaver;
import products.Product;

import java.util.*;
import java.util.stream.Collectors;


public class CustomerController{
    private boolean changed = false; //нужно ли переписывать

    private static CustomerController instance;

    public static CustomerController getCustomerControllerInstance(){
        if(instance == null){
            instance = new CustomerController();
        }
        return instance;
    }

    private HashMap<Integer, CustomerData> deletedCustomers;
    private HashMap<Integer, CustomerData> changedCustomers;

    private HashMap<Integer, CustomerData> availableCustomers;//id - customer
    private FileCustomerParser parser;

    private ProductController prodController;
    private TransactionController transactionsController;


    private HashMap<Integer, String> allCustomers;
    private List<Integer> freeCustomerId;

    public CustomerController(){
        if(instance != null)
            throw new IllegalStateException("Объект этого контроллера уже создан");

        parser = new FileCustomerParser();

        deletedCustomers = new HashMap<>();
        changedCustomers = new HashMap<>();

        availableCustomers = new HashMap<>();
        allCustomers = parser.getAllCustomers();
        freeCustomerId = parser.getFreeCustomersId();
    }

    public void initControllers(){
        prodController = ProductController.getProductControllerInstance();
        transactionsController = TransactionController.getTransactionsControllerInstance();
    }

    public CustomerData getCustomer(int id){
        if(!allCustomers.containsKey(id)){
            return null;
        }
        if(!availableCustomers.containsKey(id))
            prepareCustomer(id, allCustomers.get(id));

        return availableCustomers.get(id);
    }
    private void prepareCustomer(int id, String fullName){

        availableCustomers.put(id, parser.parseCustomer(id, fullName));
    }

    public List<String> getCustomerList(){
        ArrayList<String> list = new ArrayList<>();

        if(allCustomers.size() == 0){
            list.add("Покупателей пока не было");
            return list;
        }

        List<Map.Entry<Integer, String>> orderedList = allCustomers
                .entrySet()
                .stream()
                .sorted((entry1, entry2)->{
            return entry1.getValue().compareTo(entry2.getValue());
        }).toList();

        Map.Entry<Integer,String> entry;
        for(int i = 1; i <= orderedList.size(); i++){
            entry = orderedList.get(i - 1);

            list.add( i + ". ID: " + entry.getKey() + "\nПолное имя: " + entry.getValue());
        }

        return list;
    }
    public int getFreeIdForCustomer(){
        int res = freeCustomerId.remove(0);

        if(freeCustomerId.isEmpty())
            freeCustomerId.add(res + 1);

        return res;
    }
    public CustomerData makeNewAccount(String fullName){
        if(!changed)
            changed = true;

        CustomerData newCustomer = CustomerData.makeNewCustomer(getFreeIdForCustomer(), fullName);
        freeCustomerId.sort(Integer::compareTo);

        allCustomers.put(newCustomer.getCustomerId(), newCustomer.getFullName());
        availableCustomers.put(newCustomer.getCustomerId(), newCustomer);
        changedCustomers.put(newCustomer.getCustomerId(), newCustomer);

        return newCustomer;
    }
    public void deleteAccount(int customerId){

        if(availableCustomers.get(customerId) == null)
            throw new NullPointerException("Пользователя с таким id нет");
        if(!changed)
            changed = true;

        CustomerData deletingCustomer = availableCustomers.remove(customerId);

        List<Product> list = deletingCustomer.getOwnedGoodsList();
        list.forEach(product-> {
            deletingCustomer.removeFromOwned(product.getId());
            prodController.deleteOwnedProduct(product.getId());
        });

        deletedCustomers.put(customerId, deletingCustomer);

        if(changedCustomers.containsKey(customerId))
            changedCustomers.remove(customerId);

        allCustomers.remove(customerId);

        freeCustomerId.add(customerId);
        freeCustomerId.sort(Integer::compareTo);

    }
    public void showCustomerCart(int id){
        if(availableCustomers.get(id) == null){
            System.out.println("Нет такого покупателя");
            return;
        }

        CustomerData currentCustomer = availableCustomers.get(id);

        if(currentCustomer.isCartEmpty())
            System.out.println("Корзина пуста");
        else
            showProductList(currentCustomer.getCart());
    }

    public void buyProductTo(int customerId, int prodId){
        if(availableCustomers.get(customerId) == null && prodController.getProduct(prodId) == null){
            throw new NullPointerException("Неверный id покупателя или товара");
        }
        if(!changed)
            changed = true;

        CustomerData customer = availableCustomers.get(customerId);
        Product product = prodController.getProduct(prodId);

        customer.removeFromCart(prodId);
        customer.addProductAsOwned(product);
        transactionsController.toSellProduct(prodId);

        if(!changedCustomers.containsKey(customerId))
            changedCustomers.put(customerId, customer);

    }
    public boolean showCustomerOwnedGoods(int customerId){
        if(availableCustomers.get(customerId) == null){
            throw new NullPointerException("Нет такого покупателя");
        }

        CustomerData customer = availableCustomers.get(customerId);

        if(customer.isOwnedGoodsListEmpty()) {
            System.out.println("Список купленных товаров пуст");
            return false;
        }
        else {
            showProductList(customer.getOwnedGoodsList());
            return true;
        }
    }
    public void clearCustomerOwned(int customerId) {
        if(availableCustomers.get(customerId) == null)
            throw new NullPointerException("Нет такого покупателя");
        if(!changed)
            changed = true;

        CustomerData customer = availableCustomers.get(customerId);
        changedCustomers.put(customerId, customer);

        List<Product> prods = customer.getOwnedGoodsList();

        prods.forEach(product->{
            customer.removeFromOwned(product.getId());
            prodController.deleteOwnedProduct(product.getId());
        });

        changedCustomers.put(customerId, availableCustomers.get(customerId));
        System.out.println("Список купленных товаров очищен");
    }
    public boolean returnProduct(int customerId, int prodId){
        if(prodController.getProduct(prodId) == null || availableCustomers.get(customerId) == null){
            throw new NullPointerException("Неверный id либо продукта либо покупателя");
        }
        boolean status = transactionsController.returnProduct(prodId, prodController.getProduct(prodId).getPrice());

        if(status) {
            if(!changed)
                changed = true;

            getCustomer(customerId).removeFromOwned(prodId);
            changedCustomers.put(customerId, getCustomer(customerId));
        }

        return status;

    }

    public Product getProduct(int prodId){
        return prodController.getProduct(prodId);
    }
    public void removeProdFromCart(int prodId, int customerId){
        if(!changed)
            changed = true;
        availableCustomers.get(customerId).removeFromCart(prodId);
        changedCustomers.put(customerId, getCustomer(customerId));
    }

    private void showProductList(List<Product> list){

        List<Product> sortedCart = list
                .stream()
                .sorted((product1, product2)->product1.getTitle().compareTo(product2.getTitle()))
                .collect(Collectors.toUnmodifiableList());
        for(int i = 0; i < sortedCart.size(); i++){

            System.out.println((i + 1) + ". " + sortedCart.get(i));
        }
    }

    public void showAvailableSellPoints(){
        transactionsController.showAvailableSellPointsForCustomer();
    }
    public boolean showProductInPoint(int pointId){
        return prodController.showAvailableProductsAt(pointId);
    }

    public void putProductInCart(int customerId, int prodId){
        if(availableCustomers.get(customerId) == null || prodController.getProduct(prodId) == null){
            throw new NullPointerException("Введен либо неверный id покупателя либо id товара");
        }
        if(!changed)
            changed = true;

        availableCustomers.get(customerId).addProductToCart(prodController.getProduct(prodId));
        changedCustomers.put(customerId, getCustomer(customerId));
    }
    public boolean isProductInCart(int customerId, int prodId){

        if(availableCustomers.get(customerId) == null || prodController.getProduct(prodId) == null){
            throw new NullPointerException("Введен либо неверный id покупателя либо id товара");
        }

        return availableCustomers.get(customerId).isProductInCart(prodId);
    }

    public Product isProductOwnedBy(int customerId, int prodId){
        if(availableCustomers.get(customerId) == null)
            throw new NullPointerException("Покупателя с таким id нет");

        CustomerData customer = availableCustomers.get(customerId);

        if(customer.isProductOwned(prodId))
            return getProduct(prodId);

        return null;
    }

    public boolean isProductInPoint(int prodId, int pointId){
        return prodController.isProductInPoint(prodId, pointId);
    }

    public void saveChanges(){
        new FileCustomerSaver();
    }
    public boolean isChanged(){
        return changed;
    }
    public List<CustomerData> getDeletedCustomers(){
        return deletedCustomers.values().stream().toList();
    }
    public List<CustomerData> getChangedCustomers(){
        return changedCustomers.values().stream().toList();
    }
    public HashMap<Integer, String> getCustomersMap(){
        return allCustomers;
    }
}
