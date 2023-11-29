package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.PowerManager
import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.constant.appCertExpiryInterval
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.store.Storage
import com.srilakshmikanthanp.clipbirdroid.utility.functions.generateX509Certificate
import com.srilakshmikanthanp.clipbirdroid.utility.functions.toPem
import java.security.PrivateKey
import java.security.cert.X509Certificate


class Clipbird : Application() {
  // broad cast signal for doze mode off/on event form the Android system
  private val dozeReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED) {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        controller.setIsPongEnabled(!powerManager.isDeviceIdleMode)
      }
    }
  }

  // broad cast receiver for screen off/on event form the Android system
  private val screenReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == Intent.ACTION_SCREEN_OFF) {
        controller.setIsPongEnabled(false)
      } else if (intent?.action == Intent.ACTION_SCREEN_ON) {
        controller.setIsPongEnabled(true)
      }
    }
  }

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
    val store = Storage.getInstance(this)
    val cert = store.getHostCert()!!
    val key = store.getHostKey()!!

    // is certificate is to expiry in two months
    if (cert.notAfter.time - System.currentTimeMillis() < appCertExpiryInterval()) {
      val sslConfig = generateX509Certificate(this)
      store.setHostKey(sslConfig.first)
      store.setHostCert(sslConfig.second)
      return sslConfig
    }

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
    super.onCreate().also { controller = AppController(getSslConfig(), this) }

    // initialize the controller
    if (controller.isLastlyHostIsServer()) {
      controller.setCurrentHostAsServer()
    } else {
      controller.setCurrentHostAsClient()
    }

    // register the screen receiver
    val intentFilter: IntentFilter = IntentFilter()
    intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
    registerReceiver(screenReceiver, intentFilter)

    // register the doze receiver
    val dozeFilter: IntentFilter = IntentFilter()
    dozeFilter.addAction(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED)
    registerReceiver(dozeReceiver, dozeFilter)
  }

  // get the controller instance
  fun getController(): AppController = controller
}
