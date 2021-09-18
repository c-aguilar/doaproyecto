package com.example.mz23zx.deltaerpddrapk;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class ScanCart extends AppCompatActivity {
    EditText cart_txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_cart);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_keyboard = (FloatingActionButton) findViewById(R.id.fab_keyboard);
        fab_keyboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Keyboard();
            }
        });

        cart_txt = (EditText) findViewById(R.id.cart_txt);
        cart_txt.setShowSoftInputOnFocus(false);

        cart_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ReadCart();
                    return true;
                }
                return false;
            }
        });

        cart_txt.requestFocus();
    }

    public void Keyboard(){
        cart_txt.setShowSoftInputOnFocus(true);
        cart_txt.requestFocus();
    }

    public void ReadCart(){
        String cart_id = cart_txt.getText().toString();
        cart_txt.setText("");
        if(SQL.Current().Exists("DDR_Carts","ID",cart_id) == true){
            Integer loop = SQL.Current().GetInteger(String.format("SELECT MAX(ID) FROM DDR_CartsLoopRegister WHERE Cart = '%1$s'", cart_id));
            String status = SQL.Current().GetString("Status","DDR_CartsLoopRegister","ID", loop);

            AlertDialog.Builder dialog;
            AlertDialog alertDialog;
            switch (status){
                case "W":
                    GlobalFunctions.PopUp("Error","El carro no ha sido escaneado.",this);
                    break;
                case "I": case "P":
                    Intent options = new Intent(this, Picking.class);
                    options.putExtra("cart", cart_id);
                    options.putExtra("status", status);
                    options.putExtra("loop", loop);
                    if (status.equals("I") == true)
                        SQL.Current().Execute(String.format("UPDATE DDR_CartsLoopRegister SET [Status] = 'P', PickingDate = GETDATE(), PickingBadge = '%1$s' WHERE ID = %2$s", GlobalVariables.badge, loop));
                    startActivity(options);
                    break;
                case "O": case "Y":
                    GlobalFunctions.PopUp("Error","El carro ya fue surtido.",this);
                    break;
                case "D": case "":
                    GlobalFunctions.PopUp("Error","El carro no ha sido entregado vacio.",this);
                    break;
            }
        }
        else{
            Toast.makeText(this,"El carro no existe.",Toast.LENGTH_LONG);
        }
    }

    public void PickCart(View view){
        ReadCart();
    }
}
