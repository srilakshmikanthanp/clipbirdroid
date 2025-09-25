package com.srilakshmikanthanp.clipbirdroid

import android.app.Application
import android.content.Context
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.srilakshmikanthanp.clipbirdroid.common.functions.generateX509Certificate
import com.srilakshmikanthanp.clipbirdroid.constants.appCertExpiryInterval
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardController
import com.srilakshmikanthanp.clipbirdroid.history.HistoryController
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.LanController
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.WanController
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.security.PrivateKey
import java.security.cert.X509Certificate
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
