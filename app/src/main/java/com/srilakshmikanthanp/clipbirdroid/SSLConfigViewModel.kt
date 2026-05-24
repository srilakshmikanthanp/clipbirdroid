package com.srilakshmikanthanp.clipbirdroid

import androidx.lifecycle.ViewModel
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SSLConfigViewModel @Inject constructor(
  val sslConfig: SSLConfig
) : ViewModel()
