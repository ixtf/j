package test

import io.avaje.validation.Validator
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.io.Serializable
import org.junit.jupiter.api.Test

class TestValidator {
  @Test
  fun testI() {
    val validator = Validator.instance()
    validator.validate(Address("", listOf("")))
  }
}

data class Address(
    @NotBlank val street: String,
    @NotEmpty val suburb: List<@Valid @NotBlank String>,
) : Serializable
