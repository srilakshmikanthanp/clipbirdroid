package com.srilakshmikanthanp.clipbirdroid

import android.content.Context
import com.srilakshmikanthanp.clipbirdroid.utility.generateSslConfig
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
  @Singleton
  @Provides
  fun provideSslConfig(sslConfigProvider: SslConfigProvider): SSLConfig {
    return sslConfigProvider.getSslConfig()
  }

  @Provides
  fun provideClipbird(@ApplicationContext context: Context): Clipbird {
    return context.applicationContext as Clipbird
  }

  @Provides
  @Singleton
  fun provideApplicationScope(): CoroutineScope = MainScope()
}
