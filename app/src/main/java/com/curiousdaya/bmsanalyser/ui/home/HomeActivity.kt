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
import com.curiousdaya.bmsanalyser.util.fullScreen
import com.curiousdaya.bmsanalyser.util.showToast

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    var deviceName  = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()
        deviceName = intent.getStringExtra("key").toString()
        binding.batterySRNo.text = "Connected Device $deviceName"


    }


    }
