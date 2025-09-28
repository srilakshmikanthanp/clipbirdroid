package com.srilakshmikanthanp.clipbirdroid

import android.app.Application
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardController
import com.srilakshmikanthanp.clipbirdroid.history.HistoryController
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.LanController
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.WanController
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class Clipbird : Application() {
  @Inject lateinit var clipboardController: ClipboardController
  @Inject lateinit var historyController: HistoryController
  @Inject lateinit var lanController: LanController
  @Inject lateinit var wanController: WanController
  @Inject lateinit var storage: Storage

  override fun onCreate() {
    super.onCreate()
    val moduleInstall = ModuleInstall.getClient(this)
    val request = ModuleInstallRequest.newBuilder()
      .addApi(GmsBarcodeScanning.getClient(this))
      .build()
    moduleInstall.installModules(request)
  }
}
