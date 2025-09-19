package test

import io.avaje.inject.PostConstruct
import io.avaje.inject.Prototype
import jakarta.inject.Singleton
import java.util.Optional

@Singleton
class TestService {
  @PostConstruct
  internal fun postConstruct() {
    println("${this::class.java.simpleName} postConstruct")
  }
}

@Prototype class OptionalService {}

@Singleton
class QueryResource(
    private val testService: TestService,
    private val optionalService: OptionalService?,
    private val s1: Optional<String>?,
    private val s2: String?,
) {
  @PostConstruct
  internal fun postConstruct() {
    println("${this::class.java.simpleName} postConstruct")
    println(testService)
    println(optionalService)
    println(s1)
    println(s2)
  }
}
