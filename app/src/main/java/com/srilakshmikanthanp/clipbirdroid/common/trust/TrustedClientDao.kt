package com.srilakshmikanthanp.clipbirdroid.common.trust

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustedClientDao {
  @Query("SELECT * FROM trusted_client WHERE name = :name")
  suspend fun getOneOffByName(name: String): TrustedClientEntity?

  @Query("SELECT * FROM trusted_client")
  fun getAll(): Flow<List<TrustedClientEntity>>

  @Query("SELECT * FROM trusted_client")
  suspend fun getAllOneOff(): List<TrustedClientEntity>

  @Insert
  suspend fun insertAll(vararg clients: TrustedClientEntity)

  @Query("DELETE FROM trusted_client WHERE name = :name")
  suspend fun deleteByName(name: String)
}
