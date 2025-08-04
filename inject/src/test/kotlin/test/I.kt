package test

import io.avaje.inject.PostConstruct
import io.avaje.inject.Prototype
import jakarta.inject.Singleton

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
) {
  @PostConstruct
  internal fun postConstruct() {
    println("${this::class.java.simpleName} postConstruct")
    println(testService)
    println(optionalService)
  }
}
