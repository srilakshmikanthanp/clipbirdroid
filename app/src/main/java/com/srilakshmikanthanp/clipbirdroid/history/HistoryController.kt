package com.srilakshmikanthanp.clipbirdroid.history

import com.srilakshmikanthanp.clipbirdroid.constants.appMaxHistory
import com.srilakshmikanthanp.clipbirdroid.controller.Controller
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryController @Inject constructor() : Controller {
  private val _history = MutableStateFlow(emptyList<List<Pair<String, ByteArray>>>().toMutableList())
  val history = _history.asStateFlow()
  private val _clipboard = MutableStateFlow(emptyList<Pair<String, ByteArray>>())
  val clipboard = _clipboard.asStateFlow()

  private fun List<Pair<String, ByteArray>>.isEqual(other: List<Pair<String, ByteArray>>): Boolean {
    if (this.size != other.size) return false
    for (i in this.indices) {
      if (this[i].first != other[i].first || !this[i].second.contentEquals(other[i].second)) return false
    }
    return true
  }

  fun addHistory(clip: List<Pair<String, ByteArray>>) {
    if (_history.value.isNotEmpty() && _history.value[0].isEqual(clip)) {
      return
    }

    if (_history.value.size + 1 > appMaxHistory()) {
      val newClipHist = _history.value.toMutableList()
      newClipHist.removeAt(newClipHist.lastIndex)
      _history.value = newClipHist
    }

    _clipboard.value = clip

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
