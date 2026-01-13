package com.github.ixtf.broker

import com.github.ixtf.core.IxtfEnvString

object Env {
  var IXTF_API_BROKER_TARGET by IxtfEnvString("ixtf.api.broker.target", "0.0.0.0:39998")
  var IXTF_API_BROKER_AUTH by IxtfEnvString("ixtf.api.broker.auth")
}
