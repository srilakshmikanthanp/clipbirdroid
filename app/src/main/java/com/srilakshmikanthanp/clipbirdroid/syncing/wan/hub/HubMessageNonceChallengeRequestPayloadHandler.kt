package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.common.functions.sign
import java.util.Base64
import javax.inject.Inject

@HubMessageHandling
class HubMessageNonceChallengeRequestPayloadHandler @Inject constructor(): HubMessagePayloadHandlerBase<HubMessageNonceChallengeRequestPayload> (HubMessageNonceChallengeRequestPayload::class.java) {
  override fun handle(hub: Hub, payload: HubMessageNonceChallengeRequestPayload) {
    val signedNonce = Base64.getEncoder().encode(sign(payload.nonce.toByteArray(), hub.getHubHostDevice().privateKey.toByteArray()))
    val response = HubMessageNonceChallengeResponsePayload(nonce = payload.nonce, signature = String(signedNonce))
    val message = HubMessage(type = HubMessageType.NONCE_CHALLENGE_RESPONSE, payload = response)
    hub.sendMessage(message)
  }
}
