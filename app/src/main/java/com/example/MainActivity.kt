package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.data.local.AppDatabase
import com.example.data.repository.QrRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.QRStudioTheme
import com.example.ui.viewmodel.QrViewModel
import com.example.ui.viewmodel.QrViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: QrViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = QrRepository(database.qrRecordDao())
        QrViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()

            QRStudioTheme(themeMode = themeMode) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
