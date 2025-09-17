package com.srilakshmikanthanp.clipbirdroid

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class ClipbirdModule {
  @Provides
  fun provideClipbird(@ApplicationContext context: Context): Clipbird {
    return context.applicationContext as Clipbird
  }
}
