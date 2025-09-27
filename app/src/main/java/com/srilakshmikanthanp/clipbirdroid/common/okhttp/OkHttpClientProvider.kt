package com.srilakshmikanthanp.clipbirdroid.common.okhttp

import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class OkHttpClientProvider {
  @Provides
  @Singleton
  fun provideOkHttpClient(setting: Storage): OkHttpClient {
    return OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .pingInterval(30, TimeUnit.SECONDS)
      .addInterceptor(AuthTokenInterceptor(setting))
      .build()
  }
}
