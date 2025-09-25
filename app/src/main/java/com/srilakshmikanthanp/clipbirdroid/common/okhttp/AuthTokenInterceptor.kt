package com.srilakshmikanthanp.clipbirdroid.common.okhttp

import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthTokenInterceptor @Inject constructor(
  private val storage: Storage
): Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val jwtToken = storage.getHubAuthToken()
    val requestBuilder = chain.request().newBuilder()
    if (jwtToken != null) {
      requestBuilder.addHeader("Authorization", "Bearer ${jwtToken.token}")
    }
    return chain.proceed(requestBuilder.build())
  }
}
