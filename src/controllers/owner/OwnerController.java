package controllers.owner;

import controllers.common.ProductController;
import controllers.common.TransactionController;
import files.parsers.FileOwnerParser;
import files.savers.FileOwnerSaver;
import products.Product;
import staff.Accountable;
import staff.Cashier;
import staff.Employee;
import storages.Point;
import storages.SellPoint;
import storages.Storage;

import java.util.*;

//проверить отсортирован ли массив свободных айдишек
public class OwnerController {
    private boolean changed = false;//нужно как то переписывать данные
    private static OwnerController instance;

    public static OwnerController getOwnerControllerInstance(){
        if(instance == null){
            instance = new OwnerController();
        }
        return instance;
    }

    //хэш таблицы для перезаписи вместо флага pointsChanged к примеру
    private List<Integer> deletedSellPoints;
    private List<Integer> deletedStorages;
    private HashMap<Integer, Point> changedPoints;
    //везде id-объект соответствующий аргументу типа
    private HashMap<Integer, SellPoint> availableSellPoints;
    private HashMap<Integer, Storage> availableStorages;
    private List<Integer> freePointsId;

    private boolean employeesChanged = false;
    private HashMap<Integer, Accountable> availableManagers;
    private HashMap<Integer, Cashier> availableCashiers;
    private List<Integer> freeEmployeesId;

    private ProductController prodController;
    private TransactionController transactionController;

    private FileOwnerParser parser;

    public OwnerController(){
        if(instance != null)
            throw new IllegalStateException("Объект этого контроллера уже создан");

        parser = new FileOwnerParser();
        deletedStorages = new ArrayList<>();
        deletedSellPoints = new ArrayList<>();
        changedPoints = new HashMap<>();

        availableSellPoints = parser.getSellPoints();
        availableStorages = parser.getStorages();
        freePointsId = parser.getFreePointsId();

        availableCashiers = parser.getCashiers();
        availableManagers = parser.getAccountables();
        freeEmployeesId = parser.getFreeEmployeesId();

    }
    public void initControllers(){
        prodController = ProductController.getProductControllerInstance();
        transactionController = TransactionController.getTransactionsControllerInstance();
    }

    public List<Employee> getAllEmployees(){
        ArrayList<Employee> allEmployees = new ArrayList<>();
        allEmployees.addAll(availableManagers.values());
        allEmployees.addAll(availableCashiers.values());

        return allEmployees;
    }

    public Product getProduct(int id){
        if(prodController.getProduct(id) == null){
            System.out.println("Товара с таким id нет");
            return null;
        }

        return prodController.getProduct(id);
    }

    public List<Storage> getStorages(){
        return availableStorages
                .values()
                .stream()
                .toList();
    }
    public List<SellPoint> getSellPoints(){
        return availableSellPoints
                .values()
                .stream()
                .toList();
    }

    private List<Point> getUnfilledPoints() {
        List<Point> freePoints = new ArrayList<>();
        for (Storage storage : getStorages()) {
            if (!storage.isOverfilled())
                freePoints.add(storage);
        }

        for (SellPoint sellPoint : getSellPoints()) {
            if (!sellPoint.isOverfilled())
                freePoints.add(sellPoint);
        }

        return freePoints;
    }
    public boolean deletePoint(int id){
        if(getPoint(id) == null)
            throw new NullPointerException("Неверный id");

        Point deletingPoint = getPoint(id);
        int goodsAmount = deletingPoint.getGoodsAmount();
        List<Point> freePoints = getUnfilledPoints();

        int commonFreeSpace = 0;

        for(Point point: freePoints){
            if(point.getPointId() == id)
                    freePoints.remove(point);
            commonFreeSpace += point.getFreeSpace();
        }

        if(commonFreeSpace < goodsAmount)
            return false;

        if(!changed)
            changed = true;

        if(deletingPoint.getAccountable() != null)
            toFireEmployee(deletingPoint.getAccountable().getEmplId());
        if(deletingPoint.getClass() == SellPoint.class){
            SellPoint deletingSellPoint = (SellPoint) deletingPoint;

            if(deletingSellPoint.getCashier() != null)
                toFireEmployee(deletingSellPoint.getCashier().getEmplId());
        }

        List<Product> movingProducts = deletingPoint.getAvailableProductList();

        int i = 0;
        for(Product moving: movingProducts){
            while(freePoints.get(i).isOverfilled())
                i++;

            moveProduct(moving, deletingPoint, freePoints.get(i));
        }
        if(deletingPoint.getClass() == SellPoint.class)
            transactionController.removePoint(deletingPoint.getPointId());

        if(deletingPoint.getClass() == SellPoint.class) {
            availableSellPoints.remove(deletingPoint.getPointId());
            deletedSellPoints.add(deletingPoint.getPointId());
        }
        else {
            availableStorages.remove(deletingPoint.getPointId());
            deletedStorages.add(deletingPoint.getPointId());
        }


        freePointsId.add(id);
        freePointsId.sort(Integer::compareTo);
        return true;

    }
    public void createPoint(String city, String street, String type){
        if(!changed)
            changed = true;

        int newId = getFreePointId();

        Point newPoint = null;

        if(type.equals("storage")){
            newPoint = Storage.storageFactory(newId, city, street);

            availableStorages.put(newId, (Storage)newPoint);
        }
        else { //для определенности тип будет sellpoint
            newPoint = SellPoint.sellPointFactory(newId, city, street);

            availableSellPoints.put(newId, (SellPoint) newPoint);
            transactionController.addNewPoint(newId);
        }

        changedPoints.put(newPoint.getPointId(), newPoint);
    }
    public Point getPoint(int id){
        if(availableSellPoints.get(id) != null)
            return availableSellPoints.get(id);

        else if(availableStorages.get(id) != null)
            return availableStorages.get(id);

        return null;

    }
    public Storage getStorage(int storId){
        if(availableStorages.get(storId) == null)
            return null;

        return availableStorages.get(storId);
    }
    public SellPoint getSellPoint(int sellPointId){
        if(availableSellPoints.get(sellPointId) == null){
            return null;
        }

        return availableSellPoints.get(sellPointId);
    }

