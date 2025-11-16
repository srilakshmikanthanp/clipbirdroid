package com.srilakshmikanthanp.clipbirdroid.packets

enum class PacketType(val code: Int) {
  AuthenticationPacket(0x01),
  InvalidRequest(0x00),
  PingPongPacket(0x03),
  SyncingPacket(0x02),
  CertificateExchange(0x04)
}
