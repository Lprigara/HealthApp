package com.healthapp.leonor.healthapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;


public class InitialActivity extends Activity {

    private static final String TAG = "com.healthapp.leonor.healthapp.InitialActivity";
    private static final int    REQUEST_ENABLE_BT   = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothService servicio;
    private String nombreFichero = "dispositivoBluetooth.txt";
    private BluetoothDevice dispositivo;
    private Paquete paquete;
    private StringBuilder recogidaDatosString = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activarBluetooth();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        tempBtnClick();
        pulseBtnClick();
        oxygenBtnClick();
        conductanceBtnClick();
        resistanceBtnClick();
    }

    public String leerDispositivoDeFichero()
    {
        try
        {
            BufferedReader fin =
                    new BufferedReader(
                            new InputStreamReader(
                                    openFileInput(nombreFichero)));

            String direccionDispositivoRemoto = fin.readLine();
            fin.close();

            return direccionDispositivoRemoto;
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al leer fichero desde memoria interna");
            finish();
            return null;
        }
    }

    private void activarBluetooth()
    {
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
        if(bluetoothAdapter.isEnabled()) {
            if(servicio != null)
            {
                servicio.finalizarServicio();
                servicio.iniciarServicio();
            }
            else {
                servicio = new BluetoothService(this, handler, bluetoothAdapter);
            }

            String direccion = leerDispositivoDeFichero();
            conectarDispositivo(direccion);
        }
        else {
            peticionUsuarioActivacionBluetooth();
        }

    }

    private void peticionUsuarioActivacionBluetooth()
    {
        // Lanzamos el Intent que mostrara la interfaz de activacion del
        // Bluetooth. La respuesta de este Intent se manejara en el metodo
        // onActivityResult
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public void conectarDispositivo(String direccion)
    {
        BluetoothDevice dispositivo = bluetoothAdapter.getRemoteDevice(direccion);
        Toast.makeText(this, "Conectando a " + dispositivo.getName(), Toast.LENGTH_LONG).show();

        if(servicio != null)
        {
            BluetoothDevice dispositivoRemoto = bluetoothAdapter.getRemoteDevice(direccion);
            servicio.solicitarConexion(dispositivoRemoto);
            this.dispositivo = dispositivoRemoto;
        }
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
                    if(servicio != null)
                    {
                        servicio.finalizarServicio();
                        servicio.iniciarServicio();
                    }
                    else {
                        servicio = new BluetoothService(this, handler, bluetoothAdapter);
                    }

                    String direccion = leerDispositivoDeFichero();
                    conectarDispositivo(direccion);
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

    public void leerVariables(Message msg){
        Log.v(TAG, "Leyendo mensaje");
        String mensaje = (String) msg.obj;
        recogidaDatosString.append(mensaje);
        int caracterFinal = recogidaDatosString.indexOf("~");
        if (caracterFinal > 0) {
            String dataInPrint = recogidaDatosString.substring(0, caracterFinal);
            Log.v("Data Received = ", dataInPrint);

            String[] arrayVariables = dataInPrint.split("\\+");

            if(arrayVariables[0].equals("#")){
                paquete = new Paquete(arrayVariables);
                rellenarTextViews(paquete);
            }
            recogidaDatosString.delete(0, recogidaDatosString.length());
            dataInPrint = " ";
        }
    }
    // Handler que obtendr? informacion de BluetoothService
    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg)
        {
            byte[] buffer 	= null;
            String mensaje 	= null;

            // Atendemos al tipo de mensaje
            switch(msg.what)
            {
                // Mensaje de lectura: se mostrara en el TextView
                case BluetoothService.MSG_LEER:
                {
                    leerVariables(msg);
                    break;
                }

                // Mensaje de cambio de estado
                case BluetoothService.MSG_CAMBIO_ESTADO:
                {
                    switch(msg.arg1)
                    {
                        case BluetoothService.ESTADO_ATENDIENDO_PETICIONES:
                            break;

                        // CONECTADO: Se muestra el dispositivo al que se ha conectado
                        case BluetoothService.ESTADO_CONECTADO:
                        {
                            mensaje = getString(R.string.ConexionActual) + " " + servicio.getNombreDispositivo();
                            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                            break;
                        }

                        // REALIZANDO CONEXION: Se muestra el dispositivo al que se esta conectando
                        case BluetoothService.ESTADO_REALIZANDO_CONEXION:
                        {
                            mensaje = getString(R.string.ConectandoA) + " " + dispositivo.getName() + " [" + dispositivo.getAddress() + "]";
                            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                            break;
                        }

                        // NINGUNO: Mensaje por defecto.
                        case BluetoothService.ESTADO_NINGUNO:
                        {
                            mensaje = getString(R.string.SinConexion);
                            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                            break;
                        }
                        default:
                            break;
                    }
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK) {
            servicio.finalizarServicio();
        }
        return super.onKeyDown(keyCode, event);
    }
    // Ademas de realizar la destruccion de la actividad, eliminamos el registro del
    // BroadcastReceiver.
    @Override
    public void onDestroy() {
        super.onDestroy();
//        this.unregisterReceiver(broadcastReceiver);
        if(servicio != null)
            servicio.finalizarServicio();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(servicio != null)
        {
            if(servicio.getEstadoConexion() == BluetoothService.ESTADO_NINGUNO)
            {
                servicio.iniciarServicio();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    private void registrarEventosBluetooth()
    {
        // Registramos el BroadcastReceiver que instanciamos previamente para
        // detectar los distintos eventos que queremos recibir
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(broadcastReceiver, filtro);
    }

    public void rellenarTextViews(Paquete paquete){
        TextView tempTextView = (TextView) findViewById(R.id.tempTextView);
        tempTextView.setText(paquete.getTemperatura());
        TextView pulseTextView = (TextView) findViewById(R.id.pulseTextView);
        pulseTextView.setText(paquete.getPulso());
        TextView oxygenTextView = (TextView) findViewById(R.id.oxygenTextView);
        oxygenTextView.setText(paquete.getOxygeno());
        TextView conductanceTextView = (TextView) findViewById(R.id.conductanceTextView);
        conductanceTextView.setText(paquete.getConductancia());
        TextView resistanceTextView = (TextView) findViewById(R.id.resistanceTextView);
        resistanceTextView.setText(paquete.getResistencia());
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













