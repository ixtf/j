package com.github.ixtf.broker.domain.event

sealed interface BrokerEvent {
  data class Registered(val service: String, val principal: String, val tags: Set<String>? = null) :
    BrokerEvent
}
