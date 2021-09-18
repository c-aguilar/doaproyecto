package com.example.mz23zx.deltaerpddrapk;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class TapesActivity extends AppCompatActivity {
    EditText serial_txt;
    EditText board_txt;
    EditText badge_txt;
    EditText quantity_txt;
    Serialnumber serial;
    String board = "";
    String badge = "";
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tapes);
        setSupportActionBar(findViewById(R.id.toolbar));

        board_txt = (EditText) findViewById(R.id.board_txt);
        board_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ReadBoard();
                    return true;
                }
                return false;
            }
        });

        badge_txt = (EditText) findViewById(R.id.badge_txt);
        badge_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ReadBadge();
                    return true;
                }
                return false;
            }
        });

        serial_txt = (EditText) findViewById(R.id.serial_txt);
        serial_txt.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ReadSerial();
                    return true;
                }
                return false;
            }
        });

        quantity_txt = (EditText) findViewById(R.id.quantity_txt);
        quantity_txt.setVisibility(View.GONE);
    }

    private Boolean ReadBoard()
    {
        if(SQL.Current().Exists("Sch_MaterialBoards","Board",board_txt.getText().toString()) == true){
            board = board_txt.getText().toString();
            badge_txt.requestFocus();
            return true;
        }
        else // NO EXISTE EL TABLERO
        {
            board="";
            board_txt.setText("");
            board_txt.requestFocus();
            GlobalFunctions.PopUp("Error","El tablero no existe.",this);
            return false;
        }
    }

    private Boolean ReadBadge(){
       if(badge_txt.getText().equals("") == false){
           badge = badge_txt.getText().toString();
           serial_txt.requestFocus();
           return true;
       }
       else
       {
           badge="";
           badge_txt.setText("");
           badge_txt.requestFocus();
           GlobalFunctions.PopUp("Error","Captura el gafete.",this);
           return false;
       }
    }

    private  Boolean ReadSerial(){
        serial = new Serialnumber(serial_txt.getText().toString());
        if (serial.get_exist() == true){
            if (serial.get_redtag() == true){
                GlobalFunctions.PopUp("Error","Serie bloqueada por calidad.",this);
                CleanSerial();
            }
            else if (serial.get_invoicetrouble() == true){
                GlobalFunctions.PopUp("Error","La serie se encuentra en Tracker de Problemas.",this);
                CleanSerial();
            }
            else {
                if (serial.get_materialtype() == RawMaterial.MaterialType.Tape) {
                    switch (serial.get_status()) {
                        case New: case Pending: case Tracker: case Quality:
                            GlobalFunctions.PopUp("Error", "La serie no ha sido dada de alta.", this);
                            CleanSerial();
                            break;
                        case ServiceOnQuality:
                            GlobalFunctions.Log(serial.get_serialnumber(),"Tape_QualityLockedSerial");
                            GlobalFunctions.PopUp("Error","Serie bloqueada por calidad.",this);
                            CleanSerial();
                            break;
                        case Open:
                            if(Boolean.parseBoolean(GlobalVariables.Parameters("DDR_TapeRoutes_CheckBoardTape","False")) == true){
                                if (CheckBoardTape() == true){
                                    quantity_txt.setVisibility(View.VISIBLE);
                                    quantity_txt.requestFocus();
                                    return true;
                                }
                                else{
                                    GlobalFunctions.PopUp("Error", "Este numero de parte no se utiliza en este tablero.", this);
                                    CleanSerial();
                                    break;
                                }
                            }
                            else
                            {
                                quantity_txt.setVisibility(View.VISIBLE);
                                quantity_txt.requestFocus();
                                return true;
                            }
                        case Stored:
                            GlobalFunctions.PopUp("Error", "La serie no ha sido abierta.", this);
                            CleanSerial();
                            break;
                        case Empty:
                            GlobalFunctions.PopUp("Error", "La serie ya fue declarada vacia.", this);
                            CleanSerial();
                            break;
                        default:
                            GlobalFunctions.PopUp("Error", "Error al obtener la serie.", this);
                            CleanSerial();
                    }
                } else {
                    serial_txt.setText("");
                    GlobalFunctions.PopUp("Error", "El numero de parte no es Tape.", this);
                }
            }
        }
        else{
            serial_txt.setText("");
            GlobalFunctions.PopUp("Error","La serie no existe.",this);
        }
        quantity_txt.setVisibility(View.GONE);
        return false;
    }

    private Boolean CheckBoardTape(){
        return SQL.Current().Exists(String.format("SELECT * FROM Sys_CurrentBOM AS B INNER JOIN Sch_MaterialBoards AS M ON B.Material = M.Material WHERE B.Partnumber = '%1$s' AND M.Board = '%2$s' AND M.Active = 1",serial.get_partnumber(), board));
    }


    public void Cancel(View view)
    {
        CleanAll();
    }

    private void CleanAll()
    {
        serial= null;
        board = "";
        badge="";
        board_txt.setText("");
        badge_txt.setText("");
        serial_txt.setText("");
        quantity_txt.setText("");
        board_txt.requestFocus();
        quantity_txt.setVisibility(View.GONE);
    }

    private Boolean ValidateFields()
    {
      if (ReadBoard() == true)
            if (ReadBadge() == true)
                if (ReadSerial() == true)
                    if (GlobalFunctions.IsNumeric(quantity_txt.getText().toString()) == true)
                        return true;
                    else
                    {
                        GlobalFunctions.PopUp("Error","La cantidad es incorrecta.",this);
                        quantity_txt.setText("");
                        quantity_txt.requestFocus();
                    }
        return false;
    }

    public void Discount(View view) {
        if (ValidateFields() == true) {
            Float quantity_converted = 0.0f;
            quantity_converted = SQL.Current().GetFloat(String.format("SELECT dbo.Sys_UnitConversion('%1$s','%2$s',%3$s,'%4$s');", serial.get_partnumber(), "ROL", quantity_txt.getText().toString(), serial.get_uom().toString()));
            if (serial.get_current_qty() >= quantity_converted) {
                if (serial.DiscountTapeRoute(board ,badge, quantity_converted) == true) {
                    Toast.makeText(this, "Entrega registrada correctamente.", Toast.LENGTH_LONG).show();
                    CleanAll();
                } else {
                    GlobalFunctions.PopUp("Error", "No pudo registrarse la entrega.", this);
                }
            } else {
                quantity_txt.setText("");
                quantity_txt.requestFocus();
                GlobalFunctions.PopUp("Error", "La cantidad entregada es mayor a la cantidad actual de la serie.", this);
            }
        } else {
            GlobalFunctions.PopUp("Error", "Falta informacion por capturar.", this);
        }
    }

    private void CleanSerial(){
        serial_txt.setText("");
        serial = null;
        serial_txt.requestFocus();
    }
}