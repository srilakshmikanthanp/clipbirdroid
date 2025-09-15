package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.AuthApiRepository
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.AuthRepository
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module()
@InstallIn(SingletonComponent::class)
abstract class DeviceModuleBinder {
  abstract fun bindDeviceRepository(
    deviceApiRepository: DeviceApiRepository
  ): DeviceRepository
}
