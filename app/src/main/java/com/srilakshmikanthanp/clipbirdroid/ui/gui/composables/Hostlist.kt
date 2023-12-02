package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.srilakshmikanthanp.clipbirdroid.types.device.Device
import java.net.InetAddress
import java.util.Locale

/**
 * DeviceActionable Type Alias
 */
typealias DeviceActionable = Pair<Device, HostAction>

/**
 * Device Action Enum To Represent Connect or Disconnect
 */
enum class HostAction {
  // Allowed Types connect or disconnect
  CONNECT, DISCONNECT;

  // Enum to String in Capitalize
  override fun toString(): String {
    return super.toString().lowercase(Locale.getDefault()).replaceFirstChar {
      it.titlecase(Locale.getDefault())
    }
  }
}

/**
 * Device Composable to Represent the Device
 */
@Composable
private fun Host(
  deviceActionable: DeviceActionable,
  modifier: Modifier,
  onAction: (DeviceActionable) -> Unit
) {
  // Click Handler for the Device Action Clicked either Connect or Disconnect
  val onClickHandler = { onAction(deviceActionable) }

  // Hostname And Action placed in row name left and action right
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Align Test the Center both horizontally and vertically
    Text(
      style = MaterialTheme.typography.labelLarge,
      text = deviceActionable.first.name,
      modifier = Modifier.padding(start = 10.dp)
    )

    // Connect or Disconnect Button
    TextButton(
      onClick = onClickHandler
    ) {
      Text(text = deviceActionable.second.toString())
    }
  }
}

/**
 * Device Composable to Represent the Device Preview
 */
@Preview(showBackground = true)
@Composable
private fun HostPreview() {
  // A Constant Device Value to Preview
  val device = Device(InetAddress.getLocalHost(), 8080, "Apple MacBook Air")

  // Preview the Device
  Host(
    deviceActionable = DeviceActionable(device, HostAction.DISCONNECT),
    modifier = Modifier.fillMaxWidth(),
    onAction = {}
  )
}

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
      Host(devicesActionable[i], Modifier.fillMaxWidth().padding(horizontal = 5.dp), onAction)
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
    modifier = Modifier.fillMaxWidth().padding(10.dp)
  )
}
