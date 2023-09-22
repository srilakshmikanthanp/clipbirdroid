package com.srilakshmikanthanp.clipbirdroid.network

import com.srilakshmikanthanp.clipbirdroid.network.packets.Authentication
import com.srilakshmikanthanp.clipbirdroid.types.enums.AuthStatus
import org.junit.Test

class AuthenticationTest {
  @Test
  fun authenticationTest() {
    val packSend: Authentication = Authentication(AuthStatus.AuthOkay)
    val array = packSend.toByteArray()
    val packRecv: Authentication = Authentication.fromByteArray(array)

    assert(packRecv.getPacketType() == Authentication.PacketType.AuthStatus)
    assert(packRecv.getPacketLength() == packSend.size())
    assert(packRecv.getAuthStatus() == AuthStatus.AuthOkay)
  }
}
