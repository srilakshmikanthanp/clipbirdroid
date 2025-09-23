package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AuthApiRepository @Inject constructor(
  private val authApiClient: AuthApiClient
) : AuthRepository {
  override fun signIn(
    basicAuthRequestDto: BasicAuthRequestDto
  ): Flow<AuthToken> = flow {
    val response = authApiClient.signIn(basicAuthRequestDto)
    if (!response.isSuccessful) throw Exception("Sign in failed with code: ${response.code()}")
    val body = response.body() ?: throw Exception("Sign in failed with empty body")
    emit(body)
  }
}
