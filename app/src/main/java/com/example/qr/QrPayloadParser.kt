package com.example.qr

data class ParsedQrContent(
    val rawText: String,
    val contentType: String, // "URL", "WIFI", "CONTACT", "EMAIL", "PHONE", "SMS", "GEO", "TEXT"
    val displayTitle: String,
    val summary: String,
    val actionUrl: String? = null,
    val wifiSsid: String? = null,
    val wifiPassword: String? = null,
    val wifiSecurity: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val contactEmail: String? = null,
    val emailAddress: String? = null,
    val emailSubject: String? = null,
    val emailBody: String? = null,
    val phoneNumber: String? = null,
    val smsNumber: String? = null,
    val smsMessage: String? = null,
    val geoLatitude: Double? = null,
    val geoLongitude: Double? = null
)

object QrPayloadParser {

    fun parse(raw: String): ParsedQrContent {
        val trimmed = raw.trim()

        // 1. Wi-Fi: WIFI:S:MySSID;T:WPA;P:MyPass;;
        if (trimmed.startsWith("WIFI:", ignoreCase = true) || trimmed.startsWith("wifi:", ignoreCase = true)) {
            val ssid = extractField(trimmed, "S:")
            val pass = extractField(trimmed, "P:")
            val auth = extractField(trimmed, "T:") ?: "WPA/WPA2"
            return ParsedQrContent(
                rawText = trimmed,
                contentType = "WIFI",
                displayTitle = ssid.ifBlank { "Wi-Fi Network" },
                summary = "Network: $ssid | Security: $auth",
                wifiSsid = ssid,
                wifiPassword = pass,
                wifiSecurity = auth
            )
        }

        // 2. vCard / MeCard: BEGIN:VCARD ... END:VCARD
        if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) || trimmed.startsWith("MECARD:", ignoreCase = true)) {
            var name = ""
            var phone = ""
            var email = ""

            if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true)) {
                val lines = trimmed.lines()
                for (line in lines) {
                    val lineTrim = line.trim()
                    if (lineTrim.startsWith("FN:", ignoreCase = true)) {
                        name = lineTrim.substring(3).trim()
                    } else if (lineTrim.startsWith("N:", ignoreCase = true) && name.isBlank()) {
                        name = lineTrim.substring(2).replace(";", " ").trim()
                    } else if (lineTrim.startsWith("TEL", ignoreCase = true)) {
                        phone = lineTrim.substringAfter(":").trim()
                    } else if (lineTrim.startsWith("EMAIL", ignoreCase = true)) {
                        email = lineTrim.substringAfter(":").trim()
                    }
                }
            } else {
                name = extractField(trimmed, "N:")
                phone = extractField(trimmed, "TEL:")
                email = extractField(trimmed, "EMAIL:")
            }

            return ParsedQrContent(
                rawText = trimmed,
                contentType = "CONTACT",
                displayTitle = name.ifBlank { "Contact Card" },
                summary = listOfNotNull(
                    name.takeIf { it.isNotBlank() },
                    phone.takeIf { it.isNotBlank() },
                    email.takeIf { it.isNotBlank() }
                ).joinToString(" • "),
                contactName = name,
                contactPhone = phone,
                contactEmail = email
            )
        }

        // 3. Email: mailto:test@example.com?subject=Hello&body=World
        if (trimmed.startsWith("mailto:", ignoreCase = true)) {
            val mailWithoutScheme = trimmed.substring(7)
            val address = mailWithoutScheme.substringBefore("?").trim()
            val query = if (mailWithoutScheme.contains("?")) mailWithoutScheme.substringAfter("?") else ""
            var subject = ""
            var body = ""
            if (query.isNotBlank()) {
                val parts = query.split("&")
                for (p in parts) {
                    if (p.startsWith("subject=", ignoreCase = true)) subject = java.net.URLDecoder.decode(p.substring(8), "UTF-8")
                    if (p.startsWith("body=", ignoreCase = true)) body = java.net.URLDecoder.decode(p.substring(5), "UTF-8")
                }
            }
            return ParsedQrContent(
                rawText = trimmed,
                contentType = "EMAIL",
                displayTitle = address.ifBlank { "Email" },
                summary = "To: $address" + if (subject.isNotBlank()) " | Subj: $subject" else "",
                actionUrl = trimmed,
                emailAddress = address,
                emailSubject = subject,
                emailBody = body
            )
        }

        // 4. Telephone: tel:+1234567890
        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val number = trimmed.substring(4).trim()
            return ParsedQrContent(
                rawText = trimmed,
                contentType = "PHONE",
                displayTitle = number,
                summary = "Phone Call: $number",
                actionUrl = trimmed,
                phoneNumber = number
            )
        }

        // 5. SMS: smsto:+123456:Hello or sms:+123456?body=Hello
        if (trimmed.startsWith("smsto:", ignoreCase = true) || trimmed.startsWith("sms:", ignoreCase = true)) {
            val prefix = if (trimmed.startsWith("smsto:", ignoreCase = true)) "smsto:" else "sms:"
            val rest = trimmed.substring(prefix.length)
            val number = if (rest.contains(":")) rest.substringBefore(":") else rest.substringBefore("?")
            val msg = if (rest.contains(":")) rest.substringAfter(":") else if (rest.contains("?body=")) rest.substringAfter("?body=") else ""
            return ParsedQrContent(
                rawText = trimmed,
                contentType = "SMS",
                displayTitle = number.ifBlank { "Send SMS" },
                summary = "To: $number" + if (msg.isNotBlank()) " | Msg: $msg" else "",
                actionUrl = trimmed,
                smsNumber = number,
                smsMessage = msg
            )
        }

        // 6. Geo: geo:37.7749,-122.4194
        if (trimmed.startsWith("geo:", ignoreCase = true)) {
            val coords = trimmed.substring(4).substringBefore("?").split(",")
            val lat = coords.getOrNull(0)?.toDoubleOrNull()
            val lon = coords.getOrNull(1)?.toDoubleOrNull()
            return ParsedQrContent(
                rawText = trimmed,
                contentType = "GEO",
                displayTitle = "Location Coordinates",
                summary = if (lat != null && lon != null) "Lat: $lat, Lon: $lon" else trimmed,
                actionUrl = trimmed,
                geoLatitude = lat,
                geoLongitude = lon
            )
        }

        // 7. Web URL: http://, https://, or www.
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("www.", ignoreCase = true) ||
            (trimmed.contains(".") && !trimmed.contains(" ") && trimmed.length > 4 && (trimmed.endsWith(".com") || trimmed.endsWith(".org") || trimmed.endsWith(".net") || trimmed.endsWith(".io") || trimmed.endsWith(".app") || trimmed.endsWith(".me") || trimmed.endsWith(".dev")))
        ) {
            val validUrl = if (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true)) {
                "https://$trimmed"
            } else trimmed

            return ParsedQrContent(
                rawText = trimmed,
                contentType = "URL",
                displayTitle = trimmed,
                summary = "Web Link: $trimmed",
                actionUrl = validUrl
            )
        }

        // 8. Plain Text fallback
        val firstLine = trimmed.lines().firstOrNull()?.take(40) ?: "Text Message"
        return ParsedQrContent(
            rawText = trimmed,
            contentType = "TEXT",
            displayTitle = firstLine,
            summary = trimmed.take(120) + if (trimmed.length > 120) "..." else ""
        )
    }

    private fun extractField(source: String, key: String): String {
        val index = source.indexOf(key, ignoreCase = true)
        if (index == -1) return ""
        val start = index + key.length
        val end = source.indexOf(";", start)
        return if (end != -1) source.substring(start, end) else source.substring(start)
    }

    // Helper builders for generating formatted QR payload strings
    fun formatWifi(ssid: String, password: String, security: String = "WPA", hidden: Boolean = false): String {
        val auth = if (security.equals("None", ignoreCase = true) || security.isBlank()) "nopass" else security
        val h = if (hidden) "true" else "false"
        return "WIFI:S:$ssid;T:$auth;P:$password;H:$h;;"
    }

    fun formatVCard(name: String, phone: String = "", email: String = "", company: String = "", title: String = "", url: String = ""): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:$name")
            if (company.isNotBlank()) appendLine("ORG:$company")
            if (title.isNotBlank()) appendLine("TITLE:$title")
            if (phone.isNotBlank()) appendLine("TEL;TYPE=CELL:$phone")
            if (email.isNotBlank()) appendLine("EMAIL;TYPE=WORK:$email")
            if (url.isNotBlank()) appendLine("URL:$url")
            appendLine("END:VCARD")
        }
    }

    fun formatEmail(to: String, subject: String = "", body: String = ""): String {
        val encodedSubj = java.net.URLEncoder.encode(subject, "UTF-8").replace("+", "%20")
        val encodedBody = java.net.URLEncoder.encode(body, "UTF-8").replace("+", "%20")
        val params = mutableListOf<String>()
        if (subject.isNotBlank()) params.add("subject=$encodedSubj")
        if (body.isNotBlank()) params.add("body=$encodedBody")
        val query = if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
        return "mailto:$to$query"
    }

    fun formatSms(phone: String, message: String = ""): String {
        return if (message.isNotBlank()) "smsto:$phone:$message" else "smsto:$phone"
    }
}
