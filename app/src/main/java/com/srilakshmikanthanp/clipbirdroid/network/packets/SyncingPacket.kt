package com.srilakshmikanthanp.clipbirdroid.network.packets

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
   * Convert To Byte array Big Endian
   */
  fun toByteArray(syncingPacket: SyncingPacket): ByteArray {

  }

  /**
   * Companion Object
   */
  companion object {
    /**
     * Create Syncing Packet From ByteArray Big Endian
     */
    fun fromByteArray(byteArray: ByteArray): SyncingPacket {
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
   * Convert To Byte array Big Endian
   */
  fun toByteArray(syncingItem: SyncingItem): ByteArray {

  }

  /**
   * Companion Object
   */
  companion object {
    /**
     * Create Syncing Item From ByteArray Big Endian
     */
    fun fromByteArray(byteArray: ByteArray): SyncingItem {

    }
  }
}
