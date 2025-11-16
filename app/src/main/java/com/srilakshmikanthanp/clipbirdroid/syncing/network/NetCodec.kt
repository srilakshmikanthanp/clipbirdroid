package com.srilakshmikanthanp.clipbirdroid.syncing.network

import android.util.Log
import com.srilakshmikanthanp.clipbirdroid.packets.ErrorCode
import com.srilakshmikanthanp.clipbirdroid.packets.InvalidRequestPacket
import com.srilakshmikanthanp.clipbirdroid.packets.MalformedPacketException
import com.srilakshmikanthanp.clipbirdroid.packets.NetworkPacket
import com.srilakshmikanthanp.clipbirdroid.packets.UnknownPacketException
import com.srilakshmikanthanp.clipbirdroid.packets.toNetworkPacket
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.netty.handler.codec.ReplayingDecoder
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NetworkPacketEncoder : MessageToByteEncoder<NetworkPacket>() {
  override fun encode(ctx: ChannelHandlerContext, msg: NetworkPacket, out: ByteBuf) {
    out.writeBytes(msg.toByteArray())
  }
}

class PacketDecoder : ReplayingDecoder<Void>() {
  override fun decode(ctx: ChannelHandlerContext, inBuf: ByteBuf, out: MutableList<Any>) {
    val length: Int = inBuf.readInt()
    val rem = length - Int.SIZE_BYTES
    val bytes = inBuf.readBytes(rem)
    val tag = "PacketDecoder"
    val buffer = ByteBuffer.allocate(length)

    buffer.order(ByteOrder.BIG_ENDIAN)
    buffer.putInt(length)
    buffer.put(bytes.array())
    buffer.flip()

    try {
      out.add(buffer.toNetworkPacket())
    } catch (e: MalformedPacketException) {
      ctx.writeAndFlush(InvalidRequestPacket(e.errorCode, e.message.toByteArray()))
      return
    } catch (e: Exception) {
      Log.e(tag, e.message, e)
      return
    } catch (e: UnknownPacketException) {
      inBuf.skipBytes(inBuf.readableBytes())
      val code = ErrorCode.InvalidPacket
      val msg = "Unknown Packet".toByteArray()
      ctx.writeAndFlush(InvalidRequestPacket(code, msg))
    }
  }
}
