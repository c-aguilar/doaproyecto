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
import android.widget.TextView;
import android.widget.Toast;

public class CriticalActivity extends AppCompatActivity {
    Integer kanban_id = null;
    Integer critical_counter = 0;
    String partnumber;
    Float pieces;
    EditText kanban_txt, serial_txt;
    TextView counter_lbl;
    Serialnumber serial = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_critical);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        kanban_txt = (EditText) findViewById(R.id.kanban_txt);
        serial_txt = (EditText) findViewById(R.id.partnumber_txt);
        counter_lbl = (TextView) findViewById(R.id.counter_lbl);

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
        CleanAll();
    }


    public void Open(){
        Intent open_serial = new Intent(this, OpenSerial.class);
        startActivity(open_serial);
    }

    public void Empty(){
        Intent empty_serial = new Intent(this, EmptySerial.class);
        startActivity(empty_serial);
    }

    public void ReadKanban(){
        String kanban = kanban_txt.getText().toString().toUpperCase();
        if(kanban.matches("S\\d{8,10}")){
            kanban = kanban.replaceFirst("S","");
            kanban_id = SQL.Current().GetInteger("ID","CDR_Kanbans", new String[]{"ID"}, new Object[]{Integer.parseInt(kanban)});
            if (kanban_id != null){
                partnumber = SQL.Current().GetString("Partnumber","CDR_Kanbans","ID",Integer.parseInt(kanban));
                pieces = SQL.Current().GetFloat("Pieces","CDR_Kanbans", new String []{"ID"},new Object[] {kanban_id});
                serial_txt.requestFocus();
            }
            else{
                AlertDialog.Builder dialog;
                dialog = new AlertDialog.Builder(this);
                dialog.setMessage("La kanban no existe.");
                dialog.setTitle("Error");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
                CleanKanban();
            }
        }
        else {
            Toast.makeText(this, "Kanban incorrecta.", Toast.LENGTH_LONG).show();
            CleanKanban();
        }
    }

    public void ReadSerial()
    {
        if (kanban_id == null){
            ReadKanban();
        }
        if (kanban_id != null){
            String serial_str = serial_txt.getText().toString();
            serial = new Serialnumber(serial_str);
            if (serial.get_exist() == true)
            {
                if (serial.get_partnumber().equals(partnumber) == true){
                    if (serial.get_redtag() == true){
                        CleanSerial();
                        GlobalFunctions.PopUp("Error","Serie bloqueada por Calidad.",this);
                    }
                    else if (serial.get_invoicetrouble() == true) {
                        CleanSerial();
                        GlobalFunctions.PopUp("Error", "La Serie se encuentra en Tracker de Problemas.", this);
                    }
                    else {
                        switch (serial.get_status()) {
                            case New: case Pending: case Tracker: case Quality:
                                CleanSerial();
                                GlobalFunctions.PopUp("Error", "La Serie no ha sido dada de alta.", this);
                                break;
                            case Open: case OnCutter: case ServiceOnQuality:
                                pieces = SQL.Current().GetFloat(String.format("SELECT dbo.Sys_UnitConversion(K.Partnumber,'PC',K.Pieces,S.UoM) FROM CDR_Kanbans AS K INNER JOIN Smk_Serials AS S ON K.ID = %1$s AND S.SerialNumber = '%2$s';",kanban_id,serial.get_serialnumber()));
                                pieces = pieces == null || pieces == 0f ? 1f : pieces;
                                Float pcnt = serial.get_current_qty() / pieces;
                                if(pcnt > 1.1f){ //HAY MAS DE 110% DE PIEZAS
                                    serial.CriticalBinDiscount(pieces, kanban_id);
                                    critical_counter+=1;
                                    Toast.makeText(this, "Descuento realizado.", Toast.LENGTH_LONG).show();
                                    CleanAll();
                                }
                                else if (pcnt >= 0.9f) { //+- 10% DE VARIACION
                                    AlertDialog.Builder empty_dialog;
                                    empty_dialog = new AlertDialog.Builder(this);
                                    empty_dialog.setTitle("Vacio detectado");
                                    empty_dialog.setMessage("¿Declarar vacia la serie?");
                                    empty_dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            serial.CriticalBinDiscount(Math.min(pieces,serial.get_current_qty()), kanban_id);//DESCONTAR LA CONTENERIZACION O LAS PIEZAS RESTANTES, LO QUE SEA MENOR
                                            serial.Empty();
                                            critical_counter+=1;
                                            CleanAll();
                                            Toast.makeText(CriticalActivity.this, "Descuento realizado.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    empty_dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            serial.CriticalBinDiscount(Math.min(pieces,serial.get_current_qty()), kanban_id);//DESCONTAR LA CONTENERIZACION O LAS PIEZAS RESTANTES, LO QUE SEA MENOR
                                            critical_counter+=1;
                                            CleanAll();
                                            Toast.makeText(CriticalActivity.this, "Descuento realizado.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    empty_dialog.show();
                                    GlobalFunctions.AlertVibration(this);
                                }
                                else{ //NO SE LLENO NI EL 90% DEL CONTENEDOR
                                    AlertDialog.Builder empty_dialog;
                                    empty_dialog = new AlertDialog.Builder(CriticalActivity.this);
                                    empty_dialog.setTitle("Vacio detectado");
                                    empty_dialog.setMessage("¿Declarar vacia la serie?");
                                    empty_dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            serial.CriticalBinDiscount(Math.min(pieces,serial.get_current_qty()), kanban_id);//DESCONTAR LA CONTENERIZACION O LAS PIEZAS RESTANTES, LO QUE SEA MENOR
                                            serial.Empty();
                                            //VALIDAR SI EL BIN SE LLENO COMPLETAMENTE
                                            AlertDialog.Builder full_dialog;
                                            full_dialog = new AlertDialog.Builder( CriticalActivity.this);
                                            full_dialog.setTitle("Bin parcial detectado");
                                            full_dialog.setMessage("¿El Bin fue llenado completamente?");
                                            full_dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    critical_counter+=1;
                                                    CleanAll();
                                                }
                                            });
                                            full_dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    CleanSerial();
                                                }
                                            });
                                            full_dialog.show();
                                            GlobalFunctions.AlertVibration(CriticalActivity.this);
                                        }
                                    });
                                    empty_dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            serial.CriticalBinDiscount(Math.min(pieces,serial.get_current_qty()), kanban_id);//DESCONTAR LA CONTENERIZACION O LAS PIEZAS RESTANTES, LO QUE SEA MENOR
                                            critical_counter+=1;
                                            CleanAll();
                                            Toast.makeText(CriticalActivity.this, "Descuento realizado.", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    empty_dialog.show();
                                    GlobalFunctions.AlertVibration(this);
                                }
                                break;
                            case Stored:
                                GlobalFunctions.PopUp("Error", "La Serie se encuentra en reserva.", this);
                                CleanSerial();
                                break;
                            case Empty:
                                GlobalFunctions.PopUp("Error", "La Serie ya fue declarada vacia.", this);
                                CleanSerial();
                                break;
                            default:
                                GlobalFunctions.PopUp("Error", "Error al abrir la Serie.", this);
                                CleanSerial();
                        }
                    }
                }
                else{
                    AlertDialog.Builder dialog;
                    dialog = new AlertDialog.Builder(this);
                    dialog.setMessage("El número de parte no coincide.");
                    dialog.setTitle("Error");
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
                    CleanSerial();
                }
            }
            else{
                Toast.makeText(this, "Número de serie incorrecta.", Toast.LENGTH_LONG).show();
                CleanSerial();
            }
        }
        else{
            CleanAll();
        }
    }

    private void CleanAll(){
        CleanSerial();
        CleanKanban();
        counter_lbl.setText(critical_counter.toString());
    }
    private  void CleanKanban(){
        partnumber="";
        pieces = 0f;
        kanban_id = null;
        kanban_txt.setText("");
        kanban_txt.requestFocus();
    }
    private void CleanSerial(){
        serial_txt.setText("");
        serial_txt.requestFocus();
    }
}
