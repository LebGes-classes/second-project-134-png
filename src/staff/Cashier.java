package staff;

import storages.Point;
import storages.SellPoint;

public class Cashier extends Employee{//кассир
    private SellPoint workPlace;

    public Cashier(String fullName, int employeeId, SellPoint workPlace){
        super(fullName, employeeId);
        toHire(workPlace);
    }
    public Cashier(String fullName, int employeeId){
        super(fullName, employeeId);
    }
    @Override
    public void toFire(){
        setHired(false);
        workPlace.removeCashier();
        workPlace = null;
    }

    @Override
    public void toHire(Point workPlace){
        setHired(true);

        if(workPlace.getClass() != SellPoint.class){
            System.out.println("Кассир может работать только в пунктах продажи");
            return;
        }

        this.workPlace = (SellPoint) workPlace;
        this.workPlace.setCashier(this);
    }

    @Override
    public Point getWorkPlace(){
        return workPlace;
    }
}

