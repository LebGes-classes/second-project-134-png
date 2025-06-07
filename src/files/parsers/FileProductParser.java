package files.parsers;

import products.Product;
import utils.InputMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static utils.InputMethods.readAllLines;

public class FileProductParser{
    public static final String prodsFileName = "data/productsData/allProducts.txt";
    public static final String prodsDescriptionFile = "data/productsData/randomProductDescription.txt";

    private HashMap<Integer, Product> products;//id - продукт
    private List<List<String>> productsDescription;
    private List<Integer> freeProductsId;

    public FileProductParser(){
        parseAvailableProducts();
        parseProductsDescription();

        freeProductsId = InputMethods.parseFreeId(products.keySet().stream().toList());
    }

    private void parseAvailableProducts(){
        List<List<String>> table = readAllLines(prodsFileName, 1);
        products = new HashMap<>();

        int id, price;

        for(List<String> prod: table){

            id = Integer.parseInt(prod.get(0));
            price = Integer.parseInt(prod.get(3));
            products.put(id, new Product(id, prod.get(1), prod.get(2), price, prod.get(4)));
        }

        freeProductsId = InputMethods.parseFreeId(products.keySet().stream().toList());
    }
    private void parseProductsDescription(){
        productsDescription = readAllLines(prodsDescriptionFile, 0);

    }
    public List<List<String>> getProductsDescription(){
        return productsDescription;
    }
    public HashMap<Integer, Product> getProducts(){
        return products;
    }
    public List<Integer> getFreeProductsId(){
        return freeProductsId;
    }

}
