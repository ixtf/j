package test

import com.github.ixtf.inject.DI
import java.util.Optional

private val r: QueryResource by DI
private val s: Optional<String> by DI

fun main() {
  println("test")

  println(DI.get<QueryResource>())
  println(r)

  println(DI.get<Optional<String>>())
}
