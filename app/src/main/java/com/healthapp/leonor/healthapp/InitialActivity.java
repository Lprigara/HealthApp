package com.healthapp.leonor.healthapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;


public class InitialActivity extends Activity {

    private static final String TAG = "com.healthapp.leonor.healthapp.InitialActivity";
    private static final int    REQUEST_ENABLE_BT   = 1;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        conectarBluetooth();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        fillTextViews();

        tempBtnClick();
        pulseBtnClick();
        oxygenBtnClick();
        conductanceBtnClick();
        resistanceBtnClick();
    }

    private void conectarBluetooth(){

        // Obtenemos el adaptador Bluetooth. Si es NULL, significara que el
        // dispositivo no posee Bluetooth, por lo que mostramos dialogo de alerta y salimos.
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
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
            peticionUsuarioActivacionBluetooth();
        }
        else{
            peticionUsuarioActivacionBluetooth();

        }
    }

    private void peticionUsuarioActivacionBluetooth() {
        // Lanzamos el Intent que mostrara la interfaz de activacion del
        // Bluetooth. La respuesta de este Intent se manejara en el metodo
        // onActivityResult
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    /**
     * Handler del evento desencadenado al retornar de una actividad. En este caso, se utiliza
     * para comprobar el valor de retorno al lanzar la actividad que activara el Bluetooth.
     * En caso de que el usuario acepte, resultCode sera RESULT_OK
     * En caso de que el usuario no acepte, resultCode valdra RESULT_CANCELED
     */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch(requestCode)
        {
            case REQUEST_ENABLE_BT:
            {
                if(resultCode == RESULT_OK)
                {
                    // Acciones adicionales a realizar si el usuario activa el Bluetooth
                    Log.d(TAG, "oki");
                }
                else
                {
                    // Acciones adicionales a realizar si el usuario no activa el Bluetooth
                }
                break;
            }

            default:
                break;
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

            // Codigo que se ejecutara cuando el Bluetooth cambie su estado.
            // Manejaremos los siguientes estados:
            //		- STATE_OFF: El Bluetooth se desactiva
            //		- STATE ON: El Bluetooth se activa
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
            {
                // Solicitamos la informacion extra del intent etiquetada como BluetoothAdapter.EXTRA_STATE
                // El segundo parametro indicara el valor por defecto que se obtendra si el dato extra no existe
                final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch(estado)
                {
                    case BluetoothAdapter.STATE_OFF: {
                        bluetoothDesconectado();
                        break;
                    }
                    case BluetoothAdapter.STATE_ON: {
                        Log.d(TAG, "onReceive: Encendiendo");
                        // Lanzamos un Intent de solicitud de visibilidad Bluetooth, al que anadimos un par
                        // clave-valor que indicara la duracion de este estado, en este caso 120 segundos
                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                        startActivity(discoverableIntent);
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    };

    public void bluetoothDesconectado(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Bluetooth apagado");
        dialog.setMessage("El Bluetooth ha sido desconectado.");
        dialog.setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        dialog.show();
    }

    // Ademas de realizar la destruccion de la actividad, eliminamos el registro del
    // BroadcastReceiver.
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(broadcastReceiver);
    }

    private void registrarEventosBluetooth()
    {
        // Registramos el BroadcastReceiver que instanciamos previamente para
        // detectar los distintos eventos que queremos recibir
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(broadcastReceiver, filtro);
    }

    public void fillTextViews(){
        TextView tempTextView = (TextView) findViewById(R.id.tempTextView);
        tempTextView.setText("Temp");
        TextView pulseTextView = (TextView) findViewById(R.id.pulseTextView);
        pulseTextView.setText("Pulse");
        TextView oxygenTextView = (TextView) findViewById(R.id.oxygenTextView);
        oxygenTextView.setText("Oxygen");
        TextView conductanceTextView = (TextView) findViewById(R.id.conductanceTextView);
        conductanceTextView.setText("Conduct");
        TextView resistanceTextView = (TextView) findViewById(R.id.resistanceTextView);
        resistanceTextView.setText("Resist");
    }

    private void tempBtnClick() {
        Button tempBtn = (Button)findViewById(R.id.tempButton);
    }

    private void pulseBtnClick() {
        Button pulseBtn = (Button)findViewById(R.id.pulseButton);
    }

    private void oxygenBtnClick() {
        Button oxygenBtn = (Button)findViewById(R.id.oxygenButton);
    }

    private void conductanceBtnClick() {
        Button conductanceBtn = (Button)findViewById(R.id.conductanceButton);
    }

    private void resistanceBtnClick() {
        Button resistanceBtn = (Button)findViewById(R.id.resistanceButton);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_initial, menu);
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
}













