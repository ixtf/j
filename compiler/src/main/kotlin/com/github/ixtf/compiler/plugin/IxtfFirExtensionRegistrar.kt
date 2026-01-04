package com.github.ixtf.compiler.plugin

import com.github.ixtf.compiler.plugin.cqrs.IxtfFirDeclarationExtension
import com.github.ixtf.compiler.plugin.test.TestFirDeclarationExtension
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

class IxtfFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::TestFirDeclarationExtension
    +::IxtfFirDeclarationExtension
  }
}
