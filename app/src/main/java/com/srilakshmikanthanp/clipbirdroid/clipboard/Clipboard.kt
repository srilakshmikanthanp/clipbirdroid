package com.srilakshmikanthanp.clipbirdroid.clipboard

import android.content.ClipboardManager
import com.srilakshmikanthanp.clipbirdroid.interfaces.ClipboardChangeListener

class Clipboard(private var clipboard: ClipboardManager) {
  /// List of ClipboardChangeListener
  private var listeners: MutableList<ClipboardChangeListener> = mutableListOf()

  /// MIME Types
  private val MIME_TYPE_TEXT: String = "text/plain"
  private val MIME_TYPE_URL: String = "text/uri-list"
  private val MIME_TYPE_PNG: String = "image/png"
  private val MIME_TYPE_COLOR: String = "application/x-color"
  private val MIME_TYPE_HTML: String = "text/html"

  /**
   * ClipBoard Change Implementation
   */
  private fun onClipboardChanged() {
    // List of Clipboard Content to Notify the Listeners
    val content: MutableList<Pair<String, String>> = mutableListOf()

    // get the current clipboard content
    val clip = clipboard.primaryClip ?: return

    // get the current clipboard content count
    val count = clip.itemCount

    // iterate through the clipboard content
    for (i in 0 until count) {

    }
  }

  /**
   * Initialize the Clipboard
   */
  init {
    clipboard.addPrimaryClipChangedListener(this::onClipboardChanged)
  }

  /**
   * Add ClipboardChangeListener
   */
  fun addListener(listener: ClipboardChangeListener) {
    listeners.add(listener)
  }

  /**
   * Remove ClipboardChangeListener
   */
  fun removeListener(listener: ClipboardChangeListener) {
    listeners.remove(listener)
  }

  /**
   * Set the clipboard content
   */
  fun setClipboardContent(content: MutableList<Pair<String, ByteArray>>) {
  }

  /**
   * Clear the clipboard content
   */
  fun clearClipboardContent() {
    this.clipboard.clearPrimaryClip()
  }

  /**
   * Get the current clipboard content
   */
  fun getClipboardContent(): MutableList<Pair<String, ByteArray>> {
  }
}
