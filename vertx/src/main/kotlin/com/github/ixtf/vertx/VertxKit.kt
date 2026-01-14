package com.github.ixtf.vertx

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ixtf.core.MAPPER
import io.vertx.core.Expectation
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpResponseExpectation
import io.vertx.ext.web.client.HttpResponse

object ScSuccess : Expectation<HttpResponse<*>> {
  override fun test(res: HttpResponse<*>?): Boolean = HttpResponseExpectation.SC_SUCCESS.test(res)

  override fun describe(value: HttpResponse<*>?): Throwable {
    return RuntimeException(value?.bodyAsString())
  }
}

inline fun <reified T> Buffer.readValue(): T = MAPPER.readValue<T>(bytes)
