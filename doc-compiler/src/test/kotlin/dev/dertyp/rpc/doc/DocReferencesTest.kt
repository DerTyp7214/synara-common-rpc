package dev.dertyp.rpc.doc

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DocReferencesTest {

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

    private fun function(name: String): KSFunctionDeclaration = mockk {
        every { simpleName.asString() } returns name
        every { isPublic() } returns true
    }

    private fun service(qualifiedName: String, vararg functions: String): KSClassDeclaration = mockk {
        every { simpleName.asString() } returns qualifiedName.substringAfterLast(".")
        every { this@mockk.qualifiedName?.asString() } returns qualifiedName
        every { getAllFunctions() } returns functions.map { function(it) }.asSequence()
    }

    private fun entry(name: String): KSClassDeclaration = mockk {
        every { simpleName.asString() } returns name
        every { classKind } returns ClassKind.ENUM_ENTRY
    }

    private fun property(name: String): KSPropertyDeclaration = mockk {
        every { simpleName.asString() } returns name
    }

    private fun model(qualifiedName: String, entries: List<String> = emptyList(), properties: List<String> = emptyList()) =
        mockk<KSClassDeclaration> {
            every { simpleName.asString() } returns qualifiedName.substringAfterLast(".")
            every { this@mockk.qualifiedName?.asString() } returns qualifiedName
            every { declarations } returns entries.map { entry(it) }.asSequence()
            every { getAllProperties() } returns properties.map { property(it) }.asSequence()
        }

    private fun references() = DocReferences(
        listOf(service("dev.dertyp.services.IChangeService", "observeChanges")),
        listOf(
            model("dev.dertyp.data.ChangeTopic", entries = listOf("LISTENS")),
            model("dev.dertyp.data.Change", properties = listOf("topic"))
        ),
        logger
    )

    @Test
    fun `links service methods and models relative to the current file`() {
        val text = "Emits a @ChangeTopic through @IChangeService.observeChanges. See @Change.topic, @ChangeTopic.LISTENS or @IChangeService."

        val linked = references().link(text, source, "X", "RPC.md", "")

        assertEquals(
            "Emits a [ChangeTopic](#devdertypdatachangetopic) through " +
                "[IChangeService.observeChanges](RPC.md#devdertypservicesichangeservice-observechanges). " +
                "See [Change.topic](#devdertypdatachange), [ChangeTopic.LISTENS](#devdertypdatachangetopic) or " +
                "[IChangeService](RPC.md#devdertypservicesichangeservice).",
            linked
        )
        verify(exactly = 0) { logger.error(any(), any()) }
    }

    @Test
    fun `leaves text without references untouched`() {
        val text = "Mail me at a@Example.com, use @lowercase, `@RestGet` or an @ sign."

        assertEquals(text, references().link(text, source, "X", "RPC.md", "MODELS.md"))
        verify(exactly = 0) { logger.error(any(), any()) }
    }

    @Test
    fun `reports unresolved references once with the source and the reference`() {
        val references = references()
        val text = "Call @IChangeService.observeChange or @ChangTopic."

        val linked = references.link(text, source, "IScrobbleService.recentListens", "", "MODELS.md")
        references.link(text, source, "IScrobbleService.recentListens", "", "MODELS.md")

        assertEquals(text, linked)
        verify(exactly = 1) {
            logger.error(
                "[doc-compiler] IScrobbleService.recentListens: unresolved doc reference @IChangeService.observeChange: " +
                    "IChangeService has no method observeChange",
                source
            )
        }
        verify(exactly = 1) {
            logger.error(
                "[doc-compiler] IScrobbleService.recentListens: unresolved doc reference @ChangTopic: " +
                    "ChangTopic is neither an @RpcDoc service nor a @ModelDoc model",
                source
            )
        }
    }

    @Test
    fun `reports unknown model members`() {
        references().link("See @ChangeTopic.LIKES.", source, "Change.topic", "RPC.md", "")

        verify(exactly = 1) {
            logger.error(
                "[doc-compiler] Change.topic: unresolved doc reference @ChangeTopic.LIKES: " +
                    "ChangeTopic has no field or entry LIKES",
                source
            )
        }
    }
}
