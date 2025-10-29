package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.AuthException
import javax.inject.Inject

class DeviceApiRepository @Inject constructor(
  private val deviceApiClient: DeviceApiClient
): DeviceRepository {
  override suspend fun createDevice(
    deviceRequestDto: DeviceRequestDto
  ): DeviceResponseDto {
    val response = deviceApiClient.createDevice(deviceRequestDto)
    if (!response.isSuccessful && response.code() == 401) throw AuthException("Invalid credentials")
    if (!response.isSuccessful) throw Exception("Create device failed with code: ${response.code()}")
    return response.body() ?: throw Exception("Create device failed with empty body")
  }

  override suspend fun updateDevice(
    id: String,
    deviceRequestDto: DeviceRequestDto
  ): DeviceResponseDto {
    val response = deviceApiClient.updateDevice(id, deviceRequestDto)
    if (!response.isSuccessful && response.code() == 401) throw AuthException("Invalid credentials")
    if (!response.isSuccessful) throw Exception("Update device failed with code: ${response.code()}")
    return response.body() ?: throw Exception("Update device failed with empty body")
  }
}
