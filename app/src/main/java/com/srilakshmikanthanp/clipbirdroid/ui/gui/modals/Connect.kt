package com.srilakshmikanthanp.clipbirdroid.ui.gui.modals

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.utility.functions.isHostAvailable
import org.json.JSONObject
import java.net.InetAddress
import java.util.concurrent.Executors.newSingleThreadExecutor

/**
 * A composable function to connect to a device. it has
 * an input option for ipv4 address and port number.
 * Also it has a button to scan the barcode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Connect(
  onDismissRequest: () -> Unit,
  onConnect: (InetAddress, Int) -> Unit,
  title: String = "Connect",
  modifier: Modifier = Modifier,
) {
  // Validator function for ipv4 address and port number
  val validator: (String, Int) -> Pair<InetAddress, Int>? = validator@{ ip, port ->
    // check the ip is valid or not
    val ipv4 = try {
      InetAddress.getByName(ip)
    } catch (e: Exception) {
      return@validator null
    }

    // check the port is valid
    if (port !in 1..65535) {
      return@validator null
    }

    // check the ip is reachable
    if (!isHostAvailable(ipv4, port, 1000)) {
      return@validator null
    }

    // return the pair
    return@validator Pair(ipv4, port)
  }

  // is loading
  var isLoading by remember { mutableStateOf(false) }

  // Bar Code Results processor function
  val barCodeResult: (ScanIntentResult) -> Unit = barCodeResult@{ result ->
    // is Result user pressed back button
    if (result.contents == null) { return@barCodeResult }

    // set loading
    isLoading = true

    // Executor to get the First Reachable IP
    val executor = newSingleThreadExecutor()
    val json = JSONObject(result.contents)
    val ips = json.getJSONArray("ips")
    val port = json.getInt("port")

    // get the first reachable ip
    executor.execute {
      for (i in 0 until ips.length()) {
        val pair = validator(ips.getString(i), port) ?: continue
        onConnect(pair.first, pair.second)
        break
      }
    }
  }

  // submit handler
  val onSubmit: (String, String) -> Unit = onSubmit@{ ipv4, port ->
    newSingleThreadExecutor().execute {
      validator(ipv4, port.toInt())?.let {
        onConnect(it.first, it.second)
      }
    }.also {
      isLoading = true
    }
  }

  // Scan Launcher
  val launcher = rememberLauncherForActivityResult(
    contract = ScanContract(),
    onResult = { barCodeResult(it) },
  )

  // ip input & port input
  var ipv4 by remember { mutableStateOf("") }
  var port by remember { mutableStateOf("") }

  // dialog
  Dialog(onDismissRequest = onDismissRequest) {
    Card {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
        verticalArrangement = Arrangement.Center
      ) {
        // Title for the dialog
        Text(
          style = MaterialTheme.typography.headlineSmall,
          text = title,
          modifier = Modifier.padding(vertical = 5.dp)
        )

        // Input IPv4
        TextField(
          modifier = Modifier.padding(vertical = 5.dp),
          onValueChange = { ipv4 = it },
          value = ipv4,
          label = { Text("IPV4") }
        )

        // Input Port
        TextField(
          modifier = Modifier.padding(vertical = 5.dp),
          onValueChange = { port = it },
          value = port,
          label = { Text("Port") }
        )

        // Loading
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.padding(vertical = 5.dp)
          ).also { return@Column }
        }

        // Submit Button
        TextButton(
          modifier = Modifier.padding(vertical = 5.dp),
          onClick = { onSubmit(ipv4, port) }
        ) {
          Text("Join")
        }

        // Spacer
        Text(
          modifier = Modifier.padding(vertical = 5.dp),
          text = "OR"
        )

        // Scan Options
        val options: ScanOptions = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setBeepEnabled(true)
        options.setOrientationLocked(true)

        // Scan Button
        IconButton(
          modifier = Modifier.padding(vertical = 5.dp),
          onClick = { launcher.launch(options) }
        ) {
          Image(
            painter = painterResource(id = R.drawable.scan),
            contentDescription = "Scan"
          )
        }
      }
    }
  }
}

/**
 * Preview
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun ConnectPreview() {
  Connect(onDismissRequest = {}, onConnect = { _, _ -> })
}
