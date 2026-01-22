package com.srilakshmikanthanp.clipbirdroid.common.trust

import com.srilakshmikanthanp.clipbirdroid.common.extensions.asCertificate
import com.srilakshmikanthanp.clipbirdroid.common.extensions.toPem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrustedClientsRoom(private val trustedClientDao: TrustedClientDao) : TrustedClients {
  override val trustedClients: Flow<List<TrustedClient>> = trustedClientDao.getAll().map { entities -> entities.map { TrustedClient(it.name, it.certificate.asCertificate()) } }

  override suspend fun getTrustedClients(): List<TrustedClient> {
    return trustedClientDao.getAllOneOff().map { TrustedClient(it.name, it.certificate.asCertificate()) }
  }

  override suspend fun isTrustedClient(client: TrustedClient): Boolean {
    val trustedClient = trustedClientDao.getOneOffByName(client.name) ?: return false
    return trustedClient.certificate.asCertificate() == client.certificate
  }

  override suspend fun addTrustedClient(client: TrustedClient) {
    trustedClientDao.insertAll(TrustedClientEntity(name = client.name, certificate = client.certificate.toPem()))
  }

  override suspend fun removeTrustedClient(name: String) {
    trustedClientDao.deleteByName(name)
  }
}
