package com.github.ixtf.broker.application

import io.rsocket.RSocket
import io.vertx.kotlin.core.json.get

internal class BrokerServerRSocket(private var entity: BrokerServerEntity) : RSocket {}
