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
    public static final String NOMBRE_SEGURO = "BluetoothServiceSecure";

    public static final int ESTADO_NINGUNO = 0;
    public static final int ESTADO_CONECTADO = 1;
    public static final int ESTADO_REALIZANDO_CONEXION  = 2;
    public static final int ESTADO_ATENDIENDO_PETICIONES= 3;
    public static final int MSG_CAMBIO_ESTADO = 10;
    public static final int MSG_LEER = 11;

    public static UUID UUID_SEGURO ;

    private int estadoConexion;
    private HiloServidor    hiloServidor    = null;
    private HiloCliente     hiloCliente     = null;
    private HiloConexion    hiloConexion    = null;

    public BluetoothService(Context context, Handler handler, BluetoothAdapter adapter)
    {
        Log.v("BluetoothService()", "Iniciando metodo");

        this.context    = context;
        this.handler    = handler;
        this.bluetoothAdapter   = adapter;
        this.estadoConexion = ESTADO_NINGUNO;

        UUID_SEGURO = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    private synchronized void setEstadoConexion(int estado)
    {
        this.estadoConexion = estado;
        handler.obtainMessage(MSG_CAMBIO_ESTADO, estado, -1).sendToTarget();
    }

    public synchronized int getEstadoConexion()
    {
        return estadoConexion;
    }

    public String getNombreDispositivo()
    {
        String nombre = "";
        if(estadoConexion == ESTADO_CONECTADO)
        {
            if(hiloConexion != null)
                nombre = hiloConexion.getName();
        }

        return nombre;
    }

    // Inicia el servicio, creando un HiloServidor que se dedicara a atender las peticiones
    // de conexion.
    public synchronized void iniciarServicio()
    {
        Log.v("iniciarServicio()", "Iniciando metodo");

        // Si se esta intentando realizar una conexion mediante un hilo cliente,
        // se cancela la conexion
        if(hiloCliente != null)
        {
            hiloCliente.cancelarConexion();
            hiloCliente = null;
        }

        // Si existe una conexion previa, se cancela
        if(hiloConexion != null)
        {
            hiloConexion.cancelarConexion();
            hiloConexion = null;
        }

        // Arrancamos el hilo servidor para que empiece a recibir peticiones
        // de conexion
        if(hiloServidor == null)
        {
            hiloServidor = new HiloServidor();
            hiloServidor.start();
        }

        Log.v("iniciarServicio()", "Finalizando metodo");
    }

    public void finalizarServicio()
    {
        Log.d("finalizarServicio()", "Iniciando metodo");

        if(hiloCliente != null)
            hiloCliente.cancelarConexion();
        if(hiloConexion != null)
            hiloConexion.cancelarConexion();
        if(hiloServidor != null)
            hiloServidor.cancelarConexion();

        hiloCliente = null;
        hiloConexion = null;
        hiloServidor = null;

        setEstadoConexion(ESTADO_NINGUNO);
    }

    // Instancia un hilo conector
    public synchronized void solicitarConexion(BluetoothDevice dispositivo)
    {
        Log.d("solicitarConexion()", "Iniciando metodo");
        // Comprobamos si existia un intento de conexion en curso.
        // Si es el caso, se cancela y se vuelve a iniciar el proceso
        if(estadoConexion == ESTADO_REALIZANDO_CONEXION)
        {
            if(hiloCliente != null)
            {
                hiloCliente.cancelarConexion();
                hiloCliente = null;
            }
        }

        // Si existia una conexion abierta, se cierra y se inicia una nueva
        if(hiloConexion != null)
        {
            hiloConexion.cancelarConexion();
            hiloConexion = null;
        }

        // Se instancia un nuevo hilo conector, encargado de solicitar una conexion
        // al servidor, que sera la otra parte.
        hiloCliente = new HiloCliente(dispositivo);
        hiloCliente.start();

        setEstadoConexion(ESTADO_REALIZANDO_CONEXION);
    }

    public synchronized void realizarConexion(BluetoothSocket socket)
    {
        Log.v("realizarConexion()", "Iniciando metodo");
        hiloConexion = new HiloConexion(socket);
        hiloConexion.start();
    }

    // Hilo que hace las veces de servidor, encargado de escuchar conexiones entrantes y
    // crear un hilo que maneje la conexion cuando ello ocurra.
    // La otra parte debera solicitar la conexion mediante un HiloCliente.
    private class HiloServidor extends Thread
    {
        private final BluetoothServerSocket serverSocket;

        public HiloServidor()
        {
            Log.d("HiloServidor.new()", "Iniciando metodo");
            BluetoothServerSocket tmpServerSocket = null;

            // Creamos un socket para escuchar las peticiones de conexion
            try {
                tmpServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NOMBRE_SEGURO, UUID_SEGURO);
            } catch(IOException e) {
                Log.e("s", "HiloServidor(): Error al abrir el socket servidor", e);
            }

            serverSocket = tmpServerSocket;
        }

        public void run() {
            Log.d("HiloServidor.run()", "Iniciando metodo");
            BluetoothSocket socket = null;

            setName("HiloServidor");
            setEstadoConexion(ESTADO_ATENDIENDO_PETICIONES);
            // El hilo se mantendra en estado de espera ocupada aceptando conexiones
            // entrantes siempre y cuando no exista una conexion activa.
            // En el momento en el que entre una nueva conexion,
            while (estadoConexion != ESTADO_CONECTADO)
            {
                try {
                    // Cuando un cliente solicite la conexion se abrirá el socket.
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    Log.e("s", "HiloServidor.run(): Error al aceptar conexiones entrantes", e);
                    break;
                }
                // Si el socket tiene valor sera porque un cliente ha solicitado la conexion
                if(socket != null)
                {
                    // Realizamos un lock del objeto
                    synchronized(BluetoothService.this)
                    {
                        switch(estadoConexion)
                        {
                            case ESTADO_ATENDIENDO_PETICIONES:
                            case ESTADO_REALIZANDO_CONEXION:
                            {
                                // Estado esperado, se crea el hilo de conexion que recibir los mensajes
                                realizarConexion(socket);
                                break;
                            }
                            case ESTADO_NINGUNO:
                            case ESTADO_CONECTADO:
                            {
                                // No preparado o conexion ya realizada. Se cierra el nuevo socket.
                                try {
                                    socket.close();
                                }
                                catch(IOException e) {
                                    Log.e("s", "HiloServidor.run(): socket.close(). Error al cerrar el socket.", e);
                                }
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }

            }
        }

        public void cancelarConexion()
        {
            Log.v("HiloServidor.cancelarConexion()", "Iniciando metodo");
            try {
                serverSocket.close();
            }
            catch(IOException e) {
                Log.e("s", "HiloServidor.cancelarConexion(): Error al cerrar el socket", e);
            }
        }
    }

    // Hilo encargado de solicitar una conexion a un dispositivo que este corriendo un
    // HiloServidor.
    private class HiloCliente extends Thread
    {
        private final BluetoothDevice dispositivo;
        private final BluetoothSocket socket;

        public HiloCliente(BluetoothDevice dispositivo)
        {
            Log.v("HiloCliente.new()", "Iniciando metodo");
            BluetoothSocket tmpSocket = null;
            this.dispositivo = dispositivo;

            // Obtenemos un socket para el dispositivo con el que se quiere conectar
            try {
                tmpSocket = dispositivo.createRfcommSocketToServiceRecord(UUID_SEGURO);
            }
            catch(IOException e) {
                Log.e("s", "HiloCliente.HiloCliente(): Error al abrir el socket", e);
            }

            socket = tmpSocket;
        }

        public void run()
        {
            Log.d("HiloCliente.run()", "Iniciando metodo");
            setName("HiloCliente");

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

            // Reiniciamos el hilo cliente, ya que no lo necesitaremos mas
            synchronized(BluetoothService.this)
            {
                hiloCliente = null;
            }

            // Realizamos la conexion
            realizarConexion(socket);
        }

        public void cancelarConexion()
        {
            Log.v("cancelarConexion()", "Iniciando metodo");
            try {
                socket.close();
            }
            catch(IOException e) {
                Log.e("s", "HiloCliente.cancelarConexion(): Error al cerrar el socket", e);
            }
            setEstadoConexion(ESTADO_NINGUNO);
        }
    }

    // Hilo encargado de mantener la conexion y realizar las lecturas
    // de los mensajes intercambiados entre dispositivos.
    private class HiloConexion extends Thread
    {
        private final BluetoothSocket   socket;
        private final InputStream inputStream;

        public HiloConexion(BluetoothSocket socket)
        {
            Log.v("HiloConexion.new()", "Iniciando metodo");
            this.socket = socket;
            setName(socket.getRemoteDevice().getName() + " [" + socket.getRemoteDevice().getAddress() + "]");

            InputStream tmpInputStream = null;

            // Obtenemos los flujos de entrada y salida del socket.
            try {
                tmpInputStream = socket.getInputStream();
            } catch(IOException e){
                Log.e("d", "HiloConexion(): Error al obtener flujo de E", e);
            }
            inputStream = tmpInputStream;
        }

        // Metodo principal del hilo, encargado de realizar las lecturas
        public void run()
        {
            Log.v("HiloConexion.run()", "Iniciando metodo");
            byte[] buffer = new byte[1024];
            int bytes;
            setEstadoConexion(ESTADO_CONECTADO);
        }

        public void cancelarConexion()
        {
            Log.v("HiloConexion.cancelarConexion()", "Iniciando metodo");
            try {
                // Forzamos el cierre del socket
                socket.close();

                // Cambiamos el estado del servicio
                setEstadoConexion(ESTADO_NINGUNO);
            }
            catch(IOException e) {
                Log.e("s", "HiloConexion.cerrarConexion(): Error al cerrar la conexion", e);
            }
        }
    }

}
