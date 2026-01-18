package com.github.ixtf.compiler.fir

import com.github.ixtf.compiler.fir.cqrs.IxtfFirDeclarationExtension
import com.github.ixtf.compiler.fir.test.TestFirDeclarationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class IxtfFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::TestFirDeclarationExtension
    +::IxtfFirDeclarationExtension
  }
}
