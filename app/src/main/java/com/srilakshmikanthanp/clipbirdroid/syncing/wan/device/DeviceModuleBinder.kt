package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module()
@InstallIn(SingletonComponent::class)
abstract class DeviceModuleBinder {
  @Binds
  abstract fun bindDeviceRepository(
    deviceApiRepository: DeviceApiRepository
  ): DeviceRepository
}
