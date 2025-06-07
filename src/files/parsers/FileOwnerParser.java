package files.parsers;

import controllers.common.ProductController;
import staff.*;
import storages.Point;
import storages.SellPoint;
import storages.Storage;
import utils.InputMethods;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static utils.InputMethods.readAllLines;

public class FileOwnerParser{
    public static final String employeesFileName = "data/employeesData/allEmployees.txt";
    public static final String allPointsFileName = "data/pointsData/allPoints.txt";

    public static final String storageFileName = "data/pointsData/storages/prodsIn%d.txt";
    public static final String sellpointFileName = "data/pointsData/sellpoints/prodsIn%d.txt";

    //id - соответствующий элемент
    private HashMap<Integer, Accountable> managers;//для персонала общее множество id
    private HashMap<Integer, Cashier> cashiers;
    private List<Integer> freeEmployeesId;

    private HashMap<Integer, SellPoint> sellPoints;//для точек общее множество id
    private HashMap<Integer, Storage> storages;
    private List<Integer> freePointsId;

    private ProductController prodController;

    public FileOwnerParser(){
        try{
            parseEmployees();
            parsePoints();

            ArrayList<Integer> ids = new ArrayList<>();//сначала id сотрудников
            ids.addAll(managers.keySet().stream().toList());
            ids.addAll(cashiers.keySet().stream().toList());
            freeEmployeesId = InputMethods.parseFreeId(ids);

            ids.clear();
            ids.addAll(sellPoints.keySet().stream().toList());
            ids.addAll(storages.keySet().stream().toList());
            freePointsId = InputMethods.parseFreeId(ids);

            prodController = ProductController.getProductControllerInstance();
        }
        catch(IOException exc){exc.printStackTrace();}
    }
    private void parseEmployees()throws IOException{
        List<List<String>> table = readAllLines(employeesFileName, 1);

        managers = new HashMap<>();
        cashiers = new HashMap<>();

        int id;
        String name;
        for(List<String> employeeData: table){
            id = Integer.parseInt(employeeData.get(0));
            name = employeeData.get(1).replaceAll("_", " ");

            if(employeeData.get(2).equals("cashier"))
                cashiers.put(id, new Cashier(name, id));
            else
                managers.put(id, new Accountable(name, id));
        }
    }
    private void parsePoints()throws IOException{
        List<List<String>> table = readAllLines(allPointsFileName, 1);
        sellPoints = new HashMap<>();
        storages = new HashMap<>();

        int id;
        String street = null;
        Point currentPoint;
        for(List<String> point: table){
            id = Integer.parseInt(point.get(0));
            street = point.get(2).replaceAll("_", " ");

            if(point.get(3).equals("SellPoint")){
                currentPoint = new SellPoint(id, point.get(1), street);

                if(!point.get(4).equals("null")){
                    managers.get(Integer.parseInt(point.get(4))).toHire(currentPoint);
                }
                if(!point.get(5).equals("null")){
                    cashiers.get(Integer.parseInt(point.get(5))).toHire(currentPoint);
                }
                sellPoints.put(id, (SellPoint) currentPoint);
            }
            else{//Storage
                currentPoint = new Storage(id, point.get(1), street);

                if(!point.get(4).equals("null")){
                    managers.get(Integer.parseInt(point.get(4))).toHire(currentPoint);
                }

                storages.put(id, (Storage) currentPoint);
            }

        }
    }
    public List<Integer> parseProdsPoint(int pointId){//возвращает список id продуктов
        Point currPoint = null;

        if(sellPoints.get(pointId) != null)
            currPoint = sellPoints.get(pointId);

        else if(storages.get(pointId) != null)
            currPoint = storages.get(pointId);

        if(currPoint.isParsed()){
            return null;
        }

        currPoint.setParsed();

        String str = currPoint.getClass() == SellPoint.class ? sellpointFileName : storageFileName;
        str = str.formatted(pointId);

        List<Integer> res = readAllLines(str.formatted(pointId), 1)
                .stream()
                .map(list->String.join("", list))
                .map(strArg->Integer.parseInt(strArg))
                .toList();

        return res;

    }

    public HashMap<Integer, Storage> getStorages(){
        return storages;
    }
    public HashMap<Integer, SellPoint> getSellPoints(){
        return sellPoints;
    }
    public List<Integer> getFreePointsId(){
        return freePointsId;
    }

    public HashMap<Integer, Accountable> getAccountables(){
        return managers;
    }
    public HashMap<Integer, Cashier> getCashiers(){
        return cashiers;
    }
    public List<Integer> getFreeEmployeesId(){
        return freeEmployeesId;
    }

}
