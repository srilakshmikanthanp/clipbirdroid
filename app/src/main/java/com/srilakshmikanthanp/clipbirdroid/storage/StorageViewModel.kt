package com.srilakshmikanthanp.clipbirdroid.storage

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StorageViewModel @Inject constructor(
  val storage: Storage
) : ViewModel()
