package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MethodsForString {
    public static List<String> removeSpaces(String src){
        String[] arr = src.split("\\s+");

        ArrayList<String> res = new ArrayList<>();

        for(String str: arr){
            if(!str.equals(""))
                res.add(str);
        }
        return res;
    }
}
