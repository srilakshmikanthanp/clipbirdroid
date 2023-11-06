package com.srilakshmikanthanp.clipbirdroid.types.device

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.net.InetAddress

/**
 * Serializable for InetAddress
 */
private object InetAddressSerializer: KSerializer<InetAddress> {
  override fun serialize(encoder: Encoder, value: InetAddress) {
    value.hostAddress?.let { encoder.encodeString(it) }
  }

  override val descriptor: SerialDescriptor
    get() = PrimitiveSerialDescriptor("InetAddress", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): InetAddress {
    return InetAddress.getByName(decoder.decodeString())
  }
}

/**
 * Device class represents the device information
 */
@Serializable
data class Device(
  @Serializable(with = InetAddressSerializer::class)
  val ip: InetAddress,
  val port: Int,
  val name: String,
)
