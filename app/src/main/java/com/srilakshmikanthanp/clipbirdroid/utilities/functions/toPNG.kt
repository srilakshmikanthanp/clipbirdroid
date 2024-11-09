package com.srilakshmikanthanp.clipbirdroid.utilities.functions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

/**
 * Converts the ByteArray to PNG where the ByteArray is the image
 */
fun toPNG(bytes: ByteArray): ByteArray? {
  // Decode the ByteArray to Bitmap
  val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
  val stream = ByteArrayOutputStream()

  // Try to compress the image if it fails return null
  if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
    return null
  }

  // return the ByteArray
  return stream.toByteArray()
}
