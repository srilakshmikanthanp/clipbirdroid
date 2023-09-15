package com.srilakshmikanthanp.clipbirdroid.network.packets

import java.nio.ByteBuffer

/**
 * Packet Class for Syncing Packet
 */
class SyncingPacket(
  @JvmField var packetLength: Int,
  @JvmField var packetType: Byte,
  @JvmField var itemCount: Int,
  @JvmField var items: Array<SyncingItem>
) {
  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Byte = 0x01) {
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
  fun setPacketType(type: Byte) {
    if (type != PacketType.SyncPacket.value) {
      throw IllegalArgumentException("Invalid PacketType value: $type")
    }

    this.packetType = type
  }

  /**
   * Get the Packet Type
   */
  fun getPacketType(): Byte {
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
      // create ByteBuffer from byte array
      val buffer = ByteBuffer.wrap(byteArray)

      // get packet length
      val packetLength = buffer.int
      val packetType = buffer.get()
      val itemCount = buffer.int

      // create items
      val items = Array(itemCount) {
        SyncingItem.fromByteArray(byteArray)
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
      // create ByteBuffer
      val buffer = ByteBuffer.allocate(syncingPacket.size())

      // write fields
      buffer.putInt(syncingPacket.packetLength)
      buffer.put(syncingPacket.packetType)
      buffer.putInt(syncingPacket.itemCount)

      // write items
      for (item in syncingPacket.items) {
        buffer.put(SyncingItem.toByteArray(item))
      }

      // return ByteArray
      return buffer.array()
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
      // create ByteBuffer from byte array
      val buffer = ByteBuffer.wrap(byteArray)

      // get mime length
      val mimeLength = buffer.int
      val mimeType = ByteArray(mimeLength); buffer.get(mimeType)
      val payloadLength = buffer.int
      val payload = ByteArray(payloadLength); buffer.get(payload)

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
      // create ByteBuffer
      val buffer = ByteBuffer.allocate(syncingItem.size())

      // write fields
      buffer.putInt(syncingItem.mimeLength)
      buffer.put(syncingItem.mimeType)
      buffer.putInt(syncingItem.payloadLength)
      buffer.put(syncingItem.payload)

      // return ByteArray
      return buffer.array()
    }
  }
}
