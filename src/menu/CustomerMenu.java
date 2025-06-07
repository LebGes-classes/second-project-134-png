package menu;

import character.CustomerData;
import controllers.customer.CustomerController;
import products.Product;
import utils.MethodsForString;
import utils.Pair;

import static utils.MethodsForString.removeSpaces;
import static utils.InputMethods.readProcessing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class CustomerMenu {
    private CustomerController controller;
    private BufferedReader reader;
    private CustomerData currentCustomer;

    private Pair<String, String> pair;//управляет переключением между разными меню
    private final String regexOfLetter = "[a-zA-Zа-яА-Я]";

    public CustomerMenu(){
        reader = new BufferedReader(new InputStreamReader(System.in));
        controller = CustomerController.getCustomerControllerInstance();
    }

    public void start()throws IOException {
        System.out.println("Учетные записи покупателей, выбери свою:");
        showCustomers();
        System.out.println("Доступные действия:\n" +
                "<id> - вводишь id покупателя в чью учетную запись хочешь войти\n" +
                "make - создать новую учетную запись покупателя\n" +
                "remove <id> - удалить учетную запись");


        boolean correctInputFlag = true;
        int id;
        while(correctInputFlag) {
            String str = readProcessing("(\\d+|make|(\\s*remove\\s+\\d+\\s*))", "Неправильный ввод", reader);

            if (str.equals("back")) {
                correctInputFlag = false;
            }
            else if (str.equals("make")) {
                makeNewCustomerAccount();
                correctInputFlag = false;
            }
            else if(str.contains("remove")){
                List<String> command = MethodsForString.removeSpaces(str);

                id = Integer.parseInt(command.get(1));
                if(controller.getCustomer(id) != null){
                    correctInputFlag = false;
                    controller.deleteAccount(id);
                    System.out.println("Учетная запись " + id + " успешно удалена");
                }
                else
                    System.out.println("Нет покупателя с таким id");

            }
            else {

                id = Integer.parseInt(str);
                if (controller.getCustomer(id) != null) {
                    correctInputFlag = false;
                    currentCustomer = controller.getCustomer(id);
                    pair = new Pair<>("start", null);
                    start1();
                }
                else
                    System.out.println("Нет покупателя с таким id");
            }

        }


    }
    public void start1()throws IOException{

        boolean finishFlag = true;

        while(finishFlag){

            switch(pair.getFirstEl()){
                case "start":
                    showStartMenu();
                    break;
                case "cart":
                    showCartMenu();
                    break;
                case "ownedGoods":
                    showOwnedGoodsMenu();
                    break;
                case "showGoodsInPoints":
                    showFillCartMenu();
                    break;
                case "buy":
                    showBuyMenu();
                    break;
                case "back":
                    finishFlag = false;
                    break;
            }
        }
    }
    public void showStartMenu()throws IOException{
        System.out.println("1. Посмотреть корзину");
        System.out.println("2. Показать список купленных товаров");
        System.out.println("3. Посмотреть доступные товары в точке продажи");
        System.out.println("4. Перейти к оплате");

        String str = readProcessing("[1-4]", "Неправильный ввод", reader);

        switch (str){
            case "1":
                pair.setFirstEl("cart");
                break;
            case "2":
                pair.setFirstEl("ownedGoods");
                break;
            case "3":
                pair.setFirstEl("showGoodsInPoints");
                pair.setSecondEl("availableSellPoints");
                break;
            case "4":
                pair.setFirstEl("buy");
                break;
            case "back":
                pair.setFirstEl("back");
                break;
        }

    }
    public void showCartMenu()throws IOException{
        showCart();
        System.out.println("Если хотите удалить что-нибудь с корзины напишите id соответствующего товара:");

        boolean correctIdFlag = true;
        String str;
        Product prod;

        while(correctIdFlag) {
            str = readProcessing("\\d+", "Неправильный ввод", reader);

            if(str.equals("back")){
                pair.setFirstEl("start");
                correctIdFlag = false;
            }
            else {
                prod = controller.getProduct(Integer.parseInt(str));

                if (prod == null) {
                    System.out.println("Нет товара с таким id");
                } else {
                    controller.removeProdFromCart(prod.getId(), currentCustomer.getCustomerId());
                    correctIdFlag = false;
                    pair.setFirstEl("start");
                }
            }
        }

    }
    public void showOwnedGoodsMenu()throws IOException{
        boolean ownedGoodsStatus = showOwnedGoods();

        if(!ownedGoodsStatus){
            reader.readLine();
            pair.setFirstEl("start");
            return;
        }

        System.out.println("Чтобы вернуть какой-нибудь товар напиши его id, если хочешь очистить список купленных товаров напиши clear");

        boolean correctIdFlag = true;
        String str;
        Product returnedProduct = null;

        while(correctIdFlag){
            str = readProcessing("(clear|\\d+)", "Неправильный ввод", reader);

            if(str.equals("back")) {
                pair.setFirstEl("start");
                correctIdFlag = false;
            }
            else if(str.equals("clear")) {
                pair.setFirstEl("start");
                controller.clearCustomerOwned(currentCustomer.getCustomerId());
            }
            else{
                returnedProduct = controller.isProductOwnedBy(currentCustomer.getCustomerId(), Integer.parseInt(str));

                if(returnedProduct != null){
                    correctIdFlag = false;

                    boolean status = controller.returnProduct(currentCustomer.getCustomerId(), returnedProduct.getId());
                    if(status)
                        System.out.println("Товар вернули");
                    else
                        System.out.println("Сейчас не получится вернуть товар, все точки заполнены, попробуйте позже");
                    pair.setFirstEl("start");
                }
                else
                    System.out.println("Неправильный id");
            }
        }

    }
    public void showFillCartMenu()throws IOException{
        if(pair.getSecondEl().equals("availableSellPoints")) {

            System.out.println("Доступные точки продажи: ");
            controller.showAvailableSellPoints();
            System.out.println("Чтобы посмотреть доступные товары для покупки напиши id точки торговли, в которой хочешь посмотреть товары:");

            boolean correctIdFlag = true;
            String str;

            while (correctIdFlag) {

                str = readProcessing("\\d+", "Неправильный ввод", reader);

                if (str.equals("back")) {
                    pair.setFirstEl("start");
                    correctIdFlag = false;
                } else {
                    correctIdFlag = (!controller.showProductInPoint(Integer.parseInt(str)));

                    if(!correctIdFlag)
                        pair.setSecondEl("addingGoodInCart!" + str);
                    else
                        System.out.println("Ты ввел неверный id");
                }
            }
        }
        else if(pair.getSecondEl().contains("addingGoodInCart")) {
            int pointId = Integer.parseInt(pair.getSecondEl().replace("addingGoodInCart!", ""));

            System.out.println("Чтобы добавить в корзину продукт напиши его id:");

            boolean correctIdFlag = true;
            String str;

            while (correctIdFlag) {
                str = readProcessing("\\d+", "Неправильный ввод", reader);

                if (str.equals("back")) {
                    pair.setSecondEl("availableSellPoints");
                    correctIdFlag = false;
                } else {
                    Product product = controller.getProduct(Integer.parseInt(str));


                    if(product != null && controller.isProductInPoint(product.getId(), pointId)){

                        if(!controller.isProductInCart(currentCustomer.getCustomerId(), product.getId())) {

                            controller.putProductInCart(currentCustomer.getCustomerId(), product.getId());
                            System.out.println("Товар добавлен в корзину");
                            pair.setSecondEl("availableSellPoints");
                            correctIdFlag = false;
                        }
                        else
                            System.out.println("Товар уже находится в корзине!!!");

                    }
                    else
                        System.out.println("Продукта с таким id нет в выбранном пункте");
                }

            }
        }

    }
    public void showBuyMenu()throws IOException{
        System.out.println("Товары в корзине:");
        showCart();
        System.out.println("Чтобы купить товары напиши их id через пробелы:");

        String str = readProcessing("\\s*\\d+(\\s+\\d+)*\\s*", "Неправильный ввод", reader);

        if(str.equals("back")){
            pair.setFirstEl("start");
            return;
        }
        else{
            List<Integer> prodIds = MethodsForString.removeSpaces(str)
                    .stream()
                    .map(someStr->Integer.parseInt(someStr))
                    .filter(prodId->{
                        if(controller.isProductInCart(currentCustomer.getCustomerId(), prodId))
                            return true;
                        return false;
                    })
                    .toList();
            System.out.println("id товаров которых нет в корзине проигнорированы");
            prodIds.forEach(productId->controller.buyProductTo(currentCustomer.getCustomerId(), productId));

        }

        System.out.println("Введи что угодно чтобы вернуться в прошлое меню");
        reader.readLine();
        pair.setFirstEl("start");
    }

    private void showCustomers(){
        List<String> customers = controller.getCustomerList();

        for(String str: customers){
            System.out.println(str);
        }
    }

    private void makeNewCustomerAccount()throws IOException{
        System.out.println("Введи имя фамилию, либо просто имя:");

        String str = readProcessing("\\s*%s+(\\s+%s+|)\\s*".formatted(regexOfLetter, regexOfLetter), "Неправильный ввод", reader);

        String name = String.join(" ", removeSpaces(str));
        CustomerData newCustomer = controller.makeNewAccount(name);
        currentCustomer = newCustomer;
        System.out.println("Новая учетная запись создана");
    }
    private void showCart(){
        controller.showCustomerCart(currentCustomer.getCustomerId());
    }
    private boolean showOwnedGoods(){
        return controller.showCustomerOwnedGoods(currentCustomer.getCustomerId());
    }

}
