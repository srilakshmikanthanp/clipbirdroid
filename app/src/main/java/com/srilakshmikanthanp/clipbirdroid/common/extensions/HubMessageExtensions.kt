package com.srilakshmikanthanp.clipbirdroid.common.extensions

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub.HubMessage

fun HubMessage<*>.toJson(): String {
  return jacksonObjectMapper().writeValueAsString(this)
}
