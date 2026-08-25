package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qr.BrandPresets
import com.example.qr.GradientType
import com.example.qr.LogoPreset
import com.example.qr.QrDotStyle
import com.example.qr.QrEyeStyle
import com.example.ui.components.QrCodePreviewCard
import com.example.ui.viewmodel.ContentTypeTab
import com.example.ui.viewmodel.QrViewModel

@Composable
fun GeneratorScreen(
    viewModel: QrViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val selectedTab by viewModel.selectedTab.collectAsState()
    val styleConfig by viewModel.styleConfig.collectAsState()
    val generatedBitmap by viewModel.generatedBitmap.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val isDarkCanvas by viewModel.previewCanvasDark.collectAsState()

    // Form states
    val urlInput by viewModel.urlInput.collectAsState()
    val textInput by viewModel.textInput.collectAsState()
    val wifiSsid by viewModel.wifiSsid.collectAsState()
    val wifiPassword by viewModel.wifiPassword.collectAsState()
    val wifiAuthType by viewModel.wifiAuthType.collectAsState()
    val wifiIsHidden by viewModel.wifiIsHidden.collectAsState()

    val contactName by viewModel.contactName.collectAsState()
    val contactPhone by viewModel.contactPhone.collectAsState()
    val contactEmail by viewModel.contactEmail.collectAsState()
    val contactCompany by viewModel.contactCompany.collectAsState()
    val contactJobTitle by viewModel.contactJobTitle.collectAsState()
    val contactWebsite by viewModel.contactWebsite.collectAsState()

    val emailAddress by viewModel.emailAddress.collectAsState()
    val emailSubject by viewModel.emailSubject.collectAsState()
    val emailBody by viewModel.emailBody.collectAsState()

    val phoneNumber by viewModel.phoneNumber.collectAsState()

    val smsNumber by viewModel.smsNumber.collectAsState()
    val smsMessage by viewModel.smsMessage.collectAsState()

    val socialPlatform by viewModel.socialPlatform.collectAsState()
    val socialUsername by viewModel.socialUsername.collectAsState()

    // Photo picker launcher for custom logo embedding
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateStyleConfig(
                updater = { it.copy(customLogoUri = uri.toString(), logoPreset = LogoPreset.CUSTOM_URI) },
                context = context
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Live Interactive QR Preview Card
        QrCodePreviewCard(
            bitmap = generatedBitmap,
            isGenerating = isGenerating,
            isDarkCanvas = isDarkCanvas,
            payloadText = viewModel.computeCurrentPayload(),
            brandLabel = styleConfig.brandLabel,
            onToggleDarkCanvas = { viewModel.togglePreviewCanvasDark() },
            onRefresh = { viewModel.refreshGeneratedQr(context) },
            onSaveToHistory = { viewModel.saveCurrentToHistory() }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Content Type Selector Bar
        Text(
            text = "CONTENT TYPE",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ContentTypeTab.values()) { tab ->
                val isSelected = tab == selectedTab
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        viewModel.setSelectedTab(tab)
                        viewModel.refreshGeneratedQr(context)
                    },
                    label = { Text(tab.label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    leadingIcon = {
                        Icon(
                            imageVector = getTabIcon(tab),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Dynamic Form Input Fields Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                when (selectedTab) {
                    ContentTypeTab.URL -> {
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = {
                                viewModel.urlInput.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Website URL") },
                            placeholder = { Text("https://mycompany.com") },
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_url")
                        )
                    }
                    ContentTypeTab.TEXT -> {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = {
                                viewModel.textInput.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Message / Plain Text") },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth().testTag("input_text")
                        )
                    }
                    ContentTypeTab.WIFI -> {
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = {
                                viewModel.wifiSsid.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Network Name (SSID)") },
                            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_wifi_ssid")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = wifiPassword,
                            onValueChange = {
                                viewModel.wifiPassword.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_wifi_pass")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hidden Network", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = wifiIsHidden,
                                onCheckedChange = {
                                    viewModel.wifiIsHidden.value = it
                                    viewModel.refreshGeneratedQr(context)
                                }
                            )
                        }
                    }
                    ContentTypeTab.CONTACT -> {
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = {
                                viewModel.contactName.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = {
                                viewModel.contactPhone.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = {
                                viewModel.contactEmail.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Email Address") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = contactCompany,
                            onValueChange = {
                                viewModel.contactCompany.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Company / Organization") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ContentTypeTab.EMAIL -> {
                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = {
                                viewModel.emailAddress.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Recipient Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emailSubject,
                            onValueChange = {
                                viewModel.emailSubject.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Subject") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emailBody,
                            onValueChange = {
                                viewModel.emailBody.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Email Body") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ContentTypeTab.PHONE -> {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = {
                                viewModel.phoneNumber.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ContentTypeTab.SMS -> {
                        OutlinedTextField(
                            value = smsNumber,
                            onValueChange = {
                                viewModel.smsNumber.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = smsMessage,
                            onValueChange = {
                                viewModel.smsMessage.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Pre-filled SMS Message") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ContentTypeTab.SOCIAL -> {
                        Text("Social Platform", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(6.dp))
                        val platforms = listOf("GitHub", "LinkedIn", "Instagram", "X / Twitter", "YouTube", "TikTok")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(platforms) { platform ->
                                FilterChip(
                                    selected = socialPlatform == platform,
                                    onClick = {
                                        viewModel.socialPlatform.value = platform
                                        viewModel.refreshGeneratedQr(context)
                                    },
                                    label = { Text(platform) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = socialUsername,
                            onValueChange = {
                                viewModel.socialUsername.value = it
                                viewModel.refreshGeneratedQr(context)
                            },
                            label = { Text("Username or Handle") },
                            leadingIcon = { Text("@", modifier = Modifier.padding(start = 12.dp)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. BRANDING: Custom Logo Embedding Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CUSTOM LOGO EMBEDDING",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (styleConfig.logoPreset != LogoPreset.NONE || styleConfig.customLogoUri != null) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.updateStyleConfig(
                                    updater = { it.copy(logoPreset = LogoPreset.NONE, customLogoUri = null) },
                                    context = context
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Remove", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Upload custom photo from gallery button
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("upload_custom_logo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (styleConfig.customLogoUri != null) "Change Custom Logo Image" else "Upload Brand Logo from Gallery",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Or Choose Preset Brand Emblem",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Preset Logo Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(LogoPreset.values().filter { it != LogoPreset.CUSTOM_URI }) { preset ->
                        val isSelected = styleConfig.logoPreset == preset && styleConfig.customLogoUri == null
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .clickable {
                                    viewModel.updateStyleConfig(
                                        updater = { it.copy(logoPreset = preset, customLogoUri = null) },
                                        context = context
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = getPresetIcon(preset),
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = preset.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Logo Size Slider
                if (styleConfig.logoPreset != LogoPreset.NONE || styleConfig.customLogoUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Logo Scale / Size", style = MaterialTheme.typography.bodySmall)
                        Text("${styleConfig.logoSizePercent}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = styleConfig.logoSizePercent.toFloat(),
                        onValueChange = {
                            viewModel.updateStyleConfig(
                                updater = { c -> c.copy(logoSizePercent = it.toInt()) },
                                context = context
                            )
                        },
                        valueRange = 15f..28f,
                        steps = 13,
                        modifier = Modifier.fillMaxWidth().testTag("logo_size_slider")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. BRANDING: Color Palette & Gradients Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COLORS & GRADIENTS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Brand Color Swatches for Foreground
                Text("Foreground Brand Color", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                val brandColors = listOf(
                    "#4F46E5" to "Indigo",
                    "#06B6D4" to "Cyan",
                    "#059669" to "Emerald",
                    "#E11D48" to "Rose",
                    "#D97706" to "Amber",
                    "#8B5CF6" to "Violet",
                    "#0F172A" to "Slate",
                    "#000000" to "Black",
                    "#FFFFFF" to "White"
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(brandColors) { (hex, name) ->
                        val isSelected = styleConfig.fgColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8).copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.updateStyleConfig(
                                        updater = { it.copy(fgColorHex = hex, eyeColorHex = hex) },
                                        context = context
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (hex == "#FFFFFF") Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Background Color
                Text("Background Color", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))

                val bgColors = listOf(
                    "#FFFFFF" to "Pure White",
                    "#0F172A" to "Dark Slate",
                    "#18181B" to "Obsidian",
                    "#F8FAFC" to "Off-White",
                    "#000000" to "Jet Black"
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(bgColors) { (hex, name) ->
                        val isSelected = styleConfig.bgColorHex.equals(hex, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(android.graphics.Color.parseColor(hex)),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier
                                .height(36.dp)
                                .clickable {
                                    viewModel.updateStyleConfig(
                                        updater = { it.copy(bgColorHex = hex) },
                                        context = context
                                    )
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (hex == "#FFFFFF" || hex == "#F8FAFC") Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Gradient Toggle
                Text("Gradient Type", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(GradientType.values()) { gType ->
                        FilterChip(
                            selected = styleConfig.gradientType == gType,
                            onClick = {
                                viewModel.updateStyleConfig(
                                    updater = {
                                        it.copy(
                                            gradientType = gType,
                                            fgGradientHex = if (gType != GradientType.NONE) "#06B6D4" else null
                                        )
                                    },
                                    context = context
                                )
                            },
                            label = { Text(gType.label) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. BRANDING: Dot Shapes & Finder Eyes Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatPaint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DOT & EYE STYLING",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Module / Dot Style", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(QrDotStyle.values()) { dot ->
                        FilterChip(
                            selected = styleConfig.dotStyle == dot,
                            onClick = {
                                viewModel.updateStyleConfig(
                                    updater = { it.copy(dotStyle = dot) },
                                    context = context
                                )
                            },
                            label = { Text(dot.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Corner Finder Frame", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(QrEyeStyle.values()) { eye ->
                        FilterChip(
                            selected = styleConfig.eyeStyle == eye,
                            onClick = {
                                viewModel.updateStyleConfig(
                                    updater = { it.copy(eyeStyle = eye) },
                                    context = context
                                )
                            },
                            label = { Text(eye.label) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7. BRANDING: Custom Brand Label Footer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BRAND FOOTER BANNER",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = styleConfig.brandLabel ?: "",
                    onValueChange = { text ->
                        viewModel.updateStyleConfig(
                            updater = { it.copy(brandLabel = text.takeIf { t -> t.isNotBlank() }) },
                            context = context
                        )
                    },
                    label = { Text("Brand Title / Header") },
                    placeholder = { Text("e.g. SCAN TO CONNECT") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_brand_label")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = styleConfig.brandSubtext ?: "",
                    onValueChange = { text ->
                        viewModel.updateStyleConfig(
                            updater = { it.copy(brandSubtext = text.takeIf { t -> t.isNotBlank() }) },
                            context = context
                        )
                    },
                    label = { Text("Brand Subtitle") },
                    placeholder = { Text("e.g. Official Studio • Verified") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_brand_subtext")
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun getTabIcon(tab: ContentTypeTab): ImageVector {
    return when (tab) {
        ContentTypeTab.URL -> Icons.Default.Language
        ContentTypeTab.TEXT -> Icons.Default.Notes
        ContentTypeTab.WIFI -> Icons.Default.Wifi
        ContentTypeTab.CONTACT -> Icons.Default.ContactPage
        ContentTypeTab.EMAIL -> Icons.Default.Email
        ContentTypeTab.PHONE -> Icons.Default.Phone
        ContentTypeTab.SMS -> Icons.Default.Sms
        ContentTypeTab.SOCIAL -> Icons.Default.Share
    }
}

private fun getPresetIcon(preset: LogoPreset): ImageVector {
    return when (preset) {
        LogoPreset.STAR -> Icons.Default.Star
        LogoPreset.WEB -> Icons.Default.Public
        LogoPreset.WIFI -> Icons.Default.Wifi
        LogoPreset.HEART -> Icons.Default.Favorite
        LogoPreset.TECH -> Icons.Default.AutoAwesome
        LogoPreset.SHOP -> Icons.Default.ShoppingBag
        LogoPreset.CODE -> Icons.Default.Code
        LogoPreset.SHARE -> Icons.Default.Share
        LogoPreset.SHIELD -> Icons.Default.Security
        LogoPreset.BRIEFCASE -> Icons.Default.Work
        else -> Icons.Default.Image
    }
}