    public boolean parsePoint(int id){
        if(availableStorages.get(id) == null && availableSellPoints.get(id) == null){
            return false;
        }
        Point res = availableStorages.get(id) == null ? availableSellPoints.get(id):availableStorages.get(id);

        if(res.isParsed())
            return true;

        List<Integer> productIds = parser.parseProdsPoint(id);

        for(Integer prodId: productIds){

            res.putProduct(prodController.getProduct(prodId));
        }


        return true;
    }
    public int getFreePointId(){
        int res = freePointsId.remove(0);

        if(freePointsId.isEmpty())
            freePointsId.add(res + 1);

        return res;
    }

    public List<Accountable> getAccountables(){
        return availableManagers.values().stream().sorted((Accountable ac1, Accountable ac2)->ac1.toString().compareTo(ac2.toString())).toList();
    }
    public List<Cashier> getCashiers(){
        return availableCashiers.values().stream().sorted((Cashier ca1, Cashier ca2)->ca1.toString().compareTo(ca2.toString())).toList();
    }
    public Employee getEmployee(int id){
        if(availableManagers.get(id) == null && availableCashiers.get(id) == null) {
            System.out.println("Нет сотрудника с таким id");
            return null;
        }

        Employee res = availableCashiers.get(id) == null ? availableManagers.get(id) : availableCashiers.get(id);

        return res;
    }
    public void toFireEmployee(int id){
        if(availableManagers.get(id) == null && availableCashiers.get(id) == null){
            throw new NullPointerException("Неверный id");
        }
        Point workPlace = null;
        if(!changed)
            changed = true;
        if(!employeesChanged)
            employeesChanged = true;

        if(availableCashiers.get(id) != null){
            workPlace = availableCashiers.get(id).getWorkPlace();
            availableCashiers.get(id).toFire();
            availableCashiers.remove(id);
        }
        else {
            workPlace = availableManagers.get(id).getWorkPlace();
            availableManagers.get(id).toFire();
            availableManagers.remove(id);
        }

        changedPoints.put(workPlace.getPointId(), workPlace);

        freeEmployeesId.add(id);
        freeEmployeesId.sort(Integer::compareTo);
    }
    public void toHireNewAccountable(String name, Point newWorkingPlace){
        if(!changed)
            changed = true;
        if(!employeesChanged)
            employeesChanged = true;

        Accountable newAccountable = new Accountable(name, getFreeEmployeeId(), newWorkingPlace);

        availableManagers.put(newAccountable.getEmplId(), newAccountable);
        changedPoints.put(newWorkingPlace.getPointId(), newWorkingPlace);
    }
    public void toHireNewCashier(String name, SellPoint newWorkingPlace){
        if(!changed)
            changed = true;
        if(!employeesChanged)
            employeesChanged = true;

        Cashier newCashier = new Cashier(name, getFreeEmployeeId(), newWorkingPlace);

        availableCashiers.put(newCashier.getEmplId(), newCashier);
        changedPoints.put(newWorkingPlace.getPointId(), newWorkingPlace);
    }

    public int getFreeEmployeeId(){
        int res = freeEmployeesId.remove(0);

        if(freeEmployeesId.isEmpty())
            freeEmployeesId.add(res + 1);

        return res;
    }


