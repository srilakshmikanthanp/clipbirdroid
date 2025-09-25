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

  fun addHistory(clip: List<Pair<String, ByteArray>>) {
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
