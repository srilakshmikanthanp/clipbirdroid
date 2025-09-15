package com.srilakshmikanthanp.clipbirdroid.common.retrofit

import com.google.gson.GsonBuilder
import com.srilakshmikanthanp.clipbirdroid.constants.getClipbirdApiUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
  private val gson = GsonBuilder().create()

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  val retrofit by lazy {
    Retrofit.Builder()
      .baseUrl(getClipbirdApiUrl())
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()
  }
}
