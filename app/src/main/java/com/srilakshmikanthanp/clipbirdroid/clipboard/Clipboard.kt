package com.srilakshmikanthanp.clipbirdroid.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.srilakshmikanthanp.clipbirdroid.constants.appName
import com.srilakshmikanthanp.clipbirdroid.constants.appProvider
import com.srilakshmikanthanp.clipbirdroid.common.utility.toPNG
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class Clipboard @Inject constructor(@param:ApplicationContext private val context: Context) {
  private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  private val listeners: MutableList<ClipboardChangeListener> = mutableListOf()

  fun addClipboardChangeListener(listener: ClipboardChangeListener) {
    listeners.add(listener)
  }

  fun removeClipboardChangeListener(listener: ClipboardChangeListener) {
    listeners.remove(listener)
  }

  private val maxClipboardSize: Int = 800 * 1024

  fun interface ClipboardChangeListener {
    fun onClipboardChange(content: MutableList<ClipboardContent>)
  }

  companion object {
    const val MIME_TYPE_TEXT: String = "text/plain"
    const val MIME_TYPE_PNG: String = "image/png"
    const val MIME_TYPE_HTML: String = "text/html"
  }

  private fun getContentFromUri(uri: Uri): ClipboardContent? {
    val allowedTypes = arrayOf(MIME_TYPE_TEXT, MIME_TYPE_PNG, MIME_TYPE_HTML)

    var result = try {
      context.contentResolver.openInputStream(uri)
    } catch (e: FileNotFoundException) {
      return null
    } catch (e: SecurityException) {
      return null
    }.use {
      val mimeType = context.contentResolver.getType(uri) ?: return@use null
      val content = it?.readBytes() ?: return@use null
      return@use ClipboardContent(mimeType, content)
    } ?: return null

    if (result.mimeType.startsWith("image/")) {
      result = ClipboardContent(MIME_TYPE_PNG, result.data.toPNG()!!)
    }

    return if (allowedTypes.contains(result.mimeType)) {
      result
    } else {
      null
    }
  }

  private fun setItAsOnlyImage(contents: List<ClipboardContent>): Boolean {
    val image = contents.find { it.mimeType == MIME_TYPE_PNG } ?: return false
    val file = File.createTempFile(appName(), ".png", context.cacheDir)
    file.writeBytes(image.data)
    val uri = FileProvider.getUriForFile(context, appProvider(), file)
    val clipData = ClipData.newUri(context.contentResolver, appName(), uri)
    clipboard.setPrimaryClip(clipData).also { return true }
  }

  private fun setItAsTextAndHtml(contents: List<ClipboardContent>): Boolean {
    val textBytes = contents.find { it.mimeType == MIME_TYPE_TEXT }
    val htmlBytes = contents.find { it.mimeType == MIME_TYPE_HTML }

    if (textBytes == null || htmlBytes == null) {
      return false
    }

    val text = String(textBytes.data)
    val html = String(htmlBytes.data)

    val clipData = if (text.length + html.length >= maxClipboardSize) {
      val file = File.createTempFile(appName(), ".html", context.cacheDir)
      file.writeBytes(htmlBytes.data)
      val uri = FileProvider.getUriForFile(context, appProvider(), file)
      ClipData.newUri(context.contentResolver, appName(), uri)
    } else {
      ClipData.newHtmlText(appName(), text, html)
    }

    clipboard.setPrimaryClip(clipData).also { return true }
  }

  private fun setItAsOnlyHtml(contents: List<ClipboardContent>): Boolean {
    val html = contents.find { it.mimeType == MIME_TYPE_HTML } ?: return false

    val clipData = if (html.data.size >= maxClipboardSize) {
      val file = File.createTempFile(appName(), ".html", context.cacheDir)
      file.writeBytes(html.data)
      val uri = FileProvider.getUriForFile(context, appProvider(), file)
      ClipData.newUri(context.contentResolver, appName(), uri)
    } else {
      ClipData.newHtmlText(appName(), String(html.data), String(html.data))
    }

    clipboard.setPrimaryClip(clipData).also { return true }
  }

  private fun setItAsOnlyText(contents: List<ClipboardContent>): Boolean {
    val text = contents.find { it.mimeType == MIME_TYPE_TEXT } ?: return false

    val clipData = if (text.data.size >= maxClipboardSize) {
      val file = File.createTempFile(appName(), ".txt", context.cacheDir)
      file.writeBytes(text.data)
      val uri = FileProvider.getUriForFile(context, appProvider(), file)
      ClipData.newUri(context.contentResolver, appName(), uri)
    } else {
      ClipData.newPlainText(appName(), String(text.data))
    }

    clipboard.setPrimaryClip(clipData).also { return true }
  }

  private fun onClipboardChanged() {
    if (clipboard.primaryClipDescription?.label == appName()) {
      return
    }
    val contents = this.getClipboardContent()
    for (listener in listeners) {
      listener.onClipboardChange(contents)
    }
  }

  init {
    clipboard.addPrimaryClipChangedListener(this::onClipboardChanged)
  }

  fun getClipboardContent(): MutableList<ClipboardContent> {
    val clipData = this.clipboard.primaryClip ?: return mutableListOf()
    val contents = mutableListOf<ClipboardContent>()

    for (i in 0 until clipData.itemCount) {
      val item = clipData.getItemAt(i)

      if (item.htmlText != null) {
        contents.add(ClipboardContent(MIME_TYPE_HTML, item.htmlText.toString().toByteArray()))
      }

      if (item.uri != null) {
        getContentFromUri(item.uri)?.let { contents.add(it) }
      }

      if (item.text != null) {
        contents.add(ClipboardContent(MIME_TYPE_TEXT, item.text.toString().toByteArray()))
      }
    }

    return contents
  }

  fun setClipboardContent(contents: List<ClipboardContent>) {
    val functions = listOf(
      this::setItAsOnlyImage,
      this::setItAsTextAndHtml,
      this::setItAsOnlyHtml,
      this::setItAsOnlyText
    )

    for (function in functions) {
      if (function(contents)) return
    }
  }

  fun clearClipboardContent() {
    this.clipboard.clearPrimaryClip()
  }
}
