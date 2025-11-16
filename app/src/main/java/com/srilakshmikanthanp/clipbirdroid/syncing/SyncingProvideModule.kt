package com.srilakshmikanthanp.clipbirdroid.syncing

import com.srilakshmikanthanp.clipbirdroid.ApplicationState
import com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth.BtClientServerBrowser
import com.srilakshmikanthanp.clipbirdroid.syncing.bluetooth.BtServer
import com.srilakshmikanthanp.clipbirdroid.syncing.network.NetClientServerBrowser
import com.srilakshmikanthanp.clipbirdroid.syncing.network.NetServer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class SyncingProvideModule {
  @Provides
  fun provideClientServerBrowser(
    netClientServerBrowser: NetClientServerBrowser,
    btClientServerBrowser: BtClientServerBrowser,
    applicationState: ApplicationState
  ): ClientServerBrowser {
    return if (applicationState.shouldUseBluetooth()) {
      btClientServerBrowser
    } else {
      netClientServerBrowser
    }
  }

  @Provides
  fun provideServer(
    netServer: NetServer,
    btServer: BtServer,
    applicationState: ApplicationState
  ): Server {
    return if (applicationState.shouldUseBluetooth()) {
      btServer
    } else {
      netServer
    }
  }
}
