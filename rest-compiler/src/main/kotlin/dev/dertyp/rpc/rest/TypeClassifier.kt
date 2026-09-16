package dev.dertyp.rpc.rest

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

private const val FLOW = "kotlinx.coroutines.flow.Flow"
private const val ITERABLE = "kotlin.collections.Iterable"
private const val MAP = "kotlin.collections.Map"

private val SCALARS = setOf(
    "kotlin.String",
    "kotlin.Int",
    "kotlin.Long",
    "kotlin.Boolean",
    "kotlin.Double",
    "kotlin.Float",
    "java.util.UUID"
)

private val ITERABLE_NAMES = setOf(
    "kotlin.collections.Iterable",
    "kotlin.collections.Collection",
    "kotlin.collections.List",
    "kotlin.collections.Set",
    "kotlin.collections.MutableIterable",
    "kotlin.collections.MutableCollection",
    "kotlin.collections.MutableList",
    "kotlin.collections.MutableSet"
)

private val MAP_NAMES = setOf("kotlin.collections.Map", "kotlin.collections.MutableMap")

private val QUERY_COLLECTIONS = setOf(
    "kotlin.collections.List",
    "kotlin.collections.Collection",
    "kotlin.collections.Iterable",
    "kotlin.collections.Set"
)

class TypeClassifier(private val restPackage: String) {

    private val restConvert = ClassName(restPackage, "RestConvert")
    val flowMap = MemberName("kotlinx.coroutines.flow", "map")

    fun expand(type: KSType): KSType {
        var current = type
        var guard = 0
        while (guard++ < 16) {
            val alias = current.declaration as? KSTypeAlias ?: return current
            val actual = alias.type.resolve()
            current = if (current.isMarkedNullable) actual.makeNullable() else actual
        }
        return current
    }

    fun qualifiedName(type: KSType): String? = expand(type).declaration.qualifiedName?.asString()

    fun simpleName(type: KSType): String = expand(type).declaration.simpleName.asString()

    fun firstArgument(type: KSType): KSType? = expand(type).arguments.firstOrNull()?.type?.resolve()

    fun argument(type: KSType, index: Int): KSType? = expand(type).arguments.getOrNull(index)?.type?.resolve()

    fun isFlow(type: KSType): Boolean = qualifiedName(type) == FLOW

    fun isUnit(type: KSType): Boolean = qualifiedName(type) == "kotlin.Unit"

    fun isByteArray(type: KSType): Boolean = qualifiedName(type) == "kotlin.ByteArray"

    fun isFunctionType(type: KSType): Boolean {
        val name = qualifiedName(type) ?: return false
        return name.startsWith("kotlin.Function") || name.startsWith("kotlin.coroutines.SuspendFunction")
    }

    fun isQueryCollection(type: KSType): Boolean = qualifiedName(type) in QUERY_COLLECTIONS

    fun isSet(type: KSType): Boolean = qualifiedName(type) == "kotlin.collections.Set"

    fun isIterable(type: KSType): Boolean = isSubclassOf(expand(type).declaration, ITERABLE, ITERABLE_NAMES)

    fun isMap(type: KSType): Boolean = isSubclassOf(expand(type).declaration, MAP, MAP_NAMES)

    fun isPaginated(type: KSType): Boolean = simpleName(type) == "PaginatedResponse"

    fun isMetadataType(type: KSType): Boolean = simpleName(type) == "MetadataType"

