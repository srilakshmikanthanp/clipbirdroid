package com.srilakshmikanthanp.clipbirdroid.ui.gui

import android.app.Application
import android.content.Context
import java.lang.ref.WeakReference

class MainApp : Application() {
  companion object {
    // context of the application as weak reference
    private lateinit var context: WeakReference<Context>

    // init the context
    fun init(context: Context) {
      this.context = WeakReference(context)
    }

    // Get the context of the application
    fun getContext(): Context = context.get()!!
  }

  /**
   * @brief Called when the application is starting, before
   * any other application objects have been created.
   */
  override fun onCreate() {
    super.onCreate().also {  init(this) }
  }
}
