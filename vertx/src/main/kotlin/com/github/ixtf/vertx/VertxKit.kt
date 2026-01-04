package com.github.ixtf.vertx

import io.vertx.core.Expectation
import io.vertx.core.http.HttpResponseExpectation
import io.vertx.ext.web.client.HttpResponse

object ScSuccess : Expectation<HttpResponse<*>> {
  override fun test(res: HttpResponse<*>?): Boolean = HttpResponseExpectation.SC_SUCCESS.test(res)

  override fun describe(value: HttpResponse<*>?): Throwable? {
    return RuntimeException(value?.bodyAsString())
  }
}
