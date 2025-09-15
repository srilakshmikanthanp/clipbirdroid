package com.srilakshmikanthanp.clipbirdroid.common.extensions

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

val HubMessagePayload.type: HubMessageType
  get() = when (this) {
  is HubMessageNonceChallengeResponsePayload -> HubMessageType.NONCE_CHALLENGE_RESPONSE
  is HubMessageClipboardForwardPayload -> HubMessageType.CLIPBOARD_FORWARD
  is HubMessageClipboardDispatchPayload -> HubMessageType.CLIPBOARD_DISPATCH
  is HubMessageDeviceAddedPayload -> HubMessageType.DEVICE_ADDED
  is HubMessageDeviceRemovedPayload -> HubMessageType.DEVICE_REMOVED
  is HubMessageDeviceUpdatedPayload -> HubMessageType.DEVICE_UPDATED
  is HubMessageNonceChallengeCompletedPayload -> HubMessageType.NONCE_CHALLENGE_COMPLETED
  is HubMessageNonceChallengeRequestPayload -> HubMessageType.NONCE_CHALLENGE_REQUEST
  is HubMessageDevicesPayload -> HubMessageType.HUB_DEVICES
}

fun HubMessagePayload.toHubMessage(): HubMessage<*> {
  return HubMessage(type, this)
}
