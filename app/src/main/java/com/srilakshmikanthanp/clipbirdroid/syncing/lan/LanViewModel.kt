package com.srilakshmikanthanp.clipbirdroid.syncing.lan

import androidx.lifecycle.ViewModel
import com.srilakshmikanthanp.clipbirdroid.clipboard.ClipboardController
import com.srilakshmikanthanp.clipbirdroid.history.HistoryController
import com.srilakshmikanthanp.clipbirdroid.syncing.lan.LanController
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.WanController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanViewModel @Inject constructor(
  val lanController: LanController,
) : ViewModel() {

}
