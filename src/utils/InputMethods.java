package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class InputMethods {

    public static List<List<String>> readAllLines(String fileName, int since){

        List<List<String>> table = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
            String str;

            for(int i = 0; i < since; i++)
                reader.readLine();

            while ((str = reader.readLine()) != null) {

                table.add(Arrays.asList(str.split(" ")));
            }


        }
        catch(IOException exc){
            exc.printStackTrace();
        }
        return table;
    }

    public static String readProcessing(String regex, String badInputMsg, BufferedReader reader)throws IOException{
        String str = reader.readLine();

        while (!Pattern.matches(regex, str)) {
            if (Pattern.matches("\\s*back\\s*", str)){
                str = "back";
                break;
            }
            System.out.println(badInputMsg);
            str = reader.readLine();
        }

        return str;
    }

    //формирует список свободных id для какого либо объекта
    public static List<Integer> parseFreeId(List<Integer> busyId){
        busyId = busyId.stream().sorted().toList();

        List<Integer> freeId = new ArrayList<>();

        boolean continueFlag = true;
        int i = 0;
        int movingId = 1;
        int id;
        while(continueFlag){
            if(i == busyId.size())
                continueFlag = false;

            else {
                id = busyId.get(i);

                if (id == movingId) {
                    i++;
                } else {
                    freeId.add(movingId);
                }

                movingId++;
            }
        }
        freeId.add(movingId);

        return freeId;
    }
}
