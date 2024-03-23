package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.constant.appDonatePage
import com.srilakshmikanthanp.clipbirdroid.constant.appHomePage
import com.srilakshmikanthanp.clipbirdroid.constant.appIssuesPage
import com.srilakshmikanthanp.clipbirdroid.constant.appVersion

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
      Text(text = text, fontSize = 12.sp, modifier = Modifier.padding(5.dp))
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
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appIssuesPage()))
    context.startActivity(intent)
  }

  // website open Handler
  val onWebsiteOpen = {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appHomePage()))
    context.startActivity(intent)
  }

  // On Donation Handler
  val onDonation = {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appDonatePage()))
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
      Card (
        modifier = Modifier
          .fillMaxWidth()
          .padding(15.dp)
      ) {
        Column (
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(2.dp),
          verticalArrangement = Arrangement.Center
        ) {
          // Show the Clip Bird Logo
          Image(
            painter = painterResource(R.mipmap.ic_launcher_foreground),
            modifier = Modifier.size(140.dp),
            contentDescription = stringResource(id = R.string.logo),
          )

          // Show the Version
          Text(
            text  = "Version ${appVersion()}",
          )

          // Show the About Us
          Text(
            text  = stringResource(R.string.about_us),
            modifier = Modifier.padding(10.dp),
            textAlign = TextAlign.Center
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
      Card (
        modifier = Modifier.fillMaxWidth().padding(15.dp).clickable { onLicenseClick() }
      ) {
        Text(
          modifier = Modifier.padding(10.dp),
          text = stringResource(id = R.string.open_source_licenses)
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
