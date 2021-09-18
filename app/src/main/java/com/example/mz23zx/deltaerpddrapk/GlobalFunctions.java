package com.example.mz23zx.deltaerpddrapk;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalFunctions {
    private static Map<String,String> racks = new HashMap<>();

    public static void AlertVibration(Context cntx){
        long[] vpattern = new long[]{0, 300,    100, 300};
        Vibrator vibrator = (Vibrator) cntx.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createWaveform(vpattern,-1));
        }
        else {
            vibrator.vibrate(vpattern,-1);
        }
    }

    public static String Right(String value, int length) {
        // To get right characters from a string, change the begin index.
        return value.substring(value.length() - length);
    }

    public static String Left(String value, int length) {
        // To get right characters from a string, change the begin index.
        return value.substring(0,Math.min(value.length(),length));
    }

    public static void PopUp(String title, String msg, Context cntx)
    {
        AlertDialog.Builder dialog;
        dialog = new AlertDialog.Builder(cntx);
        dialog.setMessage(msg);
        dialog.setTitle(title);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        }).show();
        AlertVibration(cntx);
    }

    public static Boolean IsLocation(String local){
        String pattern = "^[0-9]{8}$";
        return local.matches(pattern);
    }

    public static Boolean IsSerialFormat(String serial){
        String pattern = "^[S]?\\d" + GlobalVariables.Parameters ("SYS_PlantCode","").toUpperCase() + "[0-9]{10}$";
        return serial.toUpperCase().matches(pattern);
    }

    public static Boolean IsLinkedlabelFormat(String serial){
        String pattern = "^[S]?\\d" + GlobalVariables.Parameters ("SYS_PlantCode","").toUpperCase() + "L[0-9]{9}$";
        return serial.toUpperCase().matches(pattern);
    }

    public static Boolean IsMasterSerialFormat(String serial){
        String pattern = "^[S]?\\d" + GlobalVariables.Parameters ("SYS_PlantCode","").toUpperCase() + "M[0-9]{9}$";
        return serial.toUpperCase().matches(pattern);
    }

    public static Boolean IsNumeric(String str) {
        return str != null && str.matches("^\\d*\\.?\\d+$");
        //[-+]?
    }

    public static void Log(String description , String key_word){
        SQL.Current().Insert("Sys_Log", new String[] {"[User]", "[Description]", "KeyWord"}, new Object[] {GlobalVariables.badge, GlobalFunctions.Left(description, 100), GlobalFunctions.Left(key_word, 50)});
    }

    public static String Warehouse(String location){
        if(racks.containsKey(GlobalFunctions.Left(location,2)) == true){
            return racks.get(GlobalFunctions.Left(location,2));
        }
        else{
            String value = SQL.Current().GetString("Warehouse", "Smk_Racks", "Rack", GlobalFunctions.Left(location,2));
            if(value.equals("") == true)
                value = GlobalVariables.Warehouse;
            racks.put(GlobalFunctions.Left(location,2), value);
            return value;
        }
    }

    public static Integer LinklabelID(String serial){
        return Integer.parseInt(GlobalFunctions.Right(serial,9));
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    // res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

}
