package com.srilakshmikanthanp.clipbirdroid

import android.app.Application
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Clipbird : Application() {
  override fun onCreate() {
    super.onCreate()
    val moduleInstall = ModuleInstall.getClient(this)
    val request = ModuleInstallRequest.newBuilder()
      .addApi(GmsBarcodeScanning.getClient(this))
      .build()
    moduleInstall.installModules(request)
  }
}
