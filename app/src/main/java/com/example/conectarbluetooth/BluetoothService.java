package com.example.conectarbluetooth;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService extends Service {

    public static final String CHANNEL_ID = "BluetoothServiceChannel";
    public static final String ACTION_DISCONNECT = "DISCONNECT_BT";
    public static boolean isRunning = false;

    private BluetoothSocket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Thread workerThread;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_DISCONNECT.equals(intent.getAction())) {
            stopBluetoothConnection();
            stopSelf();
            return START_NOT_STICKY;
        }

        String address = intent != null ? intent.getStringExtra("device_address") : null;
        if (address != null && !isRunning) {
            startForegroundService();
            connectToDevice(address);
        }

        return START_STICKY;
    }

    private void startForegroundService() {
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bluetooth conectado")
                .setContentText("La conexión Bluetooth está activa.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        startForeground(1, notification);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    private void connectToDevice(String address) {
        isRunning = true;
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);

        workerThread = new Thread(() -> {
            try {
                UUID uuid = (device.getUuids() != null && device.getUuids().length > 0)
                        ? device.getUuids()[0].getUuid()
                        : UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

                socket = device.createRfcommSocketToServiceRecord(uuid);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                socket.connect();

                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

                showToast("Conectado a " + device.getName());

                byte[] buffer = new byte[1024];
                int bytes;
                while (isRunning && socket.isConnected()) {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        String received = new String(buffer, 0, bytes);
                        // Aquí podrías emitir eventos o actualizar estado si lo necesitas
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                showToast("Error al conectar: " + e.getMessage());
            } finally {
                stopBluetoothConnection();
                stopSelf();
            }
        });

        workerThread.start();
    }

    private void stopBluetoothConnection() {
        isRunning = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null && socket.isConnected()) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showToast(String message) {
        new Handler(getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bluetooth Foreground Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        stopBluetoothConnection();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
