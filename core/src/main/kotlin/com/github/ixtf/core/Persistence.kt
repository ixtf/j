package com.github.ixtf.core

import com.github.ixtf.core.kotlinx.date
import com.github.ixtf.core.kotlinx.ldt
import com.google.common.collect.Lists
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.javaClass
import kotlin.let
import kotlin.takeUnless

interface IEntity : Serializable {
  val id: String
  val deleted: Boolean
}

interface IOperator : Serializable {
  val id: String
  val name: String
}

interface IEntityLoggable : IEntity {
  var creator: DefaultOperator?
  var createDateTime: Date?
  var modifier: DefaultOperator?
  var modifyDateTime: Date?

  fun log(o: IOperator, date: Date = Date()) {
    modifier = o as? DefaultOperator ?: DefaultOperator(o)
    modifyDateTime = date
    if (creator == null || creator == modifier) creator = modifier
    if (createDateTime == null) createDateTime = date
  }

  fun log(o: IEntityLoggable) {
    creator = o.creator
    createDateTime = o.createDateTime
    modifier = o.modifier
    modifyDateTime = o.modifyDateTime
  }
}

data class DefaultOperator(
    @field:NotBlank @param:NotBlank override val id: String,
    @field:NotBlank @param:NotBlank override var name: String,
) : IOperator {
  constructor(o: IOperator) : this(id = o.id, name = o.name)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as DefaultOperator
    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}

data class ProcessDuration(
    val startDateTime: Date = Date(),
    val endDateTime: Date? = null,
    val seconds: Long? = null,
) {
  fun onEnd(): ProcessDuration {
    val endLdt = LocalDateTime.now()
    val startLdt = startDateTime.ldt()
    val between = Duration.between(startLdt, endLdt)
    return copy(endDateTime = endLdt.date(), seconds = between.seconds)
  }
}

enum class SortStart {
  asc,
  desc
}

@JvmRecord data class Sort(val id: String, val start: SortStart)

@JvmRecord
data class PageQueryResult<T>(
    val first: Int,
    val pageSize: Int,
    val count: Long,
    val data: Collection<T>
)

@JvmRecord data class EntityDTO(@field:NotBlank val id: String)

@JvmRecord data class EntitiesDTO(@field:NotEmpty val ids: Set<@Valid @NotBlank String>)

interface UnitOfWork {
  fun registerSave(o: IEntity)

  fun registerNew(o: IEntity)

  fun registerDirty(o: IEntity)

  fun registerDelete(o: IEntity)

  fun registerPurge(o: IEntity)

  suspend fun commit()

  suspend fun rollback()
}

abstract class AbstractUnitOfWork : UnitOfWork {
  protected val saveList: MutableList<IEntity> =
      Collections.synchronizedList(Lists.newArrayList<IEntity>())
  protected val newList: MutableList<IEntity> =
      Collections.synchronizedList(Lists.newArrayList<IEntity>())
  protected val dirtyList: MutableList<IEntity> =
      Collections.synchronizedList(Lists.newArrayList<IEntity>())
  protected val deleteList: MutableList<IEntity> =
      Collections.synchronizedList(Lists.newArrayList<IEntity>())
  protected val purgeList: MutableList<IEntity> =
      Collections.synchronizedList(Lists.newArrayList<IEntity>())

  @Synchronized
  override fun registerSave(o: IEntity) {
    o.takeUnless { saveList.contains(o) }?.let { saveList.add(it) }
  }

  @Synchronized
  override fun registerNew(o: IEntity) {
    o.takeUnless { newList.contains(o) }?.let { newList.add(it) }
  }

  @Synchronized
  override fun registerDirty(o: IEntity) {
    o.takeUnless { dirtyList.contains(o) }?.let { dirtyList.add(it) }
  }

  @Synchronized
  override fun registerDelete(o: IEntity) {
    o.takeUnless { deleteList.contains(o) }?.let { deleteList.add(it) }
  }

  @Synchronized
  override fun registerPurge(o: IEntity) {
    o.takeUnless { purgeList.contains(o) }?.let { purgeList.add(it) }
  }
}
