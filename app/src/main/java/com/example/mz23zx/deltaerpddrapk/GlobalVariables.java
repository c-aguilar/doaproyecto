package com.example.mz23zx.deltaerpddrapk;

import java.util.HashMap;
import java.util.Map;

public class GlobalVariables {
    public static String badge;
    public static String SettingsName  = "DeltaSettings";
    public static String Server,Instance,Database,UID,Password;
    private static Map<String,String> params = new HashMap<>();
    public static String Warehouse;
    public static String Parameters(String parameter,String default_value){
        if(params.containsKey(parameter) == true){
            return params.get(parameter);
        }
        else{
            String value = SQL.Current().GetString("Value", "Sys_Parameters", "Parameter", parameter);
            if(value.equals("") == true)
                value = default_value;
            params.put(parameter,value);
            return value;
        }
    }




}
