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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.curiousdaya.bmsanalyser.databinding.ActivityQractivityBinding
import com.curiousdaya.bmsanalyser.databinding.PermissionDialogLayoutBinding
import com.curiousdaya.bmsanalyser.util.showToast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException


class QRActivity : AppCompatActivity() {
    private lateinit var cameraSource: CameraSource
    private lateinit var barcodeDetector: BarcodeDetector
    private var scannedValue = ""
    private lateinit var binding: ActivityQractivityBinding
    var deviceName = ""
    var isParedWithDevice = false
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val foundDevices = StringBuilder()
    var REQUEST_ALL_PERMISSION = 101
    var REQUEST_BLUETOOTH = 102

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)
    }
    override fun onPause() {
        super.onPause()
        // Unregister the BroadcastReceiver when the activity is not visible
        unregisterReceiver(bluetoothStateReceiver)
    }

    // Define the BroadcastReceiver as an inner class
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action: String? = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state: Int = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        if (checkPermissions())
                        {
                            enableBlueTooth()
                            showToast("It is nessary to turn on bluetooth, Please enable it.")
                        }
                        else
                        {
                            requestPermissionMySelf()
                        }
                    }}}}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(512, 512)
        requestPermissionMySelf()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (checkPermissions())
        {
           enableBlueTooth()
        }
        else
        {
            requestPermissionMySelf()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ALL_PERMISSION) {
            requestPermissionMySelf()
        }
        if (requestCode == REQUEST_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
            } else {
                // User denied to enable Bluetooth or error occurred
                showToast("It is nessary to turn on bluetooth, Please enable it.")
                if (checkPermissions())
                 enableBlueTooth()
            }
        }
    }


    fun enableBlueTooth() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_BLUETOOTH)
            }
        }

    }

    fun showSettingsDialog(context: Context) {
        val binding = PermissionDialogLayoutBinding.inflate(LayoutInflater.from(context))
        val mBuilder = AlertDialog.Builder(context).setView(binding.root).create()
        mBuilder.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mBuilder.setCancelable(false)
        mBuilder.show()
        binding.btnCancel.setOnClickListener {
            mBuilder.dismiss()
            finish()
        }
        binding.goToSettings.setOnClickListener {
            mBuilder.dismiss()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivityForResult(intent, REQUEST_ALL_PERMISSION)

        }


    }


    fun checkPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED)
        }
        return false
    }

    fun requestPermissionMySelf() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_CONNECT
            ), REQUEST_ALL_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ALL_PERMISSION) {
            var allPermissionsGranted = true
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                }
            }

            if (allPermissionsGranted)
            {
                if (checkPermissions())
                { enableBlueTooth()}
            }
            else
            {
                showSettingsDialog(this)
            }
        }
    }

    fun setupControls() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        cameraSource =
            CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(1920, 1080)
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
}