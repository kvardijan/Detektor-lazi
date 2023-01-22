import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*
import androidx.core.app.ActivityCompat


class BluetoothConnection {
    private val REQUEST_BLUETOOTH = 1
    private val TAG = "BluetoothConnection"
    private var shouldStop = false

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var device: BluetoothDevice? = null
    private var socket: BluetoothSocket? = null

    fun connect(address: String, activity: Activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN), REQUEST_BLUETOOTH)
            return
        }
        //rest of the code
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        device = bluetoothAdapter?.getRemoteDevice(address)

        try {
            socket = device?.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            socket?.connect()
            Log.d(TAG, "Connected to ESP32 device")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to connect to ESP32 device", e)
        }
    }

    fun receiveData(callback: (ByteArray?) -> Unit) {
        Thread{
            var i = 0
            while (i < 30 && !shouldStop){
                Log.d(TAG, "iteracija " + i)
                try {
                    val inputStream = socket?.inputStream
                    val buffer = ByteArray(1024)
                    Thread.sleep(100)
                    Log.d(TAG, inputStream?.available().toString())

                    if(inputStream == null){
                        Log.d(TAG, "null je")
                    }else{
                        Log.d(TAG, "nije null")
                    }

                    val bytes = inputStream?.read(buffer)
                    Log.d(TAG, "Received data: " + String(buffer))
                    i++
                    Log.d(TAG, "prije pozivanja callback")
                    callback(buffer.copyOfRange(0, bytes!!))
                    Log.d(TAG, "prije sleep")
                    Thread.sleep(1000)
                    Log.d(TAG, "poslje sleep")
                    if(i == 30){
                        Log.d(TAG, "limit")
                        disconnect()
                    }
                    //return buffer.copyOfRange(0, bytes!!)
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to receive data from ESP32 device", e)
                    callback(null)
                    return@Thread
                    //return null
                } catch (e: InterruptedException) {
                    Log.e(TAG, "Thread interrupted", e)
                    callback(null)
                    return@Thread
                }
            }
        }.start()
    }

    fun stopThread(){
        shouldStop = true
    }

    fun stopNotThread(){
        shouldStop = false
    }

    fun disconnect() {
        try {
            socket?.close()
            Log.d(TAG, "Disconnected from ESP32 device")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to disconnect from ESP32 device", e)
        }
    }
}
