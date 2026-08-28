package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    // Verificar si hay actualizaciones en GitHub (Background)
    GitHubUpdater.checkForUpdates(this)

    val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val savedUser = prefs.getString("username", "") ?: ""
    val savedPass = prefs.getString("password", "") ?: ""
    val hasSavedLogin = savedUser.isNotBlank() && savedPass.isNotBlank()
    if (hasSavedLogin) {
        UserSession.username = savedUser
        UserSession.password = savedPass
    }

    setContent {
      MyApplicationTheme {
        var isLoggedIn by remember { mutableStateOf(hasSavedLogin) }
        if (!isLoggedIn) {
          LoginScreen(onLoginSuccess = { isLoggedIn = true })
        } else {
          MainScreen()
        }
      }
    }
  }
}
