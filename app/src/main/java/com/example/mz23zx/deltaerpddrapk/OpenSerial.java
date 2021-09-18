package com.example.mz23zx.deltaerpddrapk;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OpenSerial extends AppCompatActivity {
    EditText  serial_txt;
    TextView counter_lbl;
    Integer open_containers=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_serial);
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
        Serialnumber serial = new Serialnumber(serial_str);
        if(serial.get_exist() == true){
                if (serial.get_materialtype() != RawMaterial.MaterialType.Cable){
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
                                case New: case Pending: case Tracker: case Quality:
                                    GlobalFunctions.PopUp("Error","La serie no ha sido dada de alta.",this);
                                    CleanSerial();
                                    break;
                                case Open: case OnCutter: case ServiceOnQuality:
                                    GlobalFunctions.PopUp("Error","Serie abierta anteriormente.",this);
                                    CleanSerial();
                                    break;
                                case Stored:
                                    //VALIDAR MAXIMO DE ABIERTOS
                                    Integer current_open = SQL.Current().GetInteger("COUNT(ID)","Smk_Serials",new String[] {"Partnumber","Warehouse","Status"}, new Object[]{serial.get_partnumber(),serial.get_warehouse(),"O"});
                                    Integer max = 1; //RawMaterial.Maximum(serial.get_partnumber(),serial.get_waerehouse());

                                    if (current_open < max){
                                        //VALIDAR FIFO
                                        if(Boolean.parseBoolean( GlobalVariables.Parameters("SMK_ForceFIFO","False")) == true && !(serial.get_serialnumber().equals(RawMaterial.NextFIFO(serial.get_partnumber(), serial.get_warehouse())) || serial.get_serialnumber().equals(RawMaterial.NextFIFO(serial.get_partnumber())))){
                                            GlobalFunctions.PopUp("Error","FIFO incorrecto.",this);
                                            CleanSerial();
                                        }
                                        else{
                                                if (serial.Open(serial.get_location()) == true) {
                                                    if (serial.get_consumption() == Serialnumber.ConsumptionType.Total)
                                                        serial.Empty();
                                                    ConfirmOpen();
                                                }
                                                else{
                                                    GlobalFunctions.PopUp("Error","Error al abrir la serie.",this);
                                                    CleanSerial();
                                                }
                                        }
                                    }
                                    else{
                                        GlobalFunctions.PopUp("Error","Se han abierto el maximo de contenedores.",this);
                                        CleanSerial();
                                    }
                                    break;
                                case Empty:
                                    GlobalFunctions.PopUp("Error","La serie ya fue declarada vacia.",this);
                                    CleanSerial();
                                    break;
                                default:
                                    GlobalFunctions.PopUp("Error","Error al abrir la Serie.",this);
                                    CleanSerial();
                            }
                        }

                }
                else{
                    GlobalFunctions.PopUp("Error","Opción no disponible para este numero de parte.",this);
                    serial_txt.setText("");
                    serial_txt.requestFocus();
                }
        }
        else{
            CleanSerial();
            Toast.makeText(OpenSerial.this, "Serie incorrecta.", Toast.LENGTH_LONG).show();
        }
    }


    private void CleanSerial(){
        serial_txt.setText("");
        serial_txt.requestFocus();
    }

    private void ConfirmOpen(){
        open_containers+=1;
        counter_lbl.setText(open_containers.toString());
        CleanSerial();
        Toast.makeText(OpenSerial.this, "Serie abierta correctamente.", Toast.LENGTH_LONG).show();
    }

}
