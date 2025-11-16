package com.srilakshmikanthanp.clipbirdroid.packets

import org.junit.Test

class AuthenticationPacketTest {
  @Test
  fun authenticationPacketTest() {
    val lhs = AuthenticationPacket(AuthenticationStatus.AuthOkay)
    val array = lhs.toByteArray()
    val rhs = array.toAuthenticationPacket()

    assert(rhs.getPacketType() == PacketType.AuthenticationPacket)
    assert(rhs.getPacketLength() == lhs.getPacketLength())
    assert(rhs.getAuthStatus() == AuthenticationStatus.AuthOkay)
  }
}
