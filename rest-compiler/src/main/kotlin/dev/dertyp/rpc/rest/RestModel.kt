package dev.dertyp.rpc.rest

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeName

enum class ParamSource { PATH, QUERY, QUERY_LIST, QUERY_SET, QUERY_JSON, BODY }

enum class ResponseKind { JSON, UNIT, BYTES, BYTE_FLOW, SSE }

class RestParam(
    val index: Int,
    val name: String,
    val declaredType: TypeName,
    val elementConverter: CodeBlock?,
    val safeType: TypeName,
    val nullable: Boolean,
    val hasDefault: Boolean,
    val source: ParamSource,
    val description: String?
) {
    val required: Boolean get() = !hasDefault

    val resolvedAtDeclaration: Boolean
        get() = when {
            source == ParamSource.PATH -> true
            nullable -> true
            hasDefault -> false
            source == ParamSource.QUERY_LIST || source == ParamSource.QUERY_SET -> true
            source == ParamSource.BODY -> true
            else -> false
        }
}

class RestRoute(
    val function: KSFunctionDeclaration,
    val functionName: String,
    val method: String,
    val localPath: String,
    val params: List<RestParam>,
    val responseKind: ResponseKind,
    val declaredReturnType: TypeName,
    val safeReturnType: TypeName,
    val safeItemType: TypeName?,
    val sseItemType: TypeName?,
    val sseTransform: CodeBlock?,
    val jsonTransform: CodeBlock?,
    val itemIsByteArray: Boolean,
    val isPublic: Boolean,
    val isFileResponse: Boolean,
    val isUnit: Boolean,
    val summary: String?,
    val errors: List<String>,
    val needsProbe: Boolean,
    val multiBody: Boolean
)

class ProbeFunction(
    val name: String,
    val isSuspend: Boolean,
    val parameters: List<Pair<String, TypeName>>,
    val returnType: TypeName
)

class RestService(
    val declaration: KSClassDeclaration,
    val className: ClassName,
    val simpleName: String,
    val qualifiedName: String,
    val prefix: String,
    val routes: List<RestRoute>,
    val probeFunctions: List<ProbeFunction>
) {
    val hasFileResponse: Boolean get() = routes.any { it.isFileResponse }
    val defaultsObjectName: String get() = "${simpleName}Defaults"
    val registerFunctionName: String get() = "register${simpleName}Rest"
    val fileName: String get() = "${simpleName}Rest"
}
