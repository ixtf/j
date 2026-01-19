package com.github.ixtf.broker

import com.github.ixtf.core.IxtfEnvString

var IXTF_BROKER_TARGET by IxtfEnvString("ixtf.broker.target", "0.0.0.0:39998")
var IXTF_BROKER_AUTH by IxtfEnvString("ixtf.broker.auth", IXTF_BROKER_TARGET)
