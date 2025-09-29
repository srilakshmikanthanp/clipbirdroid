package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
  private val storage: Storage,
) {
  val tokenFlow = storage.hubAuthTokenFlow

  fun setSession(token: AuthToken) {
    storage.setHubAuthToken(token)
  }

  fun signOut() {
    storage.clearHubHostDevice()
    storage.clearHubAuthToken()
  }

  fun token(): AuthToken? {
    return storage.getHubAuthToken()
  }

  fun isSignedIn(): Boolean {
    return storage.hasHubAuthToken()
  }
}
