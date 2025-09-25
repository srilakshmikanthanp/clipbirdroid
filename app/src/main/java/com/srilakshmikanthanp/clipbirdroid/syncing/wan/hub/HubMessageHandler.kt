package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import jakarta.inject.Inject
import kotlin.reflect.full.findAnnotation

class HubMessageHandler @Inject constructor(
  payloadHandlers: Set<@JvmSuppressWildcards HubMessagePayloadHandler<*>>
) {
  private val payloadHandlers: Map<Class<out HubMessagePayload>, HubMessagePayloadHandler<out HubMessagePayload>> = payloadHandlers.filter {
    it::class.findAnnotation<HubMessageHandling>() != null
  }.associateBy {
    it.payloadType
  }

  fun handle(hub: Hub, message: HubMessage<out HubMessagePayload>) {
    val handler = payloadHandlers[message.payload::class.java] as HubMessagePayloadHandler<HubMessagePayload>? ?: throw IllegalArgumentException("No handler found for payload type: ${message.payload::class.java}")
    handler.handle(hub, message.payload)
  }
}
