package com.srilakshmikanthanp.clipbirdroid.common.retrofit

import com.google.gson.GsonBuilder
import com.srilakshmikanthanp.clipbirdroid.common.okhttp.AuthTokenInterceptor
import com.srilakshmikanthanp.clipbirdroid.constants.getClipbirdApiUrl
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitProvider {
  @Provides
  @Singleton
  fun provideRetrofit(sessionManager: SessionManager): Retrofit {
    val okHttpClient = OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .addInterceptor(AuthTokenInterceptor(sessionManager))
      .build()

    val gson = GsonBuilder()
      .registerTypeAdapter(Instant::class.java, InstantTypeAdapter())
      .create()

    return Retrofit.Builder()
      .baseUrl(getClipbirdApiUrl())
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()
  }
}
