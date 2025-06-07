package menu;

import controllers.owner.OwnerController;
import products.Product;
import staff.*;
import storages.*;
import utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static utils.InputMethods.readProcessing;


public class OwnerMenu {
    private OwnerController controller;
    private BufferedReader reader;
    private Pair<String, String> pair;

    private final String regexOfLetter = "[a-zA-Zа-яА-Я]";

    public OwnerMenu(){
        reader = new BufferedReader(new InputStreamReader(System.in));
        controller = OwnerController.getOwnerControllerInstance();
    }

    public void start()throws IOException{
        boolean finishFlag = true;
        pair = new Pair("start", null);

        while(finishFlag){

            switch(pair.getFirstEl()){
                case "start":
                    showStartMenu();
                    break;
                case "storages":
                    showStorageMenu();
                    break;
                case "sellpoints":
                    showSellPointMenu();
                    break;
                case "employees":
                    showEmployeeMenu();
                    break;
                case "operations":
                    showPointOperationsMenu();
                    break;
                case "showProfitability":
                    showProfitabilityMenu();
                    break;
                case "back":
                    finishFlag = false;
                    break;
                default:
                    System.out.println("Такого не будет стопудова");
            }

        }
    }
    public void showStartMenu()throws IOException {

        System.out.println("1. Показать список складов");
        System.out.println("2. Показать список пунктов продаж");
        System.out.println("3. Показать информацию о всех сотрудниках");
        System.out.println("4. Закупить и разместить товары в точках");//выводим список всех точек
        System.out.println("5. Показать доходность предприятия");
        System.out.println("Чтобы переходить в прошлое меню вводи back");

        String str = readProcessing("\\d{1}", "Ты можешь ввести только цифры от 1 до 5", reader);

        switch(str){
            case "1":
                pair.setFirstEl("storages");
                pair.setSecondEl("showStorages");
                break;
            case "2":
                pair.setFirstEl("sellpoints");
                pair.setSecondEl("showSellPoints");
                break;
            case "3":
                pair.setFirstEl("employees");
                pair.setSecondEl("showEmployees");
                break;
            case "4":
                pair.setFirstEl("operations");
                pair.setSecondEl("showPoints");
                break;
            case "5":
                pair.setFirstEl("showProfitability");
                pair.setSecondEl(null);
                break;
            case "back":
                pair.setFirstEl("back");
        }
    }
    public void showStorageMenu()throws IOException{
        if(pair.getSecondEl().equals("showStorages")) {
            System.out.println("========");
            showStorages();
            System.out.println("========");
            System.out.println("Доступны команды(вместо <id> пишем id хранилища):\n" +
                    "show <id> - показывает информацию о складе\n" +
                    "delete <id> - удаляет склад\n" +
                    "create - открываем новый склад");

            operationsWithPoints();

        }
        else if(pair.getSecondEl().equals("createStorage")){
            createPoint();
        }
        else if(pair.getSecondEl().contains("printStorage")){
            printPoint();
        }

    }
    public void showSellPointMenu()throws IOException{
        if(pair.getSecondEl().equals("showSellPoints")){
            System.out.println("========");
            showSellPoints();
            System.out.println("========");
            System.out.println("Доступны команды(вместо <id> пишем id хранилища):\n" +
                    "show <id> - показывает информацию о точке продажи\n" +
                    "delete <id> - удаляет точку\n" +
                    "create - открываем новую точку продажи");

            operationsWithPoints();
        }
        else if(pair.getSecondEl().equals("createSellPoint")){
            createPoint();
        }
        else if(pair.getSecondEl().contains("printSellPoint")){
            printPoint();
        }
    }
    public void showEmployeeMenu()throws IOException{
        if(pair.getSecondEl().equals("showEmployees")) {
            List<Accountable> managers = controller.getAccountables();
            List<Cashier> cashiers = controller.getCashiers();

            System.out.println("Ответственные: ");
            if (managers.size() == 0) {
                System.out.println("Пока нету");
            } else {
                for (int i = 1; i <= managers.size(); i++) {
                    System.out.println(i + ". " + managers.get(i - 1));
                }
            }
            System.out.println("Кассиры: ");
            if (cashiers.size() == 0) {
                System.out.println("Пока нету");
            } else {
                for (int i = 1; i <= cashiers.size(); i++) {
                    System.out.println(i + ". " + cashiers.get(i - 1));
                }
            }

            System.out.println("Введи id сотрудника чтобы получить информацию о месте работы");

            boolean correctInputFlag = true;
            int id = -1;
            while (correctInputFlag) {
                String str = readProcessing("\\d+", "Введи только id сотрудника из списка!!!", reader);

                if (str.equals("back")) {
                    pair.setFirstEl("start");
                    return;
                }

                id = Integer.parseInt(str);

                if (controller.getEmployee(id) != null)
                    correctInputFlag = false;

            }
            pair.setSecondEl("operateOnEmployee!" + id);
        }
        else if(pair.getSecondEl().contains("operateOnEmployee")){
            int id = Integer.parseInt(pair.getSecondEl().split("!")[1]);

            Employee currentEmployee = controller.getEmployee(id);
            System.out.println("Полное имя: " + currentEmployee.getFullName());
            System.out.print("место работы:\n");

            if(currentEmployee.getWorkPlace() == null){
                System.out.println("В поисках");
            }
            else if(currentEmployee.getWorkPlace().getClass() == Storage.class)
                System.out.println("Склад: " + currentEmployee.getWorkPlace());

            else if(currentEmployee.getWorkPlace().getClass() == SellPoint.class)
                System.out.println("Пункт продажи: " + currentEmployee.getWorkPlace());

            System.out.println("Если хочешь уволить сотрудника напиши fire, либо back если хочешь вернуться в прошлое меню");

            String str = readProcessing("(fire|back)", "Введи нормально, у тебя же только два варианта...", reader);

            if(str.equals("back")){
                pair.setSecondEl("showEmployees");
                return;
            }
            else if(str.equals("fire")){
                controller.toFireEmployee(id);
                System.out.println("Сотрудник уволен");
                pair.setSecondEl("showEmployees");
            }
            System.out.println("Введи что угодно чтобы вернуться в прошлое меню");
            reader.readLine();
        }

    }
    public void showPointOperationsMenu()throws IOException{
        if(pair.getSecondEl().equals("showPoints")){
            System.out.println("Storages:");
            System.out.println("=========");
            showStorages();
            System.out.println("=========");
            System.out.println("Sell points:");
            System.out.println("=========");
            showSellPoints();
            System.out.println("=========");

            System.out.println("Доступные команды:\n" +
                    "move <fromId> <toId> - перемещает товар из точки с id - fromId в точку с id - toId\n" +
                    "purchase <storageId> - закупка товара в склад storageId");

            boolean correctIdFlag = true;

            while(correctIdFlag) {
                String str = readProcessing("\\s*move\\s+\\d+\\s+\\d+\\s*|\\s*purchase\\s+\\d+\\s*", "Неверный ввод", reader);
                List<String> command = MethodsForString.removeSpaces(str);

                if (str.equals("back")) {
                    pair.setFirstEl("start");
                    correctIdFlag = false;

                } else if (command.get(0).equals("move")) {
                    int id1 = Integer.parseInt(command.get(1));
                    int id2 = Integer.parseInt(command.get(2));

                    if (controller.getPoint(id1) != null && controller.getPoint(id2) != null){
                        controller.parsePoint(id1);
                        controller.parsePoint(id2);

                        correctIdFlag = false;
                        pair.setSecondEl("moveProducts!" + command.get(1) + "!" + command.get(2));
                    }
                    else
                        System.out.println("Неверные id складов");
                } else if (command.get(0).equals("purchase")) {
                    int id = Integer.parseInt(command.get(1));

                    if(controller.getStorage(id) != null) {
                        correctIdFlag = false;
                        pair.setSecondEl("purchaseWithProducts!" + command.get(1));
                    }
                    else{
                        System.out.println("Неверный id склада");
                    }
                }
            }
        }
        else if(pair.getSecondEl().contains("moveProducts")){
            String[] command = pair.getSecondEl().split("!");

            int from = Integer.parseInt(command[1]);
            int to = Integer.parseInt(command[2]);

            Point fromPoint = controller.getPoint(from);
            Point toPoint = controller.getPoint(to);

            if(fromPoint.isEmpty()){
                System.out.println("Пункт " + from + " пуст, введи что угодно чтобы вернуться в прошлое меню");
                reader.readLine();
                pair.setSecondEl("showPoints");
                return;
            }
            else if(toPoint.isOverfilled()){
                System.out.println("Пункт " + to + " заполнен, введи что угодно чтобы вернуться в прошлое меню");
                reader.readLine();
                pair.setSecondEl("showPoints");
                return;
            }
            if(fromPoint == toPoint){
                System.out.println("Пункты должны быть разными!!\nВведи что угодно чтобы вернуться в прошлое меню");
                reader.readLine();
                pair.setSecondEl("showPoints");
                return;
            }
            System.out.println("Товары доступные для перемещения из пункта " + from + " в пункт " + to);
            fromPoint.showProducts();

            System.out.println("Введи id товара который нужно переместить ");

            boolean correctIdFlag = true;
            Product prod = null;

            while(correctIdFlag) {
                String str = readProcessing("\\d+", "Неправильный ввод", reader);

                if (str.equals("back")) {
                    correctIdFlag = false;
                }
                else {
                    prod = controller.getProduct(Integer.parseInt(str));

                    if (prod != null) {
                        boolean status = controller.moveProduct(prod, fromPoint, toPoint);
                        if (status)
                            System.out.println("Товар успешно перенесен");
                        else
                            System.out.println("Точка куда хочешь перенести товар заполнена");

                        correctIdFlag = false;

                    }
                    else
                        System.out.println("Неверный id продукта");
                }
            }

            pair.setSecondEl("showPoints");

        }
        else if(pair.getSecondEl().contains("purchaseWithProducts")){
            int targetId = Integer.parseInt(pair.getSecondEl().replace("purchaseWithProducts!", ""));
            Point currPoint = controller.getPoint(targetId);

            if(currPoint.isOverfilled()){
                System.out.println("Пункт " + targetId + " заполнен, введи что угодно чтобы вернуться в прошлое меню");
                reader.readLine();
                pair.setSecondEl("showPoints");
                return;
            }
            int freeSpaceInPoint = currPoint.getFreeSpace();

            List<List<String>> availableProducts = controller.formTableForPurchasing();
            showProductsForPurchasing(availableProducts);
            System.out.println("На данный момент свободного места в точке - " + freeSpaceInPoint + "\n" +
                    "Введи номер строки указывающий товар и количество для закупки,\n" +
                    "к примеру: 3 34 - закупить товар под пунктом 3, в количестве 34 штук");

            boolean correctInputFlag;
            int amount = 0;
            int index = 0;

            boolean purchasingProccessFlag = true;
            String input;

            boolean notToPurchaseFlag = false;

            while(purchasingProccessFlag) {
                correctInputFlag = true;
                freeSpaceInPoint = currPoint.getFreeSpace();

                if(freeSpaceInPoint == 0) {
                    System.out.println("Склад заполнен, введи что нибудь чтобы вернуться в прошлое меню");
                    reader.readLine();
                    purchasingProccessFlag = false;
                    correctInputFlag = false;
                }
                else {

                    while (correctInputFlag) {
                        input = readProcessing("\\s*\\d+\\s+\\d+\\s*", "Неправильный ввод", reader);

                        if (input.equals("back")) {
                            pair.setSecondEl("showPoints");
                            correctInputFlag = false;
                            purchasingProccessFlag = false;

                            notToPurchaseFlag = true;
                        } else {
                            List<Integer> command = MethodsForString.removeSpaces(input)
                                    .stream()
                                    .map((String str) -> Integer.parseInt(str))
                                    .toList();

                            index = command.get(0);
                            amount = command.get(1);

                            if (amount <= freeSpaceInPoint && (index >= 1 && index <= availableProducts.size()))
                                correctInputFlag = false;
                            else
                                System.out.println("Неправильный ввод");
                        }
                    }

                    if(!notToPurchaseFlag) {
                        for (int i = 0; i < amount; i++) {
                            controller.prepareAndPutProduct(availableProducts.get(index - 1), targetId);//описание будет содержать в формате тип_продукта - наименование - бренд - цена
                        }

                        System.out.println("Товар закуплен");

                        System.out.println("Продолжаем закупку?(Да/Нет)");
                        String str = readProcessing("Да|Нет", "Неправильный ввод", reader);

                        if (str.equals("Нет") || str.equals("back"))
                            purchasingProccessFlag = false;
                    }
                }

            }

            pair.setSecondEl("showPoints");

        }
    }
    public void showProfitabilityMenu()throws IOException{
        System.out.println("Доходность всего предприятия: " + controller.getCommonProfit());
        System.out.println("Выручка со всех точек продаж:");
        System.out.println(controller.getSellpointsProceeds());

        System.out.println("Чтобы вернуться в прошлое меню введи что угодно");
        reader.readLine();
        pair.setFirstEl("start");
    }

