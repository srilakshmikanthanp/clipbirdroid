package com.srilakshmikanthanp.clipbirdroid

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.common.utility.generateSslConfig
import com.srilakshmikanthanp.clipbirdroid.common.types.SSLConfig
import com.srilakshmikanthanp.clipbirdroid.constants.appCertExpiryInterval
import com.srilakshmikanthanp.clipbirdroid.constants.appMdnsServiceName
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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ClipbirdModule {
  private fun getSslConfig(applicationState: ApplicationState, context: Context): SSLConfig {
    val sslConfig = applicationState.getHostSslConfig() ?: generateSslConfig(context)
    val currentTime = System.currentTimeMillis()
    if (sslConfig.certificate.notAfter.time - currentTime < appCertExpiryInterval()) return generateSslConfig(context)
    val x500Name = JcaX509CertificateHolder(sslConfig.certificate).subject
    val cn = x500Name.getRDNs(BCStyle.CN)[0]
    val name = IETFUtils.valueToString(cn.first.value)
    val deviceName = appMdnsServiceName(context)
    if (name != deviceName) return generateSslConfig(context)
    applicationState.setHostSslConfig(sslConfig)
    return sslConfig
  }

  @Singleton
  @Provides
  fun provideSslConfig(applicationState: ApplicationState, @ApplicationContext context: Context): SSLConfig {
    return getSslConfig(applicationState, context)
  }

  @Provides
  fun provideClipbird(@ApplicationContext context: Context): Clipbird {
    return context.applicationContext as Clipbird
  }

  @Provides
  @Singleton
  fun provideApplicationScope(): CoroutineScope = MainScope()
}
