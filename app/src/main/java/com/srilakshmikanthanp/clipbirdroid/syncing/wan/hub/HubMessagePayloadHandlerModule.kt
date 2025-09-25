package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module()
@InstallIn(SingletonComponent::class)
abstract class HubMessagePayloadHandlerModule {
  @Binds
  @IntoSet
  abstract fun bindHubMessageClipboardDispatchPayloadHandler(
    handler: HubMessageClipboardDispatchPayloadHandler
  ): HubMessagePayloadHandler<*>

  @Binds
  @IntoSet
  abstract fun bindHubMessageDeviceAddedPayloadHandler(
    handler: HubMessageDeviceAddedPayloadHandler
  ): HubMessagePayloadHandler<*>

  @Binds
  @IntoSet
  abstract fun bindHubMessageDeviceRemovedPayloadHandler(
    handler: HubMessageDeviceRemovedPayloadHandler
  ): HubMessagePayloadHandler<*>

  @Binds
  @IntoSet
  abstract fun bindHubMessageDevicesPayloadHandler(
    handler: HubMessageDevicesPayloadHandler
  ): HubMessagePayloadHandler<*>

  @Binds
  @IntoSet
  abstract fun bindHubMessageDeviceUpdatedPayloadHandler(
    handler: HubMessageDeviceUpdatedPayloadHandler
  ): HubMessagePayloadHandler<*>

  @Binds
  @IntoSet
  abstract fun bindHubMessageNonceChallengeCompletedPayloadHandler(
    handler: HubMessageNonceChallengeCompletedPayloadHandler
  ): HubMessagePayloadHandler<*>

  @Binds
  @IntoSet
  abstract fun bindHubMessageNonceChallengeRequestPayloadHandler(
    handler: HubMessageNonceChallengeRequestPayloadHandler
  ): HubMessagePayloadHandler<*>
}
