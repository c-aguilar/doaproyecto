package com.example.mz23zx.deltaerpddrapk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConduitPicklistQuantityActivity extends AppCompatActivity {
    Button q5_btn,q10_btn,q20_btn,q30_btn,q50_btn,q70_btn,q100_btn,q150_btn,q200_btn;
    Button cancel_btn,confirm_btn;
    EditText quantity_txt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conduit_picklist_quantity);
        q5_btn = (Button) findViewById(R.id.q5_btn);
        q10_btn = (Button) findViewById(R.id.q10_btn);
        q20_btn = (Button) findViewById(R.id.q20_btn);
        q30_btn = (Button) findViewById(R.id.q30_btn);
        q50_btn = (Button) findViewById(R.id.q50_btn);
        q70_btn = (Button) findViewById(R.id.q70_btn);
        q100_btn = (Button) findViewById(R.id.q100_btn);
        q150_btn = (Button) findViewById(R.id.q150_btn);
        q200_btn = (Button) findViewById(R.id.q200_btn);
        cancel_btn = (Button) findViewById(R.id.cancel_btn);
        confirm_btn = (Button) findViewById(R.id.ok_btn);
        quantity_txt = (EditText) findViewById(R.id.quantity_txt);
        q5_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnQuantity(5);
            }
        });

        q10_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnQuantity(10);
            }
        });

        q20_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnQuantity(20);
            }
        });

        q30_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnQuantity(30);
            }
        });

        q50_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnQuantity(50);
            }
        });

        q70_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnQuantity(70);
            }
        });

        q100_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnQuantity(100);
            }
        });

        q150_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnQuantity(150);
            }
        });

        q200_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnQuantity(200);
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("quantity", -1);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer quantity = -1;
                if (GlobalFunctions.IsNumeric(quantity_txt.getText().toString()) == true) {
                    quantity = Integer.parseInt(quantity_txt.getText().toString());
                    if (quantity <= 200)
                    {
                        ReturnQuantity(quantity);
                    }
                    else
                    {
                        quantity_txt.setText("");
                        quantity_txt.requestFocus();
                        GlobalFunctions.PopUp("Error", "La cantidad no puede ser mayor a 200.", ConduitPicklistQuantityActivity.this);
                    }
                }
                else{
                    quantity_txt.setText("");
                    quantity_txt.requestFocus();
                    GlobalFunctions.PopUp("Error", "Cantidad incorrecta.", ConduitPicklistQuantityActivity.this);
                }
            }
        });
    }

    private void ReturnQuantity(Integer quantity)
    {
        Intent intent = new Intent();
        intent.putExtra("quantity", quantity);
        setResult(RESULT_OK, intent);
        finish();
    }
}