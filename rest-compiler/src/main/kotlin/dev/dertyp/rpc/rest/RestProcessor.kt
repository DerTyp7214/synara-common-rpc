package dev.dertyp.rpc.rest

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

private const val RPC_ANNOTATION = "kotlinx.rpc.annotations.Rpc"
private const val REST_FILE_RESPONSE_ANNOTATION = "dev.dertyp.rpc.annotations.RestFileResponse"

class RestProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private var done = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (done) return emptyList()
        done = true

        val interfaces = discover(resolver)
        if (interfaces.isEmpty()) {
            logger.warn("[rest-compiler] no @Rpc interfaces found")
            return emptyList()
        }

        if (options["rest.spike"] == "true") {
            spike(interfaces)
            return emptyList()
        }

        val restPackage = options["rest.package"] ?: "dev.dertyp.routing.rest"
        val types = TypeClassifier(restPackage)
        val derivation = RestDerivation(types, logger)
        val services = interfaces.map { derivation.derive(it) }

        if (options["rest.dumpManifest"] == "true") dumpManifest(services)

        val emitter = KotlinEmitter(restPackage)
        services.forEach { emitter.emit(it).writeTo(codeGenerator, Dependencies(aggregating = true)) }
        ManifestEmitter(restPackage).emit(services).writeTo(codeGenerator, Dependencies(aggregating = true))

        return emptyList()
    }

    @OptIn(KspExperimental::class)
    private fun discover(resolver: Resolver): List<KSClassDeclaration> {
        val fromSource = resolver.getSymbolsWithAnnotation(RPC_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        val packages = options["rest.packages"].orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val fromPackages = packages.flatMap { pkg ->
            runCatching { resolver.getDeclarationsFromPackage(pkg).toList() }
                .getOrElse {
                    logger.warn("[rest-compiler] getDeclarationsFromPackage($pkg) failed: ${it.message}")
                    emptyList()
                }
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.INTERFACE && it.isRpc() }
        }

        val explicit = options["rest.interfaces"].orEmpty()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .mapNotNull { fqn ->
                val found = resolver.getClassDeclarationByName(resolver.getKSNameFromString(fqn))
                if (found == null) logger.warn("[rest-compiler] rest.interfaces: $fqn not found")
                found
            }

        return (fromSource + fromPackages + explicit)
            .filter { it.classKind == ClassKind.INTERFACE }
            .distinctBy { it.qualifiedName?.asString() ?: it.simpleName.asString() }
            .sortedBy { it.qualifiedName?.asString() ?: it.simpleName.asString() }
    }

    private fun KSClassDeclaration.isRpc(): Boolean = hasAnnotation(RPC_ANNOTATION)

    private fun dumpManifest(services: List<RestService>) {
        services.forEach { service ->
            service.routes.forEach { route ->
                if (route.isFileResponse) logger.warn(manifestLine(service, route, "HEAD"))
                logger.warn(manifestLine(service, route, route.method))
            }
        }
        logger.warn("[rest-manifest] routes=${services.sumOf { it.routes.size }}")
    }

    private fun manifestLine(service: RestService, route: RestRoute, method: String) =
        "[rest-manifest] $method /${service.prefix}/${route.localPath} ${service.simpleName}.${route.functionName}"

    private fun spike(interfaces: List<KSClassDeclaration>) {
        var functionCount = 0
        var defaultCount = 0
        var functionsWithDefaults = 0
        var syntheticNames = 0
        var fileResponses = 0

        interfaces.forEach { iface ->
            val functions = iface.getDeclaredFunctions().toList()
            logger.warn(
                "[rest-spike] ${iface.qualifiedName?.asString()} fromClasspath=${iface.containingFile == null} " +
                    "functions=${functions.size}"
            )
            functions.forEach { func ->
                functionCount++
                val name = func.simpleName.asString()
                val suspend = func.modifiers.contains(Modifier.SUSPEND)
                val declared = runCatching { func.returnType?.toTypeName()?.toString() }.getOrElse { "!${it.message}" }
                val resolved = runCatching { func.returnType?.resolve()?.declaration?.qualifiedName?.asString() }
                    .getOrElse { "!${it.message}" }
                val annotations = func.annotations.mapNotNull {
                    it.annotationType.resolve().declaration.qualifiedName?.asString()
                }.toList()
                if (annotations.any { it == REST_FILE_RESPONSE_ANNOTATION }) fileResponses++
                logger.warn(
                    "[rest-spike]   fn=$name suspend=$suspend declaredRet=$declared resolvedRetDecl=$resolved " +
                        "typeParams=${func.typeParameters.size} ann=$annotations"
                )
                if (func.parameters.any { it.hasDefault }) functionsWithDefaults++
                func.parameters.forEach { param ->
                    val pName = param.name?.asString()
                    if (pName == null || pName.startsWith("<")) syntheticNames++
                    if (param.hasDefault) defaultCount++
                    val pType = runCatching { param.type.toTypeName().toString() }.getOrElse { "!${it.message}" }
                    val pAnn = param.annotations.mapNotNull {
                        it.annotationType.resolve().declaration.qualifiedName?.asString()
                    }.toList()
                    logger.warn(
                        "[rest-spike]     p=$pName type=$pType hasDefault=${param.hasDefault} " +
                            "nullable=${param.type.resolve().isMarkedNullable} ann=$pAnn"
                    )
                }
            }
        }

        logger.warn(
            "[rest-spike] TOTALS interfaces=${interfaces.size} functions=$functionCount " +
                "paramsWithDefault=$defaultCount functionsWithDefaults=$functionsWithDefaults " +
                "fileResponses=$fileResponses syntheticParamNames=$syntheticNames"
        )

        if (interfaces.size < 52) logger.error("[rest-spike] only ${interfaces.size} @Rpc interfaces found, expected >= 52")
        if (syntheticNames > 0) logger.error("[rest-spike] $syntheticNames synthetic parameter names")
        if (defaultCount == 0) logger.error("[rest-spike] no parameters with defaults found")

        val songStream = interfaces.firstOrNull { it.simpleName.asString() == "ISongService" }
            ?.getDeclaredFunctions()?.firstOrNull { it.simpleName.asString() == "streamSong" }
        if (songStream == null) {
            logger.error("[rest-spike] ISongService.streamSong not found")
        } else if (!songStream.hasAnnotation(REST_FILE_RESPONSE_ANNOTATION)) {
            logger.error("[rest-spike] ISongService.streamSong lacks @RestFileResponse")
        }
    }
}

class RestProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        RestProcessor(environment.codeGenerator, environment.logger, environment.options)
}
