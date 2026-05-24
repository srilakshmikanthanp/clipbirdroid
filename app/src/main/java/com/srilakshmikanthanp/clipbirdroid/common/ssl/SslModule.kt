package com.srilakshmikanthanp.clipbirdroid.common.ssl

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.Clipbird
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class SslModule {
  @Provides
  fun provideClipbird(@ApplicationContext context: Context): Clipbird {
    return context.applicationContext as Clipbird
  }
}
