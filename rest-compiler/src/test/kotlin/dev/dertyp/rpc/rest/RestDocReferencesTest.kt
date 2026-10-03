package dev.dertyp.rpc.rest

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val MODEL_DOC = "dev.dertyp.rpc.annotations.ModelDoc"

class RestDocReferencesTest {

    private val logger = mockk<KSPLogger>(relaxed = true)
    private val source = mockk<KSNode>()

    @BeforeEach
    fun setUp() {
        mockkStatic("com.google.devtools.ksp.UtilsKt")
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun modelDoc(): KSAnnotation = mockk {
        every { annotationType.resolve().declaration.qualifiedName?.asString() } returns MODEL_DOC
    }

    private fun model(qualifiedName: String): KSClassDeclaration = mockk {
        every { simpleName.asString() } returns qualifiedName.substringAfterLast(".")
        every { this@mockk.qualifiedName?.asString() } returns qualifiedName
        every { packageName.asString() } returns qualifiedName.substringBeforeLast(".")
        every { annotations } returns sequenceOf(modelDoc())
        every { getAllProperties() } returns emptySequence()
        every { declarations } returns emptySequence()
    }

    private fun typeOf(declaration: KSClassDeclaration): KSType = mockk {
        every { this@mockk.declaration } returns declaration
        every { arguments } returns emptyList()
    }

    private fun function(name: String, returns: KSClassDeclaration? = null): KSFunctionDeclaration = mockk {
        every { simpleName.asString() } returns name
        every { isPublic() } returns true
        every { parameters } returns emptyList()
        every { returnType } returns returns?.let { declaration ->
            mockk {
                every { resolve() } returns typeOf(
                    declaration
                )
            }
        }
    }

    private fun route(functionName: String): RestRoute = mockk {
        every { this@mockk.functionName } returns functionName
    }

    private fun service(
        qualifiedName: String,
        functions: List<KSFunctionDeclaration>,
        routes: List<String>
    ): RestService {
        val declaration = mockk<KSClassDeclaration> {
            every { getAllFunctions() } returns functions.asSequence()
        }
        val simpleName = qualifiedName.substringAfterLast(".")
        return RestService(
            declaration = declaration,
            className = ClassName(qualifiedName.substringBeforeLast("."), simpleName),
            simpleName = simpleName,
            qualifiedName = qualifiedName,
            prefix = simpleName.removePrefix("I").removeSuffix("Service").replaceFirstChar { it.lowercase() },
            routes = routes.map { route(it) },
            probeFunctions = emptyList()
        )
    }

    @OptIn(KspExperimental::class)
    private fun references(): RestDocReferences {
        val change = model("dev.dertyp.data.Change")
        val changeTopic = model("dev.dertyp.data.ChangeTopic")
        val resolver = mockk<Resolver> {
            every { getDeclarationsFromPackage("dev.dertyp.data") } returns sequenceOf(change, changeTopic)
        }
        return RestDocReferences(
            listOf(
                service(
                    "dev.dertyp.services.IChangeService",
                    listOf(function("observeChanges", returns = change), function("internalOnly")),
                    listOf("observeChanges")
                ),
                service("dev.dertyp.services.IHiddenService", listOf(function("ping")), emptyList())
            ),
            resolver,
            MODEL_DOC,
            logger,
            "RPC.md",
            "MODELS.md"
        )
    }

    @Test
    fun `links routes locally, falls back to the rpc doc and links models found in scanned packages`() {
        val text = "Use @IChangeService.observeChanges, @IChangeService.internalOnly, @IHiddenService.ping or " +
                "@IHiddenService and read @ChangeTopic."

        val linked = references().link(text, source, "X")

        assertEquals(
            "Use [IChangeService.observeChanges](#devdertypservicesichangeservice-observechanges), " +
                    "[IChangeService.internalOnly](RPC.md#devdertypservicesichangeservice-internalonly), " +
                    "[IHiddenService.ping](RPC.md#devdertypservicesihiddenservice-ping) or " +
                    "[IHiddenService](RPC.md#devdertypservicesihiddenservice) and read " +
                    "[ChangeTopic](MODELS.md#devdertypdatachangetopic).",
            linked
        )
        verify(exactly = 0) { logger.error(any(), any()) }
    }

    @Test
    fun `reports unresolved references with the source and the reference`() {
        val text = "Call @IChangeService.observe, @Nope and mail a@Example.com."

        assertEquals(text, references().link(text, source, "IUiService.getHomeCardsFlow"))

        verify(exactly = 1) {
            logger.error(
                "[rest-compiler] IUiService.getHomeCardsFlow: unresolved doc reference @IChangeService.observe: " +
                        "IChangeService has no method observe",
                source
            )
        }
        verify(exactly = 1) {
            logger.error(
                "[rest-compiler] IUiService.getHomeCardsFlow: unresolved doc reference @Nope: " +
                        "Nope is neither an RPC service nor a @ModelDoc model",
                source
            )
        }
    }
}
