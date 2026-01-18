package com.github.ixtf.cqrs.consumer

import java.io.Serializable

data class MessageEnvelope<S, E>(val previous: S?, val event: E, val current: S) : Serializable
