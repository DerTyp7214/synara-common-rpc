package dev.dertyp.services.import

import dev.dertyp.rpc.annotations.FieldDoc
import dev.dertyp.rpc.annotations.ModelDoc
import kotlinx.serialization.Serializable

@Serializable
@ModelDoc("A capability an importer backend supports, so clients can discover what each backend can do.")
enum class ImporterCapability {
    @FieldDoc("Can import individual songs/tracks.")
    IMPORT_SONG,

    @FieldDoc("Can import full albums.")
    IMPORT_ALBUM,

    @FieldDoc("Can import an artist's catalog.")
    IMPORT_ARTIST,

    @FieldDoc("Can import playlists.")
    IMPORT_PLAYLIST,

    @FieldDoc("Can import music videos.")
    IMPORT_VIDEO,

    @FieldDoc("Can import curated mixes.")
    IMPORT_MIX,

    @FieldDoc("Accepts credentials injected at runtime via setImportCredentials/provideCredentials.")
    CREDENTIALS,

    @FieldDoc("Supports an interactive OAuth/login flow (importLogin).")
    LOGIN,

    @FieldDoc("Can import a user's favorite collection and sync favorites.")
    FAVORITES,

    @FieldDoc("Supports searching the source catalog.")
    SEARCH,
}
