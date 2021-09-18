package com.example.mz23zx.deltaerpddrapk;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLClientInfoException;

public class LinkAndOpenActivity extends AppCompatActivity {
    EditText serial_txt, linkserial_txt;
    TextView counter_lbl;
    Integer open_containers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_and_open);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        serial_txt = (EditText) findViewById(R.id.serial_txt);
        linkserial_txt = (EditText) findViewById(R.id.linkserial_txt);
        counter_lbl = (TextView) findViewById(R.id.counter_lbl);

        serial_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    linkserial_txt.requestFocus();
                    return true;
                }
                return false;
            }
        });

        linkserial_txt.setOnKeyListener(new View.OnKeyListener() {
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

    private void ReadSerial() {
        String link_serialnumber = linkserial_txt.getText().toString();
        String link_partnumber="";
        if(GlobalFunctions.IsLinkedlabelFormat(link_serialnumber)==true){
            if(SQL.Current().Exists("Smk_Linklabels","Linklabel",link_serialnumber) == true){
               String last_serial_str = SQL.Current().GetString(String.format("SELECT TOP 1 S.SerialNumber FROM Smk_LinkLabelMovements AS M INNER JOIN Smk_LinkLabels AS L ON M.LinkID = L.ID INNER JOIN Smk_Serials AS S ON M.SerialID = S.ID WHERE L.Linklabel = '%1$s' ORDER BY M.[Date] DESC", link_serialnumber));
               if(last_serial_str == null || last_serial_str.equals("") == true){
                   link_partnumber = SQL.Current().GetString("Partnumber","Smk_LinkLabels","Linklabel",link_serialnumber);
               }
               else{
                   String last_status = SQL.Current().GetString("[Status]","Smk_Serials","Serialnumber",last_serial_str);
                   if (last_status.toUpperCase().equals("E") == true){
                       link_partnumber = SQL.Current().GetString("Partnumber","Smk_Linklabels","Linklabel",link_serialnumber);
                   }
                   else{
                       linkserial_txt.setText("");
                       linkserial_txt.requestFocus();
                       GlobalFunctions.PopUp("Error", "La arteza aun esta enlazada a otra serie.\n" + last_serial_str, this);
                       return;
                   }
               }
            }
            else{
                linkserial_txt.setText("");
                linkserial_txt.requestFocus();
                GlobalFunctions.PopUp("Error", "La Etiqueta de Enlace no existe.", this);
                return;
            }
        }
        else{
            linkserial_txt.setText("");
            linkserial_txt.requestFocus();
            GlobalFunctions.PopUp("Error", "Etiqueta de Enlace incorrecta.", this);
            return;
        }

        String serial_str = serial_txt.getText().toString();
        Serialnumber serial = new Serialnumber(serial_str);
        if (serial.get_exist() == true) {

            if(serial.get_partnumber().toUpperCase().equals(link_partnumber.toUpperCase())==false){
                linkserial_txt.setText("");
                linkserial_txt.requestFocus();
                GlobalFunctions.PopUp("Error", "Los números de parte no coinciden.", this);
                return;
            }

            if (serial.get_materialtype() != RawMaterial.MaterialType.Cable) {
                if (serial.get_redtag() == true) {
                    GlobalFunctions.PopUp("Error", "Serie bloqueada por calidad.", this);
                    CleanSerial();
                } else if (serial.get_invoicetrouble() == true) {
                    GlobalFunctions.PopUp("Error", "La serie se encuentra en Tracker de Problemas.", this);
                    CleanSerial();
                } else {
                    switch (serial.get_status()) {
                        case New:
                        case Pending:
                        case Tracker:
                        case Quality:
                            GlobalFunctions.PopUp("Error", "La serie no ha sido dada de alta.", this);
                            CleanSerial();
                            break;
                        case Open:
                        case OnCutter:
                        case ServiceOnQuality:
                            if (serial.LinkSerial(GlobalFunctions.LinklabelID(link_serialnumber))==true){
                                ConfirmOpen();
                            }
                            else{
                                GlobalFunctions.PopUp("Error", "Error al enlazar la serie.", this);
                                CleanSerial();
                            }
                            break;
                        case Stored:
                            //VALIDAR MAXIMO DE ABIERTOS
                            Integer current_open = SQL.Current().GetInteger("COUNT(ID)", "Smk_Serials", new String[]{"Partnumber", "Warehouse", "Status"}, new Object[]{serial.get_partnumber(), serial.get_warehouse(), "O"});
                            Integer max = 1; //RawMaterial.Maximum(serial.get_partnumber(),serial.get_waerehouse());

                            if (current_open < max) {
                                //VALIDAR FIFO
                                if (Boolean.parseBoolean(GlobalVariables.Parameters("SMK_ForceFIFO", "False")) == true && !(serial.get_serialnumber().equals(RawMaterial.NextFIFO(serial.get_partnumber(), serial.get_warehouse())) || serial.get_serialnumber().equals(RawMaterial.NextFIFO(serial.get_partnumber())))) {
                                    GlobalFunctions.PopUp("Error", "FIFO incorrecto. Siguiente FIFO: ", this);
                                    CleanSerial();
                                } else {

                                    if (serial.OpenAndLink(serial.get_location(), GlobalFunctions.LinklabelID(link_serialnumber)) == true) {
                                        if (serial.get_consumption() == Serialnumber.ConsumptionType.Total)
                                            serial.Empty();
                                        ConfirmOpen();
                                        CleanSerial();
                                    } else {
                                        GlobalFunctions.PopUp("Error", "Error al abrir la serie.", this);
                                        CleanSerial();
                                    }
                                }
                            } else {
                                GlobalFunctions.PopUp("Error", "Se han abierto el maximo de contenedores.", this);
                                CleanSerial();
                            }
                            break;
                        case Empty:
                            GlobalFunctions.PopUp("Error", "La serie ya fue declarada vacia.", this);
                            CleanSerial();
                            break;
                        default:
                            GlobalFunctions.PopUp("Error", "Error al abrir la Serie.", this);
                            CleanSerial();
                    }
                }

            } else {
                GlobalFunctions.PopUp("Error", "Opción no disponible para este numero de parte.", this);
                serial_txt.setText("");
                serial_txt.requestFocus();
            }
        } else {
            CleanSerial();
            Toast.makeText(LinkAndOpenActivity.this, "Serie incorrecta.", Toast.LENGTH_LONG).show();
        }
    }


    private void CleanSerial() {
        serial_txt.setText("");
        linkserial_txt.setText("");
        serial_txt.requestFocus();
    }

    private void ConfirmOpen() {
        open_containers += 1;
        counter_lbl.setText(open_containers.toString());
        serial_txt.setText("");
        linkserial_txt.setText("");
        serial_txt.requestFocus();
        Toast.makeText(LinkAndOpenActivity.this, "Serie abierta y enlazada correctamente.", Toast.LENGTH_LONG).show();
    }
}