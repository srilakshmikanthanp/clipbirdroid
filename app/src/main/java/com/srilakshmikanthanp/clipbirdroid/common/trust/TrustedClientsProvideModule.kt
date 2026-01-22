package com.srilakshmikanthanp.clipbirdroid.common.trust

import com.srilakshmikanthanp.clipbirdroid.common.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module()
@InstallIn(SingletonComponent::class)
class TrustedClientsProvideModule {
  @Provides
  fun provideTrustedClients(appDatabase: AppDatabase): TrustedClients {
    return TrustedClientsRoom(appDatabase.trustedClientDao())
  }
}
