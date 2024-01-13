package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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

    // Permissions defined on Manifest
    val permissions = mutableListOf(
      Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
      Manifest.permission.RECEIVE_BOOT_COMPLETED,
      Manifest.permission.INTERNET,
      Manifest.permission.FOREGROUND_SERVICE,
    )

    // request code
    val requestCode = 0

    // if api >= 34
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      permissions.add(Manifest.permission.FOREGROUND_SERVICE_DATA_SYNC)
    }

    // if api >= 33
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      permissions.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    // check self permissions
    permissions.removeIf {
      checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
    }

    // if not empty
    if (permissions.isNotEmpty()) {
      requestPermissions(permissions.toTypedArray(), requestCode)
    }

    // Request Ignore Battery Optimization
    startActivity(
      Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:$packageName")
      )
    )
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
