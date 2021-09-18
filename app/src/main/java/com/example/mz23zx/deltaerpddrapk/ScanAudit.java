package com.example.mz23zx.deltaerpddrapk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.CaptivePortal;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ScanAudit extends AppCompatActivity {
    EditText serial_txt;
    TextView counter_lbl;
    Integer scan_containers =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_audit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        serial_txt = (EditText) findViewById(R.id.serial_txt);
        counter_lbl = (TextView) findViewById(R.id.counter_lbl);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CleanSerial();
            }
        });

        serial_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ReadSerial();
                    return true;
                }
                return false;
            }
        });

        serial_txt.requestFocus();
    }

    private void ReadSerial(){
        String serial_str = serial_txt.getText().toString();
        Serialnumber serial = new Serialnumber(serial_str);
        if(serial.get_exist() == true){
                if (serial.get_redtag() == true){
                    GlobalFunctions.PopUp("Error","Serie bloqueada por calidad.",this);
                    CleanSerial();
                }
                else if (serial.get_invoicetrouble() == true){
                    GlobalFunctions.PopUp("Error","La serie se encuentra en Tracker de Problemas.",this);
                    CleanSerial();
                }
                else{
                    switch (serial.get_status()){
                        case New: case Pending:
                            PopUpNoStored(serial.get_serialnumber());
                            CleanSerial();
                            break;
                        case Open: case OnCutter: case ServiceOnQuality:
                            GlobalFunctions.AlertVibration(this);
                            final EditText input = new EditText(this);
                            input.setInputType(InputType.TYPE_CLASS_NUMBER );
                            input.setHint("Escribe el Local");

                            AlertDialog.Builder open_dialog;
                            open_dialog = new AlertDialog.Builder(ScanAudit.this);
                            open_dialog.setTitle("Serie Abierta");
                            open_dialog.setMessage("Si el contenedor esta COMPLETO presiona SI para reactivarlo.");
                            open_dialog.setView(input);
                            open_dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String local = input.getText().toString();
                                    if (GlobalFunctions.IsLocation(local) == true){
                                        serial.ReturnServiceToRandom(local);
                                        ConfirmAudit();
                                    }
                                    else
                                    {
                                        GlobalFunctions.PopUp("Error","Local incorrecto. Operacion cancelada." ,ScanAudit.this);
                                    }
                                }
                            });
                            open_dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    GlobalFunctions.PopUp("Serie Abierta","Coloca el contenedor en su local de servicio. " + RawMaterial.GetServiceLocations(serial.get_partnumber()) ,ScanAudit.this);
                                }
                            });
                            open_dialog.show();
                            CleanSerial();
                            break;
                        case Stored: case Tracker: case Quality:
                            if (serial.ScanAudit() == true){
                                ConfirmAudit();
                            }
                            else{
                                GlobalFunctions.PopUp("Error","Error al auditar la Serie.",this);
                                CleanSerial();
                            }
                            break;
                        case Empty:
                            GlobalFunctions.AlertVibration(this);
                            final EditText input_empty = new EditText(this);
                            input_empty.setInputType(InputType.TYPE_CLASS_NUMBER );
                            input_empty.setHint("Escribe el Local");

                            AlertDialog.Builder empty_dialog;
                            empty_dialog = new AlertDialog.Builder(ScanAudit.this);
                            empty_dialog.setTitle("Serie Vacia");
                            empty_dialog.setMessage("Si el contenedor esta COMPLETO presiona SI para reactivarlo.");
                            empty_dialog.setView(input_empty);
                            empty_dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String local = input_empty.getText().toString();
                                    if (GlobalFunctions.IsLocation(local) == true){
                                        serial.ReturnEmptyToRandom(local);
                                        ConfirmAudit();
                                    }
                                    else
                                    {
                                        GlobalFunctions.PopUp("Error","Local incorrecto. Operacion cancelada." ,ScanAudit.this);
                                    }
                                }
                            });
                            empty_dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    GlobalFunctions.PopUp("Serie Vacia","Coloca el contenedor en su local de servicio. " + RawMaterial.GetServiceLocations(serial.get_partnumber()) ,ScanAudit.this);
                                }
                            });
                            empty_dialog.show();
                            CleanSerial();
                            break;
                        default:
                            GlobalFunctions.PopUp("Error","Error al auditar la Serie.",this);
                            CleanSerial();
                    }
                }
        }
        else{
            GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
            CleanSerial();
        }
    }


    private void CleanSerial(){
        serial_txt.setText("");
        serial_txt.requestFocus();
    }

    private void ConfirmAudit(){
        scan_containers +=1;
        counter_lbl.setText(scan_containers.toString());
        CleanSerial();
    }

    private void PopUpNoStored(String serial)
    {
        GlobalFunctions.AlertVibration(this);
        AlertDialog.Builder dialog;
        dialog = new AlertDialog.Builder(this);
        dialog.setMessage("La serie no ha sido dada de alta.");
        dialog.setTitle("Error");
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                LaunchIntentStore(serial);
               dialog.cancel();
            }
        }).show();

    }
    private void LaunchIntentStore(String serial){
        Intent i = new Intent(this, StoreActivity.class);
        i.putExtra("Serialnumber",serial);
        i.putExtra("CloseAfterStore",true);
        startActivity(i);
    }
}