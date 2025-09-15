package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

abstract class HubMessagePayloadHandlerBase<T: HubMessagePayload>(
  override val payloadType: Class<T>
): HubMessagePayloadHandler<T>
