package com.srilakshmikanthanp.clipbirdroid.common.trust

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trusted_client")
class TrustedClientEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val name: String,
  val certificate: String
)
