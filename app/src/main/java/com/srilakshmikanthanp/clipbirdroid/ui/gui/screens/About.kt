package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.constants.appDonatePage
import com.srilakshmikanthanp.clipbirdroid.constants.appHomePage
import com.srilakshmikanthanp.clipbirdroid.constants.appIssuesPage
import com.srilakshmikanthanp.clipbirdroid.constants.appVersion
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.srilakshmikanthanp.clipbirdroid.SSLConfigViewModel
import com.srilakshmikanthanp.clipbirdroid.constants.appSourcePage
import com.srilakshmikanthanp.clipbirdroid.ui.gui.utility.sha256Fingerprint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Action(icon: Int, text: String, modifier: Modifier) {
  Box(modifier = modifier) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .padding(10.dp)
        .fillMaxWidth(),
    ) {
      Image(
        painter = painterResource(icon),
        contentDescription = stringResource(id = R.string.icon)
      )
      Text(
        text = text,
        modifier = Modifier.padding(5.dp),
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About(
  onMenuClick: () -> Unit = {},
  sslConfigViewModel: SSLConfigViewModel = hiltViewModel()
) {
  val fingerprint = remember(sslConfigViewModel.sslConfig.certificate) { sha256Fingerprint(sslConfigViewModel.sslConfig.certificate) }
  var showFinglerPrintDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current

  if (showFinglerPrintDialog) {
    AlertDialog(
      onDismissRequest = { showFinglerPrintDialog = false },
      confirmButton = {
        TextButton(onClick = { showFinglerPrintDialog = false }) {
          Text("OK")
        }
      },
      title = { Text("Fingerprint") },
      text = {
        Text(
          text = fingerprint,
          style = MaterialTheme.typography.bodyMedium
        )
      }
    )
  }

  val onIssueReport = {
    val intent = Intent(Intent.ACTION_VIEW, appIssuesPage().toUri())
    context.startActivity(intent)
  }

  val onSourceOpen = {
    val intent = Intent(Intent.ACTION_VIEW, appSourcePage().toUri())
    context.startActivity(intent)
  }

  val onWebsiteOpen = {
    val intent = Intent(Intent.ACTION_VIEW, appHomePage().toUri())
    context.startActivity(intent)
  }

  val onDonation = {
    val intent = Intent(Intent.ACTION_VIEW, appDonatePage().toUri())
    context.startActivity(intent)
  }

  val onLicenseClick = {
    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
  }

  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(
        painter = painterResource(R.drawable.menu),
        contentDescription = stringResource(id = R.string.menu)
      )
    }
  }

  val aboutTopBar = @Composable {
    TopAppBar(
      navigationIcon = { menuIcon() },
      title = {
        Text(
          stringResource(id = R.string.about_clipbird),
          modifier = Modifier.padding(horizontal = 3.dp)
        )
      },
      modifier = Modifier.padding(3.dp)
    )
  }

  val content = @Composable { padding: PaddingValues ->
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(padding),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(16.dp)
        ) {
          Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier.size(140.dp)
          )

          Text(
            text = "Version ${appVersion()}",
            style = MaterialTheme.typography.bodyMedium
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = stringResource(R.string.about_us),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      FlowRow(
        maxItemsInEachRow = 3,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        val modifierBuilder = { onClick: () -> Unit ->
          Modifier
            .weight(1f)
            .padding(10.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
              onClick()
            }
        }

        Action(
          R.drawable.outline_fingerprint,
          stringResource(id = R.string.fingerprint),
          modifierBuilder { showFinglerPrintDialog = true }
        )

        Action(
          R.drawable.browser,
          stringResource(id = R.string.website),
          modifierBuilder(onWebsiteOpen)
        )

        Action(
          R.drawable.outline_folder_code,
          stringResource(id = R.string.source_code),
          modifierBuilder(onSourceOpen)
        )

        Action(
          R.drawable.bug,
          stringResource(id = R.string.report_issue),
          modifierBuilder(onIssueReport)
        )

        Action(
          R.drawable.money,
          stringResource(id = R.string.donate),
          modifierBuilder(onDonation)
        )

        Action(
          R.drawable.outline_hardware,
          stringResource(id = R.string.licenses),
          modifierBuilder(onLicenseClick)
        )
      }
    }
  }

  Scaffold(
    topBar = aboutTopBar,
    content = content,
  )
}

@Preview(showBackground = true)
@Composable
fun AboutPreview() {
  About()
}
