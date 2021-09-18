package com.example.mz23zx.deltaerpddrapk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Masterserial {
    String _masterserial;
    String _badge;
    Boolean _exists;
    Integer _id;
    ArrayList<Serialnumber> serials = new ArrayList<Serialnumber>();
    public Masterserial (String s) {
        if (s.toUpperCase().startsWith("S")) {
            s = s.substring(1, s.length() - 1);
        }
        _masterserial = s.toUpperCase();
        Map<String, Object> master = SQL.Current().GetRecord(String.format("SELECT * FROM Smk_MasterLabels WHERE Masternumber = '%1$s'", _masterserial));
        if (master != null && master.size() > 0) {
            _badge = master.get("badge").toString();
            _id = Integer.parseInt( master.get("id").toString());
            List<Map<String, Object>> table =   SQL.Current().GetTable("S.Serialnumber","Smk_MasterSerials AS M INNER JOIN Smk_Serials AS S ON M.SerialID = S.ID",new String[]{"MasterID"},new Object[]{_masterserial});
            for(Map row : table){
                serials.add(new Serialnumber(row.get("serialnumber").toString()));
            }
            _exists = true;
        }
        else
        {
            _exists = false;
        }
    }

    public Boolean Store(String location){
        String insert_qry = "";
        for (Serialnumber m_serial : this.getSerials()){
            insert_qry += String.format("INSERT INTO Smk_SerialMovements (SerialID,Movement,Quantity,Badge,Location) VALUES (%1$s,'SCR',0,'%2$s','%3$s');",m_serial.get_id(),GlobalVariables.badge,location);
            insert_qry += String.format("UPDATE Smk_Serials SET Location = '%1$s',[Status] = 'S' WHERE ID = %2$s;",location,m_serial.get_id());
        }
        if (this.getSerials().stream().filter(s -> (s.get_consumption() == Serialnumber.ConsumptionType.Obsolete || s.get_consumption() == Serialnumber.ConsumptionType.Service) && s.get_randomsloc().equals("0001") == false).count() > 0)
            insert_qry += String.format("INSERT INTO Smk_SAPTransfers (MovementID,Quantity,FromSloc,ToSloc) SELECT M.ID,S.Quantity,'0001','%1$s' FROM Smk_SerialMovements AS M INNER JOIN Smk_Serials AS S ON M.SerialID = S.ID INNER JOIN Smk_MasterSerials AS MS ON S.ID = MS.SerialID WHERE M.Movement = 'SCR' AND M.Badge = '%2$s' AND MS.MasterID = %3$s;", this.getSerials().get(0).get_randomsloc(), GlobalVariables.badge, this.get_id());
        if(SQL.Current().Execute(insert_qry) == true)
            return true;
        return false;
    }

    public Boolean ChangeLocation(String location){
        String insert_qry = "";
        for (Serialnumber m_serial : this.getSerials()){
            insert_qry += String.format("INSERT INTO Smk_SerialMovements (SerialID,Movement,Quantity,Badge,Location) VALUES (%1$s,'CLN',0,'%2$s','%3$s');",m_serial.get_id(),GlobalVariables.badge,location);
            insert_qry += String.format("UPDATE Smk_Serials SET Location = '%1$s' WHERE ID = %2$s;",location,m_serial.get_id());
        }
        if(SQL.Current().Execute(insert_qry) == true)
            return true;
        return false;
    }



    public Boolean get_exists() {
        return _exists;
    }

    public String get_badge() {
        return _badge;
    }

    public void set_badge(String badge) {
        this._badge = badge;
    }

    public String get_masterserial() {
        return _masterserial;
    }

    public Integer get_id() {
        return _id;
    }

    public ArrayList<Serialnumber> getSerials() {
        return serials;
    }

    public Float get_totalQuantity(){
        Float total = 0f;
        for (Serialnumber s: this.getSerials()) {
            total+= s.get_quantity();
        }
        return total;
    }

    public MasterStatus get_generalStatus(){
       if (this.getSerials().stream().filter(s -> s.get_status() == Serialnumber.SerialStatus.Pending).count() == this.getSerials().size()){
            return MasterStatus.Pending;
        }
        else if (this.getSerials().stream().filter(s -> s.get_status() == Serialnumber.SerialStatus.Stored).count() == this.getSerials().size()){
           return MasterStatus.Stored;
       }
       else if (this.getSerials().stream().filter(s -> s.get_status() == Serialnumber.SerialStatus.Quality).count() == this.getSerials().size()){
           return MasterStatus.Quality;
       }
       else if (this.getSerials().stream().filter(s -> s.get_status() == Serialnumber.SerialStatus.Tracker).count() == this.getSerials().size()){
           return MasterStatus.Tracker;
       }
       else if (this.getSerials().stream().filter(s -> s.get_status() == Serialnumber.SerialStatus.Deleted).count() == this.getSerials().size()){
           return MasterStatus.Deleted;
       }
       else{
           return MasterStatus.Mixed;
       }
    }

    public String get_generalLocation(){
        String location = this.getSerials().get(0).get_location();
        if(this.getSerials().stream().filter(s -> s.get_location().equals(location) == true).count() == this.getSerials().size()){
            return location;
        }
        return "";
    }

    public Boolean containsRedTag(){
        if(this.getSerials().stream().filter(s -> s.get_redtag() == true).count() > 0){
            return true;
        }
        return false;
    }

    public Boolean containsInvoiceTrouble(){
        if(this.getSerials().stream().filter(s -> s.get_invoicetrouble() == true).count() > 0){
            return true;
        }
        return false;
    }

    public enum MasterStatus{
        Pending,
        Stored,
        Quality,
        Tracker,
        Deleted,
        Mixed,
    }
}
