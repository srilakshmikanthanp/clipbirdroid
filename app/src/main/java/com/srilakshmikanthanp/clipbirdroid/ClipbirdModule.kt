package com.srilakshmikanthanp.clipbirdroid

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.functions.generateX509Certificate
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.constants.appCertExpiryInterval
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
import com.srilakshmikanthanp.clipbirdroid.storage.Storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x500.style.IETFUtils
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder
import java.security.PrivateKey
import java.security.cert.X509Certificate
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClipbirdModule {
  private fun getOldSslConfig(storage: Storage, context: Context): Pair<PrivateKey, X509Certificate> {
    val key = storage.getHostKey()!!
    val cert = storage.getHostCertificate()!!
    val x500Name = JcaX509CertificateHolder(cert).subject
    val cn = x500Name.getRDNs(BCStyle.CN)[0]
    val name = IETFUtils.valueToString(cn.first.value)
    val deviceName = appMdnsServiceName(context)
    if (name != deviceName) return getNewSslConfig(storage, context)
    return if (cert.notAfter.time - System.currentTimeMillis() < appCertExpiryInterval()) {
      val sslConfig = generateX509Certificate(context)
      storage.setHostKey(sslConfig.first)
      storage.setHostCertificate(sslConfig.second)
      sslConfig
    } else {
      Pair(key, cert)
    }
  }

  private fun getNewSslConfig(storage: Storage, context: Context): Pair<PrivateKey, X509Certificate> {
    val sslConfig = generateX509Certificate(context)
    storage.setHostKey(sslConfig.first)
    storage.setHostCertificate(sslConfig.second)
    return sslConfig
  }

  private fun getSslConfig(storage: Storage, context: Context): Pair<PrivateKey, X509Certificate> {
    return if (storage.hasHostKey() && storage.hasHostCert()) {
      getOldSslConfig(storage, context)
    } else {
      getNewSslConfig(storage, context)
    }
  }

  @Provides
  fun provideSslConfig(storage: Storage, @ApplicationContext context: Context): SSLConfig {
    val pair = getSslConfig(storage, context)
    return SSLConfig(pair.first, pair.second)
  }

  @Provides
  fun provideClipbird(@ApplicationContext context: Context): Clipbird {
    return context.applicationContext as Clipbird
  }

  @Provides
  @Singleton
  fun provideApplicationScope(): CoroutineScope = MainScope()
}
