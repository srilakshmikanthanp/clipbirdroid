package com.srilakshmikanthanp.clipbirdroid.constant

import android.content.Context
import android.os.Build
import android.provider.Settings.Secure.getString
import com.srilakshmikanthanp.clipbirdroid.BuildConfig

/**
 * @brief Get the Clipbird Version
 * @return string
 */
fun appMajorVersion(): String {
  return BuildConfig.VERSION_NAME.split(".")[0]
}

/**
 * @brief Get the Clipbird Version
 * @return string
 */
fun appMinorVersion(): String {
  return BuildConfig.VERSION_NAME.split(".")[1]
}

/**
 * @brief Get the Clipbird Version
 * @return string
 */
fun appPatchVersion(): String {
  return BuildConfig.VERSION_NAME.split(".")[2]
}

/**
 * App Version
 */
fun appVersion(): String {
  return BuildConfig.VERSION_NAME
}

/**
 * @brief Get the Clipbird Name
 * @return string
 */
fun appName(): String {
  return BuildConfig.APP_NAME
}

/**
 * @brief Get the App Home Page
 * @return string
 */
fun appHomePage(): String {
  return BuildConfig.APP_HOME
}

/**
 * @brief Get the App ISSUES Page
 * @return string
 */
fun appIssuesPage(): String {
  return BuildConfig.APP_ISSUE
}

/**
 * @brief Get the MDns Service Name
 * @return string Device name
 */
fun appMdnsServiceName(context: Context): String {
  return getString(context.contentResolver, "bluetooth_name") ?: Build.MODEL
}

/**
 * @brief Get the MDns Service Type
 *
 * @return string
 */
fun appMdnsServiceType(): String {
  return "_clipbird._tcp"
}

/**
 * @brief Get the App Org Name object
 *
 * @return string
 */
fun appOrgName(): String {
  return BuildConfig.APP_ORG
}

/**
 * @brief Get the App File Provider
 */
fun appProvider(): String {
  return BuildConfig.APPLICATION_ID + ".provider"
}

/**
 * Max Number of Clipboard History
 */
fun appMaxHistory(): Int {
  return 10
}

/**
 * Used to get the certificate expiry time should not be less than 2 months
 */
fun appCertExpiryInterval(): Long {
  return 60L * 60L * 24L * 60L * 1000L
}

/**
 * Max idle read time seconds
 */
fun appMaxIdleReadTime(): Int {
  return 60
}

/**
 * Max idle write time seconds
 */
fun appMaxIdleWriteTime(): Int {
  return 10
}
