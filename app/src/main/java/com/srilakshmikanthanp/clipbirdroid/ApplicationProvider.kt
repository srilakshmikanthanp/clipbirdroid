package com.srilakshmikanthanp.clipbirdroid

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module()
@InstallIn(SingletonComponent::class)
class ApplicationProvider {
  @Singleton
  @Provides
  fun provideApplicationState(@ApplicationContext context: Context): ApplicationState {
    return ApplicationStatePreference(context)
  }
}
