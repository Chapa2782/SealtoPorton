package com.sealtosoft.porton.sealtoporton;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentBusqueda extends Fragment {
    ImageButton BtnBuscar;
    ListView Listado;
    BluetoothAdapter adaptador;
    ArrayAdapter arrayAdapter;
    String[] dir = new String[20];


    public FragmentBusqueda() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fragment_busqueda, container, false);
        BtnBuscar = v.findViewById(R.id.btnBuscar);
        Listado = v.findViewById(R.id.Listado);

        return v;
    }

    public void Busqueda(View v){
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

}
