@file:Suppress("unused")

package com.gitee.ixtf.core

import cn.hutool.core.codec.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.collections.contentEquals
import kotlin.text.toCharArray

object Jcodec {
  @JvmStatic
  fun password(): String {
    return password("123456")
  }

  @JvmStatic
  fun password(password: String): String {
    val salt = ByteArray(16)
    SecureRandom.getInstance("SHA1PRNG").nextBytes(salt)
    val iterationCount = 30000
    val keySize = 160
    val hash = password(password, salt, iterationCount, keySize)
    val baos = ByteArrayOutputStream()
    baos.write(255)
    val oos = ObjectOutputStream(baos)
    oos.write(hash.size)
    oos.write(salt.size)
    oos.write(hash)
    oos.write(salt)
    oos.writeInt(iterationCount)
    oos.writeInt(keySize)
    oos.flush()
    return Base64.encode(baos.toByteArray())
  }

  @JvmStatic
  fun checkPassword(encryptPassword: String, password: String): Boolean {
    val bais = ByteArrayInputStream(Base64.decode(encryptPassword))
    //        val version = bais.read()
    if (bais.read() != 255) {
      return false
    }
    val ois = ObjectInputStream(bais)
    val hash = ByteArray(ois.read())
    val salt = ByteArray(ois.read())
    ois.read(hash)
    ois.read(salt)
    val iterationCount = ois.readInt()
    val keySize = ois.readInt()
    val _hash = password(password, salt, iterationCount, keySize)
    return hash.contentEquals(_hash)
  }

  private fun password(
      password: String,
      salt: ByteArray,
      iterationCount: Int,
      keySize: Int
  ): ByteArray {
    val spec = PBEKeySpec(password.toCharArray(), salt, iterationCount, keySize)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    return factory.generateSecret(spec).encoded
  }
}
