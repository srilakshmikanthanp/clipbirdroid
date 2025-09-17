package com.srilakshmikanthanp.clipbirdroid

import android.app.Application
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Clipbird : Application() {
  override fun onCreate() {
    // call super onCreate and initialize controller
    super.onCreate()

    // get the module install instance
    val moduleInstall = ModuleInstall.getClient(this)

    // create install request
    val request = ModuleInstallRequest.newBuilder()
      .addApi(GmsBarcodeScanning.getClient(this))
      .build()

    // install the module
    moduleInstall.installModules(request)
  }
}
