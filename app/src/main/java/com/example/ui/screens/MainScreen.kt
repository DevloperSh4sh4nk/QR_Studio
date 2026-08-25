package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ScanResultDialog
import com.example.ui.theme.AppThemeMode
import com.example.ui.viewmodel.ContentTypeTab
import com.example.ui.viewmodel.QrViewModel

enum class MainNavTab(val title: String) {
    GENERATOR("Studio"),
    SCANNER("Scanner"),
    PRESETS("Branding"),
    HISTORY("History")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: QrViewModel
) {
    var selectedNavTab by remember { mutableIntStateOf(0) }
    val themeMode by viewModel.themeMode.collectAsState()
    val scanResult by viewModel.scanResult.collectAsState()
    val showScanDialog by viewModel.showScanDialog.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "QR STUDIO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Theme Switcher button (Cycles through System -> Dark -> Light)
                    IconButton(
                        onClick = {
                            val nextMode = when (themeMode) {
                                AppThemeMode.SYSTEM -> AppThemeMode.DARK
                                AppThemeMode.DARK -> AppThemeMode.LIGHT
                                AppThemeMode.LIGHT -> AppThemeMode.SYSTEM
                            }
                            viewModel.setThemeMode(nextMode)
                        },
                        modifier = Modifier.testTag("theme_mode_toggle")
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                AppThemeMode.DARK -> Icons.Default.Brightness4
                                AppThemeMode.LIGHT -> Icons.Default.Brightness7
                            },
                            contentDescription = "Toggle Dark/Light Mode",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedNavTab == 0,
                    onClick = { selectedNavTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedNavTab == 0) Icons.Filled.QrCode else Icons.Outlined.QrCode,
                            contentDescription = "Generator Studio"
                        )
                    },
                    label = { Text("Studio", fontWeight = if (selectedNavTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_generator")
                )

                NavigationBarItem(
                    selected = selectedNavTab == 1,
                    onClick = { selectedNavTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedNavTab == 1) Icons.Filled.QrCodeScanner else Icons.Outlined.QrCodeScanner,
                            contentDescription = "Scanner"
                        )
                    },
                    label = { Text("Scan", fontWeight = if (selectedNavTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_scanner")
                )

                NavigationBarItem(
                    selected = selectedNavTab == 2,
                    onClick = { selectedNavTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedNavTab == 2) Icons.Filled.Palette else Icons.Outlined.Palette,
                            contentDescription = "Branding Presets"
                        )
                    },
                    label = { Text("Branding", fontWeight = if (selectedNavTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_presets")
                )

                NavigationBarItem(
                    selected = selectedNavTab == 3,
                    onClick = { selectedNavTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedNavTab == 3) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History", fontWeight = if (selectedNavTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = selectedNavTab,
                label = "screen_transition"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> GeneratorScreen(viewModel = viewModel)
                    1 -> ScannerScreen(viewModel = viewModel)
                    2 -> PresetsScreen(
                        viewModel = viewModel,
                        onNavigateToGenerator = { selectedNavTab = 0 }
                    )
                    3 -> HistoryScreen(
                        viewModel = viewModel,
                        onEditInStudio = { selectedNavTab = 0 }
                    )
                }
            }
        }

        // Scan Result Bottom Sheet
        if (showScanDialog && scanResult != null) {
            ScanResultDialog(
                result = scanResult!!,
                onDismiss = { viewModel.dismissScanDialog() },
                onCustomizeInStudio = { content ->
                    viewModel.setSelectedTab(ContentTypeTab.URL)
                    viewModel.urlInput.value = content.actionUrl ?: content.rawText
                    viewModel.refreshGeneratedQr(null)
                    selectedNavTab = 0
                }
            )
        }
    }
}
