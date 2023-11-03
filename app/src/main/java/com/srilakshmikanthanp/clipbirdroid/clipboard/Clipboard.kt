package com.srilakshmikanthanp.clipbirdroid.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.srilakshmikanthanp.clipbirdroid.constant.appName
import com.srilakshmikanthanp.clipbirdroid.constant.appProvider
import java.io.File


/**
 * Class For Managing the Clipboard
 */
class Clipboard(private val context: Context) {
  /// clipboard Manager to manage the clipboard
  private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

  /// List of ClipboardChangeListener
  private val listeners: MutableList<ClipboardChangeListener> = mutableListOf()

  // Add ClipboardChangeListener
  fun addClipboardChangeListener(listener: ClipboardChangeListener) {
    listeners.add(listener)
  }

  // Remove ClipboardChangeListener
  fun removeClipboardChangeListener(listener: ClipboardChangeListener) {
    listeners.remove(listener)
  }

  // Max Size of the Clipboard
  private val maxClipboardSize: Int = 800 * 1024

  // ClipboardChangeListener Interface
  fun interface ClipboardChangeListener {
    fun onClipboardChange(content: MutableList<Pair<String, ByteArray>>)
  }

  /// MIME Types
  companion object {
    val MIME_TYPE_TEXT: String = "text/plain"
    val MIME_TYPE_PNG: String = "image/png"
    val MIME_TYPE_HTML: String = "text/html"
  }

  /**
   * Get the Content from the URI
   */
  private fun getContentFromUri(uri: Uri): Pair<String, ByteArray>? {
    // List of Allowed Types
    val allowedTypes = arrayOf(MIME_TYPE_TEXT, MIME_TYPE_PNG, MIME_TYPE_HTML)

    // get the content
    val result = context.contentResolver.openInputStream(uri).use {
      val mimeType = context.contentResolver.getType(uri) ?: return@use null
      val content = it?.readBytes() ?: return@use null
      return@use Pair(mimeType, content)
    } ?: return null

    // if allowed type
    return if (allowedTypes.contains(result.first)) {
      result
    } else {
      null
    }
  }

  /**
   * Try to get Only Image from contents
   */
  private fun doItAsOnlyImage(contents: List<Pair<String, ByteArray>>): Boolean {
    // get the image
    val image = contents.find { it.first == MIME_TYPE_PNG } ?: return false
    val file = File.createTempFile(appName(), ".png", context.cacheDir)
    file.writeBytes(image.second)
    val uri = FileProvider.getUriForFile(context, appProvider(), file)

    // create clip data
    val clipData = ClipData.newUri(context.contentResolver, appName(), uri)
    clipboard.setPrimaryClip(clipData).also { return true }
  }

  /**
   * Try to get both Text and HTML from contents
   */
  private fun doItAsTextAndHtml(contents: List<Pair<String, ByteArray>>): Boolean {
    // get the text and html
    val textBytes = contents.find { it.first == MIME_TYPE_TEXT }
    val htmlBytes = contents.find { it.first == MIME_TYPE_HTML }

    // if any one null
    if (textBytes == null || htmlBytes == null) {
      return false
    }

    // text and Html
    val text = String(textBytes.second)
    val html = String(htmlBytes.second)

    // if content is > 800kb
    val clipData = if (text.length + html.length >= maxClipboardSize) {
      val file = File.createTempFile(appName(), ".html", context.cacheDir)
      file.writeBytes(htmlBytes.second)
      val uri = FileProvider.getUriForFile(context, appProvider(), file)
      ClipData.newUri(context.contentResolver, appName(), uri)
    } else {
      ClipData.newHtmlText(appName(), text, html)
    }

    // set the clip data
    clipboard.setPrimaryClip(clipData).also { return true }
  }

  /**
   * Try to get Only HTML from contents
   */
  private fun doItAsOnlyHtml(contents: List<Pair<String, ByteArray>>): Boolean {
    // get the html from contents
    val html = contents.find { it.first == MIME_TYPE_HTML } ?: return false

    // if content is > 800kb
    val clipData = if (html.second.size >= maxClipboardSize) {
      val file = File.createTempFile(appName(), ".html", context.cacheDir)
      file.writeBytes(html.second)
      val uri = FileProvider.getUriForFile(context, appProvider(), file)
      ClipData.newUri(context.contentResolver, appName(), uri)
    } else {
      ClipData.newHtmlText(appName(), String(html.second), String(html.second))
    }

    // set the clip data
    clipboard.setPrimaryClip(clipData).also { return true }
  }

  /**
   * Try to get Only Text from contents
   */
  private fun doItAsOnlyText(contents: List<Pair<String, ByteArray>>): Boolean {
    // get the text from contents
    val text = contents.find { it.first == MIME_TYPE_TEXT } ?: return false

    // if content is > 800kb
    val clipData = if (text.second.size >= maxClipboardSize) {
      val file = File.createTempFile(appName(), ".txt", context.cacheDir)
      file.writeBytes(text.second)
      val uri = FileProvider.getUriForFile(context, appProvider(), file)
      ClipData.newUri(context.contentResolver, appName(), uri)
    } else {
      ClipData.newPlainText(appName(), String(text.second))
    }

    // set the clip data
    clipboard.setPrimaryClip(clipData).also { return true }
  }

  /**
   * ClipBoard Change Implementation
   */
  private fun onClipboardChanged() {
    // if the content is put by clipbird
    if (clipboard.primaryClipDescription?.label == appName()) {
      return
    }

    // get the content
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
   * @brief Set the clipboard content with the given contents
   * @param contents List of Pair<String, ByteArray>
   * first -> MIME Type, second -> Raw Data
   */
  fun setClipboardContent(contents: List<Pair<String, ByteArray>>) {
    // list of function ordered with priority
    val functions = listOf(
      this::doItAsOnlyImage,
      this::doItAsTextAndHtml,
      this::doItAsOnlyHtml,
      this::doItAsOnlyText
    )

    // iterate through all the functions
    for (function in functions) {
      if (function(contents)) return
    }
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
    // get the clip data
    val clipData = this.clipboard.primaryClip ?: return mutableListOf()

    // create a list of Pair<String, ByteArray>
    val contents = mutableListOf<Pair<String, ByteArray>>()

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
