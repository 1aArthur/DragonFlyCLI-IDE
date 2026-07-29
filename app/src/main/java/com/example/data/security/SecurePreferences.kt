package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("dragonfly_secure_prefs", Context.MODE_PRIVATE)

    private val alias = "DragonflyMasterKey"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    init {
        generateSecretKey()
    }

    private fun generateSecretKey() {
        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val entry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    fun saveEncryptedString(key: String, value: String) {
        if (value.isBlank()) {
            sharedPreferences.edit().remove(key).apply()
            return
        }
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            val base64 = Base64.encodeToString(combined, Base64.DEFAULT)
            sharedPreferences.edit().putString(key, base64).apply()
        } catch (e: Exception) {
            // Fallback to direct key storage if keystore fails on specific custom ROMs
            sharedPreferences.edit().putString(key, "PLAIN:$value").apply()
        }
    }

    fun getEncryptedString(key: String, defaultValue: String = ""): String {
        val stored = sharedPreferences.getString(key, null) ?: return defaultValue
        if (stored.startsWith("PLAIN:")) {
            return stored.removePrefix("PLAIN:")
        }
        return try {
            val combined = Base64.decode(stored, Base64.DEFAULT)
            if (combined.size <= 12) return defaultValue

            val iv = ByteArray(12)
            System.arraycopy(combined, 0, iv, 0, 12)

            val encryptedBytes = ByteArray(combined.size - 12)
            System.arraycopy(combined, 12, encryptedBytes, 0, encryptedBytes.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }
}
