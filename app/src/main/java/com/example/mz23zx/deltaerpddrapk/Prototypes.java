package com.example.mz23zx.deltaerpddrapk;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Prototypes extends AppCompatActivity {
    EditText serial_txt, quantity_txt;
    TextView serial_lbl,partnumber_lbl,description_lbl,current_qty_lbl,uom_lbl;
    Button ok_btn;
    Serialnumber serial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protypes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        serial_txt = (EditText) findViewById(R.id.partnumber_txt);
        quantity_txt = (EditText) findViewById(R.id.quantity_txt);

        serial_lbl = (TextView) findViewById(R.id.serial_lbl);
        partnumber_lbl = (TextView) findViewById(R.id.partnumber_lbl);
        description_lbl = (TextView) findViewById(R.id.description_lbl);
        current_qty_lbl = (TextView) findViewById(R.id.current_qty_lbl);
        uom_lbl = (TextView) findViewById(R.id.uom_lbl);
        ok_btn = (Button) findViewById(R.id.ok_btn);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CleanAll();
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

    private void CleanAll(){
        serial = null;
        serial_lbl.setText("");
        partnumber_lbl.setText("");
        description_lbl.setText("Ninguna serie escaneada.");
        current_qty_lbl.setText("");
        uom_lbl.setText("");
        quantity_txt.setText("");
        ok_btn.setVisibility(View.GONE);
        quantity_txt.setVisibility(View.GONE);
        serial_txt.setText("");
        serial_txt.requestFocus();
    }

    public void ReadSerial()
    {
        String serial_str = serial_txt.getText().toString();
        serial_txt.setText("");
        if (GlobalFunctions.IsLinkedlabelFormat(serial_str) == true){
            //REEMPLAZAR LINKSERIAL POR SERIALNUMBER
            serial_str = SQL.Current().GetString(String.format("SELECT TOP 1 S.Serialnumber FROM Smk_LinkLabelMovements AS M INNER JOIN Smk_LinkLabels AS L ON M.LinkID = L.ID INNER JOIN Smk_Serials AS S ON M.SerialID = S.ID WHERE L.Linklabel = '%1$S' ORDER BY M.[Date] DESC",serial_str));
            if (serial_str == null || serial_str.equals("") == true){
                CleanAll();
                GlobalFunctions.PopUp("Error","La arteza no esta enlazada a ninguna serie.",this);
                return;
            }
        }
        else if (GlobalFunctions.IsSerialFormat(serial_str) != true){
            CleanAll();
            GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
            return;
        }

        serial = new Serialnumber(serial_str);
        if (serial.get_exist() == true)
        {
                if (serial.get_redtag() == true){
                    GlobalFunctions.PopUp("Error","Serie bloqueada por Calidad.",this);
                    CleanAll();
                }
                else if (serial.get_invoicetrouble() == true) {
                    GlobalFunctions.PopUp("Error", "La Serie se encuentra en Tracker de Problemas.", this);
                    CleanAll();
                }
                else {
                    switch (serial.get_status()) {
                        case New: case Pending: case Tracker: case Quality:
                            GlobalFunctions.PopUp("Error", "La Serie no ha sido dada de alta.", this);
                            serial_txt.setText("");
                            serial_txt.requestFocus();
                            break;
                        case Open: case OnCutter: case ServiceOnQuality:
                            serial_lbl.setText(serial.get_serialnumber());
                            partnumber_lbl.setText(serial.get_partnumber());
                            description_lbl.setText(serial.get_description());
                            current_qty_lbl.setText(serial.get_current_qty().toString());
                            uom_lbl.setText(serial.get_uom().toString());
                            quantity_txt.setText("");
                            quantity_txt.requestFocus();
                            quantity_txt.setVisibility(View.VISIBLE);
                            ok_btn.setVisibility(View.VISIBLE);
                            break;
                        case Stored:
                            GlobalFunctions.PopUp("Error", "La Serie se encuentra en reserva.", this);
                            serial_txt.setText("");
                            serial_txt.requestFocus();
                            break;
                        case Empty:
                            GlobalFunctions.PopUp("Error", "La Serie ya fue declarada vacia.", this);
                            serial_txt.setText("");
                            serial_txt.requestFocus();
                            break;
                        default:
                            GlobalFunctions.PopUp("Error", "Error al abrir la Serie.", this);
                            serial_txt.setText("");
                            serial_txt.requestFocus();
                    }
                }
        }
        else{
            CleanAll();
            GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
        }
    }

    public void Discount(View view){
        if (serial!= null){
           Integer input_qty =  Integer.parseInt(quantity_txt.getText().toString());
           if (input_qty != null && input_qty > 0f){
               if(serial.get_current_qty() == input_qty.floatValue()){
                   if(serial.PartialDiscount(input_qty.floatValue())){
                       serial.Empty();
                       GlobalFunctions.PopUp("Confirmación","Descuento realizado y serie declarada vacia.",this);
                       CleanAll();
                   }
                   else{
                       GlobalFunctions.PopUp("Error","Ocurrio un error.",this);
                   }
               }
               else if (serial.get_current_qty() > input_qty.floatValue()){
                   if(serial.PartialDiscount(input_qty.floatValue())){
                       GlobalFunctions.PopUp("Confirmación","Descuento realizado correctamente.",this);
                       CleanAll();
                   }
                   else{
                       GlobalFunctions.PopUp("Error","Ocurrio un error.",this);
                   }
               }
               else{
                   GlobalFunctions.PopUp("Error","Cantidad ingresada mayor a la actual.",this);
                   quantity_txt.setText("");
                   quantity_txt.requestFocus();
               }
           }
           else{
               GlobalFunctions.PopUp("Error","Cantidad ingresada incorrecta.",this);
               quantity_txt.setText("");
               quantity_txt.requestFocus();
           }
        }
        else{
            Toast.makeText(this, "Escanea primero una serie.", Toast.LENGTH_LONG).show();
            serial_txt.requestFocus();
        }
    }

}
