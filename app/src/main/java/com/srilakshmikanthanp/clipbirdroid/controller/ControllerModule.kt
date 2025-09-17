package com.srilakshmikanthanp.clipbirdroid.controller

import com.srilakshmikanthanp.clipbirdroid.Clipbird
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ControllerModule {
  @Provides
  fun provideClipboardController(clipbird: Clipbird): ClipboardController {
    return clipbird.clipboardController
  }

  @Provides
  fun provideHistoryController(clipbird: Clipbird): HistoryController = clipbird.historyController

  @Provides
  fun provideLanController(clipbird: Clipbird): LanController {
    return clipbird.lanController
  }

  @Provides
  fun provideWanController(clipbird: Clipbird): WanController {
    return clipbird.wanController
  }
}
