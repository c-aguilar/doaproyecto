package com.example.mz23zx.deltaerpddrapk;

public class RawMaterial {

    public static String NextFIFO(String partnumber, String warehouse)
    {
        String fifo  = SQL.Current().GetString(String.format("SELECT TOP 1 Serialnumber FROM vw_Smk_Serials WHERE Partnumber = '%1$s' AND Warehouse = '%2$s' AND Status IN ('S','Q','T') AND RedTag = 0 AND InvoiceTrouble = 0 AND CurrentQuantity > 0 ORDER BY ID ASC", partnumber, warehouse));
        if (fifo == "") {
            fifo = NextFIFO(partnumber);
        }
        return fifo;
    }

    public static String NextFIFO(String partnumber)
    {
        String fifo  = SQL.Current().GetString(String.format("SELECT TOP 1 Serialnumber FROM vw_Smk_Serials WHERE Partnumber = '%1$s' AND Status IN ('S','Q','T') AND RedTag = 0 AND InvoiceTrouble = 0 AND CurrentQuantity > 0 ORDER BY ID ASC", partnumber));
        return fifo;
    }

    public static Integer Minimum(String partnumber ,String warehouse){
        Integer m = SQL.Current().GetInteger("Minimum", "Smk_Map",new String[] {"Partnumber", "Warehouse"},new Object[] {partnumber, warehouse});
        return m == null ? 0 : m;
    }

    public static Integer Maximum(String partnumber ,String warehouse){
        Integer m = SQL.Current().GetInteger("Maximum", "Smk_Map",new String[] {"Partnumber", "Warehouse"},new Object[] {partnumber, warehouse});
        return m == null ? 0 : m;
    }

    public static String CurrentSerial(String partnumber, Boolean ignore_zero){
        if( ignore_zero == true) {
            return SQL.Current().GetString(String.format("SELECT TOP 1 Serialnumber FROM vw_Smk_Serials WHERE Partnumber = '%1$s' AND Status IN ('O','C') AND CurrentQuantity > 0 ORDER BY ID ASC;", partnumber));
        }
        else{
            return SQL.Current().GetString(String.format("SELECT TOP 1 Serialnumber FROM vw_Smk_Serials WHERE Partnumber = '%1$s' AND Status IN ('O','C') ORDER BY ID ASC;", partnumber)) ;
        }
    }

    public static  String GetServiceLocations(String partnumber)
    {
        return   SQL.Current().GetString(String.format("SELECT dbo.Smk_Locations('%1$s');", partnumber));
    }

    public static ConsumptionType ConsumptionType(String partnumber){
        String ct = SQL.Current().GetString("ConsumptionType","Sys_RawMaterial","Partnumber",partnumber);
        switch (ct.toLowerCase()){
            case "mixed":
                return ConsumptionType.Mixed;
            case "partial":
                return ConsumptionType.Partial;
            case "total":
                return ConsumptionType.Total;
            case "obsolete":
                return ConsumptionType.Obsolete;
            case "service":
                return ConsumptionType.Service;
            default:
                return ConsumptionType.Total;
        }
    }

    public enum ConsumptionType{
        Mixed,
        Partial,
        Total,
        Obsolete,
        Service
    }

    public enum UnitOfMeasure{
        PC,
        M,
        FT,
        LB,
        KG,
        L,
        ROL
    }
    public enum MaterialType{
        Cable,
        Terminal,
        TerminalAssembly,
        Seal,
        Component,
        Conduit,
        Tube,
        Tape,
        Chemical,
        Harness
    }
}
