package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module()
@InstallIn(SingletonComponent::class)
object DeviceModuleProvider {
  @Provides
  fun provideDeviceApiClient(retrofit: Retrofit): DeviceApiClient {
    return retrofit.create(DeviceApiClient::class.java)
  }
}
