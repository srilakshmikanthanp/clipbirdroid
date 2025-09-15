package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
  fun createDevice(
    deviceRequestDto: DeviceRequestDto
  ): Flow<DeviceResponseDto>

  fun updateDevice(
    id: String,
    deviceRequestDto: DeviceRequestDto
  ): Flow<DeviceResponseDto>
}
