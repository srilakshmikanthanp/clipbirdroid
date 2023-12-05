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
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.ui.gui.Clipbird
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SendHandler : ComponentActivity() {
  @OptIn(DelicateCoroutinesApi::class)
  override fun onWindowFocusChanged(hasFocus: Boolean) {
    // if not focused, return
    if (!hasFocus) return

    // launch the coroutine
    GlobalScope.launch {
      process()
    }

    // show toast
    runOnUiThread {
      Toast.makeText(this, R.string.synced, Toast.LENGTH_SHORT).show()
      this.finish()
    }
  }

  private fun process() {
    val controller = (this.application as Clipbird).getController()
    controller.syncClipboard(controller.getClipboard())
  }

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onStart() {
    // Call the super method
    super.onStart()

    // show loading
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
