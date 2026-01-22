package com.srilakshmikanthanp.clipbirdroid.common.database

import android.content.Context
import androidx.room.Room.databaseBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppDatabaseProvideModule {
  @Provides
  @Singleton
  fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
    return databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME).build()
  }

  companion object {
    private const val DB_NAME = "clipbird-db"
  }
}
