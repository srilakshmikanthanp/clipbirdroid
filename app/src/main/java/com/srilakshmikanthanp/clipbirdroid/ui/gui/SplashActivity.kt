package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
      val splashScreen: SplashScreen = installSplashScreen()
      splashScreen.setKeepOnScreenCondition { true }
    }
  }

  // on Start
  override fun onStart() {
    super.onStart().also { ClipbirdService.start(this) }

    Intent(
      this, MainActivity::class.java
    ).also {
      startActivity(it)
    }.also {
      finish()
    }
  }
}
