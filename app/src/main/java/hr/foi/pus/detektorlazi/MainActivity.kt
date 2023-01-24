package hr.foi.pus.detektorlazi

import BluetoothConnection
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

    lateinit var bpm: TextView
    lateinit var temp: TextView
    lateinit var hum: TextView
    lateinit var res: TextView
    lateinit var avg_bpm: TextView
    lateinit var avg_temp: TextView
    lateinit var avg_hum: TextView
    lateinit var avg_res: TextView
    lateinit var rezultat: TextView

    var suma_bpm: Float = 0f
    var suma_res: BigDecimal = BigDecimal.valueOf(0.0)
    var suma_temp: Float = 0f
    var suma_hum: Float = 0f

    var cal_bpm: Float = 0f
    var cal_res: BigDecimal = BigDecimal.valueOf(0.0)
    var cal_temp: Float = 0f
    var cal_hum: Float = 0f

    var prosjek_bpm: BigDecimal = BigDecimal.valueOf(0.0)
    var prosjek_res: BigDecimal = BigDecimal.valueOf(0.0)
    var prosjek_temp: BigDecimal = BigDecimal.valueOf(0.0)
    var prosjek_hum: BigDecimal = BigDecimal.valueOf(0.0)

    var kalibracija = true

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val TAG = "BluetoothConnection"
        val button = findViewById<Button>(R.id.btn_data)
        //val button2 = findViewById<Button>(R.id.btn_zaustavi)
        val tekst = findViewById<TextView>(R.id.txt_data)
        val bluetooth = BluetoothConnection()

        bpm = findViewById(R.id.txt_bpm)
        temp = findViewById(R.id.txt_temp)
        hum = findViewById(R.id.txt_hum)
        res = findViewById(R.id.txt_res)
        avg_bpm = findViewById(R.id.txt_avg_bpm)
        avg_temp = findViewById(R.id.txt_avg_temp)
        avg_hum = findViewById(R.id.txt_avg_hum)
        avg_res = findViewById(R.id.txt_avg_res)
        rezultat = findViewById(R.id.txt_rezultat)

        button.setOnClickListener {
            suma_bpm = 0f
            suma_res = BigDecimal.valueOf(0.0)
            suma_temp = 0f
            suma_hum = 0f
            var brojac = 0
            val REQUEST_BLUETOOTH = 1

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH
                ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN),
                    REQUEST_BLUETOOTH
                )
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
                            if (data != null) {
                                ProcitajPodatke(String(data))
                                brojac++
                                if (brojac == 30) {
                                    Log.d(TAG, "GOTOVO")
                                    IzracunajProsjek()
                                    if (kalibracija == false) {
                                        ProvjeriIstinu()
                                    }
                                    kalibracija = false
                                }
                            } else
                                tekst.text = "Error or thread interruption"
                        }
                    }
                }
            }
        }

        /*button2.setOnClickListener {
            bluetooth.disconnect()
            Log.d(TAG, "kliknut gumb stani")
            bluetooth.stopThread()
        }*/
    }

    @SuppressLint("SetTextI18n")
    fun ProvjeriIstinu() {
        var detekcija = 0
        val TAG = "BluetoothConnection"
        Log.d(TAG, cal_bpm.toString() + " " + prosjek_bpm.toString())
        Log.d(TAG, cal_res.toString() + " " + prosjek_res.toString())
        Log.d(TAG, cal_temp.toString() + " " + prosjek_temp.toString())
        Log.d(TAG, cal_hum.toString() + " " + prosjek_hum.toString())

        if (prosjek_bpm > BigDecimal.valueOf((cal_bpm+cal_bpm*0.07f).toDouble())){
            Log.d(TAG, "Pad na bpm")
            detekcija++
        }
        if (prosjek_res < BigDecimal.valueOf(1000000)){
            Log.d(TAG, "Pad na res")
            detekcija++
        }
        if (prosjek_hum > BigDecimal.valueOf((cal_hum+cal_hum*0.05f).toDouble())){
            Log.d(TAG, "Pad na hum")
            detekcija++
        }
        if (prosjek_temp > BigDecimal.valueOf((cal_temp+cal_temp*0.03f).toDouble())){
            Log.d(TAG, "Pad na temp")
            detekcija++
        }

        if(detekcija==0) {
            rezultat.text = "ISTINA"
            rezultat.setTextColor(Color.GREEN)
        }//normalno
        if(detekcija>0) {
            rezultat.text = "SUMNJIVA OČITANJA"
            rezultat.setTextColor(Color.YELLOW)
        }//sumnjivo
        if(detekcija>2) {
            rezultat.text = "LAŽ"
            rezultat.setTextColor(Color.RED)
        }//laze
    }

    @SuppressLint("SetTextI18n")
    fun IzracunajProsjek() {
        prosjek_bpm = (suma_bpm / 30).toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        prosjek_res = suma_res.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
        prosjek_hum = (suma_hum / 30).toBigDecimal().setScale(2, RoundingMode.HALF_UP)
        prosjek_temp = (suma_temp / 30).toBigDecimal().setScale(2, RoundingMode.HALF_UP)

        avg_bpm.text = "BPM:" + prosjek_bpm.toString()
        avg_res.text = "R:" + prosjek_res.toString()
        avg_hum.text = "H:" + prosjek_hum.toString()
        avg_temp.text = "T:" + prosjek_temp.toString()

        if (kalibracija == true) {
            cal_bpm = suma_bpm / 30
            cal_res = suma_res.divide(BigDecimal.valueOf(30), 2, RoundingMode.HALF_UP)
            cal_hum = suma_hum / 30
            cal_temp = suma_temp / 30
            rezultat.text = "ZAVRŠENA KALIBRACIJA"
        }
    }

    fun ProcitajPodatke(podaci: String) {
        val ocitanje = podaci.split("/n")[0]
        val podaci = ocitanje.split(";")

        val bpm_value = podaci[0].split(":")[1]
        val res_value = podaci[1].split(":")[1]
        val temp_value = podaci[2].split(":")[1]
        val hum_value = podaci[3].split(":")[1]

        bpm.text = podaci[0]
        suma_bpm += bpm_value.toFloat()
        res.text = podaci[1]
        suma_res += BigDecimal(res_value)
        temp.text = podaci[2]
        suma_temp += temp_value.toFloat()
        hum.text = podaci[3]
        suma_hum += hum_value.toFloat()
    }
}