package com.curiousdaya.bmsanalyser.util

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.curiousdaya.bmsanalyser.ui.home.HomeActivity
import com.curiousdaya.bmsanalyser.ui.qrScanner.QRActivity

fun Activity.moveActivity(activity: Activity){
   val intent = Intent(this, activity::class.java)
   startActivity(intent)
   overridePendingTransition(
      androidx.appcompat.R.anim.abc_fade_in,
      androidx.appcompat.R.anim.abc_fade_out
   )
}
fun Activity.moveActivityData(msg:String,activity: Activity){
   val intent = Intent(this, activity::class.java)
   intent.putExtra("key",msg)
   startActivity(intent)
   overridePendingTransition(
      androidx.appcompat.R.anim.abc_fade_in,
      androidx.appcompat.R.anim.abc_fade_out
   )
}


fun Activity.showToast(msg:String){
 Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}






fun Activity.fullScreen(){
   window.setFlags(512,512)

}