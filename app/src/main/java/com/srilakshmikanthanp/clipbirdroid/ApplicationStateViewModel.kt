package com.srilakshmikanthanp.clipbirdroid

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ApplicationStateViewModel @Inject constructor(
  val applicationState: ApplicationState
): ViewModel()
