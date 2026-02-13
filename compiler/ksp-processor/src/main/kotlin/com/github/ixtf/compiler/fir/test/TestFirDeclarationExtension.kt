package com.github.ixtf.compiler.fir.test

import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunction
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.expressions.builder.buildBlock
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildReturnExpression
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind

class TestFirDeclarationExtension(session: FirSession) :
  FirDeclarationGenerationExtension(session) {

  private val autoHelloClassId =
    ClassId.fromString("com/github/ixtf/compiler/annotations/AutoHello")

  @OptIn(SymbolInternals::class)
  override fun generateFunctions(
    callableId: CallableId,
    context: MemberGenerationContext?,
  ): List<FirNamedFunctionSymbol> {

    val ownerSymbol = context?.owner ?: return emptyList()
    val owner = ownerSymbol.fir as? FirRegularClass ?: return emptyList()

    if (!owner.hasAnnotation(autoHelloClassId, session)) return emptyList()
    if (callableId.callableName.asString() != "hello") return emptyList()

    return listOf(createHelloFunction(owner))
  }

  private fun createHelloFunction(owner: FirRegularClass): FirNamedFunctionSymbol {
    val classId = owner.classId
    return buildSimpleFunction {
        moduleData = session.moduleData
        name = Name.identifier("hello")
        symbol = FirNamedFunctionSymbol(CallableId(classId, Name.identifier("hello")))
        status = FirDeclarationStatusImpl(Visibilities.Public, Modality.FINAL)
        returnTypeRef = session.builtinTypes.stringType

        body = buildBlock {
          statements += buildReturnExpression {
            result =
              buildLiteralExpression(
                source = null,
                kind = ConstantValueKind.String,
                value = "Hello from ${owner.name.asString()}",
                setType = true,
              )
          }
        }
      }
      .symbol
  }
}
