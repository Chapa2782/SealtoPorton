package com.sealtosoft.porton.sealtoporton;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Set;

public class conecxion extends AppCompatActivity {
    BluetoothAdapter adaptador;
    ImageButton BtnBuscar;
    ListView Listado;
    FirebaseDatabase database;
    DatabaseReference ref;
    ArrayAdapter arrayAdapter;
    String dirMac;

    AlertDialog.Builder builder;
    BluetoothDevice pairedDevices;
    String[] dir = new String[20];
    baseDeDatos datos;


    //Coneccion a la base de datos
    baseDeDatos codigos = new baseDeDatos(this,"baseDeDatos",null,2);
    SQLiteDatabase db;
    Cursor c;
    String clave,dispositivo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conecxion);
        // Inflate the layout for this fragment
        database = FirebaseDatabase.getInstance();
        arrayAdapter = new ArrayAdapter<>(this,R.layout.adapter,R.id.dispositivos);
        Listado = findViewById(R.id.Listado);
        Listado.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ref = database.getReference("Dispositivos/" + dir[position]);
                dirMac = dir[position];
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final baseEstructura datos = dataSnapshot.getValue(baseEstructura.class);
                        if(datos == null){
                            Log.d("Datos","Valor nulo");
                            return;
                        }

                        builder = new AlertDialog.Builder(conecxion.this);
                        final EditText clave = new EditText(builder.getContext());
                        clave.setInputType(InputType.TYPE_CLASS_NUMBER);
                        builder.setView(clave);
                        builder.setTitle("Clave");
                        builder.setMessage("Ingrese la clave del producto");
                        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(datos.Clave.equals(clave.getText().toString())){
                                    //la clave es correcta
                                    Intent intent = new Intent();
                                    intent.putExtra("dirMac",dirMac);
                                    setResult(RESULT_OK,intent);
                                    finish();
                                }else{
                                    //la clave es incorrecta
                                    Toast.makeText(conecxion.this,"La clave es incorrecta",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.setCancelable(true);
                        builder.show();
                        builder.create();
                        Log.d("Datos",datos.Clave);
                        Log.d("Datos",String.valueOf(datos.Disponible));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        Listado.setAdapter(arrayAdapter);
        BtnBuscar = findViewById(R.id.btnBuscar);


        BtnBuscar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //se coloca el adaptador
                adaptador = BluetoothAdapter.getDefaultAdapter();

                // Get a set of currently paired devices and append to 'pairedDevices'
                Set<BluetoothDevice> pairedDevices = adaptador.getBondedDevices();
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


    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    public void onBackPressed() {

        /*if(panelPass.getVisibility() == View.VISIBLE){
            panelPass.setVisibility(View.GONE);
            panelPrincipal.setVisibility(View.VISIBLE);
            return;
        }*/
        super.onBackPressed();
    }
}
