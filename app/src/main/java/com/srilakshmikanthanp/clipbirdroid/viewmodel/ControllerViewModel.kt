package com.srilakshmikanthanp.clipbirdroid.viewmodel

import androidx.lifecycle.ViewModel
import com.srilakshmikanthanp.clipbirdroid.controller.ClipboardController
import com.srilakshmikanthanp.clipbirdroid.controller.HistoryController
import com.srilakshmikanthanp.clipbirdroid.controller.LanController
import com.srilakshmikanthanp.clipbirdroid.controller.WanController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ControllerViewModel @Inject constructor(
  val clipboardController: ClipboardController,
  val historyController: HistoryController,
  val lanController: LanController,
  val wanController: WanController
) : ViewModel()
