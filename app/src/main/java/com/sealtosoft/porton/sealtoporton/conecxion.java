package com.sealtosoft.porton.sealtoporton;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.Set;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.animation.AnimationUtils;

public class conecxion extends AppCompatActivity {
    BluetoothAdapter adaptador;
    EditText Pass,Descripcion;
    BluetoothDevice device;
    ImageButton btnBuscar,btnContinuarPass,btnCOntinuarDatos;
    ListView Lista;
    ArrayAdapter arrayAdapter;
    BluetoothDevice pairedDevices;
    String[] dir = new String[20];
    baseDeDatos datos;
    LinearLayout panelPrincipal, panelPass, panelFinalizado, panelDatos;


    //Coneccion a la base de datos
    baseDeDatos codigos = new baseDeDatos(this,"baseDeDatos",null,2);
    SQLiteDatabase db;
    Cursor c;
    FirebaseDatabase database;
    DatabaseReference myRef;
    String clave,dispositivo;
    FragmentTransaction transaction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conecxion);


        //Inicializa Fragment
        FragmentBusqueda fragmentBusqueda = new FragmentBusqueda();
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.Principal,fragmentBusqueda);
        transaction.commit();


    }
    public void Aprobado(){
        String parcial = Pass.getText().toString();
        if(clave.equals(parcial)){
            //Esto se usa para ocultar el teclado
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(Pass.getWindowToken(), 0);

            db = codigos.getWritableDatabase();
            String descrip,priori,modo;
            descrip = Descripcion.getText().toString();
            priori = "Principal";
            modo = "Principal";
            db.execSQL("INSERT INTO codigos (dirMac, Descrip, Prioridad, Modo) VALUES ('" + dispositivo + "','" + descrip + "','" + priori + "','" + modo + "')");
            //c = db.rawQuery("DELETE FROM codigos",null);
            myRef = database.getReference(dispositivo+"/Disponible");
            myRef.setValue(false);
            panelPass.setVisibility(View.GONE);
            panelFinalizado.setVisibility(View.VISIBLE);


        }else{
            Toast.makeText(conecxion.this,"Clave incorrecta",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onBackPressed() {

        if(panelPass.getVisibility() == View.VISIBLE){
            panelPass.setVisibility(View.GONE);
            panelPrincipal.setVisibility(View.VISIBLE);
            return;
        }
        super.onBackPressed();
    }
}
