package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

interface DeviceRepository {
  suspend fun createDevice(
    deviceRequestDto: DeviceRequestDto
  ): DeviceResponseDto

  suspend fun updateDevice(
    id: String,
    deviceRequestDto: DeviceRequestDto
  ): DeviceResponseDto
}
