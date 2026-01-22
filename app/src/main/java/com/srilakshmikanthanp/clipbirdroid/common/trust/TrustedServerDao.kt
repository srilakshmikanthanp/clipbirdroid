package com.srilakshmikanthanp.clipbirdroid.common.trust

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustedServerDao {
  @Query("SELECT * FROM trusted_server WHERE name = :name")
  suspend fun getOneOffByName(name: String): TrustedServerEntity?

  @Query("SELECT * FROM trusted_server")
  fun getAll(): Flow<List<TrustedServerEntity>>

  @Query("SELECT * FROM trusted_server")
  suspend fun getAllOneOff(): List<TrustedServerEntity>

  @Insert
  suspend fun insertAll(vararg clients: TrustedServerEntity)

  @Query("DELETE FROM trusted_server WHERE name = :name")
  suspend fun deleteByName(name: String)
}
