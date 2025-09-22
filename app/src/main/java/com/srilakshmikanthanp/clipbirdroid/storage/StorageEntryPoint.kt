package com.srilakshmikanthanp.clipbirdroid.storage

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface StorageEntryPoint {
  fun storage(): Storage
}
