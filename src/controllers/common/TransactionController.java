package controllers.common;

import controllers.owner.OwnerController;
import files.savers.FileTransactionSaver;
import storages.Point;
import storages.SellPoint;
import files.parsers.FileTransactionsParser;

import java.util.HashMap;
import java.util.List;


public class TransactionController {
    private static TransactionController instance;

    public static TransactionController getTransactionsControllerInstance(){
        if(instance == null)
            instance = new TransactionController();

        return instance;
    }

    private OwnerController ownerController;

    private FileTransactionsParser parser;

    private boolean changed = false;//нужно ли переписывать или нет
    private int commonCompanyProfit;
    private HashMap<Integer, Integer> proceeds;//id точки продажи - нынешняя выручка оттуда

    public TransactionController(){
        if(instance != null)
            throw new IllegalStateException("Объект контроллера уже создан");

        parser = new FileTransactionsParser();

        commonCompanyProfit = parser.getCommonProfit();
        proceeds = parser.getProceeds();
    }
    public void initControllers(){
        ownerController = OwnerController.getOwnerControllerInstance();
    }

    public boolean returnProduct(int prodId, int price){
        boolean status = ownerController.returnProduct(prodId);

        if(status){
            if(!changed)
                changed = true;
            commonCompanyProfit -= price;
        }

        return status;

    }
    public void showAvailableSellPointsForCustomer(){

        List<SellPoint> sellPoints = ownerController.getSellPoints();

        if(sellPoints.size() == 0){
            System.out.println("На данный момент точки продажи еще не открыты");
        }
        else {
            for (int i = 1; i <= sellPoints.size(); i++) {
                System.out.println(i + ". " + sellPoints.get(i - 1));
            }
        }
    }

    public void changeProfit(int sum){
        if(!changed)
            changed = true;

        commonCompanyProfit += sum;
    }
    public int getCommonProfit(){
        int sum = proceeds.values().stream().mapToInt(Integer::intValue).sum();
        return commonCompanyProfit + sum;
    }
    public HashMap<Integer, Integer> getTableOfProceeds(){
        return proceeds;
    }

    public void removePoint(int pointId){
        if(!changed)
            changed = true;

        int pointProceeds = proceeds.get(pointId);

        commonCompanyProfit += pointProceeds;
        proceeds.remove(pointId);
    }
    public void addNewPoint(int pointId){
        if(!changed)
            changed = true;

        proceeds.put(pointId, 0);
    }
    public void toSellProduct(int prodId){
        if(!changed)
            changed = true;

        Point point = ownerController.findPointWhereProductFrom(prodId);

        if(point != null) {
            ownerController.removeProdFrom(prodId, point);

            if (point.getClass() == SellPoint.class)
                proceeds.merge(point.getPointId(), ownerController.getProduct(prodId).getPrice(), (value1, value2) -> value1 + value2);
            else
                commonCompanyProfit += ownerController.getProduct(prodId).getPrice();
        }
        else {
            throw new NullPointerException("Не нашелся пункт продажи в котором лежал товар " + prodId);
        }

    }

    public void saveChanges(){
        new FileTransactionSaver();
    }
    public boolean isChanged(){
        return changed;
    }

}
