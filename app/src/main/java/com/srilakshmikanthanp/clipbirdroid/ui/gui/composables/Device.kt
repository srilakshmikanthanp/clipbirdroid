package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Status Type of Host
 */
enum class DeviceStatus {
  // Allowed state of Status Types
  CONNECTED,      // Client
  DISCONNECTED,   // Client
  ACTIVE,         // Server
  INACTIVE;       // Server

  // Enum to String in Capitalize
  override fun toString(): String {
    return super.toString().lowercase(Locale.getDefault()).replaceFirstChar {
      it.titlecase(Locale.getDefault())
    }
  }
}

/**
 * Status Composable
 */
@Composable
fun Device(
  hostName: String,
  status: DeviceStatus,
  fontSize: TextUnit,
  modifier: Modifier = Modifier
) {
  // Infer the foreground color from the status
  val foreground = when (status) {
    DeviceStatus.CONNECTED -> Color.Unspecified
    DeviceStatus.DISCONNECTED -> Color.Gray
    DeviceStatus.ACTIVE -> Color.Unspecified
    DeviceStatus.INACTIVE -> Color.Gray
  }

  // Status Box
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
  ) {
    Text(
      fontWeight = FontWeight.Bold,
      fontSize = fontSize,
      text = hostName,
      color = foreground,
    )
  }
}

/**
 * Preview of Status
 */
@Preview(showBackground = true)
@Composable
private fun GroupPreview() {
  Device(
    modifier = Modifier.padding(vertical = 70.dp).fillMaxWidth(),
    hostName = "Google Pixel",
    status = DeviceStatus.CONNECTED,
    fontSize = 45.sp
  )
}
