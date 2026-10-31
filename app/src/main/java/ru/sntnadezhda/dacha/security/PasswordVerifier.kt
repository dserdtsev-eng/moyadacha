package ru.sntnadezhda.dacha.security

import android.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import java.security.MessageDigest

object PasswordVerifier {

    // Параметры ДОЛЖНЫ совпадать с Python-версией (CHT_Nadezhda_v4.0.py):
    //   PASSPORT_HASH_ITERATIONS = 100_000
    //   PASSPORT_HASH_KEYLEN = 32   (байт)
    //   PASSPORT_SALT_LEN = 16      (байт)
    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH_BITS = 32 * 8   // 256 бит

    /**
     * Проверяет 4-значный пароль садовода.
     *
     * @param password    введённый пароль (4 цифры)
     * @param storedHash  base64-строка из поля passport_hash
     * @param storedSalt  base64-строка из поля passport_salt
     * @return true, если пароль верный
     */
    fun verify(password: String, storedHash: String, storedSalt: String): Boolean {
        if (storedHash.isBlank() || storedSalt.isBlank()) return false
        return try {
            val salt = Base64.decode(storedSalt, Base64.NO_WRAP)

            val spec = PBEKeySpec(
                password.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH_BITS
            )

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hashBytes = factory.generateSecret(spec).encoded

            val computedHash = Base64.encodeToString(hashBytes, Base64.NO_WRAP)

            // Сравнение с постоянным временем — защита от timing-атак
            MessageDigest.isEqual(
                computedHash.toByteArray(),
                storedHash.toByteArray()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}