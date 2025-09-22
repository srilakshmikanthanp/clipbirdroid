package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module()
@InstallIn(SingletonComponent::class)
object AuthModuleProvider {
  @Provides
  fun provideApiClient(retrofit: Retrofit): AuthApiClient {
    return retrofit.create(AuthApiClient::class.java)
  }
}
