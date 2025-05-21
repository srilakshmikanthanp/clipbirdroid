package com.srilakshmikanthanp.clipbirdroid.types.device

import com.srilakshmikanthanp.clipbirdroid.common.types.Device
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.net.InetAddress


/**
 * Device class Test for serialization/deserialization
 */
class DeviceTest {
  @Test fun deviceTest() {
    // Data to be serialized
    val sent = Device(InetAddress.getLocalHost(), 1234, "Test")
    val byteArrayOutputStream = ByteArrayOutputStream()
    val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
    objectOutputStream.writeObject(sent)
    objectOutputStream.flush()

    // Data to be received
    val byteArray = byteArrayOutputStream.toByteArray()
    val objectInputStream = java.io.ObjectInputStream(byteArray.inputStream())
    val received = objectInputStream.readObject() as Device

    // Test
    assert(sent == received)
  }
}
