package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import jakarta.inject.Inject
import kotlin.reflect.full.findAnnotation

class HubMessageHandler @Inject constructor(
  payloadHandlers: List<HubMessagePayloadHandler<HubMessagePayload>>
) {
  private val payloadHandlers: Map<Class<out HubMessagePayload>, HubMessagePayloadHandler<HubMessagePayload>> = payloadHandlers.filter {
    it::class.findAnnotation<HubMessageHandling>() != null
  }.associateBy {
    it.payloadType
  }

  fun handle(hub: Hub, message: HubMessage<out HubMessagePayload>) {
    val handler = payloadHandlers[message.payload::class.java] ?: throw IllegalArgumentException("No handler found for payload type: ${message.payload::class.java}")
    handler.handle(hub, message.payload)
  }
}
