package hr.foi.pus.detektorlazi

import BluetoothConnection
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         val TAG = "BluetoothConnection"
        var button = findViewById<Button>(R.id.btn_data)
        var button2 = findViewById<Button>(R.id.btn_zaustavi)
        var tekst = findViewById<TextView>(R.id.txt_data)
        val bluetooth = BluetoothConnection()
        button.setOnClickListener{
            val REQUEST_BLUETOOTH = 1
            Log.d(TAG, "kliknut gumb kreni")
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN), REQUEST_BLUETOOTH)
            }

            val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices

            for (device in pairedDevices) {
                Log.d(TAG, device.name + " " + device.address)
                if (device.name == "Detektor laži") {
                    Log.d(TAG, device.name + " " + device.address)


                    bluetooth.connect(device.address, this)
                    bluetooth.stopNotThread()
                    bluetooth.receiveData { data ->
                        runOnUiThread {
                            if (data != null)
                                tekst.text = String(data)
                            else
                                tekst.text = "Error or thread interruption"
                        }
                    }
                    //Log.d("Device: ", data.toString())
                    //tekst.text = String(data!!)

                }
            }
        }

        button2.setOnClickListener {
            bluetooth.disconnect()
            Log.d(TAG, "kliknut gumb stani")
            bluetooth.stopThread()
        }
    }
}