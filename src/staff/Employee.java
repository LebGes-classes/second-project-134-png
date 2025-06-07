package staff;

import storages.Point;
import storages.SellPoint;

public abstract class Employee {
    private int employeeId;
    private String fullName;

    private boolean hired = false;

    public Employee(String fullName, int employeeId){
        this.fullName = fullName;
        this.employeeId = employeeId;

    }
    public boolean isHired(){
        return hired;
    }
    public void setHired(boolean state){
        hired = state;
    }

    public String getFullName(){
        return fullName;
    }
    public int getEmplId(){
        return employeeId;
    }
    public abstract void toFire();
    public abstract void toHire(Point workPlace);
    public abstract Point getWorkPlace();

    @Override
    public String toString(){
        return fullName + " id: " + employeeId;
    }
}
