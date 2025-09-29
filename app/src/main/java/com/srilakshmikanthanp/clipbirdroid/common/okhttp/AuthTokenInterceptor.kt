package com.srilakshmikanthanp.clipbirdroid.common.okhttp

import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthTokenInterceptor @Inject constructor(
  private val sessionManager: SessionManager
): Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val jwtToken = sessionManager.token()
    val requestBuilder = chain.request().newBuilder()
    if (jwtToken != null) {
      requestBuilder.addHeader("Authorization", "Bearer ${jwtToken.token}")
    }
    return chain.proceed(requestBuilder.build())
  }
}
