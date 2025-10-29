package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import javax.inject.Inject

class AuthApiRepository @Inject constructor(
  private val authApiClient: AuthApiClient
) : AuthRepository {
  override suspend fun signIn(
    basicAuthRequestDto: BasicAuthRequestDto
  ): AuthToken {
    val response = authApiClient.signIn(basicAuthRequestDto)
    if (!response.isSuccessful && response.code() == 401) throw AuthException("Invalid credentials")
    if (!response.isSuccessful) throw Exception("Sign in failed with code: ${response.code()}")
    return response.body() ?: throw Exception("Sign in failed with empty body")
  }
}
