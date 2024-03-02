package com.curiousdaya.bmsanalyser.ui.qrScanner
import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.curiousdaya.bmsanalyser.R
import com.curiousdaya.bmsanalyser.databinding.ActivityQractivityBinding
import com.curiousdaya.bmsanalyser.ui.home.HomeActivity
import com.curiousdaya.bmsanalyser.util.moveActivity
import com.curiousdaya.bmsanalyser.util.moveActivityData
import com.curiousdaya.bmsanalyser.util.showToast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException


class QRActivity : AppCompatActivity() {
    private val requestCodeCameraPermission = 1001
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private lateinit var binding: ActivityQractivityBinding
    val REQUEST_ENABLE_BT = 101
    var deviceName  = ""
    var isParedWithDevice = false
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val foundDevices = StringBuilder()
    companion object { private const val REQUEST_CODE_NEARBY_DEVICES_PERMISSION = 101 }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setupControls()
    }

        fun setupControls()
    {
        barcodeDetector = BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true) //you should add this feature
            .build()

        binding.cameraSurfaceView.getHolder().addCallback(object : SurfaceHolder.Callback {
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    //Start preview after 1s delay
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            @SuppressLint("MissingPermission")
            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                try {
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(applicationContext, "Scanner has been closed", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue


                    //Don't forget to add this line printing value or finishing activity must run on main thread
                    runOnUiThread {
                        Handler().postDelayed({
                            cameraSource.stop()
                        },5000)
                        val deviceAddressSpeaker = scannedValue
                        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(deviceAddressSpeaker)
                        device?.let {
                            if (it.bondState != BluetoothDevice.BOND_BONDED) {
                                //createBondWithDevice(it)
                            } else {
                                runOnUiThread {
                                    showToast("Bluetooth Already Paired")

                                }
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@QRActivity, "value- else", Toast.LENGTH_SHORT).show()

                    }

                }
            }
        })
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            this@QRActivity,
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty())
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupControls()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (requestCode == REQUEST_CODE_NEARBY_DEVICES_PERMISSION)
        {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, proceed with discovering nearby devices
            } else {
                // Permissions denied, handle the error
            }
        }
    }


}