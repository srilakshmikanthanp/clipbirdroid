package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardContent
import com.srilakshmikanthanp.clipbirdroid.common.utility.toPNG
import com.srilakshmikanthanp.clipbirdroid.syncing.manager.SyncingManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import javax.inject.Inject

@AndroidEntryPoint
class ShareHandler : ComponentActivity() {
  @Inject lateinit var syncingManager: SyncingManager

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
        val content = it?.readBytes()?.toPNG() ?: return@use null
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

    val result = try {
      when (intent.action) {
        Intent.ACTION_PROCESS_TEXT -> getProcessTextData(intent)
        Intent.ACTION_SEND-> getSendActionData(intent)
        else -> throw RuntimeException("Unsupported intent action: ${intent.action}")
      }
    } catch (e: Exception) {
      Log.e("ShareHandler", "Error processing shared data", e)
      Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
      this.finish()
      return
    }

    val content = listOf(ClipboardContent(result.first, result.second))
    lifecycleScope.launch { syncingManager.synchronize(content) }

    Toast.makeText(this, R.string.synced, Toast.LENGTH_SHORT).show()
    this.finish()
  }
}
