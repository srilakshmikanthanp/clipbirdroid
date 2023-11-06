package com.srilakshmikanthanp.clipbirdroid.types.device

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import java.net.InetAddress


/**
 * Test for Device Serializable/Deserializable that sued in Intent
 */
class DeviceTest {
  @Test fun deviceTest() {
    val sent = Device(InetAddress.getLocalHost(), 1234, "Test")
    val enc = Json.encodeToString(sent)
    val recv = Json.decodeFromString<Device>(enc)

    assert(sent == recv)
  }
}
