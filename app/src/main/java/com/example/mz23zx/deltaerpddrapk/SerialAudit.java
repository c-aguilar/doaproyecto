package com.example.mz23zx.deltaerpddrapk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SerialAudit extends AppCompatActivity {
    private static UsbSerialPort sPort = null;
    private static int WRITE_WAIT_MILLIS = 100;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    EditText serial_txt, quantity_txt;
    TextView serial_lbl,link_lbl,link_location_lbl,serial_location_lbl,link_msg_lbl,partnumber_lbl,description_lbl,current_qty_lbl,uom_lbl, weight_txt, factor_lbl, lastaudit_lbl;
    Button ok_btn,cancel_btn;
    ImageView link_img, uw_img;
    Serialnumber serial;
    Switch scale_swh;
    Float unit_weight;
    Timer timer;
    String scale_data = "";
    String newline = "\r\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_audit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        serial_txt = (EditText) findViewById(R.id.partnumber_txt);
        quantity_txt = (EditText) findViewById(R.id.quantity_txt);
        serial_lbl = (TextView) findViewById(R.id.serial_lbl);
        serial_location_lbl = (TextView) findViewById(R.id.serial_location_lbl);
        link_lbl = (TextView) findViewById(R.id.link_lbl);
        link_location_lbl = (TextView) findViewById(R.id.link_location_lbl);
        partnumber_lbl = (TextView) findViewById(R.id.partnumber_lbl);
        description_lbl = (TextView) findViewById(R.id.description_lbl);
        current_qty_lbl = (TextView) findViewById(R.id.current_qty_lbl);
        uom_lbl = (TextView) findViewById(R.id.uom_lbl);
        factor_lbl = (TextView) findViewById(R.id.factor_lbl);
        ok_btn = (Button) findViewById(R.id.ok_btn);
        cancel_btn = (Button) findViewById(R.id.cancel_btn);
        lastaudit_lbl = (TextView) findViewById(R.id.lastaudit_lbl);
        weight_txt = (TextView) findViewById(R.id.Weight_txt);
        scale_swh = (Switch) findViewById(R.id.Scale_swh);
        link_img = (ImageView) findViewById(R.id.link_img);
        uw_img = (ImageView) findViewById(R.id.uw_img);
        link_msg_lbl = (TextView) findViewById(R.id.link_msg_lbl);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        scale_swh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked==true){
                    ConnectScale();
                }
                else{
                    DisconnectScale();
                }
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

    private SerialInputOutputManager mSerialIoManager;

    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {
                @Override
                public void onRunError(Exception e) {
                    //Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    SerialAudit.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SerialAudit.this.updateReceivedData(data);
                        }
                    });
                }
            };

    private void updateReceivedData(byte[] data) {
        String str_data = new String(data);
        scale_data += str_data;
        str_data = scale_data.toUpperCase().replace("LB", "").replace("KG", "").replace("G", "").replace("NET", "").replace("OZ", "").replace("N","").trim();
        weight_txt.setText(str_data);
        if (GlobalFunctions.IsNumeric(str_data) == true && unit_weight > 0f){
            Float new_quantity = Float.parseFloat(str_data) / (unit_weight/1000);
            if (serial.get_uom() == RawMaterial.UnitOfMeasure.PC){
                quantity_txt.setText(String.format("%.0f",new_quantity));
            }
            else{
                quantity_txt.setText(String.format("%.03f",new_quantity));
            }
        }
        else if (str_data.contains("?") == true){
            quantity_txt.setText("");
        }
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }

        if(timer != null)
            timer.cancel();
        timer = null;
    }

    private void startIoManager() {
        if (sPort != null) {
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);

            timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if(sPort != null && serial != null){
                        try
                        {
                            scale_data="";
                            sPort.write(("IP" + newline).getBytes() , WRITE_WAIT_MILLIS);
                        }
                        catch (IOException ex)
                        {
                            stopIoManager();
                            try {
                                sPort.close();
                            } catch (IOException e) {
                                // Ignore.
                            }
                            sPort = null;

                            final String message = "Error de conexión con báscula.";
                            SerialAudit.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    weight_txt.setText("");
                                    scale_swh.setText("Conectar báscula");
                                    scale_swh.setChecked(false);
                                    Toast.makeText(SerialAudit.this, message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            };
            timer.scheduleAtFixedRate(task,2000,2000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
                // Ignore.
            }
            sPort = null;
        }
        //finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(serial!=null)
        {
            unit_weight = SQL.Current().GetFloat(String.format("SELECT WeightOnGr / dbo.Sys_UnitConversion(Partnumber,UoM,1,'%1$s') FROM Sys_RawMaterial WHERE Partnumber = '%2$s'",serial.get_uom().toString(), serial.get_partnumber()));
            factor_lbl.setText(String.format("%1$s Kg/%2$s",String.format("%.06f",  unit_weight/1000) ,serial.get_uom().toString()));
            if(SQL.Current().Exists("Sys_RawMaterial",new String[]{"Partnumber","UnitWeightValidated"},new Object[]{serial.get_partnumber(),1}) == true)
                uw_img.setVisibility(View.VISIBLE);
            else
                uw_img.setVisibility(View.GONE);
        }
        if( sPort!= null && scale_swh.isChecked() == true) {
        startIoManager();
        }
        onDeviceStateChange();
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void CleanAll(){
        serial = null;
        unit_weight = 0f;
        serial_lbl.setText("");
        link_lbl.setText("");
        serial_location_lbl.setText("");
        link_location_lbl.setText("");
        link_img.setVisibility(View.GONE);
        uw_img.setVisibility(View.GONE);
        link_msg_lbl.setVisibility(View.GONE);
        partnumber_lbl.setText("");
        description_lbl.setText("Ninguna serie escaneada.");
        current_qty_lbl.setText("");
        uom_lbl.setText("");
        factor_lbl.setText("");
        lastaudit_lbl.setText("");

        quantity_txt.setText("");
        quantity_txt.setVisibility(View.GONE);
        ok_btn.setVisibility(View.GONE);
        cancel_btn.setVisibility(View.GONE);

        serial_txt.setVisibility(View.VISIBLE);
        serial_txt.setText("");
        serial_txt.requestFocus();
    }

    public void ReadSerial()
    {
        String serial_str = serial_txt.getText().toString();
        String link_str = "";
        Boolean is_linkedSerial=false;
        serial_txt.setText("");
        if (GlobalFunctions.IsLinkedlabelFormat(serial_str) == true){
            //REEMPLAZAR LINKSERIAL POR SERIALNUMBER
            link_str = serial_str;
            serial_str = SQL.Current().GetString(String.format("SELECT TOP 1 S.Serialnumber FROM Smk_LinkLabelMovements AS M INNER JOIN Smk_LinkLabels AS L ON M.LinkID = L.ID INNER JOIN Smk_Serials AS S ON M.SerialID = S.ID WHERE L.Linklabel = '%1$S' ORDER BY M.[Date] DESC",serial_str));
            if (serial_str == null || serial_str.equals("") == true){
                CleanAll();
                GlobalFunctions.PopUp("Error","La arteza no esta enlazada a ninguna serie.",this);
                return;
            }
            else{
                is_linkedSerial = true;
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
                        CleanAll();
                        break;
                    case Open: case OnCutter: case ServiceOnQuality:
                        serial_lbl.setText(serial.get_serialnumber());
                        partnumber_lbl.setText(serial.get_partnumber());
                        description_lbl.setText(serial.get_description());
                        current_qty_lbl.setText(serial.get_current_qty().toString());
                        uom_lbl.setText(serial.get_uom().toString());
                        unit_weight = SQL.Current().GetFloat(String.format("SELECT WeightOnGr / dbo.Sys_UnitConversion(Partnumber,UoM,1,'%1$s') FROM Sys_RawMaterial WHERE Partnumber = '%2$s'",serial.get_uom().toString(), serial.get_partnumber()));
                        factor_lbl.setText(String.format("%1$s Kg/%2$s",String.format("%.06f",  unit_weight/1000) ,serial.get_uom().toString()));

                       if(SQL.Current().Exists("Sys_RawMaterial",new String[]{"Partnumber","UnitWeightValidated"},new Object[]{serial.get_partnumber(),1}) == true)
                           uw_img.setVisibility(View.VISIBLE);
                       else
                           uw_img.setVisibility(View.GONE);

                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                        lastaudit_lbl.setText(dateFormat.format(serial.get_lastauditdate()));
                        quantity_txt.setVisibility(View.VISIBLE);
                        ok_btn.setVisibility(View.VISIBLE);
                        cancel_btn.setVisibility(View.VISIBLE);
                        serial_txt.setVisibility(View.GONE);
                        serial_location_lbl.setText(serial.get_location());

                        if(is_linkedSerial == true){
                            link_lbl.setText(link_str);
                            link_img.setVisibility(View.VISIBLE);
                            String link_location = SQL.Current().GetString("Location","Smk_Map",new String[]{"Partnumber","Warehouse"},new Object[]{serial.get_partnumber(),serial.get_warehouse()});
                            link_location_lbl.setText(link_location == null ? "" : link_location);
                            if(serial.get_location().equals(link_location)==false)
                                link_msg_lbl.setVisibility(View.VISIBLE);

                        }

                        quantity_txt.setText("");
                        quantity_txt.requestFocus();
                        break;
                    case Stored:
                        GlobalFunctions.PopUp("Error", "La Serie se encuentra en reserva.", this);
                        CleanAll();
                        break;
                    case Empty:
                        if(Boolean.parseBoolean( GlobalVariables.Parameters("SMK_IMSAllowSerialAuditReactivation","false") )== true){
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Alerta");
                            builder.setMessage("La serie se encuentra declarada vacía.")
                                    .setPositiveButton("REACTIVAR", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            serial.ReactiveSerialToService();
                                            serial_lbl.setText(serial.get_serialnumber());
                                            partnumber_lbl.setText(serial.get_partnumber());
                                            description_lbl.setText(serial.get_description());
                                            current_qty_lbl.setText(serial.get_current_qty().toString());
                                            uom_lbl.setText(serial.get_uom().toString());
                                            unit_weight = SQL.Current().GetFloat(String.format("SELECT WeightOnGr / dbo.Sys_UnitConversion(Partnumber,UoM,1,'%1$s') FROM Sys_RawMaterial WHERE Partnumber = '%2$s'",serial.get_uom().toString(), serial.get_partnumber()));
                                            factor_lbl.setText(String.format("%1$s Kg/%2$s",String.format("%.06f",  unit_weight/1000) ,serial.get_uom().toString()));

                                            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");
                                            lastaudit_lbl.setText(dateFormat.format(serial.get_lastauditdate()));
                                            quantity_txt.setVisibility(View.VISIBLE);
                                            ok_btn.setVisibility(View.VISIBLE);
                                            cancel_btn.setVisibility(View.VISIBLE);
                                            serial_txt.setVisibility(View.GONE);
                                            serial_location_lbl.setText(serial.get_location());

                                            quantity_txt.setText("");
                                            quantity_txt.requestFocus();
                                        }
                                    })
                                    .setNegativeButton("CANCELAR" +
                                            "", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            CleanAll();
                                        }
                                    });
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                            GlobalFunctions.AlertVibration(this);
                        }
                        else{
                            GlobalFunctions.PopUp("Error", "La Serie ya fue declarada vacía.", this);
                            CleanAll();
                        }
                        break;
                    default:
                        GlobalFunctions.PopUp("Error", "Error al abrir la Serie.", this);
                        CleanAll();
                }
            }
        }
        else{
            CleanAll();
            GlobalFunctions.PopUp("Error","Serie incorrecta.",this);
        }
    }

    public void ConnectScale(){
        try{
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
            if (availableDrivers.isEmpty()) {
                Toast.makeText(SerialAudit.this,"Ninguna driver encontrado.",Toast.LENGTH_LONG).show();
                scale_swh.setChecked(false);
                return;
            }

            UsbSerialDriver driver = availableDrivers.get(0);

            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                Toast.makeText(SerialAudit.this,"Ningún dispositivo encontrado.",Toast.LENGTH_LONG).show();
                scale_swh.setChecked(false);
                return;
            }
            sPort = driver.getPorts().get(0); // Most devices have just one port (port 0)
            sPort.open(connection);
            sPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            sPort.setRTS(true);
            sPort.setDTR(true);
            startIoManager();
            scale_swh.setText("Desconectar báscula");
            Toast.makeText(SerialAudit.this,"Báscula conectada.",Toast.LENGTH_SHORT).show();
        }
        catch (IOException ex){
            DisconnectScale();
        }
    }

    public void DisconnectScale(){
        stopIoManager();
        weight_txt.setText("");
        scale_swh.setText("Conectar báscula");
        scale_swh.setChecked(false);
        quantity_txt.setText("");
        Toast.makeText(SerialAudit.this,"Báscula desconectada.",Toast.LENGTH_SHORT).show();
    }

    public void Cancel(View view){
        CleanAll();
    }

    public void Discount(View view){
        if (serial!= null){
            if(scale_swh.isChecked() == false || (scale_swh.isChecked() == true && weight_txt.getText().toString().contains("?") == false)){
                if(GlobalFunctions.IsNumeric(quantity_txt.getText().toString())){
                    float input_qty =  Float.parseFloat(quantity_txt.getText().toString());
                    if ( input_qty > 0f){
                        if (serial.CycleCountAdjustment(input_qty) == true){
                            CleanAll();
                            GlobalFunctions.PopUp("Hecho","Serie ajustada.",this);
                        }
                        else{
                            GlobalFunctions.PopUp("Error","No fue posible realizar el ajuste.",this);
                        }
                    }
                    else{
                        GlobalFunctions.PopUp("Error","Cantidad ingresada incorrecta.",this);
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
                Toast.makeText(this, "Báscula inestable", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Escanea primero una serie.", Toast.LENGTH_LONG).show();
            serial_txt.requestFocus();
        }
    }

    public void Samples(View view){
        if (sPort != null)
            DisconnectScale();
        Intent i = new Intent(this, Samples.class);
        if (serial != null)
            i.putExtra("partnumber", serial.get_partnumber());
        else
            i.putExtra("partnumber", "");
        startActivity(i);
    }



}
