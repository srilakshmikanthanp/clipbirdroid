package com.srilakshmikanthanp.clipbirdroid.common.functions

import org.bouncycastle.util.io.pem.PemReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

private fun encryptAESKey(aesKey: ByteArray, rsaPublicKey: ByteArray): ByteArray {
  val keyString = with(PemReader(rsaPublicKey.inputStream().reader())) { readPemObject().content }
  val rsa = KeyFactory.getInstance("RSA")
  val publicKey = rsa.generatePublic(java.security.spec.X509EncodedKeySpec(keyString))
  val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
  cipher.init(Cipher.ENCRYPT_MODE, publicKey)
  return cipher.doFinal(aesKey)
}

private fun decryptAESKey(encryptedAesKey: ByteArray, rsaPrivateKey: ByteArray): ByteArray {
  val keyString = with(PemReader(rsaPrivateKey.inputStream().reader())) { readPemObject().content }
  val rsa = KeyFactory.getInstance("RSA")
  val privateKey = rsa.generatePrivate(PKCS8EncodedKeySpec(keyString))
  val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
  cipher.init(Cipher.DECRYPT_MODE, privateKey)
  return cipher.doFinal(encryptedAesKey)
}

private class BlockReader(private val data: ByteArray) {
  private var offset = 0

  fun nextBlock(): ByteArray {
    if (offset + Int.SIZE_BYTES > data.size) {
      throw RuntimeException("Invalid data format (missing size field)")
    }

    val size = ByteBuffer.wrap(data, offset, Int.SIZE_BYTES)
      .order(ByteOrder.BIG_ENDIAN)
      .int
    offset += Int.SIZE_BYTES

    if (size < 0) {
      throw RuntimeException("Invalid block size (negative: $size)")
    }

    if (offset + size > data.size) {
      throw RuntimeException("Invalid data format (block truncated)")
    }

    val block = data.copyOfRange(offset, offset + size)
    offset += size
    return block
  }

  fun hasMore(): Boolean {
    return offset < data.size
  }
}

private const val AES_KEY_SIZE = 32 // 256 bits
private const val IV_SIZE = 12 // 96 bits
private const val TAG_SIZE = 16 // 128 bits

fun encrypt(data: ByteArray, key: ByteArray): ByteArray {
  val aesKey = generateAESKey(bits = AES_KEY_SIZE * 8)
  val iv = generateIV(bits = IV_SIZE * 8)

  val cipher = Cipher.getInstance("AES/GCM/NoPadding")
  val secretKey = SecretKeySpec(aesKey, "AES")
  val gcmSpec = GCMParameterSpec(TAG_SIZE * 8, iv)

  cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

  val taggedCiphertext = cipher.doFinal(data)
  val tag = taggedCiphertext.takeLast(TAG_SIZE).toByteArray()
  val actualCiphertext = taggedCiphertext.dropLast(TAG_SIZE).toByteArray()

  val encryptedAesKey = encryptAESKey(aesKey, key)

  val result = mutableListOf<Byte>()
  result.addAll(ByteBuffer.allocate(4).putInt(encryptedAesKey.size).array().toList())
  result.addAll(encryptedAesKey.toList())
  result.addAll(ByteBuffer.allocate(4).putInt(iv.size).array().toList())
  result.addAll(iv.toList())
  result.addAll(ByteBuffer.allocate(4).putInt(tag.size).array().toList())
  result.addAll(tag.toList())
  result.addAll(ByteBuffer.allocate(4).putInt(actualCiphertext.size).array().toList())
  result.addAll(actualCiphertext.toList())
  return result.toByteArray()
}

fun sign(data: ByteArray, key: ByteArray): ByteArray {
  val keyString = with(PemReader(key.inputStream().reader())) { readPemObject().content }
  val rsa = KeyFactory.getInstance("RSA")
  val privateKey = rsa.generatePrivate(PKCS8EncodedKeySpec(keyString))
  val signature = Signature.getInstance("SHA256withRSA")
  signature.initSign(privateKey)
  signature.update(data)
  return signature.sign()
}

fun decrypt(data: ByteArray, key: ByteArray): ByteArray {
  val reader = BlockReader(data)
  val encryptedAesKey = reader.nextBlock()
  val iv = reader.nextBlock()
  val tag = reader.nextBlock()
  val ciphertext = reader.nextBlock()

  if (reader.hasMore()) {
    throw RuntimeException("Invalid data format (extra data found)")
  }

  val aesKey = decryptAESKey(encryptedAesKey, key)

  val cipher = Cipher.getInstance("AES/GCM/NoPadding")
  val secretKey = SecretKeySpec(aesKey, "AES")
  val gcmSpec = GCMParameterSpec(TAG_SIZE * 8, iv)

  cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

  val taggedCiphertext = ciphertext.plus(tag)
  return cipher.doFinal(taggedCiphertext)
}