    public List<List<String>> formTableForPurchasing(){
        return prodController.formTableForPurchasing();
    }
    public SellPoint distributeSpaceForProduct(){//используется только когда все точки продаж заполнены товарами
        Collection<Storage> storages = availableStorages.values();
        Storage emptyStorage = null;//условно так назвал

        for(Storage storage: storages){

            if(!storage.isOverfilled()){
                emptyStorage = storage;
                break;
            }
        }

        if(emptyStorage == null)
            return null;
        if(!changed)
            changed = true;

        SellPoint overfilledPoint = availableSellPoints.values().stream().toList().get(0);

        moveProduct(overfilledPoint.getProductInLastCell(), overfilledPoint, emptyStorage);

        return overfilledPoint;
    }
    //вызывается из TransactionController
    public boolean returnProduct(int prodId){
        List<SellPoint> list = getSellPoints()
                .stream()
                .filter(point-> {
                    return !point.isOverfilled();
                })
                .sorted((point1, point2)->{
                    return point1.getCityTitle().compareTo(point2.getCityTitle());
                })
                .toList();
        SellPoint freePoint = null;

        if(list.size() == 0){
            freePoint = distributeSpaceForProduct();

            if(freePoint == null)
                return false;
            else{
                if(!changed)
                    changed = true;

                freePoint.putProduct(prodController.getProduct(prodId));
                changedPoints.put(freePoint.getPointId(), freePoint);
                return true;
            }
        }
        freePoint = list.get(0);
        freePoint.putProduct(prodController.getProduct(prodId));

        if(!changedPoints.containsKey(freePoint.getPointId()))
            changedPoints.put(freePoint.getPointId(), freePoint);

        return true;
    }
    public boolean moveProduct(Product prod, Point from, Point to){
        if(to.isOverfilled()){
            return false;
        }
        if(!changed)
            changed = true;

        changedPoints.put(from.getPointId(), from);
        changedPoints.put(to.getPointId(), to);

        from.removeProduct(prod);
        to.putProduct(prod);

        return true;
    }
    //создание новыъ товаров во время закупки
    public boolean prepareAndPutProduct(List<String> description, int pointId) {//тип_продукта - наименование - бренд - цена
        if(!changed)
            changed = true;

        Point unfilledPoint = getPoint(pointId);

        if(unfilledPoint == null)
            return false;

        Product newProduct = prodController.makeNewProduct(description);

        transactionController.changeProfit((-1) * newProduct.getPrice());
        newProduct.setPrice((int)(newProduct.getPrice() * 1.5));

        unfilledPoint.putProduct(newProduct);
        changedPoints.put(unfilledPoint.getPointId(), unfilledPoint);

        return true;

    }
    public Point findPointWhereProductFrom(int prodId){

        for(Storage storage: getStorages()){
            if(storage.findProduct(prodId) != -1)
                return storage;
        }
        for(SellPoint sellPoint: getSellPoints()){
            if(sellPoint.findProduct(prodId) != -1)
                return sellPoint;
        }
        return null;
    }
    public void removeProdFrom(int prodId, Point p){//используется из transactioncontroller, гарантируется что и продукт и точка существуют
        if(!changed)
            changed = true;
        if(!changedPoints.containsKey(p.getPointId()))
            changedPoints.put(p.getPointId(), p);

        boolean status = p.removeProduct(getProduct(prodId));
    }


    public int getCommonProfit(){
        return transactionController.getCommonProfit();
    }
    public String getSellpointsProceeds(){
        HashMap<Integer, Integer> table = transactionController.getTableOfProceeds();

        if(table.isEmpty())
            return "Точки продаж пока не открыты";

        String res = "";
        List<Integer> keys = table.keySet().stream().toList();

        Point point;
        for(int i = 0; i < keys.size(); i++){
            point = getPoint(keys.get(i));

            res += (i + 1) + ". " + point + "\nВыручка: " + table.get(keys.get(i)) + "\n";

        }

        return res;

    }


    public void saveChanges(){
        new FileOwnerSaver();
    }
    public boolean isChanged(){
        return changed;
    }
    public boolean isEmployeesChanged(){
        return employeesChanged;
    }
    public boolean isPointsChanged(){
        if(deletedSellPoints.size() == 0 && deletedStorages.size() == 0 && changedPoints.size() == 0)
            return false;

        return true;
    }
    public List<Integer> getDeletedSellPoints(){
        return deletedSellPoints;
    }
    public List<Integer> getDeletedStorages(){
        return deletedStorages;
    }
    public List<Point> getSortedChangedPoints(){
        return changedPoints
                .values()
                .stream()
                .sorted((p1, p2)->{
                    return Integer.valueOf(p1.getPointId()).compareTo(p2.getPointId());
                })
                .toList();
    }
    public List<Point> getAvailablePoints(){
        List<Point> points = new ArrayList<>();
        points.addAll(availableSellPoints.values());
        points.addAll(availableStorages.values());

        return points.stream()
                .sorted((p1, p2)->{
                    return Integer.valueOf(p1.getPointId()).compareTo(p2.getPointId());
                })
                .toList();
    }
}

