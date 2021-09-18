package com.example.mz23zx.deltaerpddrapk;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FindActivity extends AppCompatActivity {
    EditText partnumber_txt;
    TextView local_lbl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                partnumber_txt.setText("");
                partnumber_txt.requestFocus();
            }
        });

        partnumber_txt = (EditText) findViewById(R.id.partnumber_txt);
        local_lbl = (TextView) findViewById(R.id.local_lbl);

        //METODO AL PRESIONAR ENTER
        partnumber_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    RefreshItems();
                    partnumber_txt.setText("");
                    partnumber_txt.requestFocus();
                    return true;
                }
                return false;
            }
        });

        partnumber_txt.requestFocus();
    }

    public void RefreshItems(){
        ArrayList serials = GetItems();
        final ListView lv = (ListView) findViewById(R.id.items_vw);
        lv.setAdapter(new CustomListAdapter(this, serials));
    }

    public ArrayList GetItems(){
        String partnumber = partnumber_txt.getText().toString();
        local_lbl.setText(SQL.Current().GetString(String.format("SELECT dbo.Smk_Locations('%1$s');", partnumber)));
        ArrayList<ListItem> results = new ArrayList<>();
        List<Map<String, Object>> table = SQL.Current().GetTable(String.format("SELECT Serialnumber,CASE WHEN Status IN ('N','P') THEN '' ELSE [Location] END AS [Location],ROUND(CONVERT(FLOAT,CurrentQuantity),1) AS CurrentQuantity,UoM,StatusDescription,WarehouseName FROM [vw_Smk_Serials] WHERE [Partnumber] = '%1$s' AND Status IN ('S','O','C','N','P','Q','T','U')  ORDER BY Serialnumber ASC;",partnumber));
        for(Map row : table){
            ListItem item = new ListItem();
            item.setPartnumber(row.get("serialnumber").toString()); //serie
            item.setDescription(row.get("statusdescription").toString()); // status
            item.setKanban(String.format("%1$s %2$s",row.get("currentquantity").toString(),row.get("uom").toString())); //quantity
            item.setLocation(row.get("location").toString()); // local
            results.add(item);
        }
        return results;
    }

}
