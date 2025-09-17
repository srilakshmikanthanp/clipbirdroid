package com.srilakshmikanthanp.clipbirdroid.ui.gui.handlers

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
import kotlinx.coroutines.DelicateCoroutinesApi

class SendHandler : ComponentActivity() {
  @OptIn(DelicateCoroutinesApi::class)
  override fun onWindowFocusChanged(hasFocus: Boolean) {
    if (!hasFocus) return

    val clipboardController = (this.application as Clipbird).clipboardController
    val lanController = (this.application as Clipbird).lanController
    val wanController = (this.application as Clipbird).wanController
    val content = clipboardController.getClipboard().getClipboardContent()
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
