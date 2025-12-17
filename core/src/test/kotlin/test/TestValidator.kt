package test

import io.avaje.validation.ImportValidPojo
import io.avaje.validation.Validator
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.junit.jupiter.api.Test
import java.io.Serializable

class TestValidator {
  @Test
  fun testI() {
    val validator = Validator.instance()
    validator.validate(Address("", listOf("")))
  }
}

@Valid
@JvmRecord
data class Address(
    @NotBlank val street: String,
    @NotEmpty val suburb: List<@Valid @NotBlank String>,
): Serializable
