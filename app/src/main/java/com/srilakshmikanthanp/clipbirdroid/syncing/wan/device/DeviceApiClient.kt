package com.srilakshmikanthanp.clipbirdroid.syncing.wan.device

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceApiClient {
  @POST("/devices")
  suspend fun createDevice(
    @Body request: DeviceRequestDto
  ): Response<DeviceResponseDto>

  @PATCH("/devices/{id}")
  suspend fun updateDevice(
    @Path("id") id: String,
    @Body request: DeviceRequestDto
  ): Response<DeviceResponseDto>
}