    private void showSellPoints(){
        List<SellPoint> points = controller.getSellPoints();

        if(points.isEmpty())
            System.out.println("Пока нет точек торговли");
        else {
            for (int i = 0; i < points.size(); i++) {
                System.out.println((i + 1) + ". " + points.get(i));
            }
        }
    }
    private void showStorages(){
        List<Storage> points = controller.getStorages();

        if(points.isEmpty())
            System.out.println("Пока нет складов");
        else {
            for (int i = 0; i < points.size(); i++) {
                System.out.println((i + 1) + ". " + points.get(i));
            }
        }

    }

    private void operationsWithPoints()throws IOException{
        List<String> command = null;
        String str;
        boolean flag = true;
        int pointId = -1;


        while(flag) {
            str = readProcessing("\\s*create\\s*|\\s*(show|delete)\\s+\\d+\\s*", "Неправильный ввод", reader);

            if (str.equals("back")) {
                pair.setFirstEl("start");
                return;
            }

            command = MethodsForString.removeSpaces(str);
            if (command.size() == 1) {//гарантируется что это слово create
                if (pair.getFirstEl().equals("storages"))
                    pair.setSecondEl("createStorage");
                else
                    pair.setSecondEl("createSellPoint");

                flag = false;
            }
            else {
                pointId = Integer.parseInt(command.get(1));
                boolean status = controller.parsePoint(pointId);

                if (!status) {
                    System.out.println("Нет точки с таким id");
                }
                else if(pair.getFirstEl().equals("storages") && controller.getStorage(pointId) == null){
                    System.out.println("Нет хранилища с таким id");
                }
                else if(pair.getFirstEl().equals("sellpoints") && controller.getSellPoint(pointId) == null){
                    System.out.println("Нет точки пункта торговли с таким id");
                }
                else
                    flag = false;
            }

        }
        if(command.get(0).equals("delete")){
            boolean status = controller.deletePoint(pointId);
            if(status)
                System.out.println("Сделано");
            else
                System.out.println("На данный момент нельзя закрыть точку");
        }
        else if(command.get(0).equals("show")){
            if(pair.getFirstEl().equals("storages"))
                pair.setSecondEl("printStorage!" + pointId);
            else
                pair.setSecondEl("printSellPoint!" + pointId);
        }

    }
    private void createPoint()throws IOException{
        boolean cityFlag = true;
        int choice = 0;
        List<String> sortedCities = null;

        while(cityFlag) {
            System.out.println("Введи номер города в котором откроешь точку:");

            sortedCities = Arrays.asList(City.values()).stream().map((City c) ->c.toString()).sorted().toList();

            for (int i = 0; i < sortedCities.size(); i++) {
                System.out.println((i + 1) + ". " + sortedCities.get(i));
            }

            String str1 = readProcessing("\\d+", "Неправильный ввод", reader);
            if (str1.equals("back")) {
                if(pair.getFirstEl().equals("sellpoints"))
                    pair.setSecondEl("showSellPoints");
                else
                    pair.setSecondEl("showStorages");
                return;
            }
            choice = Integer.parseInt(str1);

            if(choice >= 1 && sortedCities.size() >= choice)
                cityFlag = false;
            else
                System.out.println("Ты можешь ввести номер только из списка");

        }


        System.out.println("Введи название улицы на которой откроешь точку в формате - <Название улицы> <Номер дома>");
        String str2 = readProcessing("\\s*[a-zA-Zа-яА-Я]+\\s+\\d+\\s*", "Неправильный ввод", reader);

        if (str2.equals("back")) {
            if(pair.getFirstEl().equals("sellpoints"))
                pair.setSecondEl("showSellPoints");
            else
                pair.setSecondEl("showStorages");
            return;
        }

        str2 = String.join(" ", MethodsForString.removeSpaces(str2));
        if(pair.getFirstEl().equals("sellpoints"))
            controller.createPoint(sortedCities.get(choice - 1), str2, "sellpoint");
        else
            controller.createPoint(sortedCities.get(choice - 1), str2, "storage");
        System.out.println("Точка создана");

        if(pair.getFirstEl().equals("sellpoints"))
            pair.setSecondEl("showSellPoints");
        else
            pair.setSecondEl("showStorages");
    }
    private void printPoint()throws IOException{
        int id = Integer.parseInt(pair.getSecondEl().split("!")[1]);

        Point point = controller.getPoint(id);

        if(point == null){

            if(pair.getFirstEl().equals("sellpoints"))
                pair.setSecondEl("showSellPoints");
            else
                pair.setSecondEl("showStorages");
            return;
        }

        System.out.println(point);
        Optional<Accountable> optionalAccountable = Optional.ofNullable(point.getAccountable());

        System.out.print("Ответственный: ");
        if(optionalAccountable.isPresent())
            System.out.println(optionalAccountable.get());
        else
            System.out.println("нет");

        Optional<Cashier> optionalCashier = Optional.empty();

        if(pair.getFirstEl().equals("sellpoints")){
            SellPoint sellpoint = (SellPoint) point;
            optionalCashier = Optional.ofNullable(sellpoint.getCashier());

            System.out.print("Кассир: " );

            if(optionalCashier.isPresent())
                System.out.println(optionalCashier.get());
            else
                System.out.println("нет");
        }

        point.showProducts();
        boolean specialInput = false;
        boolean quitState = false;

        List<String> regexes = new ArrayList<>();

        if(optionalAccountable.isEmpty()) {
            System.out.println("Нету ответственного, чтобы его нанять впиши hireAcc");
            regexes.add("hireAcc");
            specialInput = true;
        }
        if(point.getClass() == SellPoint.class && optionalCashier.isEmpty()) {
            System.out.println("Нету кассира, чтобы его нанять впиши hireCashier");
            regexes.add("hireCashier");
            specialInput = true;
        }

        if(specialInput){
            String str = readProcessing("(" + String.join("|", regexes) + ")", "Неправильный ввод", reader);

            if(str.equals("hireAcc")){
                hireNewEmployee("accountable", point);
            }
            else if(str.equals("hireCashier")){
                hireNewEmployee("cashier", point);
            }
            else if(str.equals("back"))
                quitState = true;
        }
        else{
            System.out.println("Введи что угодно чтобы вернуться в прошлое меню");
            reader.readLine();
            quitState = true;
        }

        if(quitState) {
            if (pair.getFirstEl().equals("sellpoints"))
                pair.setSecondEl("showSellPoints");
            else
                pair.setSecondEl("showStorages");
        }
    }

    private void showProductsForPurchasing(List<List<String>> productsDescription){

        System.out.println("На данный момент для закупки доступно " + productsDescription.size() + " Видов товаров");

        for(int i = 1; i <= productsDescription.size(); i++){

            System.out.print(i + ". ");

            for(String str: productsDescription.get(i - 1)){

                System.out.print(str + " ");
            }
            System.out.println();
        }
    }
    private void hireNewEmployee(String state, Point point)throws IOException{
        System.out.println("Введи имя нового сотрудника");
        String name = null;

        while(true) {
            name = readProcessing("\\s*" + regexOfLetter + "+(\\s+" + regexOfLetter + "+|)\\s*", "Неправильный ввод", reader);
            name = String.join(" ", MethodsForString.removeSpaces(name));

            if(name.equals("back"))
                System.out.println("В этот раз не получится");
            else
                break;
        }

        if(state.equals("cashier"))
            controller.toHireNewCashier(name, (SellPoint) point);
        else //accountable
            controller.toHireNewAccountable(name, point);

    }
}
