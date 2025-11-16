package com.srilakshmikanthanp.clipbirdroid.common.trust

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module()
@InstallIn(SingletonComponent::class)
class TrustedClientsProvideModule {
  @Provides
  fun provideTrustedClients(@ApplicationContext context: Context): TrustedClients {
    return TrustedClientsPreference(context)
  }
}
