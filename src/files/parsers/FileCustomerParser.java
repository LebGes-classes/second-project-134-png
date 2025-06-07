package files.parsers;

import character.CustomerData;
import controllers.common.ProductController;
import utils.InputMethods;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static utils.MethodsForString.removeSpaces;

public class FileCustomerParser{

    public static final String customerFileName = "data/customers/customersAndProducts/customer%d.txt";
    public static final String allCustomersFileName = "data/customers/customers.txt";

    private HashMap<Integer, String> allCustomers;
    private List<Integer> freeCustomersId;

    private ProductController controller;
    //как хранятся данные о покупателе
    //ID Full_Name
    //1 Bobby_Tarantino
    //конкретный покупатель
    //Products_ID status
    //<productId> <true/false>

    //false -если товар только в корзине
    //true - если товар уже куплен

    public FileCustomerParser(){

        controller = ProductController.getProductControllerInstance();
        parseCustomersNames();

        freeCustomersId = InputMethods.parseFreeId(allCustomers.keySet().stream().toList());

    }

    private void parseCustomersNames(){
        List<List<String>> table = InputMethods.readAllLines(allCustomersFileName, 1);
        allCustomers = new HashMap<>();

        for(List<String> oneCustomer: table){
            allCustomers.put(Integer.parseInt(oneCustomer.get(0)), oneCustomer.get(1));

        }
    }

    public HashMap<Integer, String> getAllCustomers(){
        return allCustomers;
    }
    public CustomerData parseCustomer(int id, String fullName){

        CustomerData customer = null;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(customerFileName.formatted(id))))){

            customer = new CustomerData(id, fullName.replaceAll(" ", "_"));

            reader.readLine();

            List<String> prod;
            String str;

            while((str = reader.readLine()) != null){
                prod = removeSpaces(str);

                if(prod.get(1).equals("true"))
                    customer.addProductAsOwned(controller.getProduct(Integer.parseInt(prod.get(0))));
                else {//в этом случае будет false
                    customer.addProductToCart(controller.getProduct(Integer.parseInt(prod.get(0))));

                }
            }

        }
        catch(IOException exc){
            exc.printStackTrace();
        }

        return customer;

    }
    public List<Integer> getFreeCustomersId(){
        return freeCustomersId;
    }

}
