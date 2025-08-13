package com.srilakshmikanthanp.clipbirdroid.syncing

interface Synchronizer {
  fun synchronize(items: List<Pair<String, ByteArray>>)
}
