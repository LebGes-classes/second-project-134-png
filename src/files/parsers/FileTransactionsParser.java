package files.parsers;

import java.io.*;
import java.util.HashMap;
import java.util.List;

import static utils.InputMethods.readAllLines;

public class FileTransactionsParser{

    public static final String transactionsFileName = "data/transactions/ProfitData.txt";

    private int commonCompanyProfit;
    private HashMap<Integer, Integer> proceeds;//id точки продажи - нынешняя выручка оттуда

    public FileTransactionsParser(){
        try {
            proceeds = new HashMap<>();
            parseData();
        }
        catch(FileNotFoundException exc){
            exc.printStackTrace();
        }
    }

    private void parseData()throws FileNotFoundException {

        List<List<String>> data = readAllLines(transactionsFileName, 0);

        List<String> row;
        commonCompanyProfit = Integer.parseInt(data.get(0).get(1));

        for(int i = 2; i < data.size(); i++){
            row = data.get(i);

            proceeds.put(Integer.parseInt(row.get(0)), Integer.parseInt(row.get(1)));
        }
    }

    public HashMap<Integer, Integer> getProceeds(){
        return proceeds;
    }
    public int getCommonProfit(){
        return commonCompanyProfit;
    }
}
