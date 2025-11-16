package com.srilakshmikanthanp.clipbirdroid.packets

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SyncingItem(private var mimeType: ByteArray, private var payload: ByteArray) {
  private var mimeLength: Int
  private var payloadLength: Int

  init {
    mimeLength = mimeType.size
    payloadLength = payload.size
  }

  fun setMimeLength(length: Int) {
    this.mimeLength = length
  }

  fun getMimeLength(): Int {
    return this.mimeLength
  }

  fun setMimeType(type: ByteArray) {
    if (type.size != this.mimeLength) {
      throw IllegalArgumentException("Invalid Mime Type Length: ${type.size}")
    }
    this.mimeType = type
  }

  fun getMimeType(): ByteArray {
    return this.mimeType
  }

  fun setPayloadLength(length: Int) {
    this.payloadLength = length
  }

  fun getPayloadLength(): Int {
    return this.payloadLength
  }

  fun setPayload(payload: ByteArray) {
    if (payload.size != this.payloadLength) {
      throw IllegalArgumentException("Invalid Payload Length: ${payload.size}")
    }
    this.payload = payload
  }

  fun getPayload(): ByteArray {
    return this.payload
  }

  fun size(): Int {
    return (Int.SIZE_BYTES + this.mimeLength + Int.SIZE_BYTES + this.payloadLength)
  }

  fun toByteArray(syncingItem: SyncingItem): ByteArray {
    val byteBuffer = ByteBuffer.allocate(syncingItem.size())
    byteBuffer.order(ByteOrder.BIG_ENDIAN)
    byteBuffer.putInt(syncingItem.mimeLength)
    byteBuffer.put(syncingItem.mimeType)
    byteBuffer.putInt(syncingItem.payloadLength)
    byteBuffer.put(syncingItem.payload)
    return byteBuffer.array()
  }
}

fun ByteBuffer.toSyncingItem(): SyncingItem {
  val payloadLength: Int
  val payload: ByteArray
  val mimeLength: Int
  val mimeType: ByteArray

  try {
    mimeLength = this.int
    mimeType = ByteArray(mimeLength)
    this.get(mimeType)
    payloadLength = this.int
    payload = ByteArray(payloadLength)
    this.get(payload)
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Syncing Item")
  }

  return SyncingItem(mimeType, payload)
}

class SyncingPacket(private var items: Array<SyncingItem>): NetworkPacket {
  private var packetType: PacketType = PacketType.SyncingPacket

  init {
    this.packetType = PacketType.SyncingPacket
  }

  fun getPacketLength(): Int {
    var size = (Int.SIZE_BYTES + Int.SIZE_BYTES + Int.SIZE_BYTES)

    for (item in this.items) {
      size += item.size()
    }

    return size
  }

  fun setPacketType(type: PacketType) {
    this.packetType = type
  }

  fun getPacketType(): PacketType {
    return this.packetType
  }

  fun getItemCount(): Int {
    return this.items.size
  }

  fun setItems(items: Array<SyncingItem>) {
    this.items = items
  }

  fun getItems(): Array<SyncingItem> {
    return this.items
  }

  override fun toByteArray(): ByteArray {
    val byteBuffer = ByteBuffer.allocate(this.getPacketLength())
    byteBuffer.order(ByteOrder.BIG_ENDIAN)
    byteBuffer.putInt(this.getPacketLength())
    byteBuffer.putInt(this.packetType.code)
    byteBuffer.putInt(this.items.size)
    for (item in this.items) {
      byteBuffer.put(item.toByteArray(item))
    }
    return byteBuffer.array()
  }
}

fun ByteArray.toSyncingPacket(): SyncingPacket {
  val byteBuffer = ByteBuffer.wrap(this)
  byteBuffer.order(ByteOrder.BIG_ENDIAN)

  val packetLength: Int
  val packetType: Int
  val itemCount: Int
  val items: Array<SyncingItem>

  try {
    packetLength = byteBuffer.int
    packetType = byteBuffer.int
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  if (packetType != PacketType.SyncingPacket.code) {
    throw NotThisPacketException("Not Syncing Packet")
  }

  try {
    itemCount = byteBuffer.int
    items = Array(itemCount) { byteBuffer.toSyncingItem() }
  } catch (e: BufferUnderflowException) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  val packet = SyncingPacket(items)

  if (packetLength != packet.getPacketLength()) {
    throw MalformedPacketException(ErrorCode.CodingError, "Invalid Packet Length")
  }

  return packet
}
