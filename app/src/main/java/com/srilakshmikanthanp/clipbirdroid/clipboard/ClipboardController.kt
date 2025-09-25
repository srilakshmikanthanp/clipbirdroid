package com.srilakshmikanthanp.clipbirdroid.clipboard

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.controller.Controller
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardController @Inject constructor(
  @param:ApplicationContext private val context: Context,
  coroutineScope: CoroutineScope
): Controller {
  private val scope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())
  private val clipboard: Clipboard = Clipboard(context)

  private val _clipboardChangeEvents = MutableSharedFlow<List<Pair<String, ByteArray>>>()
  val clipboardChangeEvents = _clipboardChangeEvents.asSharedFlow()

  private fun onClipboardChange(content: MutableList<Pair<String, ByteArray>>) {
    this.scope.launch {
      this@ClipboardController._clipboardChangeEvents.emit(content)
    }
  }

  init {
    this.clipboard.addClipboardChangeListener(::onClipboardChange)
  }

  fun getClipboard(): Clipboard {
    return clipboard
  }
}
