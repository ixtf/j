package test

import com.github.ixtf.inject.DI

fun main() {
  println("test")

  println(DI.get<QueryResource>())
}
