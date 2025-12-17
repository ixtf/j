package com.github.ixtf.inject

import java.util.Optional
import org.junit.jupiter.api.Test

private val r: QueryResource by DI
private val s: Optional<String> by DI

class TestDI {
  @Test
  fun testI() {
    println("test")

    println(DI.get<QueryResource>())
    println(r)

    println(DI.get<Optional<String>>())
  }
}
