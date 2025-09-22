package com.srilakshmikanthanp.clipbirdroid.common.retrofit

import com.google.gson.GsonBuilder
import com.srilakshmikanthanp.clipbirdroid.constants.getClipbirdApiUrl
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitProvider {
  @Provides
  @Singleton
  fun provideRetrofit(setting: Storage): Retrofit {
    val okHttpClient = OkHttpClient.Builder()
      .connectTimeout(30, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .addInterceptor(AuthTokenInterceptor(setting))
      .build()

    val gson = GsonBuilder().create()

    return Retrofit.Builder()
      .baseUrl(getClipbirdApiUrl())
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()
  }
}
