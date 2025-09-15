package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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

/**
 * An Action button used in About Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Action(icon: Int, text: String, modifier: Modifier) {
  Box(modifier = modifier) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(10.dp).fillMaxWidth(),
    ) {
      Image(painter = painterResource(icon), contentDescription = stringResource(id = R.string.icon))
      Text(text = text, modifier = Modifier.padding(5.dp), style = MaterialTheme.typography.bodyMedium)
    }
  }
}

/**
 * About Page Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUs(onMenuClick: () -> Unit = {}) {
  // Get the Context of Clipbird
  val context = LocalContext.current

  // Bug Report Handler
  val onIssueReport = {
    val intent = Intent(Intent.ACTION_VIEW, appIssuesPage().toUri())
    context.startActivity(intent)
  }

  // website open Handler
  val onWebsiteOpen = {
    val intent = Intent(Intent.ACTION_VIEW, appHomePage().toUri())
    context.startActivity(intent)
  }

  // On Donation Handler
  val onDonation = {
    val intent = Intent(Intent.ACTION_VIEW, appDonatePage().toUri())
    context.startActivity(intent)
  }

  // on License Click
  val onLicenseClick = {
    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
  }

  // Menu Icon for the Top Bar
  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(painter = painterResource(R.drawable.menu), contentDescription = stringResource(id = R.string.menu))
    }
  }

  // History Top Bar
  val aboutTopBar = @Composable {
    TopAppBar(
      navigationIcon = { menuIcon() },
      title = { Text(stringResource(id = R.string.about_clipbird), modifier = Modifier.padding(horizontal = 3.dp)) },
      modifier = Modifier.padding(3.dp)
    )
  }

  // Render the Screen
  val content = @Composable { padding : PaddingValues ->
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(padding),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier.padding(16.dp)
        ) {
          // ClipBird Logo
          Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = stringResource(id = R.string.logo),
            modifier = Modifier.size(140.dp)
          )

          // Version Text
          Text(
            text = "Version ${appVersion()}",
            style = MaterialTheme.typography.bodyMedium
          )

          Spacer(modifier = Modifier.height(8.dp))

          // About Us Text
          Text(
            text = stringResource(R.string.about_us),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // Show the Icons
      Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(vertical = 10.dp)
      ) {
        val modifierBuilder = { onClick: () -> Unit ->
          Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(10.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
              onClick()
            }
        }

        Action(R.drawable.browser,  stringResource(id = R.string.website), modifierBuilder(onWebsiteOpen))
        Action(R.drawable.bug,  stringResource(id = R.string.report_issue), modifierBuilder(onIssueReport))
        Action(R.drawable.money,stringResource(id = R.string.donate) , modifierBuilder(onDonation))
      }

      // Open Source License
      Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .clickable { onLicenseClick() },
      ) {
        Text(
          modifier = Modifier.padding(16.dp),
          text = stringResource(id = R.string.open_source_licenses),
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }

  // Scaffold
  Scaffold(
    topBar = aboutTopBar,
    content = content,
  )
}

/**
 * Preview of AboutUs Screen
 */
@Preview(showBackground = true)
@Composable
fun AboutUsPreview() {
  AboutUs()
}
