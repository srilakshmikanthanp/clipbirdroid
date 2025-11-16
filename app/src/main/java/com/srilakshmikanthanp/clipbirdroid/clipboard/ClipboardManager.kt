package com.srilakshmikanthanp.clipbirdroid.clipboard

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardManager @Inject constructor(
  private val clipboard: Clipboard,
  coroutineScope: CoroutineScope
) {
  private val scope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())

  private val _clipboardChangeEvents = MutableSharedFlow<List<ClipboardContent>>()
  val clipboardChangeEvents = _clipboardChangeEvents.asSharedFlow()

  private fun onClipboardChange(content: MutableList<ClipboardContent>) {
    this.scope.launch { this@ClipboardManager._clipboardChangeEvents.emit(content) }
  }

  init {
    this.clipboard.addClipboardChangeListener(::onClipboardChange)
  }

  fun getClipboard(): Clipboard {
    return clipboard
  }
}
