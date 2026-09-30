package dev.dertyp.rpc.rest

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType

private val DOC_REFERENCE = Regex("(?<![\\w`@.])@([A-Z][A-Za-z0-9_]*)(?:\\.([A-Za-z_][A-Za-z0-9_]*))?")

private val IGNORED_FUNCTIONS = listOf("<init>", "equals", "hashCode", "toString")

internal fun anchorOf(qualifiedName: String): String = qualifiedName.lowercase().replace(".", "")

internal fun methodAnchorOf(serviceQualifiedName: String, functionName: String): String =
    "${anchorOf(serviceQualifiedName)}-${functionName.lowercase()}"

internal class RestDocReferences(
    services: List<RestService>,
    resolver: Resolver,
    private val modelDocAnnotation: String,
    private val logger: KSPLogger,
    private val rpcDoc: String,
    private val modelsDoc: String
) {
    private val servicesByName = services.groupBy { it.simpleName }
    private val modelsByName = collectModels(services, resolver).groupBy { it.simpleName.asString() }
    private val reported = mutableSetOf<Pair<String, String>>()

    fun link(text: String, source: KSNode, sourceName: String): String =
        DOC_REFERENCE.replace(text) { match ->
            val label = match.value.removePrefix("@")
            when (val target = resolve(match.groupValues[1], match.groupValues[2].ifEmpty { null })) {
                is Target.Link -> "[$label](${target.href})"
                is Target.Unresolved -> {
                    if (reported.add(sourceName to match.value)) {
                        logger.error(
                            "[rest-compiler] $sourceName: unresolved doc reference ${match.value}: ${target.reason}",
                            source
                        )
                    }
                    match.value
                }
            }
        }

    private fun resolve(name: String, member: String?): Target {
        val services = servicesByName[name].orEmpty()
        val models = modelsByName[name].orEmpty()
        if (services.size + models.size > 1) return Target.Unresolved("$name names more than one service or model")
        services.firstOrNull()?.let { return resolveService(it, member) }
        models.firstOrNull()?.let { return resolveModel(it, member) }
        return Target.Unresolved("$name is neither an RPC service nor a @ModelDoc model")
    }

    private fun resolveService(service: RestService, member: String?): Target {
        val rendered = service.routes.isNotEmpty()
        if (member == null) {
            val anchor = anchorOf(service.qualifiedName)
            return Target.Link(if (rendered) "#$anchor" else "$rpcDoc#$anchor")
        }
        val exists = service.declaration.getAllFunctions()
            .any { it.isPublic() && it.simpleName.asString() == member && member !in IGNORED_FUNCTIONS }
        if (!exists) return Target.Unresolved("${service.simpleName} has no method $member")
        val anchor = methodAnchorOf(service.qualifiedName, member)
        val hasRoute = service.routes.any { it.functionName == member }
        return Target.Link(if (hasRoute) "#$anchor" else "$rpcDoc#$anchor")
    }

    private fun resolveModel(model: KSClassDeclaration, member: String?): Target {
        if (member != null) {
            val entries = model.declarations.filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .map { it.simpleName.asString() }
            val properties = model.getAllProperties().map { it.simpleName.asString() }
            if (member !in entries && member !in properties) {
                return Target.Unresolved("${model.simpleName.asString()} has no field or entry $member")
            }
        }
        val qName = model.qualifiedName?.asString() ?: model.simpleName.asString()
        return Target.Link("$modelsDoc#${anchorOf(qName)}")
    }

    private fun collectModels(services: List<RestService>, resolver: Resolver): List<KSClassDeclaration> {
        val visited = mutableSetOf<String>()
        val models = mutableMapOf<String, KSClassDeclaration>()

        services.forEach { service ->
            service.declaration.getAllFunctions().forEach { function ->
                function.parameters.forEach { visitType(it.type.resolve(), visited, models) }
                function.returnType?.resolve()?.let { visitType(it, visited, models) }
            }
        }

        models.values.map { it.packageName.asString() }.toSet().forEach { pkg ->
            scanPackage(resolver, pkg).forEach { visitDeclaration(it, visited, models) }
        }

        return models.values.toList()
    }

    private fun visitType(type: KSType, visited: MutableSet<String>, models: MutableMap<String, KSClassDeclaration>) {
        (type.declaration as? KSClassDeclaration)?.let { visitDeclaration(it, visited, models) }
        type.arguments.forEach { argument -> argument.type?.resolve()?.let { visitType(it, visited, models) } }
    }

    private fun visitDeclaration(
        declaration: KSClassDeclaration,
        visited: MutableSet<String>,
        models: MutableMap<String, KSClassDeclaration>
    ) {
        val qName = declaration.qualifiedName?.asString() ?: return
        if (!visited.add(qName)) return
        if (!declaration.hasAnnotation(modelDocAnnotation)) return
        models[qName] = declaration
        declaration.getAllProperties().forEach { visitType(it.type.resolve(), visited, models) }
        declaration.declarations.filterIsInstance<KSClassDeclaration>().forEach { visitDeclaration(it, visited, models) }
    }

    @OptIn(KspExperimental::class)
    private fun scanPackage(resolver: Resolver, pkg: String): List<KSClassDeclaration> {
        val top = runCatching { resolver.getDeclarationsFromPackage(pkg).toList() }
            .getOrElse {
                logger.warn("[rest-compiler] getDeclarationsFromPackage($pkg) failed: ${it.message}")
                emptyList()
            }
            .filterIsInstance<KSClassDeclaration>()
        return top.flatMap { listOf(it) + it.declarations.filterIsInstance<KSClassDeclaration>() }
    }

    private sealed interface Target {
        class Link(val href: String) : Target
        class Unresolved(val reason: String) : Target
    }
}
