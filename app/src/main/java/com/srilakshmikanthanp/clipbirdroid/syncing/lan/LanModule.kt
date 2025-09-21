package com.srilakshmikanthanp.clipbirdroid.syncing.lan

import com.srilakshmikanthanp.clipbirdroid.Clipbird
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class LanModule {
  @Provides
  fun provideLanController(clipbird: Clipbird): LanController {
    return clipbird.lanController
  }
}
