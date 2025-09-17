package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

import android.content.Intent
import android.net.Uri
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
  @OptIn(DelicateCoroutinesApi::class)
  override fun onWindowFocusChanged(hasFocus: Boolean) {
    if (!hasFocus) return

    val lanController = (this.application as Clipbird).lanController
    val wanController = (this.application as Clipbird).wanController

    if (intent.type?.startsWith("image/") != true) return

    val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri ?: return

    val result = try {
      this.contentResolver.openInputStream(uri)
    } catch (e: FileNotFoundException) {
      Log.e("ShareHandler", "File not found Exception", e)
      return
    } catch (e: SecurityException) {
      Log.e("ShareHandler", "Security Exception", e)
      return
    }.use {
      val content = it?.let { toPNG(it.readBytes()) } ?: return@use null
      val mimeType = "image/png"
      return@use Pair(mimeType, content)
    } ?: return

    val content = listOf(Pair(result.first, result.second))
    lanController.synchronize(content)
    wanController.synchronize(content)

    runOnUiThread {
      Toast.makeText(this, R.string.synced, Toast.LENGTH_SHORT).show()
      this.finish()
    }
  }

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onStart() {
    super.onStart()

    setContent {
      Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }
}
