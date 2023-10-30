package com.srilakshmikanthanp.clipbirdroid.constant

import android.os.Build
import android.provider.Settings
import com.srilakshmikanthanp.clipbirdroid.BuildConfig
import com.srilakshmikanthanp.clipbirdroid.ui.gui.MainApp

/**
 * @brief Get the Application Version
 * @return string
 */
fun appMajorVersion(): String {
  return BuildConfig.VERSION_NAME.split(".")[0]
}

/**
 * @brief Get the Application Version
 * @return string
 */
fun appMinorVersion(): String {
  return BuildConfig.VERSION_NAME.split(".")[1]
}

/**
 * @brief Get the Application Version
 * @return string
 */
fun appPatchVersion(): String {
  return BuildConfig.VERSION_NAME.split(".")[2]
}

/**
 * @brief Get the Application Name
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
fun appMdnsServiceName(): String {
  return Settings.Secure.getString(
    MainApp.getContext().contentResolver,
    "device_name"
  ) ?: Build.MODEL
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
