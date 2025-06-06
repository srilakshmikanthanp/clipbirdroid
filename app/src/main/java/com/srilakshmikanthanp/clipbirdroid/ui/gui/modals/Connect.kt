package com.srilakshmikanthanp.clipbirdroid.ui.gui.modals

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.text.isDigitsOnly
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.utilities.functions.isHostAvailable
import org.json.JSONObject
import java.net.InetAddress
import java.util.concurrent.Executors.newSingleThreadExecutor

/**
 * String extension to check the string number with dot
 */
private fun String.isNumeric(): Boolean {
  return this.all { it.isDigit() || it == '.' }
}

/**
 * A composable function to connect to a device. it has
 * an input option for ipv4 address and port number.
 * Also it has a button to scan the barcode.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Connect(
  onDismissRequest: () -> Unit,
  onConnect: (InetAddress, Int) -> Unit,
  title: String,
  modifier: Modifier = Modifier,
) {
  // Validator function for ipv4 address and port number
  val validator: (String, Int) -> Boolean = validator@{ ip, port ->
    // check the ip is valid or not
    if (ip.split(".").size != 4) {
      return@validator false
    }

    // check each part of the ip is valid
    ip.split(".").forEach {
      if (it.toInt() !in 0..255) return@validator false
    }

    // check the port is valid
    if (port !in 1..65535) {
      return@validator false
    }

    // finally the ipv4 and port is valid
    return@validator true
  }

  // is loading
  var isLoading by remember { mutableStateOf(false) }

  // context for the scanner
  val context = LocalContext.current

  // Bar Code Results processor function
  val barCodeResult: (String?) -> Unit = barCodeResult@{ result ->
    // is Result user pressed back button
    if (result == null) { return@barCodeResult }

    // set loading
    isLoading = true

    // Executor to get the First Reachable IP
    val executor = newSingleThreadExecutor()

    // try to parse the json
    val (ips, port) = try {
      val json = JSONObject(result)
      val ips = json.getJSONArray("ips")
      val port = json.getInt("port")
      Pair(ips, port)
    } catch (e: Exception) {
      Toast.makeText(context, "Invalid QR Code", Toast.LENGTH_SHORT).show()
      isLoading = false
      return@barCodeResult
    }

    // get the first reachable ip
    executor.execute {
      for (i in 0 until ips.length()) {
        // check the ip and port is valid
        if (!validator(ips.getString(i), port)) {
          continue
        }

        // ipv4
        val ipv4 = ips.getString(i)

        // Validate the input
        val ip = try {
          InetAddress.getByName(ipv4)
        } catch (e: Exception) {
          continue
        }

        // if not reachable continue
        if (!isHostAvailable(ip, port)) {
          continue
        }

        // connect
        onConnect(ip, port)
        isLoading = false
        break
      }
    }
  }

  // submit handler
  val onSubmit: (String, String) -> Unit = onSubmit@{ ipv4, port ->
    // check port is number
    if (port.toIntOrNull() == null) {
      return@onSubmit
    }

    // check ipv4 and port
    if (!validator(ipv4, port.toInt())) {
      return@onSubmit
    }

    // set loading
    isLoading = true

    // Executor to connect to the host
    newSingleThreadExecutor().execute {
      // Validate the input
      val ip = try {
        InetAddress.getByName(ipv4)
      } catch (e: Exception) {
        isLoading = false
        return@execute
      }

      // connect
      if (isHostAvailable(ip, port.toInt())) {
        onConnect(ip, port.toInt())
      } else {
        isLoading = false
        return@execute
      }
    }
  }

  // Bar code scanner Options
  val options = GmsBarcodeScannerOptions.Builder()
    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
    .enableAutoZoom()
    .build()

  // Bar code scanner
  val scanner = GmsBarcodeScanning.getClient(context, options)

  // lambda to start scan
  val startScan: () -> Unit = {
    scanner.startScan()
      .addOnFailureListener {
        Toast.makeText(context, "Failed to scan", Toast.LENGTH_SHORT).show()
        isLoading = false
        Log.e("Connect", "Failed to scan", it)
      }
      .addOnSuccessListener {
        barCodeResult(it.rawValue)
      }
  }

  // ip input & port input
  var ipv4 by remember { mutableStateOf("") }
  var port by remember { mutableStateOf("") }

  // dialog
  Dialog(
    properties = DialogProperties(usePlatformDefaultWidth = false),
    onDismissRequest = onDismissRequest
  ) {
    // Loading
    if (isLoading) {
      CircularProgressIndicator(modifier = Modifier.padding(vertical = 5.dp).size(50.dp))
      return@Dialog
    }

    // content
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
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.padding(vertical = 5.dp),
          onValueChange = { if (it.isNumeric()) ipv4 = it },
          value = ipv4,
          label = { Text(stringResource(id = R.string.ipv4)) }
        )

        // Input Port
        TextField(
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.padding(vertical = 5.dp),
          onValueChange = { if (it.isDigitsOnly()) port = it },
          value = port,
          label = { Text(stringResource(id = R.string.port)) }
        )

        // Submit Button
        TextButton(
          modifier = Modifier.padding(vertical = 5.dp),
          onClick = { onSubmit(ipv4, port) }
        ) {
          Text(stringResource(id = R.string.join))
        }

        // Spacer
        Text(
          modifier = Modifier.padding(vertical = 5.dp),
          text = stringResource(id = R.string.or)
        )

        // Scan Button
        IconButton(
          modifier = Modifier.padding(vertical = 5.dp),
          onClick = { startScan() }
        ) {
          Image(
            painter = painterResource(id = R.drawable.scan),
            contentDescription = stringResource(id = R.string.scan)
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
  Connect(onDismissRequest = {}, title = "Join Group", onConnect = { _, _ -> })
}
