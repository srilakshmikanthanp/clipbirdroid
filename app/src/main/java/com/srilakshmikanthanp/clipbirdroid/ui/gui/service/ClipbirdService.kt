package com.srilakshmikanthanp.clipbirdroid.ui.gui.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.srilakshmikanthanp.clipbirdroid.controller.AppController
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import com.srilakshmikanthanp.clipbirdroid.ui.gui.MainActivity
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.QuitHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers.SendHandler
import com.srilakshmikanthanp.clipbirdroid.ui.gui.notifications.StatusNotification
import com.srilakshmikanthanp.clipbirdroid.utility.functions.generateX509Certificate

/**
 * Service for the application
 */
class ClipbirdService : Service() {
  // Create the Status Notification instance for the service instance
  private val notify = StatusNotification(this, onTapIntent(), onSendIntent(), onQuitIntent())

  // Controller foe the Whole Application Designed by GRASP Pattern
  private lateinit var controller: AppController

  // Binder instance
  private val binder = ServiceBinder()

  // Binder for the service that returns the service instance
  inner class ServiceBinder : Binder() {
    fun getService(): ClipbirdService = this@ClipbirdService
  }

  // Function used to get the Pending intent for onTap
  private fun onTapIntent(): PendingIntent {
    Intent(this, MainActivity::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  // Function used to get the Pending intent for onSend
  private fun onSendIntent(): PendingIntent {
    Intent(this, SendHandler::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  // Function used to get the Pending intent for onQuit
  private fun onQuitIntent(): PendingIntent {
    Intent(this, QuitHandler::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  // Function used to get the Pending intent for onAccept
  private fun onAcceptIntent(): PendingIntent {
    Intent(this, QuitHandler::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  // Function used to get the Pending intent for onReject
  private fun onRejectIntent(): PendingIntent {
    Intent(this, QuitHandler::class.java).also {
      return PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
    }
  }

  // Called when an client wants to join the group
  private fun onJoinRequest(device: Device) {
    notify.showJoinRequest(device.name, onAcceptIntent(), onRejectIntent())
  }

  // Initialize the controller instance
  override fun onCreate() {
    super.onCreate().also {
      controller = AppController(generateX509Certificate(this), this)
    }
  }

  // Return the binder instance
  override fun onBind(p0: Intent?): IBinder {
    return binder
  }

  // On start command of the service
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // Add the Sync Request Handler
    controller.addSyncRequestHandler(controller::setClipboard)

    // Add the AuthRequest Handler
    controller.addAuthRequestHandler(this::onJoinRequest)

    // initialize the controller
    if (controller.isLastlyHostIsServer()) {
      controller.setCurrentHostAsServer()
    } else {
      controller.setCurrentHostAsClient()
    }

    // Return code for the service
    return START_REDELIVER_INTENT
  }

  // Get the Controller instance of the service
  fun getController(): AppController = controller
}
