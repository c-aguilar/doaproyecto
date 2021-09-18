package com.example.mz23zx.deltaerpddrapk;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PicklistConduits extends AppCompatActivity {
    String last_board = "";
    EditText code_txt;
    private static final int ACTIVITY_QUANTITY = 0;
    String board;
    String mspec;
    Integer longitud ;
    String rack;
    String local;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picklist_conduits);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        last_board = SQL.Current().GetString("Board","Smk_ConduitPicklist",new String[]{"Status","Badge","Warehouse"},new Object[]{"N",GlobalVariables.badge,GlobalVariables.Warehouse});

        FloatingActionButton fab = findViewById(R.id.fab_tick);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(SQL.Current().Execute(String.format("UPDATE Smk_ConduitPicklist SET Status = 'P' WHERE Status = 'N' AND board = '%1$s' AND badge = '%2$s'",last_board,GlobalVariables.badge)) == true)
                   RefreshItems();
                else
                   GlobalFunctions.PopUp("Error", "Error al mandar el picklist", PicklistConduits.this);
            }
        });

        code_txt = (EditText) findViewById(R.id.code_txt);
        //METODO AL PRESIONAR ENTER
        code_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ReadQR();
                    return true;
                }
                return false;
            }
        });
        RefreshItems();

        code_txt.requestFocus();
    }

    private void ReadQR(){
        String qr = code_txt.getText().toString();

        String[] qr_split = qr.split(Pattern.quote("|"));
        if (qr_split.length == 5){
            board = qr_split[2].trim().toUpperCase();
            mspec = qr_split[0];
            longitud = Integer.parseInt(qr_split[1]);
            rack = qr_split[3];
            local = qr_split[4];
            if (SQL.Current().Exists(String.format("SELECT * FROM ME_BulidOutBoards WHERE board = '%1$s' AND BuildOut <= GETDATE()",board)) == true)
            {
                GlobalFunctions.PopUp("Tablero Desactivado", "Este tablero NO se debe surtir.", this);
            }
            else{
                Intent i = new Intent(this, ConduitPicklistQuantityActivity.class);
                startActivityForResult(i,ACTIVITY_QUANTITY);
            }
        }
        else{
            GlobalFunctions.PopUp("Error", "El codigo escaneado es incorrecto.", this);
        }
        code_txt.setText("");
        code_txt.requestFocus();
    }

    public void RefreshItems(){
        ArrayList picklist = GetItems();
        final ListView lv = (ListView) findViewById(R.id.items_vw);
        lv.setAdapter(new CustomListAdapter(this, picklist));
    }

    public ArrayList GetItems(){
        ArrayList<ListItem> results = new ArrayList<>();
        List<Map<String, Object>> table = SQL.Current().GetTable(String.format("SELECT Board,MSpec,Length,Rack,Location,Pieces FROM Smk_ConduitPicklist WHERE badge = '%1$s' AND status = 'N' ORDER BY [Date]",GlobalVariables.badge));
        for(Map row : table){
            ListItem item = new ListItem();
            item.setPartnumber(row.get("mspec").toString());
            item.setDescription(String.format("%1$s Rack %2$s Local %3$s",row.get("board").toString(),row.get("rack").toString(),row.get("location").toString()));
            item.setKanban(String.format("%1$s PZ",row.get("pieces").toString()));
            item.setLocation(String.format("%1$s MM", row.get("length").toString()));
            results.add(item);
        }
        return results;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that it is the SecondActivity with an OK result
        if (requestCode == ACTIVITY_QUANTITY) {
            if (resultCode == RESULT_OK) { // Activity.RESULT_OK
                Integer quantity = data.getIntExtra("quantity",0);
                if(quantity > 0){
                    if (SQL.Current().Insert("Smk_ConduitPicklist", new String[] {"Board","MSpec","Length","Rack","Location","Pieces","Badge","Warehouse"}, new Object[] {board,mspec,longitud,rack,local,quantity,GlobalVariables.badge,GlobalVariables.Warehouse}) == true)
                    {
                        if(last_board.equals("") == false &&  last_board.equals(board) == false)
                        {
                            SQL.Current().Execute(String.format("UPDATE Smk_ConduitPicklist SET Status = 'P' WHERE Status = 'N' AND board = '%1$s' AND badge = '%2$s'",last_board,GlobalVariables.badge));
                        }
                        RefreshItems();
                        last_board= board;
                    }
                    else
                    {
                        GlobalFunctions.PopUp("Error", "No fue posible registrar el poliducto.", this);
                    }
                }
                else {
                    Toast.makeText(this, "Accion cancelada.", Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(this, "Accion cancelada.", Toast.LENGTH_LONG).show();
            }
            code_txt.setText("");
            code_txt.requestFocus();
        }
    }

}
