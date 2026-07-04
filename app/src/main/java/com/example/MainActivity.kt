package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PostoAdminApp
import com.example.ui.PostoViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: PostoViewModel = viewModel()
      val themeMode by viewModel.themeMode.collectAsState()
      val systemDark = isSystemInDarkTheme()
      val useDarkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
      }
      MyApplicationTheme(darkTheme = useDarkTheme) {
        PostoAdminApp(viewModel = viewModel)
      }
    }
  }
}
