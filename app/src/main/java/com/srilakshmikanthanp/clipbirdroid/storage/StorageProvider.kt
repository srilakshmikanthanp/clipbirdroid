package com.srilakshmikanthanp.clipbirdroid.storage

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module()
@InstallIn(SingletonComponent::class)
class StorageProvider {
  @Singleton
  @Provides
  fun provideStorage(@ApplicationContext context: Context): Storage {
    return PreferenceStorage(context)
  }
}
