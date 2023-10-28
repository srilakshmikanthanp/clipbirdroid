package com.srilakshmikanthanp.clipbirdroid.types.variant

import com.srilakshmikanthanp.clipbirdroid.intface.OnVariantDataDestroyHandler

/**
 * A Helper class to hold two different types of data
 * but only one at a time.
 */
class Variant {
  // List on On Data Destroy Handlers
  private val onDataDestroyHandlers: MutableList<OnVariantDataDestroyHandler> = mutableListOf()

  // add On Data Destroy Handler
  fun addOnDataDestroyHandler(handler: OnVariantDataDestroyHandler) {
    onDataDestroyHandlers.add(handler)
  }

  // remove On Data Destroy Handler
  fun removeOnDataDestroyHandler(handler: OnVariantDataDestroyHandler) {
    onDataDestroyHandlers.remove(handler)
  }

  /**
   * Notify all the On Data Destroy Handlers
   */
  private fun notifyOnDataDestroyHandlers(obj: Any) {
    onDataDestroyHandlers.forEach { it.onVariantDataDestroy(obj) }
  }

  // Instance of data to be held
  private var obj: Any? = null

  /**
   * Set the data to be held
   */
  fun set(obj: Any): Any {
    // if data is already held
    if (hasObject()) {
      notifyOnDataDestroyHandlers(this.obj!!)
    }

    // set the data
    this.obj = obj

    // return the data
    return obj
  }

  /**
   * is data hold or not
   */
  fun hasObject(): Boolean {
    return obj != null
  }

  /**
   * Get the data held
   */
  fun get(): Any? {
    return obj
  }

  /**
   * Get the data held
   */
  fun<T> holds(type: Class<T>): Boolean {
    return type.isInstance(obj)
  }
}
