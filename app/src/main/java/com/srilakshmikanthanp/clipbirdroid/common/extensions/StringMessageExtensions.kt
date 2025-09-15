package com.srilakshmikanthanp.clipbirdroid.common.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessage
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageClipboardDispatchPayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageClipboardForwardPayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageDeviceAddedPayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageDeviceRemovedPayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageDeviceUpdatedPayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageDevicesPayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageNonceChallengeCompletedPayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageNonceChallengeRequestPayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageNonceChallengeResponsePayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessagePayload
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessageType

fun String.toHubMessage(): HubMessage<HubMessagePayload> {
  val objectMapper = jacksonObjectMapper()
  val rootNode = objectMapper.readTree(this)
  val typeNode = rootNode.get("type").takeIf { it != null && it.isTextual } ?: throw IllegalArgumentException("Invalid JSON: 'type' field is missing or not a string")
  val type = HubMessageType.valueOf(typeNode.asText())
  val payloadNode = rootNode.get("payload")

  val payload = when (type) {
    HubMessageType.NONCE_CHALLENGE_RESPONSE -> objectMapper.treeToValue(payloadNode, HubMessageNonceChallengeResponsePayload::class.java)
    HubMessageType.CLIPBOARD_FORWARD -> objectMapper.treeToValue(payloadNode, HubMessageClipboardForwardPayload::class.java)
    HubMessageType.CLIPBOARD_DISPATCH -> objectMapper.treeToValue(payloadNode, HubMessageClipboardDispatchPayload::class.java)
    HubMessageType.DEVICE_ADDED -> objectMapper.treeToValue(payloadNode, HubMessageDeviceAddedPayload::class.java)
    HubMessageType.DEVICE_REMOVED -> objectMapper.treeToValue(payloadNode, HubMessageDeviceRemovedPayload::class.java)
    HubMessageType.DEVICE_UPDATED -> objectMapper.treeToValue(payloadNode, HubMessageDeviceUpdatedPayload::class.java)
    HubMessageType.NONCE_CHALLENGE_COMPLETED -> objectMapper.treeToValue(payloadNode, HubMessageNonceChallengeCompletedPayload::class.java)
    HubMessageType.NONCE_CHALLENGE_REQUEST -> objectMapper.treeToValue(payloadNode, HubMessageNonceChallengeRequestPayload::class.java)
    HubMessageType.HUB_DEVICES -> objectMapper.treeToValue(payloadNode, HubMessageDevicesPayload::class.java)
  }

  return HubMessage(type, payload)
}
