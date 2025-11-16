package com.srilakshmikanthanp.clipbirdroid.packets

import org.junit.Test

class CertificateExchangePacketTest {
  @Test
  fun testCertificateExchangePacketSerialization() {
    val lhs = CertificateExchangePacket(byteArrayOf(0x01, 0x02, 0x03, 0x04))
    val array = lhs.toByteArray()
    val rhs = array.toCertificateExchangePacket()
    assert(rhs.getPacketType() == PacketType.CertificateExchange)
    assert(rhs.getPacketLength() == lhs.getPacketLength())
    assert(rhs.getCertificate().contentEquals(lhs.getCertificate()))
  }
}
