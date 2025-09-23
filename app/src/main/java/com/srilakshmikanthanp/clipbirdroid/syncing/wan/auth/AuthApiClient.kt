package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiClient {
  @POST("/auth/signin")
  suspend fun signIn(@Body request: BasicAuthRequestDto): Response<AuthToken>
}
