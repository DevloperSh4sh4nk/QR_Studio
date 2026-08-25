package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.QrRecord
import com.example.data.repository.QrRepository
import com.example.qr.BrandPreset
import com.example.qr.BrandPresets
import com.example.qr.GradientType
import com.example.qr.LogoPreset
import com.example.qr.ParsedQrContent
import com.example.qr.QrCodeDecoder
import com.example.qr.QrCodeGenerator
import com.example.qr.QrDotStyle
import com.example.qr.QrEyeStyle
import com.example.qr.QrPayloadParser
import com.example.qr.QrStyleConfig
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ContentTypeTab(val label: String, val iconName: String) {
    URL("Website / Link", "link"),
    TEXT("Plain Text", "notes"),
    WIFI("Wi-Fi Network", "wifi"),
    CONTACT("Contact Card", "person"),
    EMAIL("Email Address", "email"),
    PHONE("Phone Call", "phone"),
    SMS("SMS Text", "sms"),
    SOCIAL("Social Profile", "share")
}

class QrViewModel(
    private val repository: QrRepository
) : ViewModel() {

    // App Theme
    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    // Generator Tab State
    private val _selectedTab = MutableStateFlow(ContentTypeTab.URL)
    val selectedTab: StateFlow<ContentTypeTab> = _selectedTab.asStateFlow()

    // Form Fields
    val urlInput = MutableStateFlow("https://github.com")
    val textInput = MutableStateFlow("Welcome to QR Studio!")
    val wifiSsid = MutableStateFlow("Studio_HighSpeed")
    val wifiPassword = MutableStateFlow("SecurePass2026")
    val wifiAuthType = MutableStateFlow("WPA")
    val wifiIsHidden = MutableStateFlow(false)

    val contactName = MutableStateFlow("Alex Morgan")
    val contactPhone = MutableStateFlow("+1 (555) 234-5678")
    val contactEmail = MutableStateFlow("alex.morgan@brand.io")
    val contactCompany = MutableStateFlow("Studio Creative Labs")
    val contactJobTitle = MutableStateFlow("Product Architect")
    val contactWebsite = MutableStateFlow("https://alexmorgan.design")

    val emailAddress = MutableStateFlow("contact@mybrand.com")
    val emailSubject = MutableStateFlow("Inquiry from QR Code")
    val emailBody = MutableStateFlow("Hello, I would like to learn more about your services.")

    val phoneNumber = MutableStateFlow("+1 800 555 0199")

    val smsNumber = MutableStateFlow("+1 800 555 0199")
    val smsMessage = MutableStateFlow("Hi! I scanned your branded QR code.")

    val socialPlatform = MutableStateFlow("GitHub")
    val socialUsername = MutableStateFlow("google-ai-studio")

    // QR Style Configuration
    private val _styleConfig = MutableStateFlow(
        QrStyleConfig(
            fgColorHex = "#4F46E5",
            fgGradientHex = "#06B6D4",
            gradientType = GradientType.DIAGONAL,
            bgColorHex = "#FFFFFF",
            eyeColorHex = "#4F46E5",
            dotStyle = QrDotStyle.ROUNDED,
            eyeStyle = QrEyeStyle.ROUNDED,
            logoPreset = LogoPreset.TECH,
            logoSizePercent = 22,
            brandLabel = "SCAN TO CONNECT",
            brandSubtext = "QR Studio • Verified Identity"
        )
    )
    val styleConfig: StateFlow<QrStyleConfig> = _styleConfig.asStateFlow()

    // Preview Background mode for canvas testing (Light vs Dark background)
    private val _previewCanvasDark = MutableStateFlow(false)
    val previewCanvasDark: StateFlow<Boolean> = _previewCanvasDark.asStateFlow()

    fun togglePreviewCanvasDark() {
        _previewCanvasDark.value = !_previewCanvasDark.value
    }

    // Generated QR Bitmap Cache
    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap: StateFlow<Bitmap?> = _generatedBitmap.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Scanner State
    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn.asStateFlow()

    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _scanResult = MutableStateFlow<ParsedQrContent?>(null)
    val scanResult: StateFlow<ParsedQrContent?> = _scanResult.asStateFlow()

    private val _showScanDialog = MutableStateFlow(false)
    val showScanDialog: StateFlow<Boolean> = _showScanDialog.asStateFlow()

    // History & Search State
    private val _historyFilter = MutableStateFlow("ALL") // "ALL", "GENERATED", "SCANNED", "FAVORITES"
    val historyFilter: StateFlow<String> = _historyFilter.asStateFlow()

    val searchQuery = MutableStateFlow("")

    val allHistoryRecords = repository.allRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredRecords: StateFlow<List<QrRecord>> = combine(
        allHistoryRecords,
        _historyFilter,
        searchQuery
    ) { records, filter, query ->
        val filteredByType = when (filter) {
            "GENERATED" -> records.filter { it.type == "GENERATED" }
            "SCANNED" -> records.filter { it.type == "SCANNED" }
            "FAVORITES" -> records.filter { it.isFavorite }
            else -> records
        }

        if (query.isBlank()) {
            filteredByType
        } else {
            filteredByType.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true) ||
                        it.contentType.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initial bitmap generation
        refreshGeneratedQr(null)
    }

    fun setSelectedTab(tab: ContentTypeTab) {
        _selectedTab.value = tab
    }

    fun updateStyleConfig(updater: (QrStyleConfig) -> QrStyleConfig, context: Context? = null) {
        _styleConfig.value = updater(_styleConfig.value)
        refreshGeneratedQr(context)
    }

    fun applyBrandPreset(preset: BrandPreset, context: Context? = null) {
        _styleConfig.value = _styleConfig.value.copy(
            fgColorHex = preset.fgColorHex,
            fgGradientHex = preset.fgGradientHex,
            gradientType = if (preset.fgGradientHex != null) GradientType.DIAGONAL else GradientType.NONE,
            bgColorHex = preset.bgColorHex,
            eyeColorHex = preset.eyeColorHex,
            dotStyle = preset.dotStyle,
            eyeStyle = preset.eyeStyle,
            logoPreset = preset.logoPreset,
            customLogoUri = null
        )
        refreshGeneratedQr(context)
    }

    fun computeCurrentPayload(): String {
        return when (_selectedTab.value) {
            ContentTypeTab.URL -> urlInput.value.trim()
            ContentTypeTab.TEXT -> textInput.value.trim()
            ContentTypeTab.WIFI -> QrPayloadParser.formatWifi(
                wifiSsid.value.trim(),
                wifiPassword.value.trim(),
                wifiAuthType.value,
                wifiIsHidden.value
            )
            ContentTypeTab.CONTACT -> QrPayloadParser.formatVCard(
                name = contactName.value.trim(),
                phone = contactPhone.value.trim(),
                email = contactEmail.value.trim(),
                company = contactCompany.value.trim(),
                title = contactJobTitle.value.trim(),
                url = contactWebsite.value.trim()
            )
            ContentTypeTab.EMAIL -> QrPayloadParser.formatEmail(
                to = emailAddress.value.trim(),
                subject = emailSubject.value.trim(),
                body = emailBody.value.trim()
            )
            ContentTypeTab.PHONE -> "tel:${phoneNumber.value.trim()}"
            ContentTypeTab.SMS -> QrPayloadParser.formatSms(
                phone = smsNumber.value.trim(),
                message = smsMessage.value.trim()
            )
            ContentTypeTab.SOCIAL -> {
                val user = socialUsername.value.trim().removePrefix("@")
                when (socialPlatform.value) {
                    "GitHub" -> "https://github.com/$user"
                    "LinkedIn" -> "https://linkedin.com/in/$user"
                    "Instagram" -> "https://instagram.com/$user"
                    "X / Twitter" -> "https://x.com/$user"
                    "YouTube" -> "https://youtube.com/@$user"
                    "TikTok" -> "https://tiktok.com/@$user"
                    else -> "https://social.me/$user"
                }
            }
        }
    }

    fun refreshGeneratedQr(context: Context?) {
        viewModelScope.launch {
            _isGenerating.value = true
            val payload = computeCurrentPayload()
            val config = _styleConfig.value

            val bitmap = withContext(Dispatchers.Default) {
                QrCodeGenerator.generateQrBitmap(
                    content = payload,
                    size = 1024,
                    config = config,
                    context = context
                )
            }
            _generatedBitmap.value = bitmap
            _isGenerating.value = false
        }
    }

    fun saveCurrentToHistory() {
        viewModelScope.launch {
            val payload = computeCurrentPayload()
            val tab = _selectedTab.value
            val config = _styleConfig.value

            val title = when (tab) {
                ContentTypeTab.URL -> urlInput.value.takeIf { it.isNotBlank() } ?: "Website Link"
                ContentTypeTab.TEXT -> textInput.value.take(30).ifBlank { "Text Note" }
                ContentTypeTab.WIFI -> "Wi-Fi: ${wifiSsid.value}"
                ContentTypeTab.CONTACT -> contactName.value.ifBlank { "Contact Card" }
                ContentTypeTab.EMAIL -> "Email: ${emailAddress.value}"
                ContentTypeTab.PHONE -> "Call: ${phoneNumber.value}"
                ContentTypeTab.SMS -> "SMS: ${smsNumber.value}"
                ContentTypeTab.SOCIAL -> "${socialPlatform.value}: @${socialUsername.value}"
            }

            val record = QrRecord(
                type = "GENERATED",
                contentType = tab.name,
                title = title,
                content = payload,
                fgColorHex = config.fgColorHex,
                bgColorHex = config.bgColorHex,
                eyeColorHex = config.eyeColorHex,
                stylePattern = config.dotStyle.name,
                eyeStyle = config.eyeStyle.name,
                logoPreset = config.logoPreset.name,
                customLogoUri = config.customLogoUri,
                logoSizePercent = config.logoSizePercent,
                brandLabel = config.brandLabel,
                brandSubtext = config.brandSubtext
            )
            repository.insertRecord(record)
        }
    }

    // Scanner Actions
    fun onQrScanned(rawContent: String) {
        if (rawContent.isBlank()) return
        val parsed = QrPayloadParser.parse(rawContent)
        _scanResult.value = parsed
        _showScanDialog.value = true

        // Save scanned QR into history automatically
        viewModelScope.launch {
            val record = QrRecord(
                type = "SCANNED",
                contentType = parsed.contentType,
                title = parsed.displayTitle,
                content = rawContent,
                brandLabel = "Scanned Code"
            )
            repository.insertRecord(record)
        }
    }

    fun dismissScanDialog() {
        _showScanDialog.value = false
        _scanResult.value = null
    }

    fun toggleFlashlight() {
        _isFlashlightOn.value = !_isFlashlightOn.value
    }

    fun toggleCameraFacing() {
        _isFrontCamera.value = !_isFrontCamera.value
    }

    fun decodeFromGalleryUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val text = withContext(Dispatchers.IO) {
                QrCodeDecoder.decodeUri(context, uri)
            }
            if (text != null) {
                onQrScanned(text)
            }
        }
    }

    // History Actions
    fun setHistoryFilter(filter: String) {
        _historyFilter.value = filter
    }

    fun toggleFavorite(record: QrRecord) {
        viewModelScope.launch {
            repository.toggleFavorite(record)
        }
    }

    fun deleteRecord(record: QrRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun loadRecordIntoStudio(record: QrRecord, context: Context?) {
        val parsed = QrPayloadParser.parse(record.content)
        when (parsed.contentType) {
            "URL" -> {
                _selectedTab.value = ContentTypeTab.URL
                urlInput.value = parsed.actionUrl ?: record.content
            }
            "WIFI" -> {
                _selectedTab.value = ContentTypeTab.WIFI
                wifiSsid.value = parsed.wifiSsid ?: ""
                wifiPassword.value = parsed.wifiPassword ?: ""
                wifiAuthType.value = parsed.wifiSecurity ?: "WPA"
            }
            "CONTACT" -> {
                _selectedTab.value = ContentTypeTab.CONTACT
                contactName.value = parsed.contactName ?: ""
                contactPhone.value = parsed.contactPhone ?: ""
                contactEmail.value = parsed.contactEmail ?: ""
            }
            "EMAIL" -> {
                _selectedTab.value = ContentTypeTab.EMAIL
                emailAddress.value = parsed.emailAddress ?: ""
                emailSubject.value = parsed.emailSubject ?: ""
                emailBody.value = parsed.emailBody ?: ""
            }
            "PHONE" -> {
                _selectedTab.value = ContentTypeTab.PHONE
                phoneNumber.value = parsed.phoneNumber ?: ""
            }
            "SMS" -> {
                _selectedTab.value = ContentTypeTab.SMS
                smsNumber.value = parsed.smsNumber ?: ""
                smsMessage.value = parsed.smsMessage ?: ""
            }
            else -> {
                _selectedTab.value = ContentTypeTab.TEXT
                textInput.value = record.content
            }
        }

        // Apply visual styling if present
        _styleConfig.value = QrStyleConfig(
            fgColorHex = record.fgColorHex,
            bgColorHex = record.bgColorHex,
            eyeColorHex = record.eyeColorHex,
            dotStyle = try { QrDotStyle.valueOf(record.stylePattern) } catch (e: Exception) { QrDotStyle.ROUNDED },
            eyeStyle = try { QrEyeStyle.valueOf(record.eyeStyle) } catch (e: Exception) { QrEyeStyle.ROUNDED },
            logoPreset = try { LogoPreset.valueOf(record.logoPreset) } catch (e: Exception) { LogoPreset.NONE },
            customLogoUri = record.customLogoUri,
            logoSizePercent = record.logoSizePercent,
            brandLabel = record.brandLabel,
            brandSubtext = record.brandSubtext
        )

        refreshGeneratedQr(context)
    }
}