    fun isPrimitive(type: KSType): Boolean {
        val expanded = expand(type)
        val declaration = expanded.declaration
        if (isSubclassOf(declaration, ITERABLE, ITERABLE_NAMES)) {
            val item = expanded.arguments.firstOrNull()?.type?.resolve() ?: return false
            return isPrimitive(item)
        }
        val qualified = declaration.qualifiedName?.asString()
        val simple = declaration.simpleName.asString()
        if (qualified in SCALARS) return true
        if (simple == "UUID" || simple == "PlatformUUID") return true
        if (simple == "Instant" || simple == "PlatformInstant") return true
        if (simple == "MetadataType") return true
        return (declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS
    }

    fun converterFor(type: KSType): CodeBlock {
        val expanded = expand(type)
        val declaration = expanded.declaration
        val qualified = declaration.qualifiedName?.asString()
        val simple = declaration.simpleName.asString()
        return when {
            qualified == "kotlin.String" -> CodeBlock.of("%T.string", restConvert)
            qualified == "kotlin.Int" -> CodeBlock.of("%T.int", restConvert)
            qualified == "kotlin.Long" -> CodeBlock.of("%T.long", restConvert)
            qualified == "kotlin.Boolean" -> CodeBlock.of("%T.boolean", restConvert)
            qualified == "kotlin.Double" -> CodeBlock.of("%T.double", restConvert)
            qualified == "kotlin.Float" -> CodeBlock.of("%T.float", restConvert)
            qualified == "java.util.UUID" || simple == "UUID" || simple == "PlatformUUID" ->
                CodeBlock.of("%T.uuid", restConvert)
            simple == "Instant" || simple == "PlatformInstant" -> CodeBlock.of("%T.instant", restConvert)
            simple == "MetadataType" -> CodeBlock.of("%T.metadataType", restConvert)
            (declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS ->
                CodeBlock.of(
                    "%T.enum(%T.entries.toTypedArray())",
                    restConvert,
                    (declaration as KSClassDeclaration).toClassName()
                )
            else -> CodeBlock.of("%T.string", restConvert)
        }
    }

    fun declaredTypeName(type: KSType): TypeName = type.toTypeName()

    fun safeType(type: KSType): TypeName {
        val expanded = expand(type)
        val declaration = expanded.declaration
        val qualified = declaration.qualifiedName?.asString()
        val simple = declaration.simpleName.asString()

        if (qualified == "kotlin.ByteArray") return expanded.toTypeName()
        if (qualified == "java.util.UUID" || simple == "UUID" || simple == "PlatformUUID") return STRING
        if (simple == "MetadataType") return STRING

        if (qualified == FLOW) {
            val item = expanded.arguments.firstOrNull()?.type?.resolve() ?: return ANY
            return safeType(item)
        }

        val paginated = simple == "PaginatedResponse"
        if (isSubclassOf(declaration, ITERABLE, ITERABLE_NAMES) || paginated) {
            val item = expanded.arguments.firstOrNull()?.type?.resolve()
            if (item != null) {
                val safeItem = safeType(item)
                if (safeItem != expand(item).toTypeName()) {
                    val base = if (paginated) (declaration as KSClassDeclaration).toClassName() else LIST
                    return base.parameterizedBy(safeItem)
                }
            }
            return expanded.toTypeName()
        }

        if (isSubclassOf(declaration, MAP, MAP_NAMES)) return expanded.toTypeName()

        return if (expanded.arguments.isNotEmpty()) {
            val className = (declaration as? KSClassDeclaration)?.toClassName() ?: return expanded.toTypeName()
            className.parameterizedBy(List(expanded.arguments.size) { STAR })
        } else {
            expanded.toTypeName()
        }
    }

    fun responseTransform(type: KSType, receiver: CodeBlock, depth: Int): CodeBlock? {
        val expanded = expand(type)
        val declaration = expanded.declaration
        val qualified = declaration.qualifiedName?.asString()
        val simple = declaration.simpleName.asString()
        val safeCall = if (expanded.isMarkedNullable) "?." else "."
        val item = "item$depth"
        val key = "key$depth"

        if (simple == "MetadataType") return CodeBlock.of("%L%Lvalue", receiver, safeCall)

        if (qualified == FLOW) {
            val argument = expanded.arguments.firstOrNull()?.type?.resolve() ?: return null
            val inner = responseTransform(argument, CodeBlock.of("%L", item), depth + 1) ?: return null
            return CodeBlock.of("%L%L%M { %L -> %L }", receiver, safeCall, flowMap, item, inner)
        }

        if (isSubclassOf(declaration, ITERABLE, ITERABLE_NAMES)) {
            val argument = expanded.arguments.firstOrNull()?.type?.resolve() ?: return null
            val inner = responseTransform(argument, CodeBlock.of("%L", item), depth + 1) ?: return null
            return CodeBlock.of("%L%Lmap { %L -> %L }", receiver, safeCall, item, inner)
        }

        if (isSubclassOf(declaration, MAP, MAP_NAMES)) {
            val keyType = expanded.arguments.getOrNull(0)?.type?.resolve() ?: return null
            val valueType = expanded.arguments.getOrNull(1)?.type?.resolve() ?: return null
            val keyInner = responseTransform(keyType, CodeBlock.of("%L", key), depth + 1)
            val valueInner = responseTransform(valueType, CodeBlock.of("%L", item), depth + 1)
            if (keyInner == null && valueInner == null) return null
            return CodeBlock.of(
                "%L%Lentries.associate { (%L, %L) -> %L to %L }",
                receiver,
                safeCall,
                key,
                item,
                keyInner ?: CodeBlock.of("%L", key),
                valueInner ?: CodeBlock.of("%L", item)
            )
        }

        if (simple == "PaginatedResponse") {
            val argument = expanded.arguments.firstOrNull()?.type?.resolve() ?: return null
            val inner = responseTransform(argument, CodeBlock.of("%L", item), depth + 1) ?: return null
            val className = (declaration as? KSClassDeclaration)?.toClassName() ?: return null
            return CodeBlock.of(
                "%T(data = %L.data.map { %L -> %L }, page = %L.page, total = %L.total, pageSize = %L.pageSize, hasNextPage = %L.hasNextPage)",
                className,
                receiver,
                item,
                inner,
                receiver,
                receiver,
                receiver,
                receiver
            )
        }

        return null
    }

    private fun isSubclassOf(
        declaration: KSDeclaration,
        target: String,
        fastPath: Set<String>,
        depth: Int = 0
    ): Boolean {
        if (depth > 12) return false
        val classDeclaration = declaration as? KSClassDeclaration ?: return false
        val qualified = classDeclaration.qualifiedName?.asString()
        if (qualified == target || qualified in fastPath) return true
        if (qualified == "kotlin.Any") return false
        return classDeclaration.superTypes.any {
            isSubclassOf(it.resolve().declaration, target, fastPath, depth + 1)
        }
    }
}
