package com.srilakshmikanthanp.clipbirdroid.syncing.wan

import com.srilakshmikanthanp.clipbirdroid.Clipbird
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class WanModule {
  @Provides
  fun provideWanController(clipbird: Clipbird): WanController {
    return clipbird.wanController
  }
}
