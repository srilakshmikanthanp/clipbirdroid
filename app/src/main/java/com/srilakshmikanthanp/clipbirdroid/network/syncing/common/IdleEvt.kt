package com.srilakshmikanthanp.clipbirdroid.network.syncing.common

import com.srilakshmikanthanp.clipbirdroid.network.packets.PingPacket
import com.srilakshmikanthanp.clipbirdroid.types.enums.PingType
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

/**
 * On Idle Handler for Handling Ping and Pong
 */
class IdleEvt : ChannelDuplexHandler() {
  override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any?) {
    if (evt !is IdleStateEvent) return super.userEventTriggered(ctx, evt)

    if (evt.state() == IdleState.WRITER_IDLE) {
      ctx.writeAndFlush(PingPacket(PingType.Ping))
    }

    if (evt.state() == IdleState.READER_IDLE) {
      ctx.close()
    }
  }
}
