package com.sealtosoft.porton.sealtoporton;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import com.google.firebase.database.*;
import com.google.firebase.database.DatabaseReference;
public class modoSimple extends AppCompatActivity implements SensorEventListener {

    baseDeDatos codigos = new baseDeDatos(this,"baseDeDatos",null,2);
    String DirMac = "";
    String DirMacSec = "";
    Cursor c;
    SQLiteDatabase db;
    ImageButton boton_accion, estado_conexion, estado_datos;
    BluetoothSocket btSocket,btSocketSec;
    BluetoothAdapter adaptador;
    BluetoothDevice device,deviceSec;
    Handler bluetoothIn;
    String dataInPrint;
    String estado = "";
    FirebaseDatabase database;
    DatabaseReference myRef;
    ProgressDialog progreso;
    Boolean Apertura = true;
    SensorManager sensorManager;
    Boolean Cerrar = false;
    Float pasos = 0.0f;
    ToneGenerator toneGen1;

    private StringBuilder recDataString = new StringBuilder();

    final int handlerState = 0;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private connectionThreads mmConectionThreads;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modo_simple);
        inicializar();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);


        estado_datos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(estado == "abierto"){
                    if (btSocket.isConnected()) {
                        try {
                            mmConectionThreads.write("ESTADOC");
                        } catch (Exception e) {
                            try {
                                btSocket.close();
                                estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                            } catch (IOException e1) {

                            }

                        }
                    }
                }
                if(estado == "cerrado"){
                    if (btSocket.isConnected()) {
                        try {
                            mmConectionThreads.write("ESTADOA");
                        } catch (Exception e) {
                            try {
                                btSocket.close();
                                estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                            } catch (IOException e1) {

                            }

                        }
                    }
                }
            }
        });

        estado_conexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conectarBT conexion = new conectarBT();
                conexion.execute();
                if(!btSocket.isConnected()) {
                    try {
                        mmConectionThreads.run();
                    } catch (Exception e) {
                    }
                }else{
                    mmConectionThreads.write("A");
                }

            }
        });

        boton_accion.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    switch (estado){
                        case "":
                            boton_accion.setBackgroundResource(R.drawable.boton_desconectado_push);
                            break;
                        case "cerrado":
                            boton_accion.setBackgroundResource(R.drawable.boton_abrir_push);
                            break;
                        case "abierto":
                            boton_accion.setBackgroundResource(R.drawable.boton_cerrar_push);
                            break;
                        case "movimiento":
                            boton_accion.setBackgroundResource(R.drawable.boton_parar_push);
                            break;

                    }
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    switch (estado){
                        case "":
                            boton_accion.setBackgroundResource(R.drawable.boton_desconectado);
                            conectarBT conexion = new conectarBT();
                            conexion.execute();
                            if(!btSocket.isConnected()) {
                                try {
                                    mmConectionThreads.run();
                                } catch (Exception e) {
                                }
                            }else{
                                mmConectionThreads.write("A");
                            }
                            break;
                        case "cerrado":
                            boton_accion.setBackgroundResource(R.drawable.boton_abrir);
                            if (btSocket.isConnected()) {

                                try {
                                    mmConectionThreads.write("Abrir");
                                } catch (Exception e) {
                                    try {
                                        btSocket.close();
                                        estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                                    } catch (IOException e1) {

                                    }

                                }
                            }else{
                                estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                            }
                            break;
                        case "abierto":
                            boton_accion.setBackgroundResource(R.drawable.boton_cerrar);
                            if (btSocket.isConnected()) {
                                try {
                                    mmConectionThreads.write("Cerrar");
                                } catch (Exception e) {
                                    try {
                                        btSocket.close();
                                        estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                                    } catch (IOException e1) {

                                    }

                                }
                            }else{
                                estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                            }
                            break;
                        case "moviendo":
                            boton_accion.setBackgroundResource(R.drawable.boton_parar);
                            if (btSocket.isConnected()) {
                                try {
                                    mmConectionThreads.write("Parar");
                                } catch (Exception e) {
                                    try {
                                        btSocket.close();
                                        estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                                    } catch (IOException e1) {

                                    }

                                }
                            }else{
                                estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                                conectar();
                            }
                            break;
                    }


                }
                return false;
            }
        });

        bluetoothIn = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == handlerState) {          //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);              //keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");// determine the end-of-line

                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        switch (dataInPrint){
                            case "0":
                                //estado.setText("Cerrado");
                                estado = "cerrado";
                                estado_datos.setBackgroundResource(R.drawable.estados_cerrado);
                                boton_accion.setBackgroundResource(R.drawable.boton_abrir);
                                if(Apertura){
                                    Apertura = false;
                                    Cerrar = true;
                                    pasos = 0.0f;
                                    try {
                                        mmConectionThreads.write("Abrir");
                                    } catch (Exception e) {
                                        try {
                                            btSocket.close();
                                            estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                                        } catch (IOException e1) {

                                        }

                                    }
                                }

                                break;
                            case "1":
                                estado = "abierto";
                                //estado.setText("Abierto");
                                estado_datos.setBackgroundResource(R.drawable.estados_abierto);
                                boton_accion.setBackgroundResource(R.drawable.boton_cerrar);

                                break;
                            case "2":
                                estado = "moviendo";
                                estado_datos.setBackgroundResource(R.drawable.estados_abriendo);
                                boton_accion.setBackgroundResource(R.drawable.boton_parar);

                                break;
                            case "3":
                                estado = "moviendo";
                                //estado.setText("Cerrando");
                                estado_datos.setBackgroundResource(R.drawable.estados_cerrando);
                                boton_accion.setBackgroundResource(R.drawable.boton_parar);
                                break;
                            default:

                                break;
                        }


                    }
                    recDataString.delete(0, recDataString.length());      //Borra datos
                    dataInPrint = " ";

                }else{
                    Log.d("Mensaje/MSG","Salio");
                }
            }
        };
    }
    @Override
    protected void onStart() {
        super.onStart();
        db = codigos.getWritableDatabase();
        //db.execSQL("INSERT INTO codigos VALUES ('98:D3:71:F5:C3:DF')");
        c = db.rawQuery("SELECT * FROM codigos",null);
        //c = db.rawQuery("DELETE FROM codigos",null);


        if(c.moveToFirst()){
            DirMac = c.getString(0);

        }else{
            Intent registro = new Intent(modoSimple.this,conecxion.class);
            startActivity(registro);
            return;
        }
        super.onStart();
        if(adaptador.getState() == BluetoothAdapter.STATE_OFF){
            try {
                Intent enabledBT = new Intent(adaptador.ACTION_REQUEST_ENABLE);
                startActivityForResult(enabledBT, 1);

                return;
            }catch (Exception e){ }
        }
        conectarBT conexion = new conectarBT();
        conexion.execute();




    }

    void inicializar(){
        boton_accion = (ImageButton) findViewById(R.id.boton_accion);
        estado_conexion = (ImageButton) findViewById(R.id.estado_conexion);
        estado_datos = (ImageButton) findViewById(R.id.estado_datos);
        adaptador = BluetoothAdapter.getDefaultAdapter();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor contPasos = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if(contPasos != null){
            sensorManager.registerListener(this,contPasos,SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*if(Cerrar) {
            pasos = event.values[0] + pasos;
            Toast.makeText(modoSimple.this,String.valueOf(pasos),Toast.LENGTH_SHORT).show();

            if(pasos >= 8.0f){
                if(estado == "abierto"){
                    try {
                        //mmConectionThreads.write("Cerrar");
                    } catch (Exception e) {
                        try {
                            btSocket.close();
                            estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                        } catch (IOException e1) {

                        }

                    }
                    Cerrar = false;
                    pasos = 0.0f;
                }
            }
        }*/
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class conectarBT extends AsyncTask<Void,Integer,Boolean>{
        @Override
        protected void onPreExecute() {

            super.onPreExecute();
            progreso = new ProgressDialog(modoSimple.this);
            progreso.setCancelable(false);
            progreso.setTitle("Conectando...");
            progreso.show();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            progreso.dismiss();


            if (btSocket.isConnected()) {
                Log.d("Mensaje","Se conecto");
                try {
                    mmConectionThreads = new connectionThreads(btSocket);
                    mmConectionThreads.start();
                    estado_conexion.setBackgroundResource(R.drawable.estados_conectado);

                } catch (Exception e) {}
            }else{
                Log.d("Mensaje","No se conecto");
                conectarBT conexion = new conectarBT();
                conexion.execute();
                try{
                    mmConectionThreads = new connectionThreads(btSocket);
                    mmConectionThreads.start();
                }catch (Exception e){

                }
                estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                boton_accion.setBackgroundResource(R.drawable.boton_desconectado);
                estado = "";
                estado_datos.setBackgroundResource(R.drawable.estados_nulo);

            }
            if(!btSocket.isConnected()) {
                try {
                    mmConectionThreads.run();
                } catch (Exception e) {}
            }else{
                mmConectionThreads.write("A");
            }
            super.onPostExecute(aBoolean);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            super.onProgressUpdate(values);
        }

        public conectarBT() {
            super();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            publishProgress(10);

            if (adaptador != null) {
                device = adaptador.getRemoteDevice(DirMac); //MAC PRUEBA 98:D3:31:FD:23:26, MAC PORTON 98:D3:71:F5:C3:DF
                try {
                    btSocket = createBluetoothSocket(device);
                } catch (IOException e) { }
                try {
                    btSocket.connect();
                } catch (IOException e) { }


            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);
        }
    }

    private void conectar(){

        if(adaptador.getState() == BluetoothAdapter.STATE_OFF){
            try {
                Intent enabledBT = new Intent(adaptador.ACTION_REQUEST_ENABLE);
                startActivityForResult(enabledBT, 1);

                //return;
            }catch (Exception e){
                Log.d("Mensaje","No se pudo abrir");
            }
        }
        if (adaptador != null) {
            device = adaptador.getRemoteDevice(DirMac); //MAC PRUEBA 98:D3:31:FD:23:26, MAC PORTON 98:D3:71:F5:C3:DF
            try {
                btSocket = createBluetoothSocket(device);
                Log.d("Mensaje","Se intenta conectar al BT");
            } catch (IOException e) {
                Toast.makeText(this, "La creacion del Socket Fallo", Toast.LENGTH_SHORT).show();
            }
            try {
                btSocket.connect();
            } catch (IOException e) {
                Log.d("Mensaje","No se pudo conectar");
                Toast.makeText(getBaseContext(),"No se pudo conectar",Toast.LENGTH_SHORT).show();
                estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
            }
            if (btSocket.isConnected()) {
                try {
                    mmConectionThreads = new connectionThreads(btSocket);
                    mmConectionThreads.start();
                    estado_conexion.setBackgroundResource(R.drawable.estados_conectado);

                } catch (Exception e) {
                    Toast.makeText(this, "Error,Error", Toast.LENGTH_SHORT).show();

                }
            }else{
                try{
                    mmConectionThreads = new connectionThreads(btSocket);
                    mmConectionThreads.start();
                }catch (Exception e){
                    Log.d("Mensaje","No se puede inicializar el hilo");
                }
                Log.d("Mensaje","BT desconectado");
                estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                boton_accion.setBackgroundResource(R.drawable.boton_desconectado);
                estado = "";
                estado_datos.setBackgroundResource(R.drawable.estados_nulo);

            }

        } else {
            Toast.makeText(this, "El dispositivo no cuenta con Bluetooth", Toast.LENGTH_LONG).show();
        }
    }


    public class connectionThreads extends Thread {
        private OutputStream mmOutStream;
        private InputStream mmInputStream;

        private connectionThreads(BluetoothSocket socket) {
            OutputStream tmpOutStream = null;
            InputStream tmpInputStream = null;
            try {
                tmpOutStream = socket.getOutputStream();
                mmOutStream = tmpOutStream;
                tmpInputStream = socket.getInputStream();
                mmInputStream = tmpInputStream;
            } catch (IOException e) {
                mmOutStream = tmpOutStream;
            }


        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;
            while (true) {
                if(!btSocket.isConnected()){
                    estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                }

                try {
                    bytes = mmInputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState , bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
                            boton_accion.setBackgroundResource(R.drawable.boton_desconectado);
                            estado_datos.setBackgroundResource(R.drawable.estados_nulo);
                            estado = "";
                        }
                    });

                    break;
                }
            }
        }


        public void write(String input) {
            byte[] msgBuffer = input.getBytes();

            try {
                Thread.sleep(600);
                mmOutStream.write(msgBuffer);//mmOutStream.write(msgBuffer);
            } catch (Exception e) {
                estado_conexion.setBackgroundResource(R.drawable.estados_desconectado);
            }
        }

    }
}
