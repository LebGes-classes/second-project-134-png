package menu;

import controllers.common.ProductController;
import controllers.common.TransactionController;
import controllers.customer.CustomerController;
import controllers.owner.OwnerController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
//баги
//купленные товары могут остаться в корзине у покупателей, нужно срочно будет исправить
public class WelcomeMenu {

    private static OwnerMenu ownerMenu;
    private static CustomerMenu customerMenu;

    public static void main(String[] args) {
        //загрузка базы данных и контроллеров
        //сначала ProductController
        ProductController prods = ProductController.getProductControllerInstance();
        //потом TransactionController
        TransactionController transact = TransactionController.getTransactionsControllerInstance();
        //потом CustomerController
        CustomerController customer = CustomerController.getCustomerControllerInstance();
        //потом OwnerController
        OwnerController owner = OwnerController.getOwnerControllerInstance();
        //теперь инициализация полей у контроллеров для создания связи между контроллерами
        prods.initControllers();
        transact.initControllers();
        customer.initControllers();
        owner.initControllers();

        boolean finishFlag = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while(finishFlag) {

            System.out.println("Выбери режим");
            System.out.println("1. Руководитель компании");
            System.out.println("2. Покупатель");
            System.out.println("3. Завершить работу");

            try {

                String str;
                while (!Pattern.matches("\\d{1}", str = reader.readLine())) {
                    System.out.println("Ты можешь ввести только цифру от 1 до 3");
                }

                switch(str){
                    case "1":
                        ownerMenu = new OwnerMenu();
                        ownerMenu.start();
                        break;
                    case "2":
                        customerMenu = new CustomerMenu();
                        customerMenu.start();
                        break;
                    default:
                        finishFlag = false;
                }

            } catch (IOException exc) {
                System.out.println("что то пошло не так");
            }
        }
        prods.saveChanges();
        transact.saveChanges();
        customer.saveChanges();
        owner.saveChanges();

    }
}
