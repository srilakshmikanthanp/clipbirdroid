package com.srilakshmikanthanp.clipbirdroid.controller

import androidx.lifecycle.ViewModel
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardController
import com.srilakshmikanthanp.clipbirdroid.history.HistoryController
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.LanController
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.WanController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ControllerViewModel @Inject constructor(
  val clipboardController: ClipboardController,
  val historyController: HistoryController,
  val lanController: LanController,
  val wanController: WanController
) : ViewModel()
