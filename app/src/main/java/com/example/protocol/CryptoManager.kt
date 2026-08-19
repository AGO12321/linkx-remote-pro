package com.example.protocol

import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager {
    
    private var rsaPrivateKey: PrivateKey? = null
    private var rsaPublicKey: PublicKey? = null
    
    private var aesSessionKey: SecretKey? = null

    init {
        generateRsaKeyPair()
    }

    private fun generateRsaKeyPair() {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(4096)
        val pair = keyGen.generateKeyPair()
        rsaPrivateKey = pair.private
        rsaPublicKey = pair.public
    }

    fun getPublicKeyBytes(): ByteArray {
        return rsaPublicKey!!.encoded
    }

    fun loadServerPublicKey(encodedKey: ByteArray): PublicKey {
        val spec = X509EncodedKeySpec(encodedKey)
        val factory = KeyFactory.getInstance("RSA")
        return factory.generatePublic(spec)
    }

    fun generateSessionKey(): ByteArray {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        aesSessionKey = keyGen.generateKey()
        return aesSessionKey!!.encoded
    }

    fun encryptSessionKeyWithServerRsa(serverPubKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, serverPubKey)
        return cipher.doFinal(aesSessionKey!!.encoded)
    }

    fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val nonce = ByteArray(12)
        SecureRandom().nextBytes(nonce)
        val spec = GCMParameterSpec(128, nonce)
        
        cipher.init(Cipher.ENCRYPT_MODE, aesSessionKey, spec)
        val ciphertext = cipher.doFinal(plaintext)
        
        // Append nonce to the beginning of the ciphertext so the receiver can decrypt
        val combined = ByteArray(nonce.size + ciphertext.size)
        System.arraycopy(nonce, 0, combined, 0, nonce.size)
        System.arraycopy(ciphertext, 0, combined, nonce.size, ciphertext.size)
        return combined
    }

    fun decrypt(encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val nonce = ByteArray(12)
        System.arraycopy(encrypted, 0, nonce, 0, nonce.size)
        val ciphertext = ByteArray(encrypted.size - nonce.size)
        System.arraycopy(encrypted, nonce.size, ciphertext, 0, ciphertext.size)
        
        val spec = GCMParameterSpec(128, nonce)
        cipher.init(Cipher.DECRYPT_MODE, aesSessionKey, spec)
        return cipher.doFinal(ciphertext)
    }
}
