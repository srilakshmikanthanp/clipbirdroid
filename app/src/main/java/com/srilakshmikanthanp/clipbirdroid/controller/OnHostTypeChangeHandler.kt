package com.srilakshmikanthanp.clipbirdroid.controller

import com.srilakshmikanthanp.clipbirdroid.common.enums.HostType

// On Host Type Change Handler
fun interface OnHostTypeChangeHandler {
  fun onHostTypeChanged(host: HostType)
}
