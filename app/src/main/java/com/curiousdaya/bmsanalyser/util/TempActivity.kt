package com.curiousdaya.bmsanalyser.ui.qrScanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.curiousdaya.bmsanalyser.R
import com.curiousdaya.bmsanalyser.databinding.ActivityQractivityBinding
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.BarcodeDetector

class TempActivity
    : AppCompatActivity() {
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var binding: ActivityQractivityBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var scannedValue = ""
    private var deviceName = ""
    private var isParedWithDevice = false
    private val foundDevices = StringBuilder()

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1001
        private const val REQUEST_CODE_BLUETOOTH_PERMISSIONS = 101
        private const val REQUEST_ENABLE_BT = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupPermissions()
        initializeUI()
    }

    private fun setupPermissions() {
        if (Build.VERSION.CODENAME >= Build.VERSION.CODENAME)
        {
            requestBluetoothPermissions()
        } else
        {
            checkBluetoothSupport()
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_CAMERA_PERMISSION
            )
        } else {
            setupControls()
        }
    }

    private fun requestBluetoothPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                REQUEST_CODE_BLUETOOTH_PERMISSIONS
            )
        } else {
            checkBluetoothSupport()
        }
    }

    private fun checkBluetoothSupport() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                bluetoothAdapter.startDiscovery()
            }
        }
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
    }

    private fun initializeUI() {
        val aniSlide = AnimationUtils.loadAnimation(this, R.anim.scanner_animation)
        binding.barcodeLine.startAnimation(aniSlide)
        initControl()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupControls()
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }

            REQUEST_CODE_BLUETOOTH_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkBluetoothSupport()
                } else {
                    Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setupControls() {
        // Setup camera and barcode detector
    }

    private fun initControl() {
        // Initialize control listeners
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraSource.isInitialized) {
            cameraSource.stop()
        }
        if (::bluetoothAdapter.isInitialized) {
            unregisterReceiver(receiver)
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Handle Bluetooth device found
        }
    }

    // Other helper methods like unpairDevice, createBondWithDevice, etc.
}
