package com.srilakshmikanthanp.clipbirdroid.common.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClientDao
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedClientEntity
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServerDao
import com.srilakshmikanthanp.clipbirdroid.common.trust.TrustedServerEntity

@Database(
  entities = [
    TrustedClientEntity::class,
    TrustedServerEntity::class,
  ],
  version = 1,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun trustedClientDao(): TrustedClientDao
  abstract fun trustedServerDao(): TrustedServerDao
}
