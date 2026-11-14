package com.android.zenspend.feature.creditcard.domain.suggestion

/** Turns an SMS sender id ("AD-HDFCBK-S", "VM-SBIINB") into a bank / card brand name, when known. */
object BankSenders {

    private val byKeyword = listOf(
        "HDFC" to "HDFC Bank", "ICICI" to "ICICI Bank", "SBI" to "State Bank of India",
        "AXIS" to "Axis Bank", "KOTAK" to "Kotak Mahindra Bank", "PNB" to "Punjab National Bank",
        "BOB" to "Bank of Baroda", "CANBNK" to "Canara Bank", "IDFC" to "IDFC FIRST Bank",
        "YESBNK" to "Yes Bank", "INDUS" to "IndusInd Bank", "AMEX" to "American Express",
        "SCBANK" to "Standard Chartered Bank", "STDCHR" to "Standard Chartered Bank", "HSBC" to "HSBC",
        "RBL" to "RBL Bank", "AUBANK" to "AU Small Finance Bank", "FEDBNK" to "Federal Bank",
        "IDBI" to "IDBI Bank", "UNIONB" to "Union Bank of India", "BOIIND" to "Bank of India",
        "ONECRD" to "OneCard", "ONECARD" to "OneCard", "SLICE" to "Slice", "CITI" to "Citibank"
    )

    fun bankName(sender: String): String? {
        // The brand is the middle part: operator prefix "AD-", brand, optional "-S" / "-T" suffix.
        val parts = sender.uppercase().split('-')
        val brand = if (parts.size >= 2) parts[1] else parts.first()
        return byKeyword.firstOrNull { brand.contains(it.first) }?.second
    }
}
