package com.srilakshmikanthanp.clipbirdroid.common.okhttp

import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthErrorInterceptor @Inject constructor(
  private val sessionManager: SessionManager
) : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val response = chain.proceed(chain.request())

    if (response.code == 401) {
      sessionManager.signOut()
    }

    return response
  }
}
