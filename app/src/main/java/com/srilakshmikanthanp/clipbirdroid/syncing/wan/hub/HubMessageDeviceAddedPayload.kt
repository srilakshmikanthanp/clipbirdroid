package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.syncing.wan.device.DeviceResponseDto

data class HubMessageDeviceAddedPayload(
  val device: DeviceResponseDto
) : HubMessagePayload
