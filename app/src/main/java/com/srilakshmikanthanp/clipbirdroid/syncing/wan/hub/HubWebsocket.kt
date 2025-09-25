package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.common.extensions.toHubMessage
import com.srilakshmikanthanp.clipbirdroid.common.extensions.toJson
import com.srilakshmikanthanp.clipbirdroid.constants.getClipbirdWebsocketUrl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class HubWebSocketListener @AssistedInject constructor(
  @Assisted private val hubWebsocket: HubWebsocket
): WebSocketListener() {
  @Inject lateinit var hubMessageHandler: HubMessageHandler

  override fun onMessage(webSocket: WebSocket, text: String) {
    hubMessageHandler.handle(hubWebsocket, text.toHubMessage())
  }

  override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    hubWebsocket.getListeners().forEach { it.onDisconnected() }
  }

  override fun onFailure(
    webSocket: WebSocket,
    t: Throwable,
    response: Response?
  ) {
    hubWebsocket.getListeners().forEach { it.onErrorOccurred(t) }
  }
}

class HubWebsocket @AssistedInject constructor(
  @Assisted hubHostDevice: HubHostDevice,
  hubWebSocketListenerFactory: HubWebSocketListenerFactory,
  private val client: OkHttpClient,
) : AbstractHub(hubHostDevice) {
  val listener = hubWebSocketListenerFactory.create(this)
  var webSocket : WebSocket? = null

  companion object {
    private const val X_DEVICE_ID = "X-Device-ID"
  }

  fun connect() {
    val request = okhttp3.Request.Builder().url("${getClipbirdWebsocketUrl()}/hub")
      .header(X_DEVICE_ID, getHubHostDevice().id)
      .build()
    webSocket = client.newWebSocket(request, listener)
  }

  fun disconnect() {
    if (webSocket == null) {
      throw IllegalStateException("WebSocket is not connected")
    } else {
      webSocket?.close(1000, "Client closed connection")
      webSocket = null
    }
  }

  override fun sendMessage(message: HubMessage<*>) {
    if (webSocket == null) {
      throw IllegalStateException("WebSocket is not connected")
    } else {
      webSocket?.send(message.toJson())
    }
  }
}
