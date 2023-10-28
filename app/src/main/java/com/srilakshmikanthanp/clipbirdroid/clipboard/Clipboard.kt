package com.srilakshmikanthanp.clipbirdroid.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.srilakshmikanthanp.clipbirdroid.constant.appName
import java.io.File

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
   * Get the Content from the URI
   */
  private fun getContentFromUri(uri: Uri): Pair<String, ByteArray>? {
    // List of Allowed Types
    val allowedTypes = arrayOf(MIME_TYPE_TEXT, MIME_TYPE_PNG, MIME_TYPE_COLOR, MIME_TYPE_HTML)

    // get the content
    val result = context.contentResolver.openInputStream(uri).use {
      val mimeType = context.contentResolver.getType(uri) ?: return@use null
      val content = it?.readBytes() ?: return@use null
      return@use Pair(mimeType, content)
    } ?: return null

    // if allowed type
    if (!allowedTypes.contains(result.first)) {
      return null
    }

    // return result
    return result
  }

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
  fun addClipboardChangeListener(listener: ClipboardChangeListener) {
    listeners.add(listener)
  }

  /**
   * Remove ClipboardChangeListener
   */
  fun removeClipboardChangeListener(listener: ClipboardChangeListener) {
    listeners.remove(listener)
  }

  /**
   * @brief Set the clipboard content with the given contents
   * @param contents List of Pair<String, ByteArray>
   * first -> MIME Type, second -> Raw Data
   */
  fun setClipboardContent(contents: List<Pair<String, ByteArray>>) {
    // Enumerate all Mime Types
    val mimeTypes = contents.map { it.first }.toTypedArray()

    // List of URI's
    val uris = mutableListOf<Uri>()

    // create Files for contents
    for (i in contents.indices) {
      val file = File.createTempFile(appName(), ".tmp", context.cacheDir)
      file.writeBytes(contents[i].second)
      uris.add(FileProvider.getUriForFile(context, context.packageName, file))
    }

    // if less than 1 return
    if (uris.size < 1) return

    // create ClipData
    val clipDescription = ClipDescription(appName(), mimeTypes)
    val clipData = ClipData(clipDescription, ClipData.Item(uris[0]))

    // add all the items
    for (i in 1 until uris.size) {
      clipData.addItem(ClipData.Item(uris[i]))
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
    // create a list of Pair<String, ByteArray>
    val contents = mutableListOf<Pair<String, ByteArray>>()

    // get the clip data
    val clipData = this.clipboard.primaryClip ?: return mutableListOf()

    // iterate through all the items
    for (i in 0 until clipData.itemCount) {
      // get tha Item at i
      val item = clipData.getItemAt(i)

      // if has html
      if (item.htmlText != null) {
        contents.add(Pair(MIME_TYPE_HTML, item.htmlText.toString().toByteArray()))
      }

      // if has uri
      if (item.uri != null) {
        getContentFromUri(item.uri)?.let { contents.add(it) }
      }

      // if has text
      if (item.text != null) {
        contents.add(Pair(MIME_TYPE_TEXT, item.text.toString().toByteArray()))
      }
    }

    // return the contents
    return contents
  }
}
