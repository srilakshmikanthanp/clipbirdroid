package com.srilakshmikanthanp.clipbirdroid.history

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
  val clipboardHistory: ClipboardHistory,
) : ViewModel()
