package files.savers;

import controllers.common.TransactionController;
import files.parsers.FileTransactionsParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FileTransactionSaver {

    public static final String transactionsFileName = "data/transactions/ProfitData.txt";
    public static final String header1 = "CommonProfit:";
    public static final String header2 = "Point_ID Proceeds";

    private TransactionController controller;

    public FileTransactionSaver(){
        controller = TransactionController.getTransactionsControllerInstance();

        if(controller.isChanged())
            saveChanges();
    }

    public void saveChanges(){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(transactionsFileName, false))){

            writer.write(header1 + " " + controller.getCommonProfit() + "\n");
            writer.write(header2 + "\n");

            List<Map.Entry<Integer, Integer>> table = controller.getTableOfProceeds()
                    .entrySet()
                    .stream()
                    .sorted((entry1, entry2)->{
                        return entry1.getKey().compareTo(entry2.getKey());
                    })
                    .toList();

            for(Map.Entry<Integer, Integer> entry: table){
                writer.write(entry.getKey() + " " + entry.getValue() + "\n");
            }

        }
        catch(IOException exc){
            exc.printStackTrace();
        }

    }
}
