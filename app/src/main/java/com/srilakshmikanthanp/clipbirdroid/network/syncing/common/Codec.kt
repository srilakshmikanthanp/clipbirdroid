package com.srilakshmikanthanp.clipbirdroid.network.syncing.common

import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.network.packets.Authentication
import com.srilakshmikanthanp.clipbirdroid.network.packets.InvalidPacket
import com.srilakshmikanthanp.clipbirdroid.network.packets.SyncingPacket
import com.srilakshmikanthanp.clipbirdroid.types.enums.ErrorCode
import com.srilakshmikanthanp.clipbirdroid.types.except.MalformedPacket
import com.srilakshmikanthanp.clipbirdroid.types.except.NotThisPacket
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.ReplayingDecoder
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Authentication Packet encoder
 */
class AuthenticationEncoder : MessageToByteEncoder<Authentication>() {
  override fun encode(ctx: ChannelHandlerContext, msg: Authentication, out: ByteBuf) {
    out.writeBytes(msg.toByteArray())
  }
}

/**
 * Invalid Packet decoder
 */
class InvalidPacketEncoder : MessageToByteEncoder<InvalidPacket>() {
  override fun encode(ctx: ChannelHandlerContext, msg: InvalidPacket, out: ByteBuf) {
    out.writeBytes(msg.toByteArray())
  }
}

/**
 * Sync Packet encoder
 */
class SyncingPacketEncoder : MessageToByteEncoder<SyncingPacket>() {
  override fun encode(ctx: ChannelHandlerContext, msg: SyncingPacket, out: ByteBuf) {
    out.writeBytes(msg.toByteArray())
  }
}

/**
 * Replaying Decoder for Server
 */
class PacketDecoder : ReplayingDecoder<Void>() {
  override fun decode(ctx: ChannelHandlerContext, inBuf: ByteBuf, out: MutableList<Any>) {
    // get the packet elements from the buffer
    val length: Int = inBuf.readInt()
    val rem = length - Int.SIZE_BYTES
    val bytes = inBuf.readBytes(rem)

    // tag for logging
    val TAG = "PacketDecoder"

    // convert to bytes length + bytes
    val buffer = ByteBuffer.allocate(length)

    // set order
    buffer.order(ByteOrder.BIG_ENDIAN)

    // put the length
    buffer.putInt(length)

    // put the bytes
    buffer.put(bytes.array())

    // try to parse the Invalid Packet
    try {
      out.add(InvalidPacket.fromByteArray(buffer.array()))
      return
    } catch (e: MalformedPacket) {
      ctx.writeAndFlush(InvalidPacket(e.errorCode, e.message.toByteArray()))
      return
    } catch (e: NotThisPacket) {
      Log.i(TAG, "Not Invalid Packet")
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      return
    }

    // try to parse the Authentication Packet
    try {
      out.add(Authentication.fromByteArray(buffer.array()))
      return
    } catch (e: MalformedPacket) {
      ctx.writeAndFlush(InvalidPacket(e.errorCode, e.message.toByteArray()))
      return
    } catch (e: NotThisPacket) {
      Log.i(TAG, "Not Authentication Packet")
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      return
    }

    // try to parse the Syncing Packet
    try {
      out.add(SyncingPacket.fromByteArray(buffer.array()))
      return
    } catch (e: MalformedPacket) {
      ctx.writeAndFlush(InvalidPacket(e.errorCode, e.message.toByteArray()))
      return
    } catch (e: NotThisPacket) {
      Log.i(TAG, "Not Syncing Packet")
    } catch (e: Exception) {
      Log.e(TAG, e.message, e)
      return
    }

    // skip the packet
    inBuf.skipBytes(inBuf.readableBytes())

    // Unknown packet
    val code = ErrorCode.InvalidPacket
    val msg = "Unknown Packet".toByteArray()
    val packet = InvalidPacket(code, msg)
    ctx.writeAndFlush(packet)
  }
}
