package com.example.mz23zx.deltaerpddrapk;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.gsm.GsmCellLocation;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

public class StoreActivity extends AppCompatActivity {
    EditText serial_txt, local_txt;
    TextView counter_lbl;
    Integer store_containers = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        serial_txt = (EditText) findViewById(R.id.serial_txt);
        local_txt = (EditText) findViewById(R.id.local_txt);
        counter_lbl = (TextView) findViewById(R.id.counter_lbl);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                local_txt.setText("");
                local_txt.requestFocus();
            }
        });

        local_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    if(serial_txt.getText().equals("") == false)
                        ReadSerial();
                    else
                        ReadLocal();
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

        if (getIntent().hasExtra("Serialnumber") == true){
            serial_txt.setText(getIntent().getExtras().getString("Serialnumber"));

        }

        local_txt.requestFocus();
    }

    private void ReadLocal(){
        if(GlobalFunctions.IsLocation(local_txt.getText().toString())==true){
            serial_txt.setText("");
            serial_txt.requestFocus();
        }
        else
        {
            GlobalFunctions.PopUp("Error","Local incorrecto.",this);
            local_txt.setText("");
            local_txt.requestFocus();
        }
    }
    private void ReadSerial(){
        String local_str = local_txt.getText().toString();
        if (GlobalFunctions.IsLocation(local_str) == false)
        {
            GlobalFunctions.PopUp("Error","Local incorrecto.",this);
            local_txt.setText("");
            local_txt.requestFocus();
            return;
        }

        String serial_str = serial_txt.getText().toString();
        if (GlobalFunctions.IsSerialFormat(serial_str) == true){
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
                        case New: case Pending:  case Tracker: case Quality:
                            if(serial.Store(local_str) == true)
                                ConfirmStore();
                            else{
                                GlobalFunctions.PopUp("Error","Error al dar de alta la serie.",this);
                                CleanSerial();
                            }
                            break;
                        case Stored:
                            if(serial.get_location().equals(local_str) == false)
                                if(serial.ChangeLocation(local_str)==true)
                                    Toast.makeText(this, "Cambio de local realizado.", Toast.LENGTH_SHORT).show();
                                else
                                    GlobalFunctions.PopUp("Error","No fue posible actualizar el local.",this);
                            else
                                GlobalFunctions.PopUp("Error","La serie ya fue dada de alta previamente.",this);

                            CleanSerial();
                            break;
                        case ServiceOnQuality:
                            serial.UpdateStatus(Serialnumber.SerialStatus.Open);
                            if(serial.ChangeLocation(local_str)==true)
                                Toast.makeText(this, "Cambio de local realizado.", Toast.LENGTH_SHORT).show();
                            else
                                GlobalFunctions.PopUp("Error","No fue posible actualizar el local.",this);
                            CleanSerial();
                            break;
                        case OnCutter: case Open:
                            GlobalFunctions.PopUp("Error","La serie ya se encuentra en servicio.",this);
                            CleanSerial();
                            break;
                        case Empty:
                            GlobalFunctions.PopUp("Error","La serie ya fue declarada vacia.",this);
                            CleanSerial();
                            break;
                        default:
                            GlobalFunctions.PopUp("Error","Error al dar de alta la serie.",this);
                            CleanSerial();
                    }
                }
            }
            else{
                GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
                CleanSerial();
            }
        }
        else if (GlobalFunctions.IsMasterSerialFormat(serial_str) == true){
            Masterserial master = new Masterserial(serial_str);
            if(master.get_exists() ==true){
                if(master.get_generalStatus() == Masterserial.MasterStatus.Stored){
                    GlobalFunctions.PopUp("Alerta","La serie ya fue dada de alta.",this);
                }
                else if (master.containsRedTag() == true){
                    GlobalFunctions.PopUp("Alerta","Serie bloqueada por calidad.",this);
                }
                else if (master.containsInvoiceTrouble() == true){
                    GlobalFunctions.Log(master.get_masterserial() , "Smk_TryStoreSerialOnTracker");
                    GlobalFunctions.PopUp("Alerta","La serie se encuentra en el Tracker de Problemas.",this);
                }
                else{
                    if(master.get_generalStatus() == Masterserial.MasterStatus.Mixed){
                        GlobalFunctions.PopUp("Alerta","El status de alguna serie hija ya fue cambiado.",this);
                    }
                    else{
                        if(master.Store(local_str) == true)
                            ConfirmStore();
                        else
                            GlobalFunctions.PopUp("Error","No fue posible actualizar el local.",this);
                    }
                }
            }
            else{
                GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
                CleanSerial();
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
        if(getIntent().hasExtra("CloseAfterStore") == true && getIntent().getExtras().getBoolean("CloseAfterStore") == true )
            finish();
    }

    private void ConfirmStore(){
        store_containers+=1;
        counter_lbl.setText(store_containers.toString());
        Toast.makeText(StoreActivity.this, "Serie dada de alta correctamente.", Toast.LENGTH_LONG).show();
        CleanSerial();
    }

}
