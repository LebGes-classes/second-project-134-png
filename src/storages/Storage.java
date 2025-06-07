package storages;

import staff.Accountable;
import staff.Cashier;

import java.util.ArrayList;

public class Storage extends Point{
    //фабрика для создания складов в процессе работы программы
    public static Storage storageFactory(int pointId, String city, String street){
        Storage point = new Storage(pointId, city, street);
        point.setParsed();

        return point;
    }
    ///конструкторы напрямую будут использоваться только в начале программы когда буферизуются все данные
    public Storage(int storageId, String city, String street, Accountable accountable){
        super(storageId, 100, city, street);
        accountable.toHire(this);
    }
    public Storage(int storageId, String city, String street){
        super(storageId, 100, city, street);
    }
}
