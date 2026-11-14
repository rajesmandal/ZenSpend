package com.android.zenspend.feature.emi.domain

/** Common Indian banks, NBFCs and loan apps offered while typing a lender name. */
object LenderSuggestions {

    val known: List<String> = listOf(
        // Banks
        "State Bank of India", "HDFC Bank", "ICICI Bank", "Axis Bank", "Kotak Mahindra Bank",
        "Punjab National Bank", "Bank of Baroda", "Canara Bank", "Union Bank of India",
        "Bank of India", "Indian Bank", "Central Bank of India", "Indian Overseas Bank",
        "UCO Bank", "Bank of Maharashtra", "Punjab & Sind Bank", "IDBI Bank", "IndusInd Bank",
        "Yes Bank", "Federal Bank", "South Indian Bank", "Karur Vysya Bank", "RBL Bank",
        "IDFC FIRST Bank", "Bandhan Bank", "AU Small Finance Bank", "Equitas Small Finance Bank",
        "Ujjivan Small Finance Bank", "Jana Small Finance Bank", "Standard Chartered Bank",
        "HSBC", "Citibank", "DBS Bank", "City Union Bank", "Dhanlaxmi Bank", "Tamilnad Mercantile Bank",
        "Jammu & Kashmir Bank", "Karnataka Bank", "Bank of Bahrain & Kuwait", "Paytm Payments Bank",
        "India Post Payments Bank", "Airtel Payments Bank",
        // NBFCs / housing finance
        "Bajaj Finance", "Bajaj Finserv", "HDB Financial Services", "Tata Capital", "Tata Capital Finance",
        "Aditya Birla Finance", "Aditya Birla Capital", "Mahindra Finance", "Muthoot Finance",
        "Manappuram Finance", "Shriram Finance", "Shriram Transport Finance", "Cholamandalam Finance",
        "L&T Finance", "Poonawalla Fincorp", "Fullerton India", "IIFL Finance", "IIFL Home Finance",
        "Hero FinCorp", "Piramal Finance", "Capri Global Capital", "Kotak Mahindra Prime",
        "Sundaram Finance", "Muthoot Fincorp", "Edelweiss Finance", "Clix Capital", "Home Credit India",
        "HDFC Ltd", "LIC Housing Finance", "PNB Housing Finance", "Bajaj Housing Finance",
        "Aditya Birla Housing Finance", "Tata Capital Housing Finance", "Aavas Financiers",
        "Can Fin Homes", "Repco Home Finance", "Piramal Capital & Housing Finance",
        // Loan / BNPL / EMI apps
        "Navi", "KreditBee", "MoneyView", "MoneyTap", "Paytm Postpaid", "LazyPay", "Slice", "Simpl",
        "ZestMoney", "EarlyShe", "Fibe (EarlySalary)", "CASHe", "Kissht", "Flexsalary", "Axio",
        "PhonePe", "Google Pay", "Amazon Pay Later", "Flipkart Pay Later", "Mobikwik ZIP",
        "Home Credit", "Samsung Finance+", "Bajaj Finserv EMI Network"
    )

    /**
     * Every name containing [query] (case-insensitive), your own saved lenders first.
     * Nothing is returned for a blank query or when the field already holds exactly one match.
     */
    fun matching(query: String, saved: List<String>): List<String> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        val all = (saved + known).filter { it.isNotBlank() }.distinctBy { it.lowercase() }
        val hits = all.filter { it.contains(q, ignoreCase = true) }
        return if (hits.size == 1 && hits.first().equals(q, ignoreCase = true)) emptyList() else hits
    }
}
