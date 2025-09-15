package com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module()
@InstallIn(SingletonComponent::class)
abstract class AuthModuleBinder {
  abstract fun bindAuthRepository(
    authApiRepository: AuthApiRepository
  ): AuthRepository
}
