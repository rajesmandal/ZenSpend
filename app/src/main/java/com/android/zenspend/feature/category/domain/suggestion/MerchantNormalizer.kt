package com.android.zenspend.feature.category.domain.suggestion

object MerchantNormalizer {
    private val STOP_WORDS = listOf("INDIA", "LTD", "LIMITED", "PVT", "CORP", "SERVICES", "SERVICE", "PAY", "ONLINE", "WWW", "COM", "INSTAMART", "MARKET")
    private val DOMAIN_PATTERN = "\\.(com|in|org|net|co)$".toRegex(RegexOption.IGNORE_CASE)

    fun normalize(rawMerchant: String?): String? {
        if (rawMerchant.isNullOrBlank()) return null

        val name = rawMerchant.uppercase().trim()
            .replace(DOMAIN_PATTERN, "")
            .replace("[^A-Z0-9\\s]".toRegex(), " ")
            .split("\\s+".toRegex()) // Split by whitespace
            .filter { it.isNotBlank() && !STOP_WORDS.contains(it) }
            .joinToString(" ")
            .trim()

        // Common manual overrides
        if (name.contains("AMAZON")) return "AMAZON"
        if (name.contains("SWIGGY")) return "SWIGGY"
        if (name.contains("ZOMATO")) return "ZOMATO"
        if (name.contains("UBER")) return "UBER"
        if (name.contains("BLINKIT")) return "BLINKIT"
        if (name.contains("ZEPTO")) return "ZEPTO"
        if (name.contains("OLA")) return "OLA"
        if (name.contains("NETFLIX")) return "NETFLIX"
        if (name.contains("SPOTIFY")) return "SPOTIFY"

        return if (name.isBlank()) rawMerchant.uppercase().trim() else name
    }
}
