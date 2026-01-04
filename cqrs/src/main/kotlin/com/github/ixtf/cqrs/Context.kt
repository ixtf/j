package com.github.ixtf.cqrs

interface Context {
  val selfRegion: String?
}
