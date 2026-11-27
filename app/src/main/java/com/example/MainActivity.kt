package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.BodyFatDatabase
import com.example.data.BodyFatRepository
import com.example.ui.screens.BodyFatAppContent
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BodyFatViewModel
import com.example.viewmodel.BodyFatViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = BodyFatDatabase.getDatabase(this)
    val repository = BodyFatRepository(database.scanRecordDao(), database.userProfileDao())
    val factory = BodyFatViewModelFactory(application, repository)
    val viewModel = ViewModelProvider(this, factory)[BodyFatViewModel::class.java]

    setContent {
      MyApplicationTheme {
        BodyFatAppContent(viewModel = viewModel)
      }
    }
  }
}
