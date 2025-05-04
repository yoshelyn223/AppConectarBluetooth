//
//package com.example.conectarbluetooth
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.bluetooth.BluetoothAdapter
//import android.bluetooth.BluetoothDevice
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.content.IntentFilter
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.app.ActivityCompat
//import com.example.conectarbluetooth.ui.theme.ConectarBluetoothTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(
//                Manifest.permission.BLUETOOTH_CONNECT,
//                Manifest.permission.BLUETOOTH_SCAN,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ),
//            1
//        )
//
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            ConectarBluetoothTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    BluetoothUI(Modifier.padding(innerPadding))
//                }
//            }
//        }
//    }
//}
//
//
//
//
//@SuppressLint("MissingPermission")
//@Composable
//fun BluetoothUI(modifier: Modifier = Modifier) {
//    val selectedDevice = remember { mutableStateOf<BluetoothDevice?>(null) }
//    val context = LocalContext.current
//    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//    val discoveredDevices = remember { mutableStateOf(setOf<BluetoothDevice>()) }
//    val pairedDevices = remember { mutableStateOf(bluetoothAdapter?.bondedDevices ?: emptySet()) }
//
//    val receiver = remember {
//        object : BroadcastReceiver() {
//            override fun onReceive(context: Context?, intent: Intent?) {
//                when (intent?.action) {
//                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
//                        Toast.makeText(context, "Buscando dispositivos...", Toast.LENGTH_SHORT).show()
//                    }
//                    BluetoothDevice.ACTION_FOUND -> {
//                        val device: BluetoothDevice? =
//                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
//                        device?.let {
//                            discoveredDevices.value = discoveredDevices.value + it
//                        }
//                    }
//                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
//                        Toast.makeText(context, "Búsqueda finalizada", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }
//    }
//
//    // Registra múltiples acciones
//    val filter = remember {
//        IntentFilter().apply {
//            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//            addAction(BluetoothDevice.ACTION_FOUND)
//            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//        }
//    }
//
//    DisposableEffect(Unit) {
//        context.registerReceiver(receiver, filter)
//        onDispose {
//            context.unregisterReceiver(receiver)
//        }
//    }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(
//            text = bluetoothAdapter?.name ?: "Dispositivo Bluetooth",
//            fontSize = 20.sp,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        // Botón para buscar dispositivos
//        Button(onClick = {
//            if (bluetoothAdapter == null) {
//                Toast.makeText(context, "Bluetooth no disponible", Toast.LENGTH_SHORT).show()
//                return@Button
//            }
//            if (!bluetoothAdapter.isEnabled) {
//                Toast.makeText(context, "Activa el Bluetooth", Toast.LENGTH_SHORT).show()
//                return@Button
//            }
//            if (bluetoothAdapter.isDiscovering) {
//                bluetoothAdapter.cancelDiscovery()
//            }
//            discoveredDevices.value = emptySet()
//            bluetoothAdapter.startDiscovery()
//        }) {
//            Text("Buscar dispositivos")
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Lista de dispositivos descubiertos
//        Text("Dispositivos descubiertos:", fontSize = 16.sp)
//        LazyColumn(modifier = Modifier.weight(1f)) {
//            items(discoveredDevices.value.toList()) { device ->
//                Text(
//                    text = "${device.name ?: "Desconocido"} - ${device.address}",
//                    modifier = Modifier
//                        .padding(8.dp)
//                        .clickable {
//                            selectedDevice.value = device
//                            connectOrPairDevice(device, context)
//                        }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Lista de emparejados
//        Text("Dispositivos emparejados:", fontSize = 16.sp)
//        LazyColumn(modifier = Modifier.weight(1f)) {
//            items(pairedDevices.value.toList()) { device ->
//                Text(
//                    text = "${device.name ?: "Desconocido"} - ${device.address}",
//                    modifier = Modifier
//                        .padding(8.dp)
//                        .clickable {
//                            selectedDevice.value = device
//                            connectOrPairDevice(device, context)
//                        }
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Botón para desconectar
//        Button(onClick = {
//            selectedDevice.value?.let {
//                val disconnectIntent = Intent(context, BluetoothService::class.java)
//                disconnectIntent.action = "DISCONNECT_BT"
//                disconnectIntent.putExtra("device_address", it.address)
//                context.startService(disconnectIntent)
//            } ?: Toast.makeText(context, "Selecciona un dispositivo primero", Toast.LENGTH_SHORT).show()
//        }) {
//            Text("Desconectar")
//        }
//    }
//}
//
//@SuppressLint("MissingPermission")
//fun connectOrPairDevice(device: BluetoothDevice, context: Context) {
//    Thread {
//        try {
//            if (device.bondState != BluetoothDevice.BOND_BONDED) {
//                device.createBond()
//                while (device.bondState != BluetoothDevice.BOND_BONDED) {
//                    Thread.sleep(500)
//                }
//            }
//
//            BluetoothAdapter.getDefaultAdapter()?.cancelDiscovery()
//            val intent = Intent(context, BluetoothService::class.java)
//            intent.putExtra("device_address", device.address)
//            context.startService(intent)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }.start()
//}
//
//
//
//
//


