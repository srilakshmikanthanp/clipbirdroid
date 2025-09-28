package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.srilakshmikanthanp.clipbirdroid.Clipbird
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.common.functions.toPNG
import kotlinx.coroutines.DelicateCoroutinesApi
import java.io.FileNotFoundException

class ShareHandler : ComponentActivity() {
  private fun getSendActionData(intent: Intent): Pair<String, ByteArray> {
    return if (intent.type?.startsWith("image/") == true) {
      val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri ?: throw RuntimeException("No image data found")
      try {
        this.contentResolver.openInputStream(uri)
      } catch (e: FileNotFoundException) {
        throw RuntimeException("File not found", e)
      } catch (e: SecurityException) {
        throw RuntimeException("Permission denied to read the file", e)
      }.use {
        val content = it?.let { toPNG(it.readBytes()) } ?: return@use null
        val mimeType = "image/png"
        return@use Pair(mimeType, content)
      } ?: throw RuntimeException("Failed to read image data")
    } else if (intent.type?.startsWith("text/") == true) {
      val content = intent.getStringExtra(Intent.EXTRA_TEXT) ?: throw RuntimeException("No text data found")
      val mimeType = "text/plain"
      Pair(mimeType, content.toByteArray())
    } else {
      throw RuntimeException("Unsupported data type")
    }
  }

  private fun getProcessTextData(intent: Intent): Pair<String, ByteArray> {
    return if (intent.type?.startsWith("text/") == true) {
      val content = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: throw RuntimeException("No text data found")
      val mimeType = "text/plain"
      Pair(mimeType, content.toByteArray())
    } else {
      throw RuntimeException("Unsupported data type")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val lanController = (this.application as Clipbird).lanController
    val wanController = (this.application as Clipbird).wanController

    val result = try {
      if (intent.action == Intent.ACTION_SEND) {
        getSendActionData(intent)
      } else if (intent.action == Intent.ACTION_PROCESS_TEXT) {
        getProcessTextData(intent)
      } else {
        throw RuntimeException("Unsupported intent action: ${intent.action}")
      }
    } catch (e: Exception) {
      Log.e("ShareHandler", "Error processing shared data", e)
      Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
      this.finish()
      return
    }

    val content = listOf(Pair(result.first, result.second))
    lanController.synchronize(content)
    wanController.synchronize(content)

    Toast.makeText(this, R.string.synced, Toast.LENGTH_SHORT).show()
    this.finish()
  }
}
