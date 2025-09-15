package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
  fun signIn(basicAuthRequestDto: BasicAuthRequestDto): Flow<AuthToken>
}
