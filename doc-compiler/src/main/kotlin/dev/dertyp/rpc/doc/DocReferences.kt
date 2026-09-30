package dev.dertyp.rpc.doc

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode

private val DOC_REFERENCE = Regex("(?<![\\w`@.])@([A-Z][A-Za-z0-9_]*)(?:\\.([A-Za-z_][A-Za-z0-9_]*))?")

private val IGNORED_FUNCTIONS = listOf("<init>", "equals", "hashCode", "toString")

internal fun anchorOf(qualifiedName: String): String = qualifiedName.lowercase().replace(".", "")

internal fun methodAnchorOf(serviceQualifiedName: String, functionName: String): String =
    "${anchorOf(serviceQualifiedName)}-${functionName.lowercase()}"

internal fun KSClassDeclaration.documentedFunctions(): Sequence<KSFunctionDeclaration> =
    getAllFunctions().filter { it.isPublic() && it.simpleName.asString() !in IGNORED_FUNCTIONS }

internal class DocReferences(
    services: List<KSClassDeclaration>,
    models: List<KSClassDeclaration>,
    private val logger: KSPLogger
) {
    private val servicesByName = services.groupBy { it.simpleName.asString() }
    private val modelsByName = models.groupBy { it.simpleName.asString() }
    private val reported = mutableSetOf<Pair<String, String>>()

    fun link(text: String, source: KSNode, sourceName: String, serviceDoc: String, modelDoc: String): String =
        DOC_REFERENCE.replace(text) { match ->
            val name = match.groupValues[1]
            val member = match.groupValues[2].ifEmpty { null }
            val label = match.value.removePrefix("@")
            when (val target = resolve(name, member)) {
                is Target.Service -> "[$label]($serviceDoc#${target.anchor})"
                is Target.Model -> "[$label]($modelDoc#${target.anchor})"
                is Target.Unresolved -> {
                    if (reported.add(sourceName to match.value)) {
                        logger.error(
                            "[doc-compiler] $sourceName: unresolved doc reference ${match.value}: ${target.reason}",
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
        if (services.size + models.size > 1) {
            return Target.Unresolved("$name names more than one documented service or model")
        }
        services.firstOrNull()?.let { service -> return resolveService(service, member) }
        models.firstOrNull()?.let { model -> return resolveModel(model, member) }
        return Target.Unresolved("$name is neither an @RpcDoc service nor a @ModelDoc model")
    }

    private fun resolveService(service: KSClassDeclaration, member: String?): Target {
        val qName = service.qualifiedName?.asString() ?: service.simpleName.asString()
        if (member == null) return Target.Service(anchorOf(qName))
        val exists = service.documentedFunctions().any { it.simpleName.asString() == member }
        if (!exists) return Target.Unresolved("${service.simpleName.asString()} has no method $member")
        return Target.Service(methodAnchorOf(qName, member))
    }

    private fun resolveModel(model: KSClassDeclaration, member: String?): Target {
        val qName = model.qualifiedName?.asString() ?: model.simpleName.asString()
        if (member != null) {
            val entries = model.declarations.filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .map { it.simpleName.asString() }
            val properties = model.getAllProperties().map { it.simpleName.asString() }
            if (member !in entries && member !in properties) {
                return Target.Unresolved("${model.simpleName.asString()} has no field or entry $member")
            }
        }
        return Target.Model(anchorOf(qName))
    }

    private sealed interface Target {
        class Service(val anchor: String) : Target
        class Model(val anchor: String) : Target
        class Unresolved(val reason: String) : Target
    }
}
