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
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.curiousdaya.bmsanalyser.databinding.ActivityQractivityBinding
import com.curiousdaya.bmsanalyser.databinding.PermissionDialogLayoutBinding
import com.curiousdaya.bmsanalyser.ui.home.HomeActivity
import com.curiousdaya.bmsanalyser.util.Prefs
import com.curiousdaya.bmsanalyser.util.moveActivity
import com.curiousdaya.bmsanalyser.util.showToast
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException


class QRActivity : AppCompatActivity() {
    //01:B6:EC:DB:45:B9 SPEAKER
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
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(bluetoothStateReceiver, filter)
        requestPermissionMySelf()
        if (checkPermissions()) {
            if (bluetoothAdapter.isEnabled)
            {
                setupQrScanner()
            } else
            {
                enableBlueTooth()
            }
        } else {
            requestPermissionMySelf()
        }
        initControl()
    }



    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action: String? = intent.action

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state: Int = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        if (checkPermissions()) {
                            enableBlueTooth()
                            showToast("It is nessary to turn on bluetooth, Please enable it.")
                        } else {
                            requestPermissionMySelf()
                        }
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQractivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(512, 512)


    }



    private fun initControl() {
        binding.mUnpairDevices.setOnClickListener {
            unpairAllDevices()
        }
    }

    fun unpairAllDevices() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
        {
            val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
            val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            pairedDevices?.forEach { device -> unpairDevice(device) }
        }

    }

    private fun unpairDevice(device: BluetoothDevice) {
        try {
            val method = device.javaClass.getMethod("removeBond")
            method.invoke(device)
        } catch (exception: Exception) { Log.e("UnpairDevice", "Could not unpair device: ", exception) }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraSource.isInitialized) {
            cameraSource.stop()
        }
        if (::bluetoothAdapter.isInitialized) {
            unregisterReceiver(bluetoothStateReceiver)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ALL_PERMISSION) {
            requestPermissionMySelf()
        }
        if (requestCode == REQUEST_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
            } else
            {
                showToast("It is nessary to turn on bluetooth, Please enable it.")
                if (checkPermissions())
                    enableBlueTooth()
            }
        }
    }


    fun enableBlueTooth() {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled)
            {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) { return }
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

            if (allPermissionsGranted) {
                if (checkPermissions()) {
                    enableBlueTooth()
                }
            } else {
                showSettingsDialog(this)
            }
        }
    }

    fun setupQrScanner() {
        barcodeDetector =
            BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.ALL_FORMATS).build()
        cameraSource =
            CameraSource.Builder(this, barcodeDetector).setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build()

        binding.cameraSurfaceView.getHolder().addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (checkPermissions()) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                this@QRActivity,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        cameraSource.start(holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                try {
                    if (ActivityCompat.checkSelfPermission(
                            this@QRActivity,
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    cameraSource.start(holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
              //  cameraSource.stop()
            }
        })


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() == 1) {
                    scannedValue = barcodes.valueAt(0).rawValue
                    runOnUiThread {
                        Handler().postDelayed({
                           // cameraSource.stop()
                        }, 4000)
                        val deviceAddressSpeaker = scannedValue
                        val device: BluetoothDevice? =
                            bluetoothAdapter.getRemoteDevice(deviceAddressSpeaker)
                        device?.let {
                            if (ActivityCompat.checkSelfPermission(
                                    this@QRActivity,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                if (it.bondState != BluetoothDevice.BOND_BONDED) {
                                     createBondWithDevice(it)
                                }
                                else
                                {
                                   // runOnUiThread { showToast("Bluetooth Already Paired") }
                                    Log.d("TAG", "receiveDetections: Bluetooth Already Paired")
                                }
                            } else {
                                requestPermissionMySelf()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun createBondWithDevice(device: BluetoothDevice) {
        if (device.bondState == BluetoothDevice.BOND_NONE)
        {
            val bondStateReceiver = object : BroadcastReceiver()
            {
                override fun onReceive(context: Context?, intent: Intent)
                {
                    val action = intent.action
                    if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action)
                    {
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                        val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)

                        when (bondState) {
                            BluetoothDevice.BOND_BONDED ->
                                {
                                    if (ActivityCompat.checkSelfPermission(this@QRActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
                                    {
                                        val deviceName = device?.name.toString()
                                        val deviceAddress = device?.address
                                        runOnUiThread {
                                            showToast("Paired successfully with ${device?.name}")
                                        }
                                        unregisterReceiver(this)
                                        Prefs.getInstance(this@QRActivity).bluetoothDeviceName = deviceName
                                        Prefs.getInstance(this@QRActivity).bluetoothDeviceAddress = deviceAddress.toString()
                                        moveActivity(HomeActivity())
                                        finishAffinity()

                                    }


                            }
                            BluetoothDevice.BOND_NONE -> {
                                if (previousBondState == BluetoothDevice.BOND_BONDING) {
                                    runOnUiThread {
                                      //  showToast("Pairing with ${device?.name} was failed")

                                    }
                                }
                                unregisterReceiver(this) // Clean up receiver once done
                            }
                        }
                    }
                }
            }
            val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            registerReceiver(bondStateReceiver, filter)
            if (ActivityCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED )
            { device.createBond() }
        }
    }



}
