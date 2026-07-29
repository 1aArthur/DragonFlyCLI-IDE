package com.example.utils.git

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class GitCredentialsState(
    val username: String = "",
    val tokenOrSshKey: String = "",
    val remoteUrl: String = "",
    val isSecuredInKeystore: Boolean = false,
    val lastSyncStatus: String = "Pronto"
)

/**
 * Android Keystore Hardware-Backed Encryption Helper for Git Credentials
 */
class GitKeystoreManager(private val context: Context) {

    private val KEY_ALIAS = "DragonflyGitKeystoreAlias"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val PREFS_NAME = "dragonfly_git_keystore_prefs"
    private val PREF_USERNAME = "enc_username"
    private val PREF_TOKEN = "enc_token"
    private val PREF_URL = "enc_url"
    private val PREF_IV = "enc_iv"

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(GitCredentialsState())
    val state: StateFlow<GitCredentialsState> = _state.asStateFlow()

    init {
        loadCredentials()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    fun saveCredentials(username: String, tokenOrSshKey: String, remoteUrl: String): Boolean {
        return try {
            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val combinedPayload = "$username::$tokenOrSshKey::$remoteUrl"
            val encryptedBytes = cipher.doFinal(combinedPayload.toByteArray(Charsets.UTF_8))

            val encBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

            prefs.edit()
                .putString(PREF_TOKEN, encBase64)
                .putString(PREF_IV, ivBase64)
                .apply()

            _state.value = GitCredentialsState(
                username = username,
                tokenOrSshKey = tokenOrSshKey,
                remoteUrl = remoteUrl,
                isSecuredInKeystore = true,
                lastSyncStatus = "✔ Credenciais salvas com segurança no Android Keystore hardware-backed."
            )
            true
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                lastSyncStatus = "❌ Falha ao criptografar no Keystore: ${e.localizedMessage}"
            )
            false
        }
    }

    fun loadCredentials(): GitCredentialsState {
        try {
            val encBase64 = prefs.getString(PREF_TOKEN, null)
            val ivBase64 = prefs.getString(PREF_IV, null)

            if (encBase64 == null || ivBase64 == null) {
                _state.value = GitCredentialsState(
                    isSecuredInKeystore = false,
                    lastSyncStatus = "Nenhuma credencial configurada no Android Keystore."
                )
                return _state.value
            }

            val secretKey = getOrCreateSecretKey()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val encryptedBytes = Base64.decode(encBase64, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            val decryptedPayload = String(decryptedBytes, Charsets.UTF_8)

            val parts = decryptedPayload.split("::")
            val username = parts.getOrNull(0) ?: ""
            val token = parts.getOrNull(1) ?: ""
            val url = parts.getOrNull(2) ?: ""

            _state.value = GitCredentialsState(
                username = username,
                tokenOrSshKey = token,
                remoteUrl = url,
                isSecuredInKeystore = true,
                lastSyncStatus = "✔ Credenciais recuperadas do Android Keystore."
            )
        } catch (e: Exception) {
            _state.value = GitCredentialsState(
                isSecuredInKeystore = false,
                lastSyncStatus = "Aviso: Nenhuma chave no Keystore ou alteração de hardware."
            )
        }
        return _state.value
    }

    fun clearCredentials() {
        prefs.edit().clear().apply()
        _state.value = GitCredentialsState(
            username = "",
            tokenOrSshKey = "",
            remoteUrl = "",
            isSecuredInKeystore = false,
            lastSyncStatus = "Credenciais removidas do Android Keystore."
        )
    }
}
