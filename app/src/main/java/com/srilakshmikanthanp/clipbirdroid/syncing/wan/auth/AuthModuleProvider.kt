package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import com.srilakshmikanthanp.clipbirdroid.common.retrofit.RetrofitInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module()
@InstallIn(SingletonComponent::class)
object AuthModuleProvider {
  @Provides
  fun provideApiClient(): AuthApiClient {
    return RetrofitInstance.retrofit.create(AuthApiClient::class.java)
  }
}
