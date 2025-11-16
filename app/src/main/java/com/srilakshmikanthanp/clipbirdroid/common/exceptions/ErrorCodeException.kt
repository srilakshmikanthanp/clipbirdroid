package com.srilakshmikanthanp.clipbirdroid.common.exceptions

class ErrorCodeException(
  val errorCode: Int,
  message: String? = null,
  cause: Throwable? = null
) : Exception(message, cause)
