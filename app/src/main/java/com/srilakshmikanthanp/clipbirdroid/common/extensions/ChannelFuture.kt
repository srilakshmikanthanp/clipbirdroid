package com.srilakshmikanthanp.clipbirdroid.common.extensions

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun ChannelFuture.awaitSuspend(): Channel = suspendCancellableCoroutine { continuation ->
  this.addListener(GenericFutureListener<ChannelFuture> {
    if (it.isSuccess) {
      continuation.resume(it.channel())
    } else {
      continuation.resumeWithException(it.cause())
    }
  })

  continuation.invokeOnCancellation {
    this.cancel(true)
  }
}
