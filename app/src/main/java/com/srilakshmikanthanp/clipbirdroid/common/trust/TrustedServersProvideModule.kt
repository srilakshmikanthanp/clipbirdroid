package com.srilakshmikanthanp.clipbirdroid.common.trust

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module()
@InstallIn(SingletonComponent::class)
class TrustedServersProvideModule {
  @Provides
  fun provideTrustedServers(@ApplicationContext context: Context): TrustedServers {
    return TrustedServersPreference(context)
  }
}
