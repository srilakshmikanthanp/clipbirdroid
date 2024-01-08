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
  // companion object
  companion object {
    val QUIT_ACTION = "com.srilakshmikanthanp.clipbirdroid.ui.gui.SplashActivity.QUIT_ACTION"
  }

  // handle Intent
  private fun handleIntent(intent: Intent?) {
    if (intent?.action == QUIT_ACTION) stopService().also { this.finishAndRemoveTask() }
  }

  // Set up the Service Connection
  private fun setUpService() {
    Intent(this, ClipbirdService::class.java).also {
      startForegroundService(it)
    }
  }

  // dispose the service
  private fun stopService() {
    stopService(Intent(this, ClipbirdService::class.java))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
      val splashScreen: SplashScreen = installSplashScreen()
      splashScreen.setKeepOnScreenCondition { true }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent).also { handleIntent(intent) }
  }

  // on Start
  override fun onStart() {
    super.onStart().also { handleIntent(intent) }

    ClipbirdService.start(this)

    Intent(
      this@SplashActivity,
      MainActivity::class.java
    ).also {
      startActivity(it)
    }.also {
      finish()
    }
  }
}
