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

import java.util.concurrent.TimeUnit;

public class ChangeLocationActivity extends AppCompatActivity {
    EditText serial_txt, local_txt;
    TextView counter_lbl;
    Integer change_containers = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        serial_txt = (EditText) findViewById(R.id.serial_txt);
        local_txt = (EditText) findViewById(R.id.local_txt);
        counter_lbl = (TextView) findViewById(R.id.counter_lbl);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serial_txt.setText("");
                local_txt.setText("");
                local_txt.requestFocus();
            }
        });

        local_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
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
                    switch (serial.get_status()){
                        case Quality: case ServiceOnQuality:
                            if(serial.get_redtag() == false && serial.get_invoicetrouble() == true)
                                serial.UpdateStatus(Serialnumber.SerialStatus.Tracker);
                            else if(serial.get_redtag() == false && serial.get_invoicetrouble() == false)
                                serial.UpdateStatus(Serialnumber.SerialStatus.Stored);

                            if (serial.ChangeLocation(local_str) == true)
                                ConfirmChange();
                            else
                                GlobalFunctions.PopUp("Error","No fue posible actualizar el local.",this);
                            break;
                        case Tracker:
                            if(serial.get_redtag() == true && serial.get_invoicetrouble() == false)
                                serial.UpdateStatus(Serialnumber.SerialStatus.Quality);
                            else if(serial.get_redtag() == false && serial.get_invoicetrouble() == false)
                                serial.UpdateStatus(Serialnumber.SerialStatus.Stored);

                            if (serial.ChangeLocation(local_str) == true)
                                ConfirmChange();
                            else
                                GlobalFunctions.PopUp("Error","No fue posible actualizar el local.",this);
                            break;
                        case Stored: case Open:
                            if(serial.ChangeLocation(local_str)==true)
                                ConfirmChange();
                            else
                                GlobalFunctions.PopUp("Error","No fue posible actualizar el local.",this);
                            break;
                        case New: case Pending:
                            PopUpNoStored(serial.get_serialnumber());
                            CleanSerial();
                            break;
                        case OnCutter:
                            GlobalFunctions.PopUp("Error","La serie se encuentra en cortadoras.",this);
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
            else{
                GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
                CleanSerial();
            }
        }
        else if (GlobalFunctions.IsMasterSerialFormat(serial_str) == true){
            Masterserial master = new Masterserial(serial_str);
            if(master.get_exists() ==true){
               switch (master.get_generalStatus()){
                   case Pending:
                       PopUpNoStored(master.get_masterserial());
                       CleanSerial();
                       break;
                   case Stored: case Quality: case Tracker:
                       for (Serialnumber m_serial : master.getSerials()) {
                           switch (m_serial.get_status()){
                               case Quality:
                                   if(m_serial.get_redtag() == false && m_serial.get_invoicetrouble() == true)
                                       m_serial.UpdateStatus(Serialnumber.SerialStatus.Tracker);
                                   else if (m_serial.get_redtag() == false && m_serial.get_invoicetrouble() ==false)
                                       m_serial.UpdateStatus(Serialnumber.SerialStatus.Stored);
                                   break;
                               case Tracker:
                                   if(m_serial.get_redtag() == true && m_serial.get_invoicetrouble() == false)
                                       m_serial.UpdateStatus(Serialnumber.SerialStatus.Quality);
                                   else if (m_serial.get_redtag() == false && m_serial.get_invoicetrouble() ==false)
                                       m_serial.UpdateStatus(Serialnumber.SerialStatus.Stored);
                                   break;
                           }
                       }
                       if(master.ChangeLocation(local_str) == true)
                           ConfirmChange();
                       else
                           GlobalFunctions.PopUp("Error","No fue posible actualizar el local.",this);
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
    }

    private void ConfirmChange(){
        change_containers +=1;
        counter_lbl.setText(change_containers.toString());
        Toast.makeText(ChangeLocationActivity.this, "Local actualizado correctamente.", Toast.LENGTH_LONG).show();
        serial_txt.setText("");
        serial_txt.requestFocus();
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
