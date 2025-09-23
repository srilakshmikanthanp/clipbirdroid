package com.srilakshmikanthanp.clipbirdroid.ui.gui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.srilakshmikanthanp.clipbirdroid.R
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.AuthViewModel
import com.srilakshmikanthanp.clipbirdroid.syncing.wan.auth.BasicAuthRequestDto

@Composable
fun SignedIn(authViewModel: AuthViewModel = hiltViewModel()) {
  Box(
    modifier = Modifier.fillMaxSize().padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(24.dp),
      elevation = CardDefaults.cardElevation(8.dp)
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Filled.CheckCircle,
          contentDescription = "Signed In",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(96.dp).padding(bottom = 16.dp)
        )
        Text(
          text = "You're signed in!",
          style = MaterialTheme.typography.headlineSmall,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Welcome back 👋",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
          onClick = { authViewModel.signOut() },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Sign Out")
        }
      }
    }
  }
}


@Composable
fun SignIn(authViewModel: AuthViewModel = hiltViewModel<AuthViewModel>()) {
  val authUIState by authViewModel.authUIState.collectAsState()
  var userName by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
      modifier = Modifier.padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        painter = painterResource(R.drawable.login),
        contentDescription = "Sign In",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(72.dp)
      )
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Sign In",
        style = MaterialTheme.typography.headlineMedium
      )
      Spacer(modifier = Modifier.height(24.dp))
      OutlinedTextField(
        enabled = !authUIState.isLoading,
        value = userName,
        onValueChange = { userName = it },
        label = { Text("Username") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        visualTransformation = PasswordVisualTransformation(),
        enabled = !authUIState.isLoading,
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(24.dp))
      Button(
        onClick = { authViewModel.signIn(BasicAuthRequestDto(userName, password)) },
        enabled = !authUIState.isLoading,
        modifier = Modifier.fillMaxWidth()
      ) {
        if (authUIState.isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
          )
        } else {
          Text("Sign In")
        }
      }
      if (authUIState.error != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text("${authUIState.error?.message}", color = MaterialTheme.colorScheme.error)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun Account(
  onMenuClick: () -> Unit,
  authViewModel: AuthViewModel = hiltViewModel<AuthViewModel>()
) {
  val authUIState by authViewModel.authUIState.collectAsState()

  val menuIcon = @Composable {
    IconButton(onClick = onMenuClick) {
      Image(
        painter = painterResource(R.drawable.menu),
        contentDescription = stringResource(id = R.string.menu)
      )
    }
  }

  val accountTopBar = @Composable {
    TopAppBar(
      navigationIcon = { menuIcon() },
      title = {
        Text(
          stringResource(id = R.string.account),
          modifier = Modifier.padding(horizontal = 3.dp)
        )
      },
      modifier = Modifier.padding(3.dp)
    )
  }

  val content = @Composable { padding: PaddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .imePadding(),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (authUIState.authToken != null) {
          SignedIn(authViewModel)
        } else {
          SignIn(authViewModel)
        }
      }
    }
  }

  Scaffold(
    topBar = accountTopBar,
    content = content,
  )
}
