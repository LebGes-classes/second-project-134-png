package files.savers;

import character.CustomerData;
import controllers.customer.CustomerController;
import products.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileCustomerSaver {
    public static final String customerListHeader = "ID Full_Name";
    public static final String oneCustomerHeader = "Product_ID State";

    public static final String customerFileName = "data/customers/customersAndProducts/customer%d.txt";
    public static final String allCustomersFileName = "data/customers/customers.txt";

    private CustomerController controller;

    public FileCustomerSaver(){
        controller = CustomerController.getCustomerControllerInstance();

        if(controller.isChanged())
            startSaving();
    }

    private void startSaving(){
        List<CustomerData> deletedCustomers = controller.getDeletedCustomers();
        List<CustomerData> changedCustomers = controller.getChangedCustomers();

        for(CustomerData customer: deletedCustomers){
            File f = new File(customerFileName.formatted(customer.getCustomerId()));

            if(f.exists())
                f.delete();
        }

        for(CustomerData customer: changedCustomers){
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(customerFileName.formatted(customer.getCustomerId()), false))){

                writer.write(oneCustomerHeader + "\n");

                List<Product> prods = new ArrayList<>();
                prods.addAll(customer.getCart());
                prods.addAll(customer.getOwnedGoodsList());
                prods = prods.stream().sorted((prod1, prod2)->{
                    return Integer.valueOf(prod1.getId()).compareTo(prod2.getId());
                }).toList();

                for(Product prod: prods){
                    writer.write(prod.getId() + " ");

                    if(customer.isProductOwned(prod.getId()))
                        writer.write("true\n");
                    else
                        writer.write("false\n");
                }

                writer.flush();
            }
            catch(IOException exc){
                exc.printStackTrace();
            }
        }

        HashMap<Integer, String> allCustomers = controller.getCustomersMap();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(allCustomersFileName, false))){
            writer.write(customerListHeader + "\n");

            for(Map.Entry<Integer, String> customer: allCustomers.entrySet()){
                String name = customer.getValue().replaceAll(" ", "_");

                writer.write(customer.getKey() + " " + name + "\n");
            }
            writer.flush();
        }
        catch(IOException exc){
            exc.printStackTrace();
        }
    }
}
