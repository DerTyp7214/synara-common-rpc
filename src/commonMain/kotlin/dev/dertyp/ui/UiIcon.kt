@file:UseContextualSerialization(PlatformUUID::class)

package dev.dertyp.ui

import dev.dertyp.PlatformUUID
import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization

@Serializable
@ModelDoc("Semantic icons clients map to their own icon set. Adding an entry requires a UI schema version bump.")
enum class UiIconName {
    @FieldDoc("Settings / gear.") SETTINGS,
    @FieldDoc("Music note.") MUSIC,
    @FieldDoc("Album.") ALBUM,
    @FieldDoc("Artist.") ARTIST,
    @FieldDoc("Playlist.") PLAYLIST,
    @FieldDoc("Picture.") IMAGE,
    @FieldDoc("Storage / disk.") STORAGE,
    @FieldDoc("Statistics / chart.") STATS,
    @FieldDoc("Scheduled task.") TASK,
    @FieldDoc("Play.") PLAY,
    @FieldDoc("Pause.") PAUSE,
    @FieldDoc("Download.") DOWNLOAD,
    @FieldDoc("Import.") IMPORT,
    @FieldDoc("Queue / list.") QUEUE,
    @FieldDoc("Connected service / plug.") PLUG,
    @FieldDoc("Login.") LOGIN,
    @FieldDoc("Heart / favorites.") HEART,
    @FieldDoc("Sync / refresh arrows.") SYNC,
    @FieldDoc("Search.") SEARCH,
    @FieldDoc("Key / credentials.") KEY,
    @FieldDoc("Database.") DATABASE,
    @FieldDoc("Warning.") WARNING,
    @FieldDoc("Error.") ERROR,
    @FieldDoc("Info.") INFO,
    @FieldDoc("User / person.") USER,
    @FieldDoc("Checkmark / done.") CHECK,
    @FieldDoc("Close / dismiss.") CLOSE,
    @FieldDoc("Link.") LINK,
    @FieldDoc("Generic file / document.") FILE,
    @FieldDoc("More / overflow menu.") MORE,
    @FieldDoc("Barcode scanner.") BARCODE,
}

@Serializable
@ModelDoc("An icon: a semantic name from UiIconName, an external URL, or a server image.")
sealed class UiIcon {
    @Serializable
    @SerialName("named")
    @ModelDoc("A semantic icon the client renders from its own icon set.")
    data class Named(
        @FieldDoc("The icon.")
        val name: UiIconName,
    ) : UiIcon()

    @Serializable
    @SerialName("url")
    @ModelDoc("An image loaded from an external URL, rendered at icon size.")
    data class Url(
        @FieldDoc("Image URL.")
        val url: String,
    ) : UiIcon()

    @Serializable
    @SerialName("image")
    @ModelDoc("A server image (IImageService) rendered at icon size.")
    data class Image(
        @FieldDoc("Image id.")
        val imageId: PlatformUUID,
    ) : UiIcon()

    companion object {
        operator fun invoke(name: UiIconName): UiIcon = Named(name)
    }
}
