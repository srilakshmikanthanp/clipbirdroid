package com.srilakshmikanthanp.clipbirdroid.clipboard

import com.srilakshmikanthanp.clipbirdroid.Clipbird
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ClipbirdModule {
  @Provides
  fun provideClipboardController(clipbird: Clipbird): ClipboardController {
    return clipbird.clipboardController
  }
}
