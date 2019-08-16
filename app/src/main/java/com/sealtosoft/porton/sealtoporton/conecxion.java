package com.sealtosoft.porton.sealtoporton;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conecxion);

        btnBuscar =  findViewById(R.id.btnBuscar);
        btnContinuarPass = findViewById(R.id.btnContinuarPass);
        Pass = findViewById(R.id.Pass);
        panelPrincipal = findViewById(R.id.panelPrincipal);
        panelPass = findViewById(R.id.panelPass);
        panelFinalizado = findViewById(R.id.panelFinalizado);
        btnCOntinuarDatos = findViewById(R.id.btnContinuar);
        panelDatos = findViewById(R.id.panelDatos);
        Descripcion = findViewById(R.id.txtDescripcion);
        Lista = findViewById(R.id.listado);
        adaptador = BluetoothAdapter.getDefaultAdapter();
        arrayAdapter = new ArrayAdapter(this,R.layout.adapter,R.id.dispositivos);
        Lista.setAdapter(arrayAdapter);
        database = FirebaseDatabase.getInstance();
        btnCOntinuarDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Descripcion.getText().length() < 4){
                    Toast.makeText(conecxion.this,"Especifique una descripcion de 5 caracteres o mas",Toast.LENGTH_SHORT);
                    return;
                }
                panelDatos.setVisibility(View.GONE);
                panelPass.setVisibility(View.VISIBLE);
            }
        });


        Lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView texto = (TextView) view.findViewById(R.id.dispositivos);
                dispositivo = dir[position];
                myRef = database.getReference("Dispositivos/" + dispositivo);

                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        baseEstructura datos = dataSnapshot.getValue(baseEstructura.class);
                        if(datos.Disponible){
                            clave = datos.Clave;
                            panelPrincipal.setVisibility(View.GONE);
                            panelDatos.setVisibility(View.VISIBLE);
                        }else{
                            Toast.makeText(conecxion.this,"Dispositivo no disponible",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        Descripcion.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (Descripcion.getText().length() < 4) {
                        Toast.makeText(conecxion.this, "Especifique una descripcion de 5 caracteres o mas", Toast.LENGTH_SHORT);
                        return false;
                    }
                    panelDatos.setVisibility(View.GONE);
                    panelPass.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });
        Pass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    Aprobado();
                }
                return false;
            }
        });
        btnContinuarPass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               Aprobado();
            }
        });


        btnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                adaptador = BluetoothAdapter.getDefaultAdapter();

                // Get a set of currently paired devices and append to 'pairedDevices'
                Set <BluetoothDevice> pairedDevices = adaptador.getBondedDevices();
                int pos = 0;
                for(BluetoothDevice d : pairedDevices){
                        arrayAdapter.add(d.getName());
                        dir[pos] = d.getAddress();
                        pos++;
                }


                // Add previosuly paired devices to the array
                if (pairedDevices.size() > 0) {
                    Object nombres[] =  pairedDevices.toArray();

                } else {
                    String noDevices = "Ningun dispositivo pudo ser emparejado";
                    arrayAdapter.add(noDevices);
                }

            }
        });
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
