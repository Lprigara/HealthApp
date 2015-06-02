package com.healthapp.leonor.healthapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class BluetoothConfigActivity extends Activity implements View.OnClickListener {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> listaDispositivos;
    private ArrayAdapter arrayAdapter;					// Adaptador para el listado de dispositivos
    private Button btnBuscarDispositivo;
    private ListView lvDispositivos;
    private String nombreFichero = "dispositivoBluetooth.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activarBluetooth();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_config);
        btnBuscarDispositivo = (Button)findViewById(R.id.buscarButton);
        btnBuscarDispositivo.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buscarButton:
                buscarDispositivos();
                break;
        }
    }

    private void activarBluetooth(){

        // Obtenemos el adaptador Bluetooth. Si es NULL, el dispositivo no posee Bluetooth.
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Bluetooth no disponible");
            dialog.setMessage("Lo sentimos, este dispositivo no dispone de Bluetooth.");
            dialog.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            dialog.show();
        }
        if(!bluetoothAdapter.isEnabled()){
            peticionUsuarioActivacionBluetooth();
        }
        registrarEventosBluetooth();

    }
    private void peticionUsuarioActivacionBluetooth() {
        // Lanzamos el Intent que mostrara la interfaz de activacion del
        // Bluetooth. La respuesta de este Intent se manejara en el metodo
        // onActivityResult
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, 1);
    }

    private void buscarDispositivos(){
        if(listaDispositivos != null){
            listaDispositivos.clear();
        }
        // Comprobamos si existe un descubrimiento en curso. En caso afirmativo, se cancela.
        if(bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();

        // Iniciamos la busqueda de dispositivos
        if(bluetoothAdapter.startDiscovery()) {
            Toast.makeText(this, "Iniciando busqueda de dispositivos", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "No se pudo iniciar busqueda de dispositivos", Toast.LENGTH_SHORT).show();
        }
    }

    private void configurarListaDispositivos()
    {
        lvDispositivos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapter, View view, int position, long arg)
            {
            // El ListView tiene un adaptador de tipo BluetoothDeviceArrayAdapter.
            // Invocamos el metodo getItem() del adaptador para recibir el dispositivo
            BluetoothDevice dispositivo = (BluetoothDevice)lvDispositivos.getAdapter().getItem(position);

            guardarDispositivoEnFichero(dispositivo);
            }
        });
    }

    private void guardarDispositivoEnFichero(BluetoothDevice dispositivo)
    {
        try
        {
            OutputStreamWriter fileOut= new OutputStreamWriter(
                    openFileOutput(nombreFichero , Context.MODE_PRIVATE));
            fileOut.write(dispositivo.toString());

            fileOut.flush();
            fileOut.close();
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al escribir fichero a memoria interna");
        }
    }

    // Instanciamos un BroadcastReceiver que se encargara de detectar si el estado
    // del Bluetooth del dispositivo ha cambiado mediante su handler onReceive
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            // Cada vez que se descubra un nuevo dispositivo por Bluetooth, se ejecutara
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                if(listaDispositivos == null)
                    listaDispositivos = new ArrayList<>();

                // Extraemos el dispositivo del intent mediante la clave BluetoothDevice.EXTRA_DEVICE
                BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                listaDispositivos.add(dispositivo);

                String descripcionDispositivo = dispositivo.getName() + " [" + dispositivo.getAddress() + "]";
                Toast.makeText(getBaseContext(), getString(R.string.DetectadoDispositivo) + ": " + descripcionDispositivo, Toast.LENGTH_SHORT).show();

                Log.v("Dispositivo encontrado: ", descripcionDispositivo);
            }

            // Cuando finalice la busqueda, se ejecutara
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                lvDispositivos = (ListView)findViewById(R.id.lvDispositivos);

                arrayAdapter = new AdaptadorDispositivosBluetooth(getBaseContext(), android.R.layout.simple_list_item_2, listaDispositivos);
                lvDispositivos.setAdapter(arrayAdapter);

                Toast.makeText(getBaseContext(), "Fin de la busqueda", Toast.LENGTH_SHORT).show();
                
                configurarListaDispositivos();
            }
        }
    };

    private void registrarEventosBluetooth()
    {
        // Registramos el BroadcastReceiver para detectar los distintos eventos
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filtro.addAction(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(broadcastReceiver, filtro);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_config, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

}
