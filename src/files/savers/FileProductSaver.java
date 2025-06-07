package files.savers;

import controllers.common.ProductController;
import products.Product;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileProductSaver {
    public static final String productsHeader = "id title brand price type";
    public static final String prodsFileName = "data/productsData/allProducts.txt";

    private ProductController controller;

    public FileProductSaver(){
        controller = ProductController.getProductControllerInstance();

        if(controller.isChanged())
            saveChanges();
    }

    private void saveChanges(){
        File f = new File(prodsFileName);
        if(f.exists())
            f.delete();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(prodsFileName, false))){

            writer.write(productsHeader + "\n");

            List<Product> sortedList = controller.getSortedProductList();
            for(Product prod: sortedList){
                System.out.println(prod.getId() + " title: " + prod.getTitle() + " br: " + prod.getBrand() + " " + prod.getPrice() + " " + prod.getType());
                writer.write(prod.getId() + " " + prod.getTitle() + " " + prod.getBrand() + " " + prod.getPrice() + " " + prod.getType() + "\n");
            }
        }
        catch(IOException exc){
            exc.printStackTrace();
        }
    }
}
