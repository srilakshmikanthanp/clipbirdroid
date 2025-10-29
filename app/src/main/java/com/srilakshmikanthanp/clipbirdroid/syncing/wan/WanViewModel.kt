package com.srilakshmikanthanp.clipbirdroid.syncing.wan

import androidx.lifecycle.ViewModel
import com.srilakshmikanthanp.clipbirdroid.Clipbird
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WanViewModel @Inject constructor(
  val wanService: WanService,
  val storage: Storage,
  val clipbird: Clipbird
) : ViewModel() {
  val wanConnectionState = wanService.wanConnectionState

  fun connectToHub() {
    wanService.connectToHub()
  }

  fun disconnectFromHub() {
    wanService.disconnectFromHub()
  }
}
