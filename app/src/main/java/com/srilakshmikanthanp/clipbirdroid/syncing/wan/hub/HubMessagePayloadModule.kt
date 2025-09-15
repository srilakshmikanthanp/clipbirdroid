package com.srilakshmikanthanp.clipbirdroid.syncing.wan.hub

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module()
@InstallIn(SingletonComponent::class)
abstract class HubMessagePayloadModule {
  @Binds
  @IntoSet
  abstract fun bindHubMessageClipboardDispatchPayloadHandler(
    handler: HubMessageClipboardDispatchPayloadHandler
  ): HubMessagePayloadHandler<HubMessageClipboardDispatchPayload>

  @Binds
  @IntoSet
  abstract fun bindHubMessageDeviceAddedPayloadHandler(
    handler: HubMessageDeviceAddedPayloadHandler
  ): HubMessagePayloadHandler<HubMessageDeviceAddedPayload>

  @Binds
  @IntoSet
  abstract fun bindHubMessageDeviceRemovedPayloadHandler(
    handler: HubMessageDeviceRemovedPayloadHandler
  ): HubMessagePayloadHandler<HubMessageDeviceRemovedPayload>

  @Binds
  @IntoSet
  abstract fun bindHubMessageDeviceUpdatedPayloadHandler(
    handler: HubMessageDeviceUpdatedPayloadHandler
  ): HubMessagePayloadHandler<HubMessageDeviceUpdatedPayload>

  @Binds
  @IntoSet
  abstract fun bindHubMessageNonceChallengeCompletedPayloadHandler(
    handler: HubMessageNonceChallengeCompletedPayloadHandler
  ): HubMessagePayloadHandler<HubMessageNonceChallengeCompletedPayload>

  @Binds
  @IntoSet
  abstract fun bindHubMessageNonceChallengeRequestPayloadHandler(
    handler: HubMessageNonceChallengeRequestPayloadHandler
  ): HubMessagePayloadHandler<HubMessageNonceChallengeRequestPayload>
}
