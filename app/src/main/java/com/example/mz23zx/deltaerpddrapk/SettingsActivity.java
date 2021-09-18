package com.example.mz23zx.deltaerpddrapk;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    EditText server_txt,instance_txt,db_txt,user_txt,password_txt,warehouse_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        server_txt = (EditText)  findViewById(R.id.server_txt);
        instance_txt = (EditText)  findViewById(R.id.instance_txt);
        db_txt = (EditText)  findViewById(R.id.db_txt);
        user_txt = (EditText)  findViewById(R.id.user_txt);
        password_txt = (EditText)  findViewById(R.id.password_txt);
        warehouse_txt = (EditText) findViewById(R.id.warehouse_txt);

        SharedPreferences settings = getSharedPreferences(GlobalVariables.SettingsName,0);
        server_txt.setText(settings.getString("server",""));
        instance_txt.setText(settings.getString("instance",""));
        db_txt.setText(settings.getString("database","Delta"));
        user_txt.setText(settings.getString("uid",""));
        password_txt.setText(settings.getString("password",""));
        warehouse_txt.setText(settings.getString("warehouse",""));
    }

    public void Save(View view){
        SharedPreferences settings = getSharedPreferences(GlobalVariables.SettingsName,0);
        SharedPreferences.Editor editor =settings.edit();
        editor.putString("server",server_txt.getText().toString());
        editor.putString("instance",instance_txt.getText().toString());
        editor.putString("database",db_txt.getText().toString());
        editor.putString("uid",user_txt.getText().toString());
        editor.putString("password",password_txt.getText().toString());
        editor.putString("warehouse",warehouse_txt.getText().toString());
        editor.commit();

        GlobalVariables.Server = server_txt.getText().toString();
        GlobalVariables.Instance = instance_txt.getText().toString();
        GlobalVariables.Database = db_txt.getText().toString();
        GlobalVariables.UID = user_txt.getText().toString();
        GlobalVariables.Password = password_txt.getText().toString();
        GlobalVariables.Warehouse = warehouse_txt.getText().toString();

        if(SQL.Current().Available() == true){
            Toast.makeText(this,"Conexión establecida con exito.",Toast.LENGTH_LONG).show();
            if (SQL.Current().Exists("Smk_Warehouses",new String[]{"Warehouse","Active"},new Object[]{GlobalVariables.Warehouse,1}) == false){
                GlobalFunctions.PopUp("Error", "Estación incorrecta.", this);
            }
        }
        else{
            Toast.makeText(this,"Error al establecer la conexión.",Toast.LENGTH_LONG).show();
        }
    }

}
