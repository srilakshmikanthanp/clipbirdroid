package com.srilakshmikanthanp.clipbirdroid.common.variant

/**
 * A Helper class to hold two different types of data
 * but only one at a time.
 */
class Variant {
  // Auxiliary datum to be held
  private var aux = HashMap<String, Any>()

  // Instance to be held
  private var obj: Any? = null

  /**
   * Set the data to be held
   */
  fun set(obj: Any?): Any? {
    this.obj = obj
    aux.clear()
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
   * Add auxiliary data
   */
  fun addAux(key: String, value: Any): Any {
    aux[key] = value
    return value
  }

  /**
   * Get the auxiliary data
   */
  fun getAux(key: String): Any {
    return aux[key] ?: throw Exception("No such key")
  }

  /**
   * Get the data held
   */
  fun<T> holds(type: Class<T>): Boolean {
    return type.isInstance(obj)
  }
}
