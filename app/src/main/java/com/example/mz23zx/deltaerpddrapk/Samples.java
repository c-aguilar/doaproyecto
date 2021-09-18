package com.example.mz23zx.deltaerpddrapk;

import android.content.Context;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Samples extends AppCompatActivity {

    private static UsbSerialPort sPort = null;
    private static int WRITE_WAIT_MILLIS = 100;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    EditText partnumber_txt, samples_txt;
    TextView partnumber_lbl,description_lbl, weight_txt, factor_lbl;
    TextView pc_lbl, new_factor_lbl;
    Button ok_btn,cancel_btn;
    String partnumber = "";
    Switch scale_swh;
    Float unit_weight,new_unit_weight;
    Timer timer;
    String scale_data = "";
    String newline = "\r\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_samples);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        partnumber_txt = (EditText) findViewById(R.id.partnumber_txt);
        samples_txt = (EditText) findViewById(R.id.samples_txt);
        partnumber_lbl = (TextView) findViewById(R.id.partnumber_lbl);
        description_lbl = (TextView) findViewById(R.id.description_lbl);
        factor_lbl = (TextView) findViewById(R.id.factor_lbl);
        new_factor_lbl = (TextView) findViewById(R.id.new_factor_lbl);
        pc_lbl = (TextView) findViewById(R.id.pc_lbl);
        ok_btn = (Button) findViewById(R.id.ok_btn);
        cancel_btn = (Button) findViewById(R.id.cancel_btn);
        weight_txt = (TextView) findViewById(R.id.weight_txt);
        scale_swh = (Switch) findViewById(R.id.scale_swh);

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

        partnumber_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ReadPartnumber();
                    return true;
                }
                return false;
            }
        });

        CleanAll();

        Bundle b = getIntent().getExtras();
        partnumber = b.getString("partnumber","");
        if (partnumber.equals("") == false) {
            partnumber_txt.setText(partnumber);
            ReadPartnumber();
        }
        //scale_swh.setChecked(true);
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
                    Samples.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Samples.this.updateReceivedData(data);
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
            pc_lbl.setText(String.format("%.0f",new_quantity));
        }
        else if (str_data.contains("?") == true){
            pc_lbl.setText("-");
        }

        String txt_qty = samples_txt.getText().toString().trim();
        if (GlobalFunctions.IsNumeric(str_data) == true && GlobalFunctions.IsNumeric(txt_qty) == true){
            new_unit_weight = Float.parseFloat(str_data) / Float.parseFloat(txt_qty);
            new_factor_lbl.setText(String.format("%1$s Kg/%2$s",String.format("%.06f",  new_unit_weight) ,"PC"));
        }
        else{
            new_unit_weight = 0f;
            new_factor_lbl.setText("-");
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
                    if(sPort != null && partnumber.equals("") == false){
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
                            Samples.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    weight_txt.setText("");
                                    scale_swh.setText("Conectar báscula");
                                    scale_swh.setChecked(false);
                                    Toast.makeText(Samples.this, message, Toast.LENGTH_SHORT).show();
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
        finish();
    }

    @Override protected void onResume() {
        super.onResume();
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
        partnumber="";
        unit_weight = 0f;
        new_unit_weight = 0f;
        partnumber_lbl.setText("");
        description_lbl.setText("Ningún no. de parte escaneado.");
        pc_lbl.setText("");
        factor_lbl.setText("");
        new_factor_lbl.setText("");

        samples_txt.setText("");
        samples_txt.setVisibility(View.GONE);
        ok_btn.setVisibility(View.GONE);
        cancel_btn.setVisibility(View.GONE);

        partnumber_txt.setVisibility(View.VISIBLE);
        partnumber_txt.setText("");
        partnumber_txt.requestFocus();
    }

    public void ReadPartnumber()
    {
        if (partnumber_txt.getText().toString().equals("") == false && SQL.Current().Exists("Sys_RawMaterial","Partnumber",partnumber_txt.getText().toString().trim())){
            partnumber = partnumber_txt.getText().toString().toUpperCase().trim();

            partnumber_lbl.setText(partnumber);
            description_lbl.setText(SQL.Current().GetString("[Description]","Sys_RawMaterial","Partnumber",partnumber));
            unit_weight = SQL.Current().GetFloat(String.format("SELECT WeightOnGr / dbo.Sys_UnitConversion(Partnumber,UoM,1,'%1$s') FROM Sys_RawMaterial WHERE Partnumber = '%2$s'","PC", partnumber));
            factor_lbl.setText(String.format("%1$s Kg/%2$s",String.format("%.06f",  unit_weight/1000) ,"PC"));

            samples_txt.setVisibility(View.VISIBLE);
            ok_btn.setVisibility(View.VISIBLE);
            cancel_btn.setVisibility(View.VISIBLE);
            partnumber_txt.setVisibility(View.GONE);
            samples_txt.setText("");
            samples_txt.requestFocus();
        }
        else{
            CleanAll();
            Toast.makeText(this,"No. de Parte incorrecto", Toast.LENGTH_LONG).show();
        }
    }

    public void ConnectScale(){
        try{
            UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
            if (availableDrivers.isEmpty()) {
                Toast.makeText(Samples.this,"Ninguna driver encontrado.",Toast.LENGTH_LONG).show();
                scale_swh.setChecked(false);
                return;
            }

            UsbSerialDriver driver = availableDrivers.get(0);

            UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
            if (connection == null) {
                Toast.makeText(Samples.this,"Ningún dispositivo encontrado.",Toast.LENGTH_LONG).show();
                scale_swh.setChecked(false);
                return;
            }
            sPort = driver.getPorts().get(0); // Most devices have just one port (port 0)
            sPort.open(connection);
            sPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            sPort.setRTS(true);
            sPort.setDTR(true);
            startIoManager();
            scale_swh.setText("Desconectar bascula");
            Toast.makeText(Samples.this,"Báscula conectada.",Toast.LENGTH_SHORT).show();
        }
        catch (IOException ex){
            DisconnectScale();
        }
    }

    public void DisconnectScale(){
        stopIoManager();
        weight_txt.setText("");
        scale_swh.setText("Conectar bascula");
        scale_swh.setChecked(false);
        samples_txt.setText("");
        Toast.makeText(Samples.this,"Báscula desconectada.",Toast.LENGTH_SHORT).show();
    }

    public void Cancel(View view){
        CleanAll();
    }

    public void SetNewWeight(View view){
        Float gotten_weight = new_unit_weight;
        if(partnumber.equals("") == false && gotten_weight > 0f){
            if (SQL.Current().Execute(String.format("UPDATE Sys_RawMaterial SET WeightOnGr = dbo.Sys_UnitConversion(Partnumber,'PC',%1$s * 1000,UoM), UnitWeightValidated = 1 WHERE Partnumber = '%2$s'",gotten_weight.toString(),partnumber)) == true){
                Toast.makeText(this,"Peso unitario actualizado.",Toast.LENGTH_LONG).show();
                CleanAll();
            }
            else{
                GlobalFunctions.PopUp("Error","No fue posible actualizar el peso unitario.",this);
            }
        }
    }

}
