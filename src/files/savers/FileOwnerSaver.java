package files.savers;

import controllers.owner.OwnerController;
import products.Product;
import staff.Employee;
import storages.Point;
import storages.SellPoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FileOwnerSaver {
    public static final String employeesFileName = "data/employeesData/allEmployees.txt";
    public static final String allPointsFileName = "data/pointsData/allPoints.txt";

    public static final String storageFileName = "data/pointsData/storages/prodsIn%d.txt";
    public static final String sellpointFileName = "data/pointsData/sellpoints/prodsIn%d.txt";

    public static final String employeeHeader = "ID name state //state - cashier or accountable";
    public static final String pointsHeader = "ID city street pointType accountableID cashierID";
    public static final String pointProductsHeader = "ProdID";

    private OwnerController controller;

    public FileOwnerSaver(){
        controller = OwnerController.getOwnerControllerInstance();

        if(controller.isChanged())
            saveChanges();
    }

    public void saveChanges(){
        if(controller.isEmployeesChanged())
            saveEmployees();
        if(controller.isPointsChanged())
            savePoints();

    }

    private void saveEmployees(){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(employeesFileName, false))){
            writer.write(employeeHeader + "\n");

            List<Employee> allEmpoyees = controller.getAllEmployees()
                    .stream()
                    .sorted((empl1, empl2)->{
                        return Integer.valueOf(empl1.getEmplId()).compareTo(empl2.getEmplId());
                    })
                    .toList();

            for(Employee empl: allEmpoyees){
                writer.write(empl.getEmplId() + " " + empl.getFullName().replaceAll(" ", "_") + " " + empl.getClass().getSimpleName().toLowerCase() + "\n");
            }
            writer.flush();

        }
        catch(IOException exc){
            exc.printStackTrace();
        }
    }
    private void savePoints(){
        deletePoints();

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(allPointsFileName, false))){
            writer.write(pointsHeader + "\n");
            List<Point> allPoints = controller.getAvailablePoints();

            for(Point p: allPoints){
                writer.write(p.getPointId() + " " +
                        p.getCityTitle() + " " +
                        p.getStreet().replaceAll(" ", "_") + " " +
                        p.getClass().getSimpleName() + " "
                );

                Optional<Employee> optional = Optional.ofNullable(p.getAccountable());
                if(optional.isEmpty())
                    writer.write("null ");
                else
                    writer.write(optional.get().getEmplId() + " ");

                if(p.getClass() == SellPoint.class){
                    SellPoint sp = (SellPoint) p;

                    optional = Optional.ofNullable(sp.getCashier());

                    if(optional.isEmpty())
                        writer.write("null\n");
                    else
                        writer.write(optional.get().getEmplId() + "\n");
                }
                else
                    writer.write("null\n");
            }
            writer.flush();

        }
        catch(IOException exc){
            exc.printStackTrace();
        }

        List<Point> changedPoints = controller.getSortedChangedPoints();
        String fileName = null;
        List<Product> products = null;

        for(Point p: changedPoints){
            if(p.getClass() == SellPoint.class)
                fileName = sellpointFileName.formatted(p.getPointId());
            else
                fileName = storageFileName.formatted(p.getPointId());

            try(BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false))){
                writer.write(pointProductsHeader + "\n");
                products = p.getAvailableProductList();

                for(Product prod: products){

                    writer.write(prod.getId() + "\n");
                }

                writer.flush();

            }
            catch(IOException exc){
                exc.printStackTrace();
            }

        }
    }
    private void deletePoints(){
        List<Integer> deletedStorages = controller.getDeletedStorages();
        List<Integer> deletedSellPoints = controller.getDeletedSellPoints();

        File f;
        for(Integer id: deletedStorages){
            f = new File(storageFileName.formatted(id));

            if(f.exists())
                f.delete();
        }

        for(Integer id: deletedSellPoints){
            f = new File(sellpointFileName.formatted(id));

            if(f.exists())
                f.delete();
        }
    }
}
