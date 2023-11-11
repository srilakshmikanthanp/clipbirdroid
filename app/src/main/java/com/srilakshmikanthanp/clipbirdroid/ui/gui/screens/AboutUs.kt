package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.constant.appHomePage
import com.srilakshmikanthanp.clipbirdroid.constant.appIssuesPage
import com.srilakshmikanthanp.clipbirdroid.constant.appVersion

/**
 * An Action button used in About Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Action(icon: Int, text: String, modifier: Modifier, onClick: () -> Unit) {
  Card(modifier = modifier, onClick = onClick) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(10.dp).fillMaxWidth()
    ) {
      Image(painter = painterResource(icon), contentDescription = "Icon")
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
  // Get the Context of Application
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
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appHomePage()))
    context.startActivity(intent)
  }

  // Menu Icon for the Top Bar
  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(painter = painterResource(R.drawable.menu), contentDescription = "Menu",)
    }
  }

  // History Top Bar
  val aboutTopBar = @Composable {
    TopAppBar(
      navigationIcon = { menuIcon() },
      title = { Text("About Clipbird", modifier = Modifier.padding(horizontal = 3.dp)) },
      modifier = Modifier.padding(3.dp)
    )
  }

  // Render the Screen
  val content = @Composable { padding : PaddingValues ->
    Column(
      modifier = Modifier.fillMaxWidth().padding(padding),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // Shoe the Clip Bird Logo
      Image(
        painter = painterResource(R.mipmap.ic_launcher_foreground),
        contentDescription = "Logo"
      )

      // Show the Version
      Text(
        text  = "Version ${appVersion()}",
        color = Color.Gray
      )

      // Show the Icons
      Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.padding(vertical = 10.dp)
      ) {
        val cardModifier = Modifier.weight(1f).fillMaxWidth().padding(10.dp)
        Action(R.drawable.browser, "Website", cardModifier , onWebsiteOpen)
        Action(R.drawable.bug, "Report Issue", cardModifier, onIssueReport)
        Action(R.drawable.money, "Donate", cardModifier, onDonation)
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
