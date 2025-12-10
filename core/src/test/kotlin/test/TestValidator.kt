package test

import io.avaje.validation.Validator
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

fun main() {
  val validator = Validator.instance()
    validator.validate(Address("", listOf("")))
}

data class Address(
    @field:NotBlank val street: String,
    @field:NotEmpty val suburb: List<@Valid @NotBlank String>,
)
