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
        every { parentDeclaration } returns null
        every { getAllFunctions() } returns functions.map { function(it) }.asSequence()
    }

    private fun entry(name: String): KSClassDeclaration = mockk {
        every { simpleName.asString() } returns name
        every { classKind } returns ClassKind.ENUM_ENTRY
    }

    private fun property(name: String): KSPropertyDeclaration = mockk {
        every { simpleName.asString() } returns name
    }

    private fun model(
        qualifiedName: String,
        entries: List<String> = emptyList(),
        properties: List<String> = emptyList(),
        parent: KSClassDeclaration? = null
    ) =
        mockk<KSClassDeclaration> {
            every { simpleName.asString() } returns qualifiedName.substringAfterLast(".")
            every { this@mockk.qualifiedName?.asString() } returns qualifiedName
            every { parentDeclaration } returns parent
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
        val text =
            "Emits a @ChangeTopic through @IChangeService.observeChanges. See @Change.topic, @ChangeTopic.LISTENS or @IChangeService."

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
                    "IChangeService has no method or nested model observeChange",
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
                    "ChangeTopic has no nested model, field or entry LIKES",
                source
            )
        }
    }

    private fun nestedReferences(): DocReferences {
        val metadataService = service("dev.dertyp.services.metadata.IMetadataService", "searchAlbum")
        val queueWriteResult = model("dev.dertyp.data.QueueWriteResult")
        val settingsWriteResult = model("dev.dertyp.data.ClientSettingsWriteResult")
        return DocReferences(
            listOf(metadataService),
            listOf(
                model("dev.dertyp.data.Album", properties = listOf("musicBrainzId")),
                model("dev.dertyp.services.metadata.IMetadataService.Album", parent = metadataService),
                queueWriteResult,
                settingsWriteResult,
                model("dev.dertyp.data.QueueWriteResult.Conflict", parent = queueWriteResult),
                model("dev.dertyp.data.ClientSettingsWriteResult.Conflict", parent = settingsWriteResult),
                model("dev.dertyp.data.Image"),
                model("dev.dertyp.ui.Image")
            ),
            logger
        )
    }

    @Test
    fun `prefers the single top-level model over nested models of the same name`() {
        val linked = nestedReferences().link("See @Album and @Album.musicBrainzId.", source, "X", "RPC.md", "")

        assertEquals("See [Album](#devdertypdataalbum) and [Album.musicBrainzId](#devdertypdataalbum).", linked)
        verify(exactly = 0) { logger.error(any(), any()) }
    }

    @Test
    fun `links nested models through their outer service or model`() {
        val linked = nestedReferences().link(
            "Read @IMetadataService.Album and @QueueWriteResult.Conflict.",
            source,
            "X",
            "RPC.md",
            ""
        )

        assertEquals(
            "Read [IMetadataService.Album](#devdertypservicesmetadataimetadataservicealbum) and " +
                "[QueueWriteResult.Conflict](#devdertypdataqueuewriteresultconflict).",
            linked
        )
        verify(exactly = 0) { logger.error(any(), any()) }
    }

    @Test
    fun `keeps service method references before nested models`() {
        val linked = nestedReferences().link("Call @IMetadataService.searchAlbum.", source, "X", "RPC.md", "")

        assertEquals(
            "Call [IMetadataService.searchAlbum](RPC.md#devdertypservicesmetadataimetadataservice-searchalbum).",
            linked
        )
        verify(exactly = 0) { logger.error(any(), any()) }
    }

    @Test
    fun `reports names that stay ambiguous`() {
        val text = "See @Conflict or @Image."

        assertEquals(text, nestedReferences().link(text, source, "X", "RPC.md", ""))
        verify(exactly = 1) {
            logger.error(
                "[doc-compiler] X: unresolved doc reference @Conflict: " +
                    "Conflict names more than one documented service or model",
                source
            )
        }
        verify(exactly = 1) {
            logger.error(
                "[doc-compiler] X: unresolved doc reference @Image: " +
                    "Image names more than one documented service or model",
                source
            )
        }
    }
}
