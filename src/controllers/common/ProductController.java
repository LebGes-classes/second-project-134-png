package controllers.common;

import controllers.owner.OwnerController;
import files.parsers.FileProductParser;
import files.savers.FileProductSaver;
import products.Product;
import storages.Point;
import storages.SellPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.random.RandomGenerator;

public class ProductController {
    //только один объект
    private static ProductController instance;
    private FileProductParser parser;

    private boolean changed = false;//нужно ли переписывать
    private HashMap<Integer, Product> products;
    private List<List<String>> productsDescription;
    private List<Integer> freeProductsId;

    private OwnerController ownerController;

    public static ProductController getProductControllerInstance(){
        if(instance == null){
            instance = new ProductController();
        }
        return instance;
    }

    public ProductController(){
        if(instance != null)
            throw new IllegalStateException("объект этого класса уже создан!!");

        parser = new FileProductParser();
        products = parser.getProducts();
        productsDescription = parser.getProductsDescription();
        freeProductsId = parser.getFreeProductsId();

    }
    public void initControllers(){
        ownerController = OwnerController.getOwnerControllerInstance();
    }

    public Product getProduct(int id){
        return products.get(id);
    }
    public List<List<String>> formTableForPurchasing(){
        List<List<String>> used = new ArrayList<>();
        int availableGoodsForPurchasing = 10 + (RandomGenerator.getDefault().nextInt(11));

        List<String> randomStr;
        int randomIndex;

        for(int i = 0; i < availableGoodsForPurchasing; i++){
            randomIndex = (int) (Math.random() * productsDescription.size());
            randomStr = productsDescription.remove(randomIndex);
            used.add(randomStr);

        }

        productsDescription.addAll(used);

        return used;
    }

    public void deleteOwnedProduct(int id){
        if(!changed)
            changed = true;

        Product product = products.get(id);
        if(product == null){
            System.out.println("========");
            System.out.println("Удалять нечего");
            System.out.println("========");
        }

        products.remove(id);

        freeProductsId.add(id);
        freeProductsId.sort(Integer::compareTo);
    }

    public boolean showAvailableProductsAt(int pointId){

        SellPoint point = ownerController.getSellPoint(pointId);

        if(point == null)
            return false;

        ownerController.parsePoint(pointId);

        List<Product> availableProducts = point.getAvailableProductList();
        if(availableProducts.size() == 0)
            System.out.println("Пункт продажи пуст");

        for(int i = 1; i <= availableProducts.size(); i++){
            System.out.println(i + ". " + availableProducts.get(i - 1));
        }

        return true;
    }

    private int getFreeIdForProduct(){
        int res = freeProductsId.remove(0);

        if(freeProductsId.isEmpty())
            freeProductsId.add(res + 1);

        return res;
    }
    public Product makeNewProduct(List<String> description){//тип_продукта - наименование - бренд - цена
        if(!changed)
            changed = true;

        Product prod = new Product(getFreeIdForProduct(), description.get(1), description.get(2), Integer.parseInt(description.get(3)), description.get(0));

        products.put(prod.getId(), prod);

        return prod;
    }

    public boolean isProductInPoint(int prodId, int pointId){
        if(getProduct(prodId) == null || ownerController.getPoint(pointId) == null){
            throw new NullPointerException("Неверный id товара или точки");
        }

        Point p = ownerController.getPoint(pointId);

        if(p.findProduct(prodId) == -1)
            return false;

        return true;
    }

    public void saveChanges(){
        new FileProductSaver();
    }
    public boolean isChanged(){
        return changed;
    }
    public List<Product> getSortedProductList(){
        return products.values().stream().sorted((prod1, prod2)->{
            return Integer.valueOf(prod1.getPrice()).compareTo(prod2.getId());
        }).toList();
    }
}
