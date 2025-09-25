package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

import javax.inject.Inject

class DeviceApiRepository @Inject constructor(
  private val deviceApiClient: DeviceApiClient
): DeviceRepository {
  override suspend fun createDevice(
    deviceRequestDto: DeviceRequestDto
  ): DeviceResponseDto {
    return deviceApiClient.createDevice(deviceRequestDto)
  }

  override suspend fun updateDevice(
    id: String,
    deviceRequestDto: DeviceRequestDto
  ): DeviceResponseDto {
    return deviceApiClient.updateDevice(id, deviceRequestDto)
  }
}
