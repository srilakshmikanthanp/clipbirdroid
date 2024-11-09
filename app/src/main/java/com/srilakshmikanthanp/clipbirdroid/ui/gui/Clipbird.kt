package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.app.Application
import android.util.Log
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.srilakshmikanthanp.clipbirdroid.constant.appCertExpiryInterval
import com.srilakshmikanthanp.clipbirdroid.constant.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.utilities.functions.generateX509Certificate
import com.srilakshmikanthanp.clipbirdroid.utilities.functions.toPem
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.security.PrivateKey
import java.security.cert.X509Certificate


class Clipbird : Application() {
  // Function used to get the Private Key and the Certificate New
  private fun getNewSslConfig(): Pair<PrivateKey, X509Certificate> {
    val sslConfig = generateX509Certificate(this)
    val store = Storage.getInstance(this)
    store.setHostKey(sslConfig.first)
    store.setHostCert(sslConfig.second)
    return sslConfig
  }

  // Function used to get the Private Key and the Certificate Old
  private fun getOldSslConfig(): Pair<PrivateKey, X509Certificate> {
    // Get the Certificate Details
    val store = Storage.getInstance(this)
    val key = store.getHostKey()!!
    val cert = store.getHostCert()!!

    // Get the Required parameters
    val x500Name = JcaX509CertificateHolder(cert).subject
    val cn = x500Name.getRDNs(BCStyle.CN)[0]
    val name = IETFUtils.valueToString(cn.first.value)

    // device name
    val deviceName = appMdnsServiceName(this)

    // check the name is same
    if (name != deviceName) {
      return getNewSslConfig()
    }

    // is certificate is to expiry in two months
    if (cert.notAfter.time - System.currentTimeMillis() < appCertExpiryInterval()) {
      val sslConfig = generateX509Certificate(this)
      store.setHostKey(sslConfig.first)
      store.setHostCert(sslConfig.second)
      return sslConfig
    }

    // done return
    return Pair(key, cert)
  }

  // Function used to get the the Private Key and the Certificate
  private fun getSslConfig(): Pair<PrivateKey, X509Certificate> {
    // Get the Storage instance for the application
    val store = Storage.getInstance(this)

    // Check the Host key and cert is available
    val config = if (store.hasHostKey() && store.hasHostCert()) {
      getOldSslConfig()
    } else {
      getNewSslConfig()
    }

    // log the certificate and key
    Log.i("SSL", "Certificate: ${config.second.toPem()}")
    Log.i("SSL", "Key: ${config.first.toPem()}")

    // return the config
    return config
  }

  // Controller foe the Whole Clipbird Designed by GRASP Pattern
  private lateinit var controller: AppController

  // Initialize the controller instance
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

  // Initialize the controller instance
  fun initialize() {
    if (!this::controller.isInitialized) {
      // create the controller instance
      controller = AppController(getSslConfig(), this)

      // initialize the controller
      if (controller.isLastlyHostIsServer()) {
        controller.setCurrentHostAsServer()
      } else {
        controller.setCurrentHostAsClient()
      }
    }
  }

  // get the controller instance
  fun getController(): AppController = controller
}
