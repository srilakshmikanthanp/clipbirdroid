package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import java.net.InetAddress

/**
 * HostList Composable to Represent the List of Hosts
 */
@Composable
fun HostList(
  devicesActionable: List<DeviceActionable>,
  onAction: (DeviceActionable) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(contentPadding = PaddingValues(5.dp), modifier = modifier) {
    items(devicesActionable.size) { i ->
      Host(devicesActionable[i], Modifier.fillMaxWidth(), onAction)
    }
  }
}

/**
 * Preview for the HostList Composable
 */
@Preview(showBackground = true)
@Composable
fun HostListPreview() {
  // A Constant Device Value to Preview
  val device = Device(InetAddress.getByAddress(
    byteArrayOf(127, 0, 0, 1)
  ), 8080, "Apple MacBook Air")

  // List of Devices Actionable
  val devicesActionable = listOf<DeviceActionable>(
    Pair(device, HostAction.DISCONNECT),
    Pair(device, HostAction.CONNECT),
    Pair(device, HostAction.DISCONNECT)
  )

  // Preview the HostList Composable
  HostList(
    devicesActionable = devicesActionable,
    onAction = {},
    modifier = Modifier.fillMaxWidth()
  )
}
