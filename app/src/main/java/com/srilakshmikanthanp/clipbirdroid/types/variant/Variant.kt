package com.srilakshmikanthanp.clipbirdroid.types.variant

/**
 * A Helper class to hold two different types of data
 * but only one at a time.
 */
class Variant {
  // Instance of data to be held
  private var obj: AutoCloseable? = null

  /**
   * Set the data to be held
   */
  fun set(obj: AutoCloseable): Any {
    // if data is already held
    if (hasObject()) {
      this.obj!!.close()
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
  fun get(): AutoCloseable? {
    return obj
  }

  /**
   * Get the data held
   */
  fun<T> holds(type: Class<T>): Boolean {
    return type.isInstance(obj)
  }
}
