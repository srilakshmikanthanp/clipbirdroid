package com.srilakshmikanthanp.clipbirdroid.common.utility

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

fun ByteArray.toPNG(): ByteArray? {
  val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
  val stream = ByteArrayOutputStream()

  if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
    return null
  }

  return stream.toByteArray()
}
