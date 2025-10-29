package com.srilakshmikanthanp.clipbirdroid.syncing.lan

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanViewModel @Inject constructor(
  val lanController: LanController,
) : ViewModel() {

}
