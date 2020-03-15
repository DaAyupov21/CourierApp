package com.damirayupov.corierapp

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_report_chat.*
import org.json.JSONObject


class ReportChatActivity : AppCompatActivity() {

    companion object {
        const val USER_KEY:String = "USER_KEY"
        const val ENABLE_BLUETOOTH = 1
        const val REQUEST_ENABLE_DISCOVERY = 2
        const val REQUEST_ACCESS_COARSE_LOCATION = 3
    }
    var devices = mutableListOf<BluetoothDevice?>()
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    private val bluetoothDiscoveryResult = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                devices.add(device)
            }
        }
    }

    private fun toast(message: CharSequence){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun initBluetooth() {

        if (bluetoothAdapter.isDiscovering) return

        if (bluetoothAdapter.isEnabled) {
            enableDiscovery()
        } else {
            // Bluetooth isn't enabled - prompt user to turn it on
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(intent, ENABLE_BLUETOOTH)
        }
    }

    private fun enableDiscovery() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
        startActivityForResult(intent, REQUEST_ENABLE_DISCOVERY)
    }

    private fun monitorDiscovery() {
        registerReceiver(bluetoothDiscoveryMonitor, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
        registerReceiver(bluetoothDiscoveryMonitor, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
    }


    fun hasPermission(permission: String) = ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED


    private fun startDiscovery() {
        if (hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            if (bluetoothAdapter.isEnabled && !bluetoothAdapter.isDiscovering) {
                beginDiscovery()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_ACCESS_COARSE_LOCATION
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH -> if (resultCode == Activity.RESULT_OK) {
                enableDiscovery()
            }
            REQUEST_ENABLE_DISCOVERY -> if (resultCode == Activity.RESULT_CANCELED) {
                toast("Discovery cancelled.")
            } else {
                startDiscovery()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_ACCESS_COARSE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    beginDiscovery()
                } else {
                    toast("Permission required to scan for devices.")
                }
            }
        }
    }

    private fun beginDiscovery() {
        registerReceiver(bluetoothDiscoveryResult, IntentFilter(BluetoothDevice.ACTION_FOUND))
        devices.clear()
        monitorDiscovery()
        bluetoothAdapter.startDiscovery()
    }

    private val bluetoothDiscoveryMonitor = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    toast("Scan started...")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    val nameDevices = mutableListOf<String>()
                    for (i in devices){
                        i?.name?.let { nameDevices.add(it) }
                    }
                    val gson = Gson()
                    val reportTask = ReportTask()
                    reportTask.execute(nameDevices)
                    val requestCode : JSONObject = reportTask.get()
                    Log.d("Scan", requestCode.toString())
                    val profile = gson.fromJson(reportTask.get().toString(), Profile::class.java)

                    if (profile.client_data.isNotEmpty()){
                        Log.d("Scan", "http://filmilk.herokuapp.com" + profile.client_data[0]["photo"].toString())
                        name_text_view.text = profile.client_data[0]["name"].toString()
                        Picasso.get().load("http://filmilk.herokuapp.com" + profile.client_data[0]["photo"].toString()).into(avatar_profile_imageview)
                    } else {
                        toast("Не найден пользователь")
                    }

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothDiscoveryMonitor)
        unregisterReceiver(bluetoothDiscoveryResult)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_chat)
        val bundle = intent.extras
        val user = bundle?.getSerializable(User::class.java.simpleName) as User
        initBluetooth()
    }
}
