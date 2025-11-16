package com.srilakshmikanthanp.clipbirdroid.clipboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClipboardViewModel @Inject constructor(
  val clipboardManager: ClipboardManager,
) : ViewModel()
