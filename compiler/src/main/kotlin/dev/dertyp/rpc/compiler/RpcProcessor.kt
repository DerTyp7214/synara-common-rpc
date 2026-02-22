package dev.dertyp.rpc.compiler

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

class RpcProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation("kotlinx.rpc.annotations.Rpc")
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty()) return emptyList()

        val sourceFiles = symbols.mapNotNull { it.containingFile }.toTypedArray()
        val dependencies = Dependencies(false, *sourceFiles)
        val file = codeGenerator.createNewFile(
            dependencies,
            "dev.dertyp.rpc",
            "NativeDispatchers"
        )

        file.use { out ->
            // @file annotations MUST be the absolute first thing in the file
            out.writeLine("@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)")
            out.writeLine("@file:kotlinx.serialization.UseContextualSerialization(dev.dertyp.PlatformUUID::class)")
            out.writeLine("")
            out.writeLine("package dev.dertyp.rpc")
            out.writeLine("")
            out.writeLine("import dev.dertyp.rpc.BaseRpcServiceManager")
            out.writeLine("import dev.dertyp.serializers.*")
            out.writeLine("import kotlinx.serialization.*")
            out.writeLine("import kotlinx.serialization.builtins.*")
            out.writeLine("import kotlinx.coroutines.flow.Flow")
            out.writeLine("import kotlinx.coroutines.Job")
            out.writeLine("import kotlinx.coroutines.launch")
            out.writeLine("import kotlinx.coroutines.CoroutineScope")
            out.writeLine("import dev.dertyp.*")
            out.writeLine("import kotlinx.serialization.ExperimentalSerializationApi")
            out.writeLine("import kotlinx.serialization.serializer")
            out.writeLine("import kotlin.time.Duration")

            symbols.forEach { symbol ->
                out.writeLine("import ${symbol.qualifiedName?.asString()}")
            }

            out.writeLine("")
            out.writeLine("// Generated List Serializers to avoid contextual issues with Lists of platform types")
            out.writeLine("private object PlatformDateListSerializer : KSerializer<List<PlatformDate>> by ListSerializer(DateSerializer)")
            out.writeLine("private object PlatformLocalDateListSerializer : KSerializer<List<PlatformLocalDate>> by ListSerializer(LocalDateSerializer)")
            out.writeLine("private object PlatformLocalDateTimeListSerializer : KSerializer<List<PlatformLocalDateTime>> by ListSerializer(LocalDateTimeSerializer)")
            out.writeLine("private object PlatformOffsetDateTimeListSerializer : KSerializer<List<PlatformOffsetDateTime>> by ListSerializer(OffsetDateTimeSerializer)")
            out.writeLine("private object DurationListSerializer : KSerializer<List<Duration>> by ListSerializer(DurationSerializer)")
            out.writeLine("private object PlatformInstantListSerializer : KSerializer<List<PlatformInstant>> by ListSerializer(InstantSerializer)")

            out.writeLine("")
            out.writeLine("@Suppress(\"UNCHECKED_CAST\")")
            out.writeLine("suspend fun dispatchService(manager: BaseRpcServiceManager, service: String, method: String, args: ByteArray): ByteArray {")
            out.writeLine("    return when (service) {")
            symbols.forEach { symbol ->
                val name = symbol.simpleName.asString()
                out.writeLine("        \"$name\" -> dispatch$name(manager, method, args)")
            }
            out.writeLine("        else -> throw IllegalArgumentException(\"Unknown service: \$service\")")
            out.writeLine("    }")
            out.writeLine("}")

            out.writeLine("")
            out.writeLine("@Suppress(\"UNCHECKED_CAST\")")
            out.writeLine("fun subscribeService(scope: CoroutineScope, manager: BaseRpcServiceManager, service: String, method: String, args: ByteArray, onEach: (ByteArray) -> Unit): Job {")
            out.writeLine("    return when (service) {")
            symbols.forEach { symbol ->
                val name = symbol.simpleName.asString()
                out.writeLine("        \"$name\" -> subscribe$name(scope, manager, method, args, onEach)")
            }
            out.writeLine("        else -> throw IllegalArgumentException(\"Unknown service: \$service\")")
            out.writeLine("    }")
            out.writeLine("}")

            symbols.forEach { symbol ->
                generateServiceDispatcher(out, symbol)
            }
        }

        return symbols.filterNot { it.validate() }
    }

    private fun generateServiceDispatcher(out: OutputStream, symbol: KSClassDeclaration) {
        val name = symbol.simpleName.asString()
        val allFunctions = symbol.getAllFunctions().filter { it.isPublic() && it.simpleName.asString() != "<init>" }.toList()

        // Generate Args classes for multi-arg functions
        allFunctions.filter { it.parameters.size > 1 }.forEach { func ->
            val funcName = func.simpleName.asString().replaceFirstChar { it.uppercase() }
            out.writeLine("@Serializable")
            out.writeLine("data class ${name}${funcName}Args(")
            func.parameters.forEachIndexed { index, param ->
                val pName = param.name?.asString() ?: "arg$index"
                val pType = param.type.toTypeString()
                val comma = if (index == func.parameters.size - 1) "" else ","
                
                val annotation = getSerializerAnnotation(pType)
                
                out.writeLine("    $annotation val $pName: $pType$comma")
            }
            out.writeLine(")")
            out.writeLine("")
        }

        // Suspend Dispatcher
        out.writeLine("")
        out.writeLine("@Suppress(\"UNCHECKED_CAST\")")
        out.writeLine("private suspend fun dispatch$name(manager: BaseRpcServiceManager, method: String, args: ByteArray): ByteArray {")
        out.writeLine("    val s = manager.getService<$name>()")
        out.writeLine("    return when (method) {")
        allFunctions.filter { it.modifiers.contains(Modifier.SUSPEND) }.forEach { func ->
            val funcName = func.simpleName.asString()
            out.writeLine("        \"$funcName\" -> {")
            when {
                func.parameters.isEmpty() -> {
                    val returnType = func.returnType?.toTypeString() ?: "Unit"
                    val serializer = getSerializerForType(returnType)
                    if (serializer != null) {
                        out.writeLine("            AppCbor.encodeToByteArray($serializer as KSerializer<Any>, s.$funcName() as Any)")
                    } else {
                        out.writeLine("            AppCbor.encodeToByteArray(s.$funcName())")
                    }
                }
                func.parameters.size == 1 -> {
                    val paramType = func.parameters[0].type.toTypeString()
                    val paramSerializer = getSerializerForType(paramType)
                    val decodeCall = if (paramSerializer != null) {
                        "AppCbor.decodeFromByteArray($paramSerializer as KSerializer<Any>, args) as $paramType"
                    } else {
                        "AppCbor.decodeFromByteArray<$paramType>(args)"
                    }

                    val returnType = func.returnType?.toTypeString() ?: "Unit"
                    val returnSerializer = getSerializerForType(returnType)
                    if (returnSerializer != null) {
                        out.writeLine("            AppCbor.encodeToByteArray($returnSerializer as KSerializer<Any>, s.$funcName($decodeCall) as Any)")
                    } else {
                        out.writeLine("            AppCbor.encodeToByteArray(s.$funcName($decodeCall))")
                    }
                }
                else -> {
                    val argsClassName = "${name}${funcName.replaceFirstChar { it.uppercase() }}Args"
                    out.writeLine("            val a = AppCbor.decodeFromByteArray<$argsClassName>(args)")
                    val callArgs = func.parameters.mapIndexed { index, param ->
                        "a.${param.name?.asString() ?: "arg$index"}"
                    }.joinToString(", ")

                    val returnType = func.returnType?.toTypeString() ?: "Unit"
                    val returnSerializer = getSerializerForType(returnType)
                    if (returnSerializer != null) {
                        out.writeLine("            AppCbor.encodeToByteArray($returnSerializer as KSerializer<Any>, s.$funcName($callArgs) as Any)")
                    } else {
                        out.writeLine("            AppCbor.encodeToByteArray(s.$funcName($callArgs))")
                    }
                }
            }
            out.writeLine("        }")
        }
        out.writeLine("        else -> throw IllegalArgumentException(\"Unknown method: \$method\")")
        out.writeLine("    }")
        out.writeLine("}")

        // Flow Subscriber
        out.writeLine("")
        out.writeLine("@Suppress(\"UNCHECKED_CAST\")")
        out.writeLine("private fun subscribe$name(scope: CoroutineScope, manager: BaseRpcServiceManager, method: String, args: ByteArray, onEach: (ByteArray) -> Unit): Job {")
        out.writeLine("    val s = manager.getService<$name>()")
        out.writeLine("    return scope.launch {")
        out.writeLine("        val flow = when (method) {")
        allFunctions.filter { !it.modifiers.contains(Modifier.SUSPEND) && it.returnType?.resolve()?.declaration?.simpleName?.asString() == "Flow" }.forEach { func ->
            val funcName = func.simpleName.asString()
            out.writeLine("            \"$funcName\" -> {")
            when {
                func.parameters.isEmpty() -> {
                    out.writeLine("                s.$funcName()")
                }
                func.parameters.size == 1 -> {
                    val paramType = func.parameters[0].type.toTypeString()
                    val paramSerializer = getSerializerForType(paramType)
                    val decodeCall = if (paramSerializer != null) {
                        "AppCbor.decodeFromByteArray($paramSerializer as KSerializer<Any>, args) as $paramType"
                    } else {
                        "AppCbor.decodeFromByteArray<$paramType>(args)"
                    }
                    out.writeLine("                s.$funcName($decodeCall)")
                }
                else -> {
                    val argsClassName = "${name}${funcName.replaceFirstChar { it.uppercase() }}Args"
                    out.writeLine("                val a = AppCbor.decodeFromByteArray<$argsClassName>(args)")
                    val callArgs = func.parameters.mapIndexed { index, param ->
                        "a.${param.name?.asString() ?: "arg$index"}"
                    }.joinToString(", ")
                    out.writeLine("                s.$funcName($callArgs)")
                }
            }
            out.writeLine("            }")
        }
        out.writeLine("            else -> throw IllegalArgumentException(\"Unknown method: \$method\")")
        out.writeLine("        } as Flow<Any>")
        out.writeLine("        flow.collect { item ->")
        out.writeLine("            val serializer = when (method) {")
        allFunctions.filter { !it.modifiers.contains(Modifier.SUSPEND) && it.returnType?.resolve()?.declaration?.simpleName?.asString() == "Flow" }.forEach { func ->
            val funcName = func.simpleName.asString()
            val flowType = func.returnType?.resolve()?.arguments?.firstOrNull()?.type?.toTypeString() ?: "Any"
            val serializer = getSerializerForType(flowType)
            if (serializer != null) {
                out.writeLine("                \"$funcName\" -> $serializer")
            } else {
                out.writeLine("                \"$funcName\" -> AppCbor.serializersModule.serializer<$flowType>()")
            }
        }
        out.writeLine("                else -> AppCbor.serializersModule.serializer<Any>()")
        out.writeLine("            } as KSerializer<Any>")
        out.writeLine("            onEach(AppCbor.encodeToByteArray(serializer, item))")
        out.writeLine("        }")
        out.writeLine("    }")
        out.writeLine("}")
    }

    private fun getSerializerAnnotation(type: String): String {
        val s = getSerializerForType(type) ?: return ""
        return "@Serializable(with = $s::class) "
    }

    private fun getSerializerForType(type: String): String? {
        val baseType = type.removeSuffix("?")
        if (baseType == "dev.dertyp.PlatformUUID" || baseType == "PlatformUUID") return null // Contextual
        
        // Handle Lists/Collections/Sets/Iterables
        if (baseType.startsWith("kotlin.collections.List<") || baseType.startsWith("List<") || 
            baseType.startsWith("kotlin.collections.Collection<") || baseType.startsWith("Collection<") ||
            baseType.startsWith("kotlin.collections.Iterable<") || baseType.startsWith("Iterable<") ||
            baseType.startsWith("kotlin.collections.Set<") || baseType.startsWith("Set<")) {
            val inner = baseType.substringAfter("<").substringBeforeLast(">").substringAfterLast(".")
            return when (inner) {
                "PlatformDate" -> "PlatformDateListSerializer"
                "PlatformLocalDate" -> "PlatformLocalDateListSerializer"
                "PlatformLocalDateTime" -> "PlatformLocalDateTimeListSerializer"
                "PlatformOffsetDateTime" -> "PlatformOffsetDateTimeListSerializer"
                "Duration" -> "DurationListSerializer"
                "PlatformInstant" -> "PlatformInstantListSerializer"
                "PlatformUUID" -> "UUIDByteListSerializer"
                else -> null
            }
        }
        
        return when (baseType.substringAfterLast(".")) {
            "PlatformDate" -> "DateSerializer"
            "PlatformLocalDate" -> "LocalDateSerializer"
            "PlatformLocalDateTime" -> "LocalDateTimeSerializer"
            "PlatformOffsetDateTime" -> "OffsetDateTimeSerializer"
            "Duration" -> "DurationSerializer"
            "PlatformInstant" -> "InstantSerializer"
            else -> null
        }
    }

    private fun OutputStream.writeLine(str: String) {
        this.write((str + "\n").toByteArray())
    }

    private fun KSTypeReference.toTypeString(): String = resolve().toTypeString()

    private fun KSType.toTypeString(): String {
        val qName = declaration.qualifiedName?.asString() ?: return "Any"
        val nullability = if (isMarkedNullable) "?" else ""
        if (arguments.isEmpty()) return "$qName$nullability"
        val args = arguments.joinToString(", ") { arg ->
            val variance = when (arg.variance) {
                Variance.INVARIANT -> ""
                Variance.COVARIANT -> "out "
                Variance.CONTRAVARIANT -> "in "
                Variance.STAR -> return@joinToString "*"
            }
            val argType = arg.type?.resolve()
            val typeStr = argType?.toTypeString() ?: "Any"
            "$variance$typeStr"
        }
        return "$qName<$args>$nullability"
    }
}

class RpcProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RpcProcessor(environment.codeGenerator, environment.logger)
    }
}
