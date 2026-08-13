package com.android.zenspend.feature.category.domain.suggestion

import org.junit.Assert.assertEquals
import org.junit.Test

class MerchantNormalizerTest {

    @Test
    fun `test merchant normalization`() {
        assertEquals("AMAZON", MerchantNormalizer.normalize("AMAZON INDIA"))
        assertEquals("AMAZON", MerchantNormalizer.normalize("Amazon.in"))
        assertEquals("SWIGGY", MerchantNormalizer.normalize("Swiggy Ltd"))
        assertEquals("SWIGGY", MerchantNormalizer.normalize("SWIGGY INSTAMART"))
        assertEquals("ZOMATO", MerchantNormalizer.normalize("Zomato Online"))
        assertEquals("UBER", MerchantNormalizer.normalize("UBER PAY"))
        assertEquals("BLINKIT", MerchantNormalizer.normalize("Blinkit Market"))
    }
}
