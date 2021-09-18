package com.example.mz23zx.deltaerpddrapk;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Picking extends AppCompatActivity {
    String cart, status, partnumber,warehouse;
    Integer loop,kanban_loop_id, total_kanbans, kanban_id;
    EditText kanban_txt, serial_txt;
    TextView counter_lbl;
    Float pieces;
    Serialnumber serial = null;
    FloatingActionButton fab_critical;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_open = (FloatingActionButton) findViewById(R.id.fab_open);
        fab_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Open();
            }
        });

        FloatingActionButton fab_empty = (FloatingActionButton) findViewById(R.id.fab_empty);
        fab_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Empty();
            }
        });

        FloatingActionButton fab_keyboard = (FloatingActionButton) findViewById(R.id.fab_keyboard);
        fab_keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Keyboard();
            }
        });

        fab_critical = (FloatingActionButton) findViewById(R.id.fab_critical);
        fab_critical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMissing();
            }
        });

        cart = getIntent().getExtras().getString("cart");
        status = getIntent().getExtras().getString("status");
        loop = getIntent().getExtras().getInt("loop");
        warehouse = SQL.Current().GetString("Warehouse","DDR_Carts","ID", cart);

        kanban_txt = (EditText) findViewById(R.id.kanban_txt);
        serial_txt = (EditText) findViewById(R.id.partnumber_txt);
        counter_lbl = (TextView) findViewById(R.id.counter_lbl);

        kanban_txt.setShowSoftInputOnFocus(false);
        serial_txt.setShowSoftInputOnFocus(false);

        //METODO AL PRESIONAR ENTER
        kanban_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ReadKanban();
                    return true;
                }
                return false;
            }
        });

        serial_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ReadSerial();
                    return true;
                }
                return false;
            }
        });

        total_kanbans = SQL.Current().GetInteger(String.format("SELECT COUNT(ID) FROM DDR_CartsLoopKanbans WHERE LoopID = %1$s",loop));
        RefreshItems();
        CleanAll();
    }

    public void Keyboard(){
        kanban_txt.setShowSoftInputOnFocus(true);
        serial_txt.setShowSoftInputOnFocus(true);
        kanban_txt.requestFocus();
    }

    public void ReadKanban(){
        String kanban = kanban_txt.getText().toString().toUpperCase();
        if(kanban.matches("S\\d{8,10}")){
            kanban = kanban.replaceFirst("S","");
            kanban_loop_id = SQL.Current().GetInteger("ID","DDR_CartsLoopKanbans", new String[]{"Kanban","LoopID"}, new Object[]{Integer.parseInt(kanban),loop});
            if (kanban_loop_id != null){
                String kanban_loop_id_status = SQL.Current().GetString("Status","DDR_CartsLoopKanbans","ID",kanban_loop_id);
                if(kanban_loop_id_status.equals("F")){
                    GlobalFunctions.PopUp("Error", "El Bin ya fue surtido.", this);
                }
                else if (kanban_loop_id_status.equals("P")){
                    String part_kanban = SQL.Current().GetString("S.Partnumber","Smk_DDRSerialDiscount AS D INNER JOIN Smk_SerialMovements AS M ON D.SerialMovementID = M.ID INNER JOIN Smk_Serials AS S ON M.SerialID = S.ID","D.KanbanLoopID",kanban_loop_id);
                    partnumber = part_kanban == null ? "" : part_kanban;
                    fab_critical.show();
                    CleanSerial();
                }
                else{
                    kanban_id = SQL.Current().GetInteger("ID","CDR_Kanbans", new String[]{"ID"}, new Object[]{Integer.parseInt(kanban)});
                    String part_kanban = SQL.Current().GetString("Partnumber","CDR_Kanbans","ID",Integer.parseInt(kanban));
                    partnumber = part_kanban == null ? "" : part_kanban;
                    fab_critical.show();
                    CleanSerial();
                }
            }
            else{
                GlobalFunctions.PopUp("Error", "La kanban no fue escaneada al entrar.", this);
                CleanKanban();
            }
        }
        else {
            Toast.makeText(Picking.this, "Kanban incorrecta.", Toast.LENGTH_LONG).show();
            CleanKanban();
        }
    }

    public void SetMissing(){
        if (kanban_loop_id != null){
            //TENER EL CUENTA EL MATERIAL CONTROLADO
            Integer current_available = SQL.Current().GetInteger(String.format("SELECT COUNT(ID) FROM Smk_Serials WHERE Partnumber = '%1$s' AND Status IN ('O') AND Warehouse = '%2$s' AND Location <> '88888888'", partnumber, warehouse));
            if (current_available == 0){
                current_available = SQL.Current().GetInteger(String.format("SELECT COUNT(ID) FROM Smk_Serials WHERE Partnumber = '%1$s' AND ((Status = 'S' AND Warehouse = '%2$s') OR Status IN ('N','P'))", partnumber, warehouse));
                if (SQL.Current().Exists("Smk_MissingAlerts",new String[] {"Partnumber","Warehouse", "Badge", "Active"},new Object[] {partnumber,warehouse, GlobalVariables.badge, 1}) == false)
                    SQL.Current().Insert("Smk_MissingAlerts",new String[] {"Partnumber", "Warehouse", "Badge"},new Object[] {partnumber, warehouse, GlobalVariables.badge});

                SQL.Current().Update("DDR_CartsLoopKanbans", "Status", "M", "ID", kanban_loop_id); //STATUS MISSING
                CleanAll();
                RefreshItems();
                if(current_available > 0)
                    Toast.makeText(this,"Faltante reportado a dueño de Rack.",Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(this,"Critico reportado.",Toast.LENGTH_LONG).show();
            }
            else{
                GlobalFunctions.PopUp("Error", "Hay material en sistema para surtir el Bin.", this);
            }
        }
    }

    public void ReadSerial()
    {
        if(kanban_loop_id == null){
            ReadKanban();
        }
        if (kanban_loop_id != null){
            String serial_str = serial_txt.getText().toString();
            if (GlobalFunctions.IsLinkedlabelFormat(serial_str) == true){
                //REEMPLAZAR LINKSERIAL POR SERIALNUMBER
                serial_str = SQL.Current().GetString(String.format("SELECT TOP 1 S.Serialnumber FROM Smk_LinkLabelMovements AS M INNER JOIN Smk_LinkLabels AS L ON M.LinkID = L.ID INNER JOIN Smk_Serials AS S ON M.SerialID = S.ID WHERE L.Linklabel = '%1$S' ORDER BY M.[Date] DESC",serial_str));
                if (serial_str == null || serial_str.equals("") == true){
                    CleanSerial();
                    GlobalFunctions.PopUp("Error","La arteza no esta enlazada a ninguna serie.",this);
                    return;
                }
            }
            else if (GlobalFunctions.IsSerialFormat(serial_str) != true){
                CleanSerial();
                GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
                return;
            }

            serial = new Serialnumber(serial_str);
            if (serial.get_exist() == true)
            {
                if (serial.get_partnumber().equals(partnumber) == true || partnumber.equals("") == true){
                    if (serial.get_redtag() == true){
                        GlobalFunctions.PopUp("Error","Serie bloqueada por Calidad.",this);
                        CleanSerial();
                    }
                    else if (serial.get_invoicetrouble() == true) {
                        GlobalFunctions.PopUp("Error", "La serie se encuentra en Tracker de Problemas.", this);
                        CleanSerial();
                    }
                    else {
                        switch (serial.get_status()) {
                            case New: case Pending: case Tracker: case Quality:
                                GlobalFunctions.PopUp("Error", "La serie no ha sido dada de alta.", this);
                                CleanSerial();
                                break;
                            case Open: case OnCutter: case ServiceOnQuality:
                                pieces = SQL.Current().GetFloat(String.format("SELECT dbo.Sys_UnitConversion(K.Partnumber,'PC',K.Pieces,S.UoM) FROM CDR_Kanbans AS K INNER JOIN Smk_Serials AS S ON K.ID = %1$s AND S.SerialNumber = '%2$s';",kanban_id,serial.get_serialnumber()));
                                pieces = pieces == null || pieces == 0f ? 1f : pieces;
                                Float pcnt = serial.get_current_qty() / pieces;
                                if(pcnt > 1.1f){ //HAY MAS DE 110% DE PIEZAS
                                    DiscountQty(pieces);
                                    SQL.Current().Update("DDR_CartsLoopKanbans", "Status", "F", "ID", kanban_loop_id);
                                    RefreshItems();
                                    ValidateEnd();
                                    CleanAll();
                                }
                                else if (pcnt >= 0.9f) { //+- 10% DE VARIACION
                                    AlertDialog.Builder empty_dialog;
                                    empty_dialog = new AlertDialog.Builder(Picking.this);
                                    empty_dialog.setTitle("Vacio detectado");
                                    empty_dialog.setMessage("¿Declarar vacia la serie?");
                                    empty_dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            DiscountQty(Math.min(pieces,serial.get_current_qty())); //DESCONTAR LA CONTENERIZACION O LAS PIEZAS RESTANTES, LO QUE SEA MENOR
                                            serial.Empty();
                                            SQL.Current().Update("DDR_CartsLoopKanbans", "Status", "F", "ID", kanban_loop_id); //STATUS FINISHED
                                            RefreshItems();
                                            ValidateEnd();
                                            CleanAll();
                                        }
                                    });
                                    empty_dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            DiscountQty(Math.min(pieces,serial.get_current_qty())); //DESCONTAR LA CONTENERIZACION O LAS PIEZAS RESTANTES, LO QUE SEA MENOR
                                            SQL.Current().Update("DDR_CartsLoopKanbans", "Status", "F", "ID", kanban_loop_id); //STATUS FINISHED
                                            RefreshItems();
                                            ValidateEnd();
                                            CleanAll();
                                        }
                                    });
                                    empty_dialog.show();
                                    GlobalFunctions.AlertVibration(this);
                                }
                                else{ //NO SE LLENO NI EL 90% DEL CONTENEDOR
                                    AlertDialog.Builder empty_dialog;
                                    empty_dialog = new AlertDialog.Builder(Picking.this);
                                    empty_dialog.setTitle("Vacio detectado");
                                    empty_dialog.setMessage("¿Declarar vacia la serie?");
                                    empty_dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            DiscountQty(Math.min(pieces,serial.get_current_qty())); //DESCONTAR LA CONTENERIZACION O LAS PIEZAS RESTANTES, LO QUE SEA MENOR
                                            serial.Empty();
                                            //VALIDAR SI EL BIN SE LLENO COMPLETAMENTE
                                            AlertDialog.Builder full_dialog;
                                            full_dialog = new AlertDialog.Builder(Picking.this);
                                            full_dialog.setTitle("Bin parcial detectado");
                                            full_dialog.setMessage("¿El Bin fue llenado completamente?");
                                            full_dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    SQL.Current().Update("DDR_CartsLoopKanbans", "Status", "F", "ID", kanban_loop_id); //STATUS FINISHED
                                                    RefreshItems();
                                                    ValidateEnd();
                                                    CleanAll();
                                                }
                                            });
                                            full_dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    SQL.Current().Update("DDR_CartsLoopKanbans", "Status", "P", "ID", kanban_loop_id); //STATUS PARTIAL
                                                    RefreshItems();
                                                    ValidateEnd();
                                                    CleanAll();
                                                }
                                            });
                                            full_dialog.show();
                                        }
                                    });
                                    empty_dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            DiscountQty(Math.min(pieces,serial.get_current_qty())); //DESCONTAR LA CONTENERIZACION O LAS PIEZAS RESTANTES, LO QUE SEA MENOR
                                            SQL.Current().Update("DDR_CartsLoopKanbans", "Status", "F", "ID", kanban_loop_id); //STATUS FINISHED
                                            RefreshItems();
                                            ValidateEnd();
                                            CleanAll();
                                        }
                                    });
                                    empty_dialog.show();
                                    GlobalFunctions.AlertVibration(this);
                                }
                                break;
                            case Stored:
                                GlobalFunctions.PopUp("Error", "La serie se encuentra en reserva.", this);
                                CleanSerial();
                                break;
                            case Empty:
                                if(Boolean.parseBoolean(GlobalVariables.Parameters("SMK_IMSAllowEmptyRelation","False")) == true){
                                    DiscountQty(0f);
                                    SQL.Current().Update("DDR_CartsLoopKanbans", "Status", "F", "ID", kanban_loop_id);
                                    RefreshItems();
                                    ValidateEnd();
                                    CleanAll();
                                }
                                else{
                                    GlobalFunctions.PopUp("Error", "La serie ya fue declarada vacia.", this);
                                    CleanSerial();
                                }

                                break;
                            default:
                                GlobalFunctions.PopUp("Error", "Error al abrir la serie.", this);
                                CleanSerial();
                        }
                    }
                }
                else{
                    GlobalFunctions.PopUp("Error","El número de parte no coincide.",this);
                    CleanSerial();
                }
            }
            else{
                GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
                CleanSerial();
            }
        }
        else{
            CleanAll();
        }
    }

    public void CleanAll(){
        CleanSerial();
        CleanKanban();
        kanban_txt.setShowSoftInputOnFocus(false);
        serial_txt.setShowSoftInputOnFocus(false);
    }

    public void CleanKanban(){
        kanban_txt.requestFocus();
        kanban_loop_id = null;
        partnumber="";
        pieces=0f;
        kanban_txt.setText("");
        fab_critical.hide();
    }

    public void CleanSerial(){
        serial_txt.requestFocus();
        serial_txt.setText("");
        serial=null;
    }

    public void DiscountQty(Float qty){
        serial.DDRPartialDiscount(qty,kanban_loop_id);
        Toast.makeText(Picking.this, "Descuento realizado.", Toast.LENGTH_LONG).show();
    }

    public void ValidateEnd(){
        final ListView lv = (ListView) findViewById(R.id.items_vw);
        if (lv.getCount() == 0)
        {
            SQL.Current().Execute("UPDATE DDR_CartsLoopRegister SET PickingEndDate = GETDATE() WHERE ID = " + loop.toString());
            this.onBackPressed();
        }
    }

    public void RefreshItems(){
        ArrayList kanbans = GetItems();
        final ListView lv = (ListView) findViewById(R.id.items_vw);
        lv.setAdapter(new CustomListAdapter(this, kanbans));
        Integer counter = SQL.Current().GetInteger(String.format("SELECT COUNT(ID) FROM DDR_CartsLoopKanbans WHERE [Status] IN ('F','C','M') AND LoopID = %1$s",loop));
        counter_lbl.setText(String.format("%1$s/%2$s",counter,total_kanbans));

    }

    public ArrayList GetItems(){
        ArrayList<ListItem> results = new ArrayList<>();
        List<Map<String, Object>> table = SQL.Current().GetTable(String.format("SELECT ISNULL(K.Code,'S'+RIGHT('000000'+CONVERT(VARCHAR(12),L.Kanban),8)) AS Code,ISNULL(K.Partnumber,'N/D') AS Partnumber,ISNULL(R.[Description],'N/D') AS [Description],ISNULL(M.Location,'00000000') AS Location,L.[Status] FROM DDR_CartsLoopKanbans AS L LEFT OUTER JOIN CDR_Kanbans AS K ON L.Kanban = K.ID LEFT OUTER JOIN Sys_RawMaterial AS R ON K.Partnumber = R.Partnumber LEFT OUTER JOIN Smk_Map AS M ON K.Partnumber = M.Partnumber AND M.Warehouse = '%2$s' WHERE L.LoopID = %1$s AND L.Status IN ('H','P') ORDER BY M.Location",loop,warehouse));
        for(Map row : table){
            ListItem item = new ListItem();
            item.setPartnumber(row.get("partnumber").toString());
            item.setDescription(row.get("description").toString());
            item.setKanban(row.get("code").toString());
            item.setLocation(row.get("location").toString());
            results.add(item);
        }
        return results;
    }

    public void Open(){
        Intent open_serial = new Intent(this, OpenSerial.class);
        startActivity(open_serial);
    }

    public void Empty(){
        Intent empty_serial = new Intent(this, EmptySerial.class);
        startActivity(empty_serial);
    }
}
