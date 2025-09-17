package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.common.extensions.toHubMessage
import com.srilakshmikanthanp.clipbirdroid.common.extensions.toJson
import com.srilakshmikanthanp.clipbirdroid.constants.getClipbirdWebsocketUrl
import jakarta.inject.Inject
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class HubWebSocketListener(private val hubWebsocket: HubWebsocket): WebSocketListener() {
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

class HubWebsocket(hubHostDevice: HubHostDevice) : AbstractHub(hubHostDevice) {
  @Inject lateinit var hubMessageHandler: HubMessageHandler
  val request = okhttp3.Request.Builder().url("${getClipbirdWebsocketUrl()}/hub").build()
  val client = OkHttpClient()
  val listener = HubWebSocketListener(this)
  val webSocket : WebSocket = client.newWebSocket(request, listener)

  fun disconnect() {
    webSocket.close(1000, "Client closed connection")
  }

  override fun sendMessage(message: HubMessage<*>) {
    webSocket.send(message.toJson())
  }
}
