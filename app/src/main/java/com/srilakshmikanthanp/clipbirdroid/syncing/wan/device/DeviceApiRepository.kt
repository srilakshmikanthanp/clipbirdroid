package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

import javax.inject.Inject

class DeviceApiRepository @Inject constructor(
  private val deviceApiClient: DeviceApiClient
): DeviceRepository {
  override fun createDevice(
    deviceRequestDto: DeviceRequestDto
  ): kotlinx.coroutines.flow.Flow<DeviceResponseDto> = kotlinx.coroutines.flow.flow {
    val response = deviceApiClient.createDevice(deviceRequestDto)
    emit(response)
  }

  override fun updateDevice(
    id: String,
    deviceRequestDto: DeviceRequestDto
  ): kotlinx.coroutines.flow.Flow<DeviceResponseDto> = kotlinx.coroutines.flow.flow {
    val response = deviceApiClient.updateDevice(id, deviceRequestDto)
    emit(response)
  }
}
