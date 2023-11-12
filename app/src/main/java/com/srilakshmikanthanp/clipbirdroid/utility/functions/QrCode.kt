package com.srilakshmikanthanp.clipbirdroid.utility.functions

import android.graphics.Bitmap
import android.util.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.MultiFormatWriter
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer

/**
 * Function to generate QR code from string using zxing library
 */
fun encode(value: String, color: Color, size: Size = Size(800, 800)): Bitmap? {
  val matrix: BitMatrix = try {
    MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, size.width, size.height)
  } catch (e: Exception) {
    return null
  }

  val bmp = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)

  for (x in 0 until matrix.width) {
    for (y in 0 until matrix.height) {
      bmp.setPixel(x, y, if (matrix[x, y]) color.toArgb() else Color.Transparent.toArgb())
    }
  }

  return bmp
}

/**
 * Function to generate String from QR code using zxing library
 */
fun decode(bitmap: Bitmap): String? {
  val intArray = IntArray(bitmap.width * bitmap.height)
  val width = bitmap.width
  val height = bitmap.height

  bitmap.getPixels(intArray, 0, width, 0, 0, width, height)

  val source = RGBLuminanceSource(width, height, intArray)
  val bitmapBinary = BinaryBitmap(HybridBinarizer(source))

  return try {
    MultiFormatReader().decode(bitmapBinary).text
  } catch (e: Exception) {
    null
  }
}
