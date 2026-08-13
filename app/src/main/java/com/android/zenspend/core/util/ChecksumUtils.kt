package com.android.zenspend.core.util

import java.security.MessageDigest

object ChecksumUtils {
    fun calculateSha256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
