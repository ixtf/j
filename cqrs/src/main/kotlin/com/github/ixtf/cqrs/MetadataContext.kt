package com.github.ixtf.cqrs

interface MetadataContext : Context {
  val metadata: Metadata
}
