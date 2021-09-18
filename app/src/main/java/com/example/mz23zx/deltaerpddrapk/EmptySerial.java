package com.example.mz23zx.deltaerpddrapk;

import android.os.Bundle;
import android.support.v4.app.NotificationCompatSideChannelService;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EmptySerial extends AppCompatActivity {
    EditText serial_txt;
    TextView counter_lbl;
    Integer empty_containers = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_serial);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        serial_txt = (EditText) findViewById(R.id.serial_txt);
        counter_lbl = (TextView) findViewById(R.id.counter_lbl);

        serial_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
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
        Boolean is_linkedserial = false;

        if (GlobalFunctions.IsLinkedlabelFormat(serial_str) == true){
            //REEMPLAZAR LINKSERIAL POR SERIALNUMBER
            serial_str = SQL.Current().GetString(String.format("SELECT TOP 1 S.Serialnumber FROM Smk_LinkLabelMovements AS M INNER JOIN Smk_LinkLabels AS L ON M.LinkID = L.ID INNER JOIN Smk_Serials AS S ON M.SerialID = S.ID WHERE L.Linklabel = '%1$S' ORDER BY M.[Date] DESC",serial_str));
            if (serial_str == null || serial_str.equals("") == true){
                CleanSerial();
                GlobalFunctions.PopUp("Error","La arteza no esta enlazada a ninguna serie.",this);
                return;
            }
            else{
                is_linkedserial=true;
            }

        }
        else if (GlobalFunctions.IsSerialFormat(serial_str) != true){
            CleanSerial();
            GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
            return;
        }

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
                                GlobalFunctions.PopUp("Error","La serie no ha sido dada de alta.",this);
                                CleanSerial();
                                break;
                            case Open: case OnCutter: case ServiceOnQuality:
                                if(serial.get_linklabel().equals("") == false){ //LA SERIE ESTA ENLAZADA

                                    String linklabel_location = SQL.Current().GetString("Location","Smk_Map",new String[]{"Partnumber","Warehouse"},new Object[]{serial.get_partnumber(),serial.get_warehouse()});
                                    linklabel_location = linklabel_location == null ? "" : linklabel_location;
                                    if (serial.get_location().equals(linklabel_location) == true){
                                        //MATERIAL COMPLETO EN LA ARTEZA
                                        //SE DEBE USAR LA SERIE DE LA ARTEZA PARA DECLARAR VACIO, DE LO CONTRARIO ESO INDICA QUE LA CAJA AUN ESTA FISICAMENTE Y PUDIERA HABER MATERIAL EN AMBOS LOCALES
                                        if(is_linkedserial == true){ //SE ESCANEO LA ARTEZA
                                            if(serial.Empty() == true){
                                                ConfirmEmpty();
                                            }
                                            else{
                                                GlobalFunctions.PopUp("Error","Error al declarar vacia.",this);
                                            }
                                        }
                                        else{ //SE ESCANEO LA CAJA
                                            GlobalFunctions.PopUp("Error","La serie esta enlazada, debes usar la arteza para declarar vacio.\nArteza: " + serial.get_linklabel() + "\nLocal: " + linklabel_location,this);
                                        }
                                    }
                                    else{
                                        //DIFERENTES LOCALES
                                        GlobalFunctions.PopUp("Error","La serie esta enlazada. Debes asegurar el inventario del local " + serial.get_location() + " y hacer el cambio antes de declarar vacio.",this);
                                    }
                                }
                                else{
                                    if(serial.Empty() == true){
                                        ConfirmEmpty();
                                    }
                                    else{
                                        GlobalFunctions.PopUp("Error","Error al declarar vacia.",this);
                                    }
                                }
                                CleanSerial();
                                break;
                            case Stored: case Tracker: case Quality:
                                GlobalFunctions.PopUp("Error","Serie no ha sido abierta.",this);
                                CleanSerial();
                                break;
                            case Empty:
                                GlobalFunctions.PopUp("Error","La serie ya fue declarada vacia.",this);
                                CleanSerial();
                                break;
                            default:
                                GlobalFunctions.PopUp("Error","Error al abrir la serie.",this);
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

    private void ConfirmEmpty(){
        empty_containers+=1;
        counter_lbl.setText(empty_containers.toString());
        Toast.makeText(EmptySerial.this, "Serie declarada vacia correctamente.", Toast.LENGTH_LONG).show();
        serial_txt.setText("");
        serial_txt.requestFocus();
    }

}
