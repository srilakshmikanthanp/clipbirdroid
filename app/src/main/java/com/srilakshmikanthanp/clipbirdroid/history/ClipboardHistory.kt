package com.srilakshmikanthanp.clipbirdroid.history

import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardContent
import com.srilakshmikanthanp.clipbirdroid.constants.appMaxHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardHistory @Inject constructor() {
  private val _history = MutableStateFlow(emptyList<List<ClipboardContent>>().toMutableList())
  val history = _history.asStateFlow()

  private val _clipboard = MutableStateFlow<List<ClipboardContent>>(emptyList())
  val clipboard = _clipboard.asStateFlow()

  private fun List<ClipboardContent>.isEqual(other: List<ClipboardContent>): Boolean {
    if (this.size != other.size) return false
    for (i in this.indices) {
      if (this[i].mimeType != other[i].mimeType || !this[i].data.contentEquals(other[i].data)) return false
    }
    return true
  }

  fun addHistory(clip: List<ClipboardContent>) {
    if (_history.value.isNotEmpty() && _history.value[0].isEqual(clip)) {
      return
    }

    _clipboard.value = clip

    if (_history.value.size + 1 > appMaxHistory()) {
      val newClipHist = _history.value.toMutableList()
      newClipHist.removeAt(newClipHist.lastIndex)
      _history.value = newClipHist
    }

    val newClipHist = _history.value.toMutableList()
    newClipHist.add(0, clip)
    _history.value = newClipHist
  }

  fun deleteHistoryAt(index: Int) {
    val newHist = _history.value.toMutableList()
    newHist.removeAt(index)
    _history.value = newHist
  }
}