package com.example.conectarbluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.conectarbluetooth.ui.theme.ConectarBluetoothTheme
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            1
        )

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConectarBluetoothTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BluetoothUI(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothUI(modifier: Modifier = Modifier) {
    val selectedDevice = remember { mutableStateOf<BluetoothDevice?>(null) }
    val context = LocalContext.current
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val discoveredDevices = remember { mutableStateOf(setOf<BluetoothDevice>()) }
    val pairedDevices = remember { mutableStateOf(bluetoothAdapter?.bondedDevices ?: emptySet()) }

    // Variables para mostrar los datos del smartwatch
    val heartRate = remember { mutableStateOf("No disponible") }
    val steps = remember { mutableStateOf("No disponible") }

    val receiver = remember {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        Toast.makeText(context, "Buscando dispositivos...", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            discoveredDevices.value = discoveredDevices.value + it
                        }
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Toast.makeText(context, "Búsqueda finalizada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Registra múltiples acciones
    val filter = remember {
        IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
    }

    DisposableEffect(Unit) {
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = bluetoothAdapter?.name ?: "Dispositivo Bluetooth",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Botón para buscar dispositivos
        Button(onClick = {
            if (bluetoothAdapter == null) {
                Toast.makeText(context, "Bluetooth no disponible", Toast.LENGTH_SHORT).show()
                return@Button
            }
            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(context, "Activa el Bluetooth", Toast.LENGTH_SHORT).show()
                return@Button
            }
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            discoveredDevices.value = emptySet()
            bluetoothAdapter.startDiscovery()
        }) {
            Text("Buscar dispositivos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de dispositivos descubiertos
        Text("Dispositivos descubiertos:", fontSize = 16.sp)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(discoveredDevices.value.toList()) { device ->
                Text(
                    text = "${device.name ?: "Desconocido"} - ${device.address}",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            selectedDevice.value = device
                            connectOrPairDevice(device, context, heartRate, steps)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostrar los datos del smartwatch (Frecuencia Cardíaca, Pasos, etc.)
        Text("Frecuencia Cardiaca: ${heartRate.value}", fontSize = 16.sp)
        Text("Pasos: ${steps.value}", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de dispositivos emparejados
        Text("Dispositivos emparejados:", fontSize = 16.sp)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(pairedDevices.value.toList()) { device ->
                Text(
                    text = "${device.name ?: "Desconocido"} - ${device.address}",
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            selectedDevice.value = device
                            connectOrPairDevice(device, context, heartRate, steps)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para desconectar
        Button(onClick = {
            selectedDevice.value?.let {
                val disconnectIntent = Intent(context, BluetoothService::class.java)
                disconnectIntent.action = "DISCONNECT_BT"
                disconnectIntent.putExtra("device_address", it.address)
                context.startService(disconnectIntent)
            } ?: Toast.makeText(context, "Selecciona un dispositivo primero", Toast.LENGTH_SHORT).show()
        }) {
            Text("Desconectar")
        }
    }
}

@SuppressLint("MissingPermission")
fun connectOrPairDevice(
    device: BluetoothDevice,
    context: Context,
    heartRate: MutableState<String>,
    steps: MutableState<String>
) {
    val bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Leer características del smartwatch (por ejemplo, frecuencia cardíaca, pasos)
                val heartRateService = gatt.getService(UUID.fromString(HEART_RATE_SERVICE_UUID))
                val heartRateChar = heartRateService.getCharacteristic(UUID.fromString(HEART_RATE_CHAR_UUID))
                gatt.readCharacteristic(heartRateChar)

                // Aquí puedes leer otras características como pasos, calorías, etc.
                val stepsService = gatt.getService(UUID.fromString(STEPS_SERVICE_UUID))
                val stepsChar = stepsService.getCharacteristic(UUID.fromString(STEPS_CHAR_UUID))
                gatt.readCharacteristic(stepsChar)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Aquí procesas el valor de la característica
                when (characteristic.uuid.toString()) {
                    HEART_RATE_CHAR_UUID -> {
                        val heartRateValue = characteristic.value[0].toString()
                        heartRate.value = "$heartRateValue BPM"
                    }
                    STEPS_CHAR_UUID -> {
                        val stepsValue = characteristic.value[0].toString()
                        steps.value = "$stepsValue pasos"
                    }
                }
            }
        }
    })
}

// UUIDs de ejemplo (estos deben ser específicos para tu dispositivo)
val HEART_RATE_SERVICE_UUID = "0000180d-0000-1000-8000-00805f9b34fb"
val HEART_RATE_CHAR_UUID = "00002a37-0000-1000-8000-00805f9b34fb"
val STEPS_SERVICE_UUID = "00001806-0000-1000-8000-00805f9b34fb"
val STEPS_CHAR_UUID = "00002a54-0000-1000-8000-00805f9b34fb"
