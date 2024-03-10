package com.curiousdaya.bmsanalyser.ui.home

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.curiousdaya.bmsanalyser.R
import com.curiousdaya.bmsanalyser.databinding.ActivityHomeBinding
import com.curiousdaya.bmsanalyser.ui.qrScanner.QRActivity
import com.curiousdaya.bmsanalyser.util.Prefs
import com.curiousdaya.bmsanalyser.util.fullScreen
import com.curiousdaya.bmsanalyser.util.moveActivity
import com.curiousdaya.bmsanalyser.util.showToast

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()
        binding.batterySRNo.text = "DeviceName:::${Prefs.getInstance(this).bluetoothDeviceName}"
        binding.bMSSRNo.text = "DeviceAddress:::${Prefs.getInstance(this).bluetoothDeviceAddress}"
        initControl()
    }

    private fun initControl() {
        binding.logoutIcon.setOnClickListener {
            moveActivity(QRActivity())
            showToast("You are logging out.")
            Prefs.getInstance(this).bluetoothDeviceName = ""
            Prefs.getInstance(this).bluetoothDeviceAddress = ""
            unpairAllDevices()
            finishAffinity()
        }
    }

    fun unpairAllDevices() {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.forEach { device -> unpairDevice(device) }
    }

    private fun unpairDevice(device: BluetoothDevice) {
        try {
            // Correctly getting the method without parameters
            val method = device.javaClass.getMethod("removeBond")
            method.invoke(device)
        } catch (exception: Exception) {
            Log.e("UnpairDevice", "Could not unpair device: ${device.name}", exception)
        }
    }



}
