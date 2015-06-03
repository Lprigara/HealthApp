package com.healthapp.leonor.healthapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BluetoothService {

    private final Handler handler;

    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private static final String TAG = "com.healthapp.leonor.healthapp.BluetoothServices";

    public static final int ESTADO_NINGUNO = 0;
    public static final int ESTADO_CONECTADO = 1;
    public static final int ESTADO_REALIZANDO_CONEXION  = 2;
    public static final int MSG_CAMBIO_ESTADO = 10;

    public static UUID UUID_SEGURO;
    public static UUID UUID_INSEGURO;

    private int estadoConexion;
    private HiloCliente     hiloCliente     = null;
    private HiloConexion    hiloConexion    = null;

    public BluetoothService(Context context, Handler handler, BluetoothAdapter adapter)
    {
        Log.v("BluetoothService()", "Iniciando metodo");

        this.context    = context;
        this.handler    = handler;
        this.bluetoothAdapter   = adapter;
        this.estadoConexion = ESTADO_NINGUNO;

        UUID_SEGURO = generarUUID();
        UUID_INSEGURO = generarUUID();
    }

    private synchronized void setEstadoConexion(int estado)
    {
        this.estadoConexion = estado;
        handler.obtainMessage(MSG_CAMBIO_ESTADO, estado, -1).sendToTarget();
    }

    // Instancia un hilo conector
    public synchronized void solicitarConexion(BluetoothDevice dispositivo)
    {
        Log.d(TAG, "Solicitando Conexion");

        // Si existia una conexion abierta, se cierra y se inicia una nueva
        if(hiloConexion != null)
        {
            hiloConexion.cancelarConexion();
            hiloConexion = null;
        }

        // Se instancia un nuevo hilo conector
        hiloCliente = new HiloCliente(dispositivo);
        hiloCliente.start();

        setEstadoConexion(ESTADO_REALIZANDO_CONEXION);
    }

    public synchronized void realizarConexion(BluetoothSocket socket, BluetoothDevice dispositivo)
    {
        Log.v("realizarConexion()", "Iniciando metodo");
        hiloConexion = new HiloConexion(socket);
        hiloConexion.start();
    }

    // Hilo encargado de solicitar una conexion a un dispositivo que este corriendo un
    // HiloServidor.
    private class HiloCliente extends Thread
    {
        private final BluetoothDevice dispositivo;
        private final BluetoothSocket socket;

        public HiloCliente(BluetoothDevice dispositivo)
        {
            BluetoothSocket tmpSocket = null;
            this.dispositivo = dispositivo;

            // Obtenemos un socket para el dispositivo con el que se quiere conectar
            try {
                tmpSocket = dispositivo.createRfcommSocketToServiceRecord(UUID_SEGURO);
            }
            catch(IOException e) {
                Log.e(TAG, "HiloCliente.HiloCliente(): Error al abrir el socket", e);
            }
            socket = tmpSocket;
        }

        public void run()
        {
            Log.d("HiloCliente.run()", "Iniciando metodo");
            setName("HiloCliente");
            if(bluetoothAdapter.isDiscovering())
                bluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
                setEstadoConexion(ESTADO_REALIZANDO_CONEXION);
            }
            catch(IOException e) {
                Log.e("s", "HiloCliente.run(): socket.connect(): Error realizando la conexion", e);
                try {
                    socket.close();
                }
                catch(IOException inner) {
                    Log.e("s", "HiloCliente.run(): Error cerrando el socket", inner);
                }
                setEstadoConexion(ESTADO_NINGUNO);
            }

            synchronized(BluetoothService.this)
            {
                hiloCliente = null;
            }

            // Realizamos la conexion
            realizarConexion(socket,dispositivo);
        }
    }

    // Hilo encargado de mantener la conexion
    private class HiloConexion extends Thread
    {
        private final BluetoothSocket   socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public HiloConexion(BluetoothSocket socket)
        {
            this.socket = socket;
            setName(socket.getRemoteDevice().getName() + " [" + socket.getRemoteDevice().getAddress() + "]");

            InputStream tmpInputStream = null;
            OutputStream tmpOutputStream = null;

            // Obtenemos los flujos de entrada y salida del socket.
            try {
                tmpInputStream = socket.getInputStream();
                tmpOutputStream = socket.getOutputStream();
            }
            catch(IOException e){
                Log.e("d", "HiloConexion(): Error al obtener flujos de E/S", e);
            }
            inputStream = tmpInputStream;
            outputStream = tmpOutputStream;
        }

        public void run()
        {
            setEstadoConexion(ESTADO_CONECTADO);
        }

        public void cancelarConexion()
        {
            try {
                socket.close();
                setEstadoConexion(ESTADO_NINGUNO);
            }
            catch(IOException e) {
                Log.e(TAG, "HiloConexion.cerrarConexion(): Error al cerrar la conexion", e);
            }
        }
    }


    private UUID generarUUID()
    {
        ContentResolver appResolver = context.getApplicationContext().getContentResolver();
        final TelephonyManager tManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        final String deviceId = String.valueOf(tManager.getDeviceId());
        final String simSerialNumber = String.valueOf(tManager.getSimSerialNumber());
        final String androidId  = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID uuid = new UUID(androidId.hashCode(), ((long)deviceId.hashCode() << 32) | simSerialNumber.hashCode());
        uuid = new UUID((long)1000, (long)23);
        return uuid;
    }


}
