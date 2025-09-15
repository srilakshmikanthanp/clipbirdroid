package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

import com.srilakshmikanthanp.clipbirdroid.common.retrofit.RetrofitInstance
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.AuthApiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module()
@InstallIn(SingletonComponent::class)
object DeviceModuleProvider {
  @Provides
  fun provideDeviceApiClient(): DeviceApiClient {
    return RetrofitInstance.retrofit.create(DeviceApiClient::class.java)
  }
}
