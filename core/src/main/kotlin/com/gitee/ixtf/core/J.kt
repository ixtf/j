@file:Suppress("unused")

package com.gitee.ixtf.core

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.collection.IterUtil
import cn.hutool.core.date.DateUtil
import cn.hutool.core.date.TimeInterval
import cn.hutool.core.io.FileUtil
import cn.hutool.core.map.MapUtil
import cn.hutool.core.util.ArrayUtil
import cn.hutool.core.util.IdUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.crypto.digest.DigestUtil
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.gitee.ixtf.core.cli.SaferExec
import com.gitee.ixtf.core.kotlinx.*
import jakarta.validation.ConstraintViolationException
import java.io.File
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAccessor
import java.util.*
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object J {
  fun timer(): TimeInterval = DateUtil.timer()

  @OptIn(ExperimentalContracts::class)
  inline fun <T> J.timer(s: String, block: () -> T): T {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val timer = timer()
    val ret = block()
    println("$s: ${timer.intervalPretty()}")
    return ret
  }

  fun exec(vararg command: String, waitTime: Int = 0): String = SaferExec(waitTime).exec(*command)

  fun execV(vararg command: String, waitTime: Int = 0): String = SaferExec(waitTime).execV(*command)

  @JvmStatic fun file(file: File, vararg o: String): File = FileUtil.file(file, *o)

  @JvmStatic fun file(p: String, vararg o: String): File = FileUtil.file(p, *o)

  @JvmStatic
  fun <T> inputCommand(o: T): T =
      o.apply {
        val violations = VALIDATOR.validate(o)
        if (violations.isNotEmpty()) throw ConstraintViolationException(violations)
      }

  @JvmStatic fun objectId(): String = IdUtil.objectId()

  @JvmStatic fun nanoId(): String = IdUtil.nanoId()

  @JvmStatic fun password(o: String = "123456"): String = DigestUtil.bcrypt(o)

  @JvmStatic
  fun passwordCheck(password: String, hashed: String) = DigestUtil.bcryptCheck(password, hashed)

  @JvmStatic fun objectNode(): ObjectNode = JsonNodeFactory.instance.objectNode()

  @JvmStatic fun arrayNode(): ArrayNode = JsonNodeFactory.instance.arrayNode()

  @JvmStatic
  fun localIp(): String =
      DatagramSocket().use {
        it.connect(InetAddress.getByName("8.8.8.8"), 10002)
        it.localAddress.hostAddress
      }

  @JvmStatic fun fileName(o: File): String = FileUtil.getName(o)

  @JvmStatic fun mainName(o: File): String = FileUtil.mainName(o)

  @JvmStatic fun extName(o: File): String = FileUtil.extName(o)

  @JvmStatic fun fileName(o: String): String = FileUtil.getName(o)

  @JvmStatic fun mainName(o: String): String = FileUtil.mainName(o)

  @JvmStatic fun extName(o: String): String = FileUtil.extName(o)

  @JvmStatic fun <T> inputCommand(o: File, type: Class<T>): T = inputCommand(readJson(o, type))

  @JvmStatic fun <T> inputCommand(o: String, type: Class<T>): T = inputCommand(readJson(o, type))

  @JvmStatic fun <T> inputCommand(o: ByteArray, type: Class<T>): T = inputCommand(readJson(o, type))

  @JvmStatic fun readJson(o: File): JsonNode = o.readJson()

  @JvmStatic fun <T> readJson(o: File, type: Class<T>): T = objectMap(o.name).readValue(o, type)

  @JvmStatic
  fun <T> readJson(o: File, ref: TypeReference<T>): T = objectMap(o.name).readValue(o, ref)

  @JvmStatic fun readJsonFile(o: String) = readJson(file(o))

  @JvmStatic fun <T> readJsonFile(o: String, type: Class<T>) = readJson(file(o), type)

  @JvmStatic fun <T> readJsonFile(o: String, ref: TypeReference<T>): T = readJson(file(o), ref)

  @JvmStatic fun readJson(o: String): JsonNode = o.readJson()

  @JvmStatic fun <T> readJson(o: String, type: Class<T>): T = MAPPER.readValue(o, type)

  @JvmStatic fun <T> readJson(o: String, ref: TypeReference<T>): T = MAPPER.readValue(o, ref)

  @JvmStatic fun readJson(o: ByteArray): JsonNode = o.readJson()

  @JvmStatic fun <T> readJson(o: ByteArray, type: Class<T>): T = MAPPER.readValue(o, type)

  @JvmStatic fun <T> readJson(o: ByteArray, ref: TypeReference<T>): T = MAPPER.readValue(o, ref)

  @JvmStatic fun writeJson(file: File, o: Any) = file.writeJson(o)

  @JvmStatic fun writeJson(s: String, o: Any) = writeJson(file(s), o)

  @JvmStatic fun date(): Date = DateUtil.date()

  @JvmStatic fun date(o: Long): Date = DateUtil.date(o)

  @JvmStatic fun date(o: TemporalAccessor): Date = DateUtil.date(o)

  @JvmStatic fun ldt(o: Date): LocalDateTime = DateUtil.toLocalDateTime(o)

  @JvmStatic fun ld(o: Date): LocalDate = DateUtil.toLocalDateTime(o).toLocalDate()

  @JvmStatic fun lt(o: Date): LocalTime = DateUtil.toLocalDateTime(o).toLocalTime()

  @JvmStatic fun isBlank(o: CharSequence?) = StrUtil.isBlank(o)

  @JvmStatic
  @OptIn(ExperimentalContracts::class)
  fun nonBlank(o: CharSequence?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isBlank(o)
  }

  @JvmStatic
  fun blankToDefault(o: CharSequence?, str: String): String = StrUtil.blankToDefault(o, str)

  @JvmStatic fun blankToDefault(o: CharSequence?): String = blankToDefault(o, StrUtil.EMPTY)

  @JvmStatic fun <T> stream(o: Iterable<T>?): Stream<T> = stream(o, false)

  @JvmStatic
  fun <T> stream(o: Iterable<T>?, parallel: Boolean = false): Stream<T> =
      o?.run { StreamSupport.stream(spliterator(), parallel) } ?: Stream.empty()

  @JvmStatic
  @OptIn(ExperimentalContracts::class)
  fun isEmpty(o: Collection<*>?): Boolean {
    contract { returns(false) implies (o != null) }
    return CollUtil.isEmpty(o)
  }

  @JvmStatic
  @OptIn(ExperimentalContracts::class)
  fun nonEmpty(o: Collection<*>?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isEmpty(o)
  }

  @JvmStatic
  @OptIn(ExperimentalContracts::class)
  fun isEmpty(o: Iterable<*>?): Boolean {
    contract { returns(false) implies (o != null) }
    return IterUtil.isEmpty(o)
  }

  @JvmStatic
  @OptIn(ExperimentalContracts::class)
  fun nonEmpty(o: Iterable<*>?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isEmpty(o)
  }

  @JvmStatic
  @OptIn(ExperimentalContracts::class)
  fun isEmpty(o: Map<*, *>?): Boolean {
    contract { returns(false) implies (o != null) }
    return MapUtil.isEmpty(o)
  }

  @JvmStatic
  @OptIn(ExperimentalContracts::class)
  fun nonEmpty(o: Map<*, *>?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isEmpty(o)
  }

  @JvmStatic
  @OptIn(ExperimentalContracts::class)
  fun isEmpty(o: Array<*>?): Boolean {
    contract { returns(false) implies (o != null) }
    return ArrayUtil.isEmpty(o)
  }

  @JvmStatic
  @OptIn(ExperimentalContracts::class)
  fun nonEmpty(o: Array<*>?): Boolean {
    contract { returns(true) implies (o != null) }
    return !isEmpty(o)
  }

  @JvmStatic fun base58(o: ByteArray) = o.base58()

  @JvmStatic fun sha256Hex(o: File) = o.sha256Hex()

  @JvmStatic fun sm3Hex(o: File) = o.sm3Hex()

  @JvmStatic fun <T : Iterable<*>> emptyToNull(o: T?): T? = if (nonEmpty(o)) o else null

  @JvmStatic fun <T : Map<*, *>> emptyToNull(o: T?): T? = if (nonEmpty(o)) o else null

  @JvmStatic fun <T> emptyToNull(o: Array<T>?): Array<T>? = if (nonEmpty(o)) o else null

  @JvmStatic
  fun base58(id1: String, id2: String, vararg data: String): String =
      listOf(id1, id2, *data).run {
        val ids = joinToString()
        base58(ids.toByteArray(UTF_8))
      }
}
