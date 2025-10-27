package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import com.srilakshmikanthanp.clipbirdroid.common.extensions.toHubMessage
import com.srilakshmikanthanp.clipbirdroid.common.extensions.toJson
import com.srilakshmikanthanp.clipbirdroid.constants.getClipbirdWebsocketUrl
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.math.pow

class HubWebsocket @AssistedInject constructor(
  @Assisted hubHostDevice: HubHostDevice,
  @Assisted coroutineScope: CoroutineScope,
  private val client: OkHttpClient,
  private val hubMessageHandler: HubMessageHandler,
) : AbstractHub(hubHostDevice) {
  private inner class Listener: WebSocketListener() {
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
      this@HubWebsocket.webSocket = null
      getListeners().forEach { it.onErrorOccurred(t) }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
      getListeners().forEach { it.onOpened() }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
      hubMessageHandler.handle(this@HubWebsocket, text.toHubMessage())
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
      this@HubWebsocket.webSocket = null
      getListeners().forEach { it.onDisconnected(code, reason) }
    }
  }

  private val hubUrl = "${getClipbirdWebsocketUrl()}/hub"
  private val listener = Listener()
  private val scope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())
  private var webSocket : WebSocket? = null

  companion object {
    private const val NORMAL_CLOSURE_STATUS = 1000
    private const val X_DEVICE_ID = "X-Device-ID"
  }

  private fun makeConnection() {
    val deviceId = getHubHostDevice().id
    val request = Request.Builder().url(hubUrl).header(X_DEVICE_ID, deviceId).build()
    getListeners().forEach { it.onConnecting() }
    webSocket = client.newWebSocket(request, listener)
  }

  fun connect() {
    if (isConnected()) throw RuntimeException("WebSocket is already connected")
    this.makeConnection()
  }

  fun isConnected(): Boolean {
    return webSocket != null
  }

  fun disconnect() {
    val ws = requireNotNull(webSocket) { "WebSocket is not connected" }
    ws.close(NORMAL_CLOSURE_STATUS, "Client closed connection")
    webSocket = null
  }

  override fun sendMessage(message: HubMessage<*>) {
    val ws = requireNotNull(webSocket) { "WebSocket is not connected" }
    ws.send(message.toJson())
  }
}
