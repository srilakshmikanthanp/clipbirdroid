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
import com.srilakshmikanthanp.clipbirdroid.storage.PreferenceStorage
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.inject.Inject

@HiltAndroidApp
class Clipbird : Application() {
  private val applicationScope = MainScope()

  val clipboardController = ClipboardController(this, applicationScope)
  val historyController = HistoryController()
  val lanController = LanController(getSslConfig(this), this, applicationScope)
  val wanController = WanController(applicationScope)

  @Inject lateinit var storage: Storage

  private fun getNewSslConfig(context: Context): Pair<PrivateKey, X509Certificate> {
    val sslConfig = generateX509Certificate(context)
    storage.setHostKey(sslConfig.first)
    storage.setHostCert(sslConfig.second)
    return sslConfig
  }

  private fun getOldSslConfig(context: Context): Pair<PrivateKey, X509Certificate> {
    val key = storage.getHostKey()!!
    val cert = storage.getHostCert()!!
    val x500Name = JcaX509CertificateHolder(cert).subject
    val cn = x500Name.getRDNs(BCStyle.CN)[0]
    val name = IETFUtils.valueToString(cn.first.value)
    val deviceName = appMdnsServiceName(context)
    if (name != deviceName) return getNewSslConfig(context)

    if (cert.notAfter.time - System.currentTimeMillis() < appCertExpiryInterval()) {
      val sslConfig = generateX509Certificate(context)
      storage.setHostKey(sslConfig.first)
      storage.setHostCert(sslConfig.second)
      return sslConfig
    }

    return Pair(key, cert)
  }

  fun getSslConfig(context: Context): Pair<PrivateKey, X509Certificate> {
    return if (storage.hasHostKey() && storage.hasHostCert()) {
      getOldSslConfig(context)
    } else {
      getNewSslConfig(context)
    }
  }

  override fun onCreate() {
    super.onCreate()
    val moduleInstall = ModuleInstall.getClient(this)
    val request = ModuleInstallRequest.newBuilder()
      .addApi(GmsBarcodeScanning.getClient(this))
      .build()
    moduleInstall.installModules(request)
  }
}
