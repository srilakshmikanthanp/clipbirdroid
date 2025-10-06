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

class HubWebsocket @AssistedInject constructor(
  @Assisted hubHostDevice: HubHostDevice,
  private val client: OkHttpClient,
  private val hubMessageHandler: HubMessageHandler,
) : AbstractHub(hubHostDevice) {
  private val webSocketListener = object : WebSocketListener() {
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
      this@HubWebsocket.webSocket = null
      getListeners().forEach {
        it.onErrorOccurred(t)
      }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
      hubMessageHandler.handle(this@HubWebsocket, text.toHubMessage())
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
      this@HubWebsocket.webSocket = null
      getListeners().forEach {
        it.onDisconnected(code, reason)
      }
    }
  }

  private var webSocket : WebSocket? = null

  companion object {
    private const val NORMAL_CLOSURE_STATUS = 1000
    private const val X_DEVICE_ID = "X-Device-ID"
  }

  fun connect() {
    val request = okhttp3.Request.Builder().url("${getClipbirdWebsocketUrl()}/hub")
      .header(X_DEVICE_ID, getHubHostDevice().id)
      .build()
    webSocket = client.newWebSocket(request, webSocketListener)
  }

  fun isConnected(): Boolean {
    return webSocket != null
  }

  fun disconnect() {
    if (webSocket == null) {
      throw IllegalStateException("WebSocket is not connected")
    } else {
      webSocket?.close(NORMAL_CLOSURE_STATUS, "Client closed connection")
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
