package com.srilakshmikanthanp.clipbirdroid.network.packets

import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode
import com.srilakshmikanthanp.clipbirdroid.types.except.MalformedPacket
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder


/**
 * Class For Syncing Item
 */
class SyncingItem(
  private var mimeType: ByteArray,
  private var payload: ByteArray
) {
  // packet fields
  private var mimeLength: Int
  private var payloadLength: Int

  // init
  init {
    mimeLength = mimeType.size
    payloadLength = payload.size
  }

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
   * Convert To ByteBuffer Big Endian
   */
  fun toByteBuffer(syncingItem: SyncingItem): ByteBuffer {
    // create an ByteBuffer to serialize the packet
    val byteBuffer = ByteBuffer.allocate(syncingItem.size())

    // set the order
    byteBuffer.order(ByteOrder.BIG_ENDIAN)

    // set the fields
    byteBuffer.putInt(syncingItem.mimeLength)
    byteBuffer.put(syncingItem.mimeType)
    byteBuffer.putInt(syncingItem.payloadLength)
    byteBuffer.put(syncingItem.payload)

    // return the byte array
    return byteBuffer
  }

  /**
   * Convert To Byte array Big Endian
   */
  fun toByteArray(syncingItem: SyncingItem): ByteArray {
    return this.toByteBuffer(syncingItem).array()
  }

  /**
   * Companion Object
   */
  companion object {
    /**
     * Create Syncing Item From ByteBuffer Big Endian
     */
    fun fromByteBuffer(byteBuffer: ByteBuffer): SyncingItem {
      val payloadLength: Int; val payload: ByteArray
      val mimeLength: Int; val mimeType: ByteArray

      // try to get the fields
      try {
        mimeLength = byteBuffer.int
        mimeType = ByteArray(mimeLength)
        byteBuffer.get(mimeType)
        payloadLength = byteBuffer.int
        payload = ByteArray(payloadLength)
        byteBuffer.get(payload)
      } catch (e: BufferUnderflowException) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid Syncing Item")
      }

      // return the SyncingItem
      return SyncingItem(mimeType, payload)
    }

    /**
     * Create Syncing Item From ByteArray Big Endian
     */
    fun fromByteArray(byteArray: ByteArray): SyncingItem {
      // create an ByteBuffer to deserialize the packet
      val byteBuffer = ByteBuffer.wrap(byteArray)

      // set the order
      byteBuffer.order(ByteOrder.BIG_ENDIAN)

      // get the packet
      return this.fromByteBuffer(byteBuffer)
    }
  }
}

/**
 * Packet Class for Syncing Packet
 */
class SyncingPacket(
  private var itemCount: Int,
  private var items: Array<SyncingItem>
) {
  // packet fields
  private var packetType: PacketType
  private var packetLength: Int

  // init
  init {
    this.packetType = PacketType.SyncPacket
    this.packetLength = this.size()
  }

  /**
   * Allowed packet Types
   */
  enum class PacketType(val value: Int) {
    SyncPacket(0x02),
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
  fun setPacketType(type: PacketType) {
    this.packetType = type
  }

  /**
   * Get the Packet Type
   */
  fun getPacketType(): PacketType {
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
    var size = (Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES)

    for (item in this.items) {
      size += item.size()
    }

    return size
  }

  /**
   * Convert To Byte array Big Endian
   */
  fun toByteArray(): ByteArray {
    // create an ByteBuffer to serialize the packet
    val byteBuffer = ByteBuffer.allocate(this.size())

    // set the order
    byteBuffer.order(ByteOrder.BIG_ENDIAN)

    // set the fields
    byteBuffer.putInt(this.packetLength)
    byteBuffer.putInt(this.packetType.value)
    byteBuffer.putInt(this.itemCount)

    for (item in this.items) {
      byteBuffer.put(item.toByteArray(item))
    }

    // return the byte array
    return byteBuffer.array()
  }

  /**
   * Companion Object
   */
  companion object {
    /**
     * Create Syncing Packet From ByteArray Big Endian
     */
    fun fromByteArray(byteArray: ByteArray): SyncingPacket {
      // create an ByteBuffer to deserialize the packet
      val byteBuffer = ByteBuffer.wrap(byteArray)

      // set the order
      byteBuffer.order(ByteOrder.BIG_ENDIAN)

      // get the fields
      val packetLength: Int
      val packetType: Int
      val itemCount: Int
      val items: Array<SyncingItem>

      // try to get the fields
      try {
        packetLength = byteBuffer.int
        packetType = byteBuffer.int
        itemCount = byteBuffer.int
        items = Array(itemCount) { SyncingItem.fromByteBuffer(byteBuffer) }
      } catch (e: BufferUnderflowException) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid Syncing Packet")
      }

      // check the packet type
      if (packetType != PacketType.SyncPacket.value) {
        throw MalformedPacket(ErrorCode.CodingError, "Invalid PacketType value: $packetType")
      }

      // create the SyncingPacket
      val packet = SyncingPacket(itemCount, items)

      // check length
      if (packet.packetLength != packetLength) {
        throw MalformedPacket(ErrorCode.CodingError, "Length Does match")
      }

      // done return
      return packet
    }
  }
}
