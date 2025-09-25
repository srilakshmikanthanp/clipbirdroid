package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

interface AuthRepository {
  suspend fun signIn(basicAuthRequestDto: BasicAuthRequestDto): AuthToken
}
