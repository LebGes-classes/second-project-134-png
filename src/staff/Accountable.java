package staff;

import storages.Point;

public class Accountable extends Employee{//лицо ответственное за пвз

    private Point managingPoint;

    public Accountable(String fullName, int employeeId){
        super(fullName, employeeId);
    }
    public Accountable(String fullName, int employeeId, Point managingPoint){
        super(fullName, employeeId);
        toHire(managingPoint);
    }

    @Override
    public void toFire(){
        setHired(false);
        managingPoint.removeAccountable();
        managingPoint = null;
    }

    @Override
    public void toHire(Point workPlace){
        managingPoint = workPlace;
        workPlace.setAccountable(this);
        setHired(true);
    }

    @Override
    public Point getWorkPlace(){
        return managingPoint;
    }
}
