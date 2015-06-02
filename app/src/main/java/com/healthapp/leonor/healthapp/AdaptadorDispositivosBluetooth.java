package com.healthapp.leonor.healthapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class AdaptadorDispositivosBluetooth extends ArrayAdapter {

    private List<BluetoothDevice> listaDispositivos;
    private Context context;

    public AdaptadorDispositivosBluetooth(Context context, int textViewId,
                                          List<BluetoothDevice> objects) {
        super(context, textViewId, objects);

        this.listaDispositivos = objects;
        this.context = context;
    }

    @Override
    public int getCount()
    {
        if(listaDispositivos != null)
            return listaDispositivos.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position)
    {
        return (listaDispositivos == null ? null : listaDispositivos.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if((listaDispositivos == null) || (context == null)){
            return null;
        }
        // Usamos un LayoutInflater para crear las vistas
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Creamos una vista a partir de simple_list_item_2, que contiene dos TextView.
        View elemento = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);

        TextView tvNombre = (TextView)elemento.findViewById(android.R.id.text1);
        TextView tvDireccion = (TextView)elemento.findViewById(android.R.id.text2);

        // Obtenemos el dispositivo del array y obtenemos su nombre y direccion, asociandosela
        // a los dos TextView del elemento
        BluetoothDevice dispositivo = (BluetoothDevice)getItem(position);
        if(dispositivo != null)
        {
            tvNombre.setText(dispositivo.getName());
            tvDireccion.setText(dispositivo.getAddress());
        }
        else
        {
            tvNombre.setText("ERROR");
        }

        // Devolvemos el elemento con los dos TextView cumplimentados
        return elemento;
    }

}
