package storages;

import staff.Accountable;
import staff.Cashier;

import java.util.ArrayList;

public class SellPoint extends Point{

    private Cashier cashier;
    //фабрика для создания пункт продаж в процессе работы программы
    public static SellPoint sellPointFactory(int pointId, String city, String street){
        SellPoint point = new SellPoint(pointId, city, street);
        point.setParsed();

        return point;
    }
    //конструкторы напрямую будут использоваться только в начале программы когда буферизуются все данные
    public SellPoint(int pointId, String city, String street, Accountable accountable, Cashier cashier){
        super(pointId, 25, city, street);
        accountable.toHire(this);
        cashier.toHire(this);

    }

    public SellPoint(int pointId, String city, String street){
        super(pointId, 25, city, street);
    }

    public void setCashier(Cashier newCashier){
        if(cashier != null){
            System.out.println("У точки уже есть кассир");
            return;
        }

        cashier = newCashier;
    }

    public void removeCashier(){
        if(cashier == null){
            System.out.println("У пункта на данный момент нет кассира");
            return;
        }

        cashier = null;
    }
    public Cashier getCashier(){
        return cashier;
    }

}
