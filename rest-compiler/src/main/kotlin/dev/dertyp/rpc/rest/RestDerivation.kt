package dev.dertyp.rpc.rest

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

private const val ANNOTATIONS = "dev.dertyp.rpc.annotations"
private const val REST_GET = "$ANNOTATIONS.RestGet"
private const val REST_POST = "$ANNOTATIONS.RestPost"
private const val REST_PUT = "$ANNOTATIONS.RestPut"
private const val REST_DELETE = "$ANNOTATIONS.RestDelete"
private const val REST_PATH = "$ANNOTATIONS.RestPath"
private const val REST_PUBLIC = "$ANNOTATIONS.RestPublic"
private const val REST_FILE_RESPONSE = "$ANNOTATIONS.RestFileResponse"
private const val RPC_DOC = "$ANNOTATIONS.RpcDoc"
private const val RPC_PARAM_DOC = "$ANNOTATIONS.RpcParamDoc"

private val IGNORED_FUNCTIONS = setOf("<init>", "equals", "hashCode", "toString")

class RestDerivation(
    private val types: TypeClassifier,
    private val logger: KSPLogger
) {

    fun derive(declaration: KSClassDeclaration): RestService {
        val simpleName = declaration.simpleName.asString()
        val prefix = simpleName.removePrefix("I").removeSuffix("Service").replaceFirstChar { it.lowercase() }

        val declared = declaration.getDeclaredFunctions().toList()
        val routes = declared
            .filter { it.simpleName.asString() !in IGNORED_FUNCTIONS }
            .filter { isRestFunction(it) }
            .map { deriveRoute(declaration, it) }

        routes.groupBy { it.method to it.localPath }.filterValues { it.size > 1 }.forEach { (key, duplicates) ->
            logger.error(
                "[rest-compiler] $simpleName: duplicate route ${key.first} /$prefix/${key.second} from " +
                    duplicates.joinToString { it.functionName }
            )
        }

        val probeFunctions = declaration.getAllFunctions()
            .filter { it.isAbstract && it.simpleName.asString() !in IGNORED_FUNCTIONS }
            .map { function ->
                ProbeFunction(
                    name = function.simpleName.asString(),
                    isSuspend = function.modifiers.contains(Modifier.SUSPEND),
                    parameters = function.parameters.map { parameterName(it) to it.type.toTypeName() },
                    returnType = function.returnType?.toTypeName() ?: UNIT
                )
            }
            .toList()

        declaration.getAllProperties().forEach {
            logger.warn("[rest-compiler] $simpleName declares property ${it.simpleName.asString()}; not supported")
        }

        return RestService(
            declaration = declaration,
            className = declaration.toClassName(),
            simpleName = simpleName,
            qualifiedName = declaration.qualifiedName?.asString() ?: simpleName,
            prefix = prefix,
            routes = routes,
            probeFunctions = probeFunctions
        )
    }

    private fun isRestFunction(function: KSFunctionDeclaration): Boolean {
        if (function.modifiers.contains(Modifier.SUSPEND)) return true
        val returnType = function.returnType?.resolve() ?: return false
        return types.isFlow(returnType)
    }

    private fun deriveRoute(owner: KSClassDeclaration, function: KSFunctionDeclaration): RestRoute {
        val ownerName = owner.simpleName.asString()
        val functionName = function.simpleName.asString()
        if (function.typeParameters.isNotEmpty()) {
            logger.error("[rest-compiler] $ownerName.$functionName is generic; generic functions are not supported")
        }

        val (method, rawName) = methodAndName(function)
        val explicitSegment = function.annotation(REST_PATH)?.argument("segment") as? String
        val localName = explicitSegment ?: rawName.replaceFirstChar { it.lowercase() }

        val parameters = function.parameters
        val typeParameter = parameters.firstOrNull {
            parameterName(it) == "type" && types.isPrimitive(it.type.resolve()) && isPathCandidate(it)
        }
        val idParameter = parameters.firstOrNull {
            val name = parameterName(it)
            name != "type" && (name == "id" || name.endsWith("Id")) &&
                types.isPrimitive(it.type.resolve()) && isPathCandidate(it)
        }

        var path = localName
        if (typeParameter != null) path = "{type}/$path"
        if (idParameter != null) path = "$path/{${parameterName(idParameter)}}"

        val restParams = parameters.mapIndexed { index, parameter ->
            deriveParam(ownerName, functionName, index, parameter, method, typeParameter, idParameter)
        }

        val returnType = function.returnType?.resolve()
        val isUnit = returnType == null || types.isUnit(returnType)
        val isFlow = returnType != null && types.isFlow(returnType)
        val flowItem = if (isFlow) types.firstArgument(returnType!!) else null
        val itemIsByteArray = flowItem != null && types.isByteArray(flowItem)

        val responseKind = when {
            isFlow && itemIsByteArray -> ResponseKind.BYTE_FLOW
            isFlow -> ResponseKind.SSE
            returnType != null && types.isByteArray(returnType) -> ResponseKind.BYTES
            isUnit -> ResponseKind.UNIT
            else -> ResponseKind.JSON
        }

        val jsonTransform = if (responseKind == ResponseKind.JSON && returnType != null) {
            types.responseTransform(returnType.makeNotNullable(), CodeBlock.of("result"), 0)
        } else {
            null
        }
        val sseTransform = if (responseKind == ResponseKind.SSE && returnType != null) {
            types.responseTransform(returnType.makeNotNullable(), CodeBlock.of("result"), 0)
        } else {
            null
        }

        val doc = function.annotation(RPC_DOC)
        val summary = doc?.let { it.argument("description") as? String }
        val errors = doc?.let { annotation ->
            @Suppress("UNCHECKED_CAST")
            (annotation.argument("errors") as? List<Any?>)?.mapNotNull { it as? String }
        } ?: emptyList()

        val needsProbe = restParams.any { it.source != ParamSource.PATH && !it.nullable && it.hasDefault }

        return RestRoute(
            function = function,
            functionName = functionName,
            method = method,
            localPath = path,
            params = restParams,
            responseKind = responseKind,
            declaredReturnType = returnType?.toTypeName() ?: UNIT,
            safeReturnType = returnType?.let { types.safeType(it) } ?: UNIT,
            safeItemType = flowItem?.let { types.safeType(it) },
            sseItemType = flowItem?.let { types.declaredTypeName(it.makeNotNullable()) },
            sseTransform = sseTransform,
            jsonTransform = jsonTransform,
            itemIsByteArray = itemIsByteArray,
            isPublic = function.hasAnnotation(REST_PUBLIC),
            isFileResponse = function.hasAnnotation(REST_FILE_RESPONSE),
            isUnit = isUnit,
            summary = summary,
            errors = errors,
            needsProbe = needsProbe,
            multiBody = restParams.count { it.source == ParamSource.BODY } > 1
        )
    }

    private fun deriveParam(
        ownerName: String,
        functionName: String,
        index: Int,
        parameter: KSValueParameter,
        method: String,
        typeParameter: KSValueParameter?,
        idParameter: KSValueParameter?
    ): RestParam {
        val name = parameterName(parameter)
        val type = parameter.type.resolve()
        if (types.isFunctionType(type)) {
            logger.error("[rest-compiler] $ownerName.$functionName parameter $name has a function type")
        }

        val isPath = parameter == typeParameter || parameter == idParameter
        val primitive = types.isPrimitive(type)
        val source = when {
            isPath -> ParamSource.PATH
            method != "GET" && !primitive -> ParamSource.BODY
            types.isQueryCollection(type) && types.isPrimitive(type) ->
                if (types.isSet(type)) ParamSource.QUERY_SET else ParamSource.QUERY_LIST
            primitive -> ParamSource.QUERY
            else -> ParamSource.QUERY_JSON
        }

        if (isPath && !primitive) {
            logger.error("[rest-compiler] $ownerName.$functionName path parameter $name is not primitive")
        }

        val elementConverter = when (source) {
            ParamSource.PATH, ParamSource.QUERY -> types.converterFor(type)
            ParamSource.QUERY_LIST, ParamSource.QUERY_SET ->
                types.firstArgument(type)?.let { types.converterFor(it) } ?: types.converterFor(type)
            else -> null
        }

        return RestParam(
            index = index,
            name = name,
            declaredType = parameter.type.toTypeName(),
            elementConverter = elementConverter,
            safeType = types.safeType(type),
            nullable = type.isMarkedNullable,
            hasDefault = parameter.hasDefault,
            source = source,
            description = parameter.annotation(RPC_PARAM_DOC)?.argument("description") as? String
        )
    }

    private fun methodAndName(function: KSFunctionDeclaration): Pair<String, String> {
        val explicitMethod = when {
            function.hasAnnotation(REST_GET) -> "GET"
            function.hasAnnotation(REST_POST) -> "POST"
            function.hasAnnotation(REST_PUT) -> "PUT"
            function.hasAnnotation(REST_DELETE) -> "DELETE"
            else -> null
        }
        val (derivedMethod, derivedName) = methodAndNameFromPrefix(function.simpleName.asString())
        return (explicitMethod ?: derivedMethod) to derivedName
    }

    private fun methodAndNameFromPrefix(name: String): Pair<String, String> {
        return when {
            name.startsWithWord("by") -> "GET" to name
            name.startsWithWord("all") -> "GET" to name
            name.startsWithWord("liked") -> "GET" to name
            name.startsWithWord("stream") -> "GET" to name
            name.startsWithWord("import") -> "GET" to name
            name.startsWithWord("download") -> "GET" to name
            name.startsWithWord("get") -> "GET" to name.removePrefix("get")
            name.startsWithWord("list") -> "GET" to name.removePrefix("list")
            name.startsWithWord("find") -> "GET" to name.removePrefix("find")
            name.startsWithWord("fetch") -> "GET" to name.removePrefix("fetch")
            name.startsWithWord("search") -> "GET" to name.removePrefix("search")
            name.startsWithWord("ranked") -> "GET" to name.removePrefix("ranked")
            name.startsWithWord("exists") -> "GET" to name
            name.contains("Exists") -> "GET" to name
            name.startsWithWord("add") -> "POST" to name
            name.startsWithWord("post") -> "POST" to name.removePrefix("post")
            name.startsWithWord("create") -> "POST" to name.removePrefix("create")
            name.startsWithWord("put") -> "PUT" to name.removePrefix("put")
            name.startsWithWord("set") -> "PUT" to name.removePrefix("set")
            name.startsWithWord("update") -> "PUT" to name.removePrefix("update")
            name.startsWithWord("delete") -> "DELETE" to name.removePrefix("delete")
            name.startsWithWord("remove") -> "DELETE" to name.removePrefix("remove")
            else -> "POST" to name
        }
    }

    private fun isPathCandidate(parameter: KSValueParameter): Boolean =
        !parameter.hasDefault && !parameter.type.resolve().isMarkedNullable

    private fun String.startsWithWord(prefix: String): Boolean {
        if (!startsWith(prefix)) return false
        if (length == prefix.length) return true
        val next = this[prefix.length]
        return next.isUpperCase() || next.isDigit()
    }

    private fun parameterName(parameter: KSValueParameter): String = parameter.name?.asString() ?: "arg"
}

internal fun KSAnnotated.annotation(qualifiedName: String): KSAnnotation? = annotations.firstOrNull {
    it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
}

internal fun KSAnnotated.hasAnnotation(qualifiedName: String): Boolean = annotation(qualifiedName) != null

internal fun KSAnnotation.argument(name: String): Any? =
    arguments.firstOrNull { it.name?.asString() == name }?.value
