package com.srilakshmikanthanp.clipbirdroid.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context

/**
 * Clipboard Change Listener Interface
 */
fun interface ClipboardChangeListener {
  fun onClipboardChange(content: MutableList<Pair<String, ByteArray>>)
}

/**
 * Class For Managing the Clipboard
 */
class Clipboard(private val context: Context) {
  /// clipboard Manager to manage the clipboard
  private val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

  /// List of ClipboardChangeListener
  private val listeners: MutableList<ClipboardChangeListener> = mutableListOf()

  /// MIME Types
  private val MIME_TYPE_TEXT: String = "text/plain"
  private val MIME_TYPE_PNG: String = "image/png"
  private val MIME_TYPE_COLOR: String = "application/x-color"
  private val MIME_TYPE_HTML: String = "text/html"

  /**
   * ClipBoard Change Implementation
   */
  private fun onClipboardChanged() {
    val contents = this.getClipboardContent()
    for (listener in listeners) {
      listener.onClipboardChange(contents)
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
   * @brief Set the clipboard content with the given contents
   * @param contents List of Pair<String, ByteArray>
   * first -> MIME Type, second -> Raw Data
   */
  fun setClipboardContent(contents: MutableList<Pair<String, ByteArray>>) {
    // create the clip data & description
    val description = ClipDescription(null, Array(contents.size) { contents[it].first });
    val clipData = ClipData(description, null);

    // loop through the contents
    for (content in contents) {
      val (mimeType, rawData) = Pair(content.first, content.second)

      if (mimeType == this.MIME_TYPE_TEXT) {
        clipData.addItem(ClipData.Item(rawData.toString()))
      }

      if (mimeType == this.MIME_TYPE_COLOR) {
        clipData.addItem(ClipData.Item(rawData.toString()))
      }

      if (mimeType == this.MIME_TYPE_HTML) {
        clipData.addItem(ClipData.Item(rawData.toString()))
      }

      if (mimeType == this.MIME_TYPE_PNG) {
        // TODO: Implement
      }
    }

    // set the clip data
    this.clipboard.setPrimaryClip(clipData)
  }

  /**
   * Clear the clipboard content
   */
  fun clearClipboardContent() {
    this.clipboard.clearPrimaryClip()
  }

  /**
   * @brief Get the current clipboard content as a list of Pair<String, ByteArray>
   * first -> MIME Type, second -> Raw Data
   * @return MutableList<Pair<String, ByteArray>>
   */
  fun getClipboardContent(): MutableList<Pair<String, ByteArray>> {
    // check if the clipboard is empty if so return empty list
    val primaryClipData = this.clipboard.primaryClip ?: return mutableListOf()
    val contents: MutableList<Pair<String, ByteArray>> = mutableListOf()

    // loop through the items
    for (i in 0 until primaryClipData.itemCount) {
      val mimeType = primaryClipData.description.getMimeType(i)
      val item = primaryClipData.getItemAt(i)

      if (mimeType == this.MIME_TYPE_TEXT) {
        contents.add(Pair(mimeType, item.text.toString().toByteArray()))
      }

      if (mimeType == this.MIME_TYPE_COLOR) {
        contents.add(Pair(mimeType, item.text.toString().toByteArray()))
      }

      if (mimeType == this.MIME_TYPE_HTML) {
        contents.add(Pair(mimeType, item.text.toString().toByteArray()))
      }

      if (mimeType == this.MIME_TYPE_PNG) {
        // TODO: Implement
      }
    }

    // return the contents
    return contents
  }
}
