package com.example.mz23zx.deltaerpddrapk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends AppCompatActivity {
    TextView version_lbl,warehouse_lbl;
    EditText badge_txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences settings = getSharedPreferences(GlobalVariables.SettingsName,0);
        GlobalVariables.Server = settings.getString("server","");
        GlobalVariables.Instance = settings.getString("instance","");
        GlobalVariables.Database = settings.getString("database","");
        GlobalVariables.UID = settings.getString("uid","");
        GlobalVariables.Password = settings.getString("password","");
        GlobalVariables.Warehouse = settings.getString("warehouse","");

        badge_txt = (EditText) findViewById(R.id.Badge_txt);
        badge_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    In();
                    return true;
                }
                return false;
            }
        });

        version_lbl = (TextView) findViewById(R.id.version_lbl);
        version_lbl.setText("1.21.07.01");

        warehouse_lbl = (TextView) findViewById(R.id.warehouse_lbl);
        if (SQL.Current().Available() == true)
            warehouse_lbl.setText(SQL.Current().GetString("Name","Smk_Warehouses","Warehouse",GlobalVariables.Warehouse));
    }

    public void In()
    {
        if (SQL.Current().Available() == true)
        {
            if (SQL.Current().Exists("Smk_Warehouses",new String[]{"Warehouse","Active"},new Object[]{GlobalVariables.Warehouse,1}) == false){
                GlobalFunctions.PopUp("Error", "Estación incorrecta.", this);
            }
            else
            {
                if (SQL.Current().Exists("Smk_Operators", new String[]{"Badge", "Active"}, new Object[]{badge_txt.getText().toString(), 1}) == true) {
                    Intent options = new Intent(this, OptionsActivity.class);
                    //options.putExtra("badge", badge_txt.getText().toString());
                    GlobalVariables.badge = badge_txt.getText().toString();

                    //guardar mac address
                    //WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    //WifiInfo info = manager.getConnectionInfo();
                    GlobalFunctions.Log(GlobalFunctions.getMacAddr(),"Sys_ScannerLogin");


                    badge_txt.setText("");
                    startActivity(options);
                } else {
                    Toast.makeText(this, "El gafete no existe.", Toast.LENGTH_LONG).show();
                }
            }
        }
        else {
            Toast.makeText(this, "Conexión a servidor no disponible.", Toast.LENGTH_LONG).show();
        }
    }

    public void Login(View view){
        In();
    }

    public void Settings(View view){
        Intent options = new Intent(this, SettingsActivity.class);
        startActivity(options);
    }

}
