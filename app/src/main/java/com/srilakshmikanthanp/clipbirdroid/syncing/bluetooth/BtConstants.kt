package com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth

import com.srilakshmikanthanp.clipbirdroid.BuildConfig
import java.util.UUID

object BtConstants {
  val serviceUuid: UUID = UUID.fromString(BuildConfig.APP_UUID)
}
