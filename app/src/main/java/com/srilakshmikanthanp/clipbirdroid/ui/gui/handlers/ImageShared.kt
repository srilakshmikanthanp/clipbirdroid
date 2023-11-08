package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import com.srilakshmikanthanp.clipbirdroid.ui.gui.service.ClipbirdService
import com.srilakshmikanthanp.clipbirdroid.utility.functions.toPNG
import java.io.FileNotFoundException

class ImageShared : AbstractHandler() {
  // Called when the connection is ready to service
  override fun onConnectionReady(binder: ClipbirdService.ServiceBinder) {
    // Allowed Types image/png, image/jpeg, image/gif
    val allowedTypes = arrayOf("image/png", "image/jpeg", "image/gif")

    // Get the controller
    val controller = binder.getService().getController()

    // if the Type in image png
    if (!allowedTypes.contains(intent.type)) {
      return
    }

    // Get the URI
    val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri ?: return

    // get the content
    val result = try {
      this.contentResolver.openInputStream(uri)
    } catch (e: FileNotFoundException) {
      Log.e("ImageShared", "File not found Exception", e)
      return
    } catch (e: SecurityException) {
      Log.e("ImageShared", "Security Exception", e)
      return
    }.use {
      val content = it?.let { toPNG(it.readBytes()) } ?: return@use null
      val mimeType = "image/png"
      return@use Pair(mimeType, content)
    } ?: return

    // Sync the Clipboard
    controller.syncClipboard(listOf(Pair(result.first, result.second)))

    // Show toast
    val toast = Toast(this)
    toast.duration = Toast.LENGTH_SHORT
    toast.setText("Image Synced")
    toast.show()

    // Close the activity
    this.finish()
  }
}
