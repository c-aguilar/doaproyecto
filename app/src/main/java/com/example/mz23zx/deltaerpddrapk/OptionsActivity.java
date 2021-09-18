package com.example.mz23zx.deltaerpddrapk;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

public class OptionsActivity extends AppCompatActivity {
    private String badge;
    TabLayout tabLayout;
    ViewPager viewPager;
    PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        //TextView operator_lbl = (TextView) findViewById(R.id.operator_lbl);
        //badge = getIntent().getExtras().getString("badge");

        //operator_lbl.setText(SQL.Current().GetString("Fullname","Smk_Operators","Badge", GlobalVariables.badge));

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition()==0){
                    pagerAdapter.notifyDataSetChanged();
                }
                if (tab.getPosition()==1){
                    pagerAdapter.notifyDataSetChanged();
                }
                if (tab.getPosition()==2){
                    pagerAdapter.notifyDataSetChanged();
                }
                if (tab.getPosition()==3){
                    pagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

    }
    /* SMK */
    public void Open(View view){
        Intent i = new Intent(this, OpenSerial.class);
        startActivity(i);
    }

    public void LinkAndOpen(View view){
        Intent i = new Intent(this, LinkAndOpenActivity.class);
        startActivity(i);
    }

    public void Empty(View view){
        Intent i = new Intent(this, EmptySerial.class);
        startActivity(i);
    }

    public void Find(View view){
        Intent i = new Intent(this, FindActivity.class);
        startActivity(i);
    }

    public void Audit(View view){
        Intent i = new Intent(this, SerialAudit.class);
        startActivity(i);
    }

    public void ScanAudit(View view){
        Intent i = new Intent(this, ScanAudit.class);
        startActivity(i);
    }

    public void Store(View view){
        Intent i = new Intent(this, StoreActivity.class);
        startActivity(i);
    }
    public void ChangeLocal(View view){
        Intent i = new Intent(this, ChangeLocationActivity.class);
        startActivity(i);
    }

    /* RECEIVING */
    public void Labeling(View view){
        Intent i = new Intent(this, ScanCart.class);
        startActivity(i);
    }

    public void Print(View view) {
        Intent i = new Intent(this, Prototypes.class);
        startActivity(i);
    }

    public void PreSmk(View view){
        Intent i = new Intent(this, CriticalActivity.class);
        startActivity(i);
    }

    /* CDR */
    public void StartCart(View view){
        Intent i = new Intent(this, ScanCart.class);
        startActivity(i);
    }

    public void Prototypes(View view) {
        Intent i = new Intent(this, Prototypes.class);
        startActivity(i);
    }

    public void Critical(View view){
        Intent i = new Intent(this, CriticalActivity.class);
        startActivity(i);
    }

    public void Tapes(View view){
        Intent i = new Intent(this, TapesActivity.class);
        startActivity(i);
    }

    /* CONDUITS*/
    public void Conduits(View view){
        Intent i = new Intent(this, PicklistConduits.class);
        startActivity(i);
    }
}
