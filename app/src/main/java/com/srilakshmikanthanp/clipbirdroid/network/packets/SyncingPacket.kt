package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.google.protobuf.ByteString
import com.srilakshmikanthanp.clipbirdroid.Syncingpacket as SyncingPacketPacket

/**
 * Packet Class for Syncing Packet
 */
class SyncingPacket(
  @JvmField var packetLength: Int,
  @JvmField var packetType: Int,
  @JvmField var itemCount: Int,
  @JvmField var items: Array<SyncingItem>
) {
  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Int = 0x01) {
    SyncPacket(0x01),
  }

  /**
   * Set the Packet Length
   */
  fun setPacketLength(length: Int) {
    this.packetLength = length
  }

  /**
   * Get the Packet Length
   */
  fun getPacketLength(): Int {
    return this.packetLength
  }

  /**
   * Set the Packet Type
   */
  fun setPacketType(type: Int) {
    if (type != PacketType.SyncPacket.value) {
      throw IllegalArgumentException("Invalid PacketType value: $type")
    }

    this.packetType = type
  }

  /**
   * Get the Packet Type
   */
  fun getPacketType(): Int {
    return this.packetType
  }

  /**
   * Set the Item Count
   */
  fun setItemCount(count: Int) {
    this.itemCount = count
  }

  /**
   * Get the Item Count
   */
  fun getItemCount(): Int {
    return this.itemCount
  }

  /**
   * Set the Items
   */
  fun setItems(items: Array<SyncingItem>) {
    if (items.size != this.itemCount) {
      throw IllegalArgumentException("Invalid Item Count: ${items.size}")
    }

    this.items = items
  }

  /**
   * Get the Items
   */
  fun getItems(): Array<SyncingItem> {
    return this.items
  }

  /**
   * Size of Packet
   */
  fun size(): Int {
    var size = (Int.SIZE_BYTES + Byte.SIZE_BYTES + Int.SIZE_BYTES)

    for (item in this.items) {
      size += item.size()
    }

    return size
  }

  /**
   * Companion Object
   */
  companion object {
    /**
     * Create Syncing Packet From ByteArray Big Endian
     */
    fun fromByteArray(byteArray: ByteArray): SyncingPacket {
      // create the protobuf packet
      val packet = SyncingPacketPacket.SyncingPacket.parseFrom(byteArray)

      // if any error
      if (!packet.isInitialized) {
        throw IllegalArgumentException("Invalid Packet") // TODO change exception type
      }

      // read fields
      val packetLength = packet.packetLength
      val packetType = packet.packetType
      val itemCount = packet.itemCount
      val items = packet.itemsList.map { SyncingItem.fromProto(it) }.toTypedArray()

      // check packetType
      if (packetType != PacketType.SyncPacket.value) {
        throw IllegalArgumentException("Invalid PacketType value: $packetType")
      }

      // return SyncingPacket
      return SyncingPacket(
        packetLength,
        packetType,
        itemCount,
        items
      )
    }

    /**
     * Convert To Byte array Big Endian
     */
    fun toByteArray(syncingPacket: SyncingPacket): ByteArray {
      // create protobuf builder
      val packet = SyncingPacketPacket.SyncingPacket.newBuilder()

      // set fields
      packet.packetLength = syncingPacket.packetLength
      packet.packetType = syncingPacket.packetType
      packet.itemCount = syncingPacket.itemCount
      packet.addAllItems(syncingPacket.items.map { SyncingItem.toProto(it) })

      // return ByteArray
      return packet.build().toByteArray()
    }
  }
}

/**
 * Class For Syncing Item
 */
class SyncingItem(
  @JvmField var mimeLength: Int,
  @JvmField var mimeType: ByteArray,
  @JvmField var payloadLength: Int,
  @JvmField var payload: ByteArray
) {
  /**
   * Set the Mime Length
   */
  fun setMimeLength(length: Int) {
    this.mimeLength = length
  }

  /**
   * Get the Mime Length
   */
  fun getMimeLength(): Int {
    return this.mimeLength
  }

  /**
   * Set the Mime Type
   */
  fun setMimeType(type: ByteArray) {
    if (type.size != this.mimeLength) {
      throw IllegalArgumentException("Invalid Mime Type Length: ${type.size}")
    }

    this.mimeType = type
  }

  /**
   * Get the Mime Type
   */
  fun getMimeType(): ByteArray {
    return this.mimeType
  }

  /**
   * Set the Payload Length
   */
  fun setPayloadLength(length: Int) {
    this.payloadLength = length
  }

  /**
   * Get the Payload Length
   */
  fun getPayloadLength(): Int {
    return this.payloadLength
  }

  /**
   * Set the Payload
   */
  fun setPayload(payload: ByteArray) {
    if (payload.size != this.payloadLength) {
      throw IllegalArgumentException("Invalid Payload Length: ${payload.size}")
    }

    this.payload = payload
  }

  /**
   * Get the Payload
   */
  fun getPayload(): ByteArray {
    return this.payload
  }

  /**
   * Size of Syncing Item
   */
  fun size(): Int {
    return (Int.SIZE_BYTES + this.mimeLength + Int.SIZE_BYTES + this.payloadLength)
  }

  /**
   * Companion Object
   */
  companion object {
    /**
     * Create Syncing Item From ByteArray Big Endian
     */
    fun fromByteArray(byteArray: ByteArray): SyncingItem {
      // create the protobuf packet
      val item = SyncingPacketPacket.SyncingItem.parseFrom(byteArray)

      // if any error
      if (!item.isInitialized) {
        throw IllegalArgumentException("Invalid Item") // TODO change exception type
      }

      // read fields
      val mimeLength = item.mimeLength
      val mimeType = item.mimeType.toByteArray()
      val payloadLength = item.payloadLength
      val payload = item.payload.toByteArray()

      // return SyncingItem
      return SyncingItem(
        mimeLength,
        mimeType,
        payloadLength,
        payload
      )
    }

    /**
     * Convert To Byte array Big Endian
     */
    fun toByteArray(syncingItem: SyncingItem): ByteArray {
      // create protobuf builder
      val item = SyncingPacketPacket.SyncingItem.newBuilder()

      // set fields
      item.mimeLength = syncingItem.mimeLength
      item.mimeType = ByteString.copyFrom(syncingItem.mimeType)
      item.payloadLength = syncingItem.payloadLength
      item.payload = ByteString.copyFrom(syncingItem.payload)

      // return ByteArray
      return item.build().toByteArray()
    }

    /**
     * Create Syncing Item From Proto Packet
     */
    fun fromProto(item: SyncingPacketPacket.SyncingItem): SyncingItem {
      // read fields
      val mimeLength = item.mimeLength
      val mimeType = item.mimeType.toByteArray()
      val payloadLength = item.payloadLength
      val payload = item.payload.toByteArray()

      // return SyncingItem
      return SyncingItem(
        mimeLength,
        mimeType,
        payloadLength,
        payload
      )
    }

    /**
     * Convert To Proto Packet
     */
    fun toProto(syncingItem: SyncingItem): SyncingPacketPacket.SyncingItem {
      // create protobuf builder
      val item = SyncingPacketPacket.SyncingItem.newBuilder()

      // set fields
      item.mimeLength = syncingItem.mimeLength
      item.mimeType = ByteString.copyFrom(syncingItem.mimeType)
      item.payloadLength = syncingItem.payloadLength
      item.payload = ByteString.copyFrom(syncingItem.payload)

      // return ByteArray
      return item.build()
    }
  }
}
