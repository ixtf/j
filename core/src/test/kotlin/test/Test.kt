package test

import com.gitee.ixtf.core.J
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
private fun nonEmpty(o: Iterable<*>?): Boolean {
  contract { returns(true) implies (o != null) }
  return J.nonEmpty(o)
}

fun main() {
  requireNotNull("")
}

private fun test1(o: Iterable<*>?) {
  if (J.isEmpty(o)) return
  o.forEach { println(it) }
}

private fun test2(o: Iterable<*>?) {
  if (J.nonEmpty(o)) {
    o.forEach { println(it) }
  }
}
