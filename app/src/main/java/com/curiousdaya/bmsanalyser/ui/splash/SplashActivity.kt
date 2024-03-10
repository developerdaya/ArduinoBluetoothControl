package com.curiousdaya.bmsanalyser.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.curiousdaya.bmsanalyser.R
import com.curiousdaya.bmsanalyser.databinding.ActivitySplashBinding
import com.curiousdaya.bmsanalyser.ui.home.HomeActivity
import com.curiousdaya.bmsanalyser.ui.qrScanner.QRActivity
import com.curiousdaya.bmsanalyser.util.Prefs
import com.curiousdaya.bmsanalyser.util.fullScreen

class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()
        setAnim()
        delayMove()
    }
    fun delayMove()
    {
        Handler().postDelayed({
            if (Prefs.getInstance(this).bluetoothDeviceAddress!="")
            {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            else
            {
                val intent = Intent(this, QRActivity::class.java)
                startActivity(intent)
                finish()
            }
            overridePendingTransition(
                androidx.appcompat.R.anim.abc_fade_in,
                androidx.appcompat.R.anim.abc_fade_out)
        }, 1500)

    }

     fun setAnim()
     {
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.bms.startAnimation(fadeInAnimation) }
}