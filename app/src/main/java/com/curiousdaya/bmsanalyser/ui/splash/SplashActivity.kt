package com.curiousdaya.bmsanalyser.ui.splash

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.curiousdaya.bmsanalyser.R
import com.curiousdaya.bmsanalyser.databinding.ActivitySplashBinding
import com.curiousdaya.bmsanalyser.ui.home.HomeActivity
import com.curiousdaya.bmsanalyser.util.delayMove
import com.curiousdaya.bmsanalyser.util.fullScreen
import com.curiousdaya.bmsanalyser.util.moveActivity

class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fullScreen()
        delayMove()
        setAnim()
    }

     fun setAnim()
     {
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.bms.startAnimation(fadeInAnimation) }
}