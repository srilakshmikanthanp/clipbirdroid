package com.srilakshmikanthanp.clipbirdroid

import com.srilakshmikanthanp.clipbirdroid.common.ssl.SslConfigProvider
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClipbirdModule {
  @Singleton
  @Provides
  fun provideSslConfig(sslConfigProvider: SslConfigProvider): SSLConfig {
    return sslConfigProvider.getSslConfig()
  }

  @Provides
  @Singleton
  fun provideApplicationScope(): CoroutineScope = MainScope()
}
