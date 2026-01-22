package com.srilakshmikanthanp.clipbirdroid.common.trust

import com.srilakshmikanthanp.clipbirdroid.common.extensions.asCertificate
import com.srilakshmikanthanp.clipbirdroid.common.extensions.toPem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrustedServersRoom(private val trustedServerDao: TrustedServerDao) : TrustedServers {
  override val trustedServers: Flow<List<TrustedServer>> = trustedServerDao.getAll().map { entities -> entities.map { TrustedServer(it.name, it.certificate.asCertificate()) } }

  override suspend fun getTrustedServers(): List<TrustedServer> {
    return trustedServerDao.getAllOneOff().map { TrustedServer(it.name, it.certificate.asCertificate()) }
  }

  override suspend fun isTrustedServer(server: TrustedServer): Boolean {
    val trustedServer = trustedServerDao.getOneOffByName(server.name) ?: return false
    return trustedServer.certificate.asCertificate() == server.certificate
  }

  override suspend fun hasTrustedServer(name: String): Boolean {
    return trustedServerDao.getOneOffByName(name) != null
  }

  override suspend fun addTrustedServer(server: TrustedServer) {
    trustedServerDao.insertAll(TrustedServerEntity(name = server.name, certificate = server.certificate.toPem()))
  }

  override suspend fun removeTrustedServer(name: String) {
    trustedServerDao.deleteByName(name)
  }
}
