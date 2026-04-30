use serde::{Deserialize, Serialize};
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_int, c_void};
use futures_util::Stream;
use std::pin::Pin;
use std::task::{Context, Poll};
use std::sync::Arc;
use tokio::sync::mpsc;
use serde_bytes;

pub type FlowCallback = extern "C" fn(*mut c_void, *const u8, c_int);

#[repr(C)]
pub struct NativeRpcManager { _unused: [u8; 0] }

impl NativeRpcManager {
    pub fn new() -> *mut Self {
        unsafe { common_rpc_manager_create() }
    }
}

extern "C" {
    pub fn common_rpc_manager_create() -> *mut NativeRpcManager;
    pub fn common_rpc_manager_release(ptr: *mut NativeRpcManager);
    pub fn common_rpc_free_buffer(ptr: *mut u8);
    pub fn common_rpc_call(ptr: *mut NativeRpcManager, service: *const c_char, method: *const c_char, args: *const u8, len: c_int, out_len: *mut c_int) -> *mut u8;
    pub fn common_rpc_subscribe(ptr: *mut NativeRpcManager, service: *const c_char, method: *const c_char, args: *const u8, len: c_int, ctx: *mut c_void, cb: FlowCallback) -> *mut c_void;
    pub fn common_rpc_unsubscribe(job: *mut c_void);
    pub fn common_rpc_set_url(ptr: *mut NativeRpcManager, url: *const c_char);
    pub fn common_rpc_get_url(ptr: *mut NativeRpcManager) -> *mut c_char;
    pub fn common_rpc_validate_server(ptr: *mut NativeRpcManager, url: *const c_char) -> bool;
    pub fn common_rpc_update_auth(ptr: *mut NativeRpcManager, args: *const u8, len: c_int);
    pub fn common_rpc_is_authenticated(ptr: *mut NativeRpcManager) -> bool;
    pub fn common_rpc_get_auth_token(ptr: *mut NativeRpcManager) -> *mut c_char;
    pub fn common_rpc_get_refresh_token(ptr: *mut NativeRpcManager) -> *mut c_char;
    pub fn common_rpc_is_token_expired(ptr: *mut NativeRpcManager) -> bool;
}

pub struct RpcStream<T> {
    rx: mpsc::UnboundedReceiver<T>,
    job: *mut c_void,
}

impl<T> RpcStream<T> {
    pub fn new(rx: mpsc::UnboundedReceiver<T>, job: *mut c_void) -> Self {
        Self { rx, job }
    }
}

impl<T> Stream for RpcStream<T> {
    type Item = T;
    fn poll_next(mut self: Pin<&mut Self>, cx: &mut Context<'_>) -> Poll<Option<Self::Item>> {
        self.rx.poll_recv(cx)
    }
}

impl<T> Drop for RpcStream<T> {
    fn drop(&mut self) {
        unsafe { common_rpc_unsubscribe(self.job) };
    }
}
unsafe impl<T> Send for RpcStream<T> {}

extern "C" fn flow_callback_handler<T: for<'de> Deserialize<'de> + Send + 'static>(ctx: *mut c_void, data: *const u8, len: c_int) {
    let tx = unsafe { &*(ctx as *const mpsc::UnboundedSender<T>) };
    let bytes = unsafe { std::slice::from_raw_parts(data, len as usize) };
    if let Ok(val) = serde_cbor::from_slice(bytes) {
        let _ = tx.send(val);
    }
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)] #[serde(transparent)] pub struct PlatformUUID(pub serde_bytes::ByteBuf);
#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)] #[serde(transparent)] pub struct PlatformDate(pub i64);
#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)] #[serde(transparent)] pub struct PlatformDateTime(pub String);
#[derive(Serialize, Deserialize, Debug, Clone, PartialEq, Default)] #[serde(transparent)] pub struct SuspendFunction0(pub serde_json::Value);

#[derive(Serialize, Deserialize, Debug, Clone)] pub struct RpcEnvelope { pub data: Option<serde_bytes::ByteBuf>, pub error: Option<String> }

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMirrorServiceGetSongDataArgs {
    #[serde(rename = "songId")]
    pub song_id: PlatformUUID,
    pub quality: i32,
    #[serde(rename = "chunkSize")]
    pub chunk_size: i32,
    pub force: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDiscoveryServiceGetSimilarSongsArgs {
    #[serde(rename = "seedSongIds")]
    pub seed_song_ids: Vec<PlatformUUID>,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDiscoveryServiceGetSimilarSongsByPlaylistArgs {
    #[serde(rename = "playlistId")]
    pub playlist_id: PlatformUUID,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDiscoveryServiceGetSimilarSongsByBpmArgs {
    #[serde(rename = "seedSongIds")]
    pub seed_song_ids: Vec<PlatformUUID>,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDiscoveryServiceGetSimilarSongsByEnergyArgs {
    #[serde(rename = "seedSongIds")]
    pub seed_song_ids: Vec<PlatformUUID>,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDiscoveryServiceGetSimilarSongsByMoodArgs {
    #[serde(rename = "seedSongIds")]
    pub seed_song_ids: Vec<PlatformUUID>,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDiscoveryServiceGetSongsBySameComposersArgs {
    #[serde(rename = "seedSongIds")]
    pub seed_song_ids: Vec<PlatformUUID>,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDiscoveryServiceGetSongsBySameLyricistsArgs {
    #[serde(rename = "seedSongIds")]
    pub seed_song_ids: Vec<PlatformUUID>,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDiscoveryServiceGetSongsBySameProducersArgs {
    #[serde(rename = "seedSongIds")]
    pub seed_song_ids: Vec<PlatformUUID>,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ILyricsSearchSearchLyricsArgs {
    pub artist: String,
    pub title: String,
    #[serde(rename = "syncedOnly")]
    pub synced_only: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IPlaylistServiceRankedSearchArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    pub query: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IPlaylistServiceAllPlaylistsArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IFavSyncServiceInsertFavSyncArgs {
    pub service: SyncServiceType,
    #[serde(rename = "syncedAt")]
    pub synced_at: PlatformDate,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IUserPlaylistServiceRankedSearchArgs {
    pub creator: Option<PlatformUUID>,
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    pub query: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IUserPlaylistServiceAllPlaylistsArgs {
    pub creator: Option<PlatformUUID>,
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IUserPlaylistServiceGetOrAddPlaylistArgs {
    pub user: User,
    #[serde(rename = "customIdentifier")]
    pub custom_identifier: Option<String>,
    pub playlist: InsertablePlaylist,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IUserPlaylistServiceAddToPlaylistArgs {
    pub id: PlatformUUID,
    #[serde(rename = "songIds")]
    pub song_ids: Vec<(i64, PlatformUUID)>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IUserPlaylistServiceRemoveFromPlaylistArgs {
    pub id: PlatformUUID,
    #[serde(rename = "songIds")]
    pub song_ids: Vec<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IUserPlaylistServiceSetPlaylistImageArgs {
    pub id: PlatformUUID,
    #[serde(rename = "imageId")]
    pub image_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceSearchArtistsArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    pub query: String,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceSearchArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    pub query: String,
    pub limit: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceSearchAlbumsArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    pub query: String,
    pub limit: i32,
    #[serde(rename = "includeTracks")]
    pub include_tracks: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetAlbumIdByTrackIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "trackId")]
    pub track_id: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetImageUrlByAlbumIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "albumId")]
    pub album_id: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetArtistByMbIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "mbId")]
    pub mb_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetAlbumByMbIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "mbId")]
    pub mb_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetTrackByMbIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "mbId")]
    pub mb_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetImageUrlByArtistMbIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "mbId")]
    pub mb_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetImageUrlByAlbumMbIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "mbId")]
    pub mb_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetImageUrlByTrackMbIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "mbId")]
    pub mb_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetImageUrlsByAlbumIdsArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "albumIds")]
    pub album_ids: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetImageUrlByImageIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "imageId")]
    pub image_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetTrackByIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "trackId")]
    pub track_id: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetTracksByIdsArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "trackIds")]
    pub track_ids: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetAlbumsByIdsArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "albumIds")]
    pub album_ids: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceAlbumExistsByIdArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "albumId")]
    pub album_id: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceGetArtistsByIdsArgs {
    #[serde(rename = "type")]
    pub r#type: MetadataType,
    #[serde(rename = "artistIds")]
    pub artist_ids: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMusicBrainzServiceSearchRecordingArgs {
    pub title: String,
    pub artists: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMusicBrainzServiceSearchReleaseArgs {
    pub title: String,
    pub artists: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ILyricsServiceTranscribeLyricsArgs {
    #[serde(rename = "songId")]
    pub song_id: PlatformUUID,
    pub lyrics: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ICustomAudioServiceUploadCustomAudioArgs {
    #[serde(rename = "fileData")]
    pub file_data: serde_bytes::ByteBuf,
    #[serde(rename = "fileName")]
    pub file_name: String,
    pub metadata: Option<CustomMetadata>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IPlaybackServiceSetPlaybackStateArgs {
    #[serde(rename = "sessionId")]
    pub session_id: PlatformUUID,
    pub state: PlaybackState,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IArtistServiceRankedSearchArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    pub query: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IArtistServiceSetGroupArgs {
    pub id: PlatformUUID,
    #[serde(rename = "artistIds")]
    pub artist_ids: Option<Vec<PlatformUUID>>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IArtistServiceByGroupArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    #[serde(rename = "groupId")]
    pub group_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IArtistServiceAllArtistsArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IArtistServiceCreateArtistArgs {
    pub name: String,
    #[serde(rename = "isGroup")]
    pub is_group: bool,
    pub about: String,
    #[serde(rename = "musicBrainzId")]
    pub music_brainz_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IArtistServiceSearchArtistOnMusicBrainzArgs {
    pub query: String,
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IArtistServiceSetMusicBrainzIdArgs {
    pub id: PlatformUUID,
    #[serde(rename = "musicBrainzId")]
    pub music_brainz_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IAuthServiceAuthenticateArgs {
    pub username: String,
    pub password: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IReleaseServiceGetRecentReleasesArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IAlbumServiceByNameArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    pub name: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IAlbumServiceRankedSearchArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    pub query: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IAlbumServiceAllAlbumsArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IAlbumServiceSetMusicBrainzIdArgs {
    pub id: PlatformUUID,
    #[serde(rename = "musicBrainzId")]
    pub music_brainz_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IAlbumServiceByArtistArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    #[serde(rename = "artistId")]
    pub artist_id: PlatformUUID,
    pub singles: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IImageServiceGetImageDataArgs {
    pub id: PlatformUUID,
    pub size: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IImageServiceCreateImageArgs {
    pub bytes: serde_bytes::ByteBuf,
    pub origin: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IImageServiceMoveImagesArgs {
    #[serde(rename = "oldPath")]
    pub old_path: String,
    #[serde(rename = "newPath")]
    pub new_path: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceSetLikedArgs {
    pub id: PlatformUUID,
    pub liked: bool,
    #[serde(rename = "addedAt")]
    pub added_at: Option<PlatformDateTime>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceSetLyricsArgs {
    pub id: PlatformUUID,
    pub lyrics: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceSetArtistsArgs {
    pub id: PlatformUUID,
    #[serde(rename = "artistIds")]
    pub artist_ids: Vec<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceSetMusicBrainzIdArgs {
    pub id: PlatformUUID,
    #[serde(rename = "musicBrainzId")]
    pub music_brainz_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceByTitleArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    pub title: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceByArtistArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    #[serde(rename = "artistId")]
    pub artist_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceLikedByArtistArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    #[serde(rename = "artistId")]
    pub artist_id: PlatformUUID,
    pub explicit: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceByAlbumArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    #[serde(rename = "albumId")]
    pub album_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceByPlaylistArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    #[serde(rename = "playlistId")]
    pub playlist_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceByUserPlaylistArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    #[serde(rename = "playlistId")]
    pub playlist_id: PlatformUUID,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceLikedSongsArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    pub explicit: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceAllSongsArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    pub explicit: bool,
    pub tags: Vec<SongTag>,
    #[serde(rename = "invertTags")]
    pub invert_tags: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceRankedSearchArgs {
    pub page: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    pub query: String,
    pub explicit: bool,
    pub liked: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceStreamSongArgs {
    pub id: PlatformUUID,
    pub offset: i64,
    #[serde(rename = "chunkSize")]
    pub chunk_size: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceDownloadSongArgs {
    pub id: PlatformUUID,
    pub quality: i32,
    pub offset: i64,
    #[serde(rename = "chunkSize")]
    pub chunk_size: i32,
    pub force: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceGetDownloadSizeArgs {
    pub id: PlatformUUID,
    pub quality: i32,
    pub force: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceAllSongIdsArgs {
    pub explicit: bool,
    pub tags: Vec<SongTag>,
    #[serde(rename = "invertTags")]
    pub invert_tags: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceMoveSongsArgs {
    #[serde(rename = "oldPath")]
    pub old_path: String,
    #[serde(rename = "newPath")]
    pub new_path: String,
    #[serde(rename = "originalIdPrefix")]
    pub original_id_prefix: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IRemoteMirrorServiceGetRemoteImageDataArgs {
    pub config: RemoteServerConfig,
    #[serde(rename = "imageId")]
    pub image_id: PlatformUUID,
    pub size: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDownloadServiceDownloadIdsArgs {
    pub ids: Vec<PrefixedId>,
    #[serde(rename = "type")]
    pub r#type: Type,
    pub downloader: Option<DownloadBackend>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDownloadServiceExistsByOriginalIdArgs {
    pub id: PrefixedId,
    #[serde(rename = "type")]
    pub r#type: Type,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDownloadServiceSearchArgs {
    pub query: Option<String>,
    pub title: Option<String>,
    pub artist: Option<String>,
    pub count: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct RemoteServerPaths {
    #[serde(rename = "tracksPath")]
    pub tracks_path: Option<String>,
    #[serde(rename = "albumsPath")]
    pub albums_path: Option<String>,
    #[serde(rename = "playlistsPath")]
    pub playlists_path: Option<String>,
    #[serde(rename = "customAudioPath")]
    pub custom_audio_path: Option<String>,
    #[serde(rename = "secondaryTracksPaths")]
    pub secondary_tracks_paths: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Song {
    pub id: PlatformUUID,
    pub title: String,
    pub artists: Vec<Artist>,
    pub album: Option<Album>,
    pub duration: i64,
    pub explicit: bool,
    #[serde(rename = "releaseDate")]
    pub release_date: Option<PlatformDateTime>,
    pub lyrics: String,
    pub path: String,
    #[serde(rename = "originalUrl")]
    pub original_url: String,
    #[serde(rename = "trackNumber")]
    pub track_number: i32,
    #[serde(rename = "discNumber")]
    pub disc_number: i32,
    pub copyright: String,
    #[serde(rename = "sampleRate")]
    pub sample_rate: i32,
    #[serde(rename = "bitsPerSample")]
    pub bits_per_sample: i32,
    #[serde(rename = "bitRate")]
    pub bit_rate: i64,
    #[serde(rename = "fileSize")]
    pub file_size: i64,
    #[serde(rename = "coverId")]
    pub cover_id: Option<PlatformUUID>,
    #[serde(rename = "musicBrainzId")]
    pub music_brainz_id: Option<PlatformUUID>,
    pub genres: Vec<Genre>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Artist {
    pub id: PlatformUUID,
    pub name: String,
    #[serde(rename = "isGroup")]
    pub is_group: bool,
    pub artists: Vec<Artist>,
    pub about: String,
    pub genres: Vec<Genre>,
    #[serde(rename = "imageId")]
    pub image_id: Option<PlatformUUID>,
    #[serde(rename = "musicbrainzId")]
    pub musicbrainz_id: Option<PlatformUUID>,
    #[serde(rename = "isFollowed")]
    pub is_followed: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Genre {
    pub id: PlatformUUID,
    pub name: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Album {
    pub id: PlatformUUID,
    pub name: String,
    pub artists: Vec<Artist>,
    #[serde(rename = "songCount")]
    pub song_count: i32,
    #[serde(rename = "releaseDate")]
    pub release_date: Option<PlatformDateTime>,
    #[serde(rename = "totalDuration")]
    pub total_duration: i64,
    #[serde(rename = "totalSize")]
    pub total_size: i64,
    #[serde(rename = "coverId")]
    pub cover_id: Option<PlatformUUID>,
    pub genres: Vec<Genre>,
    #[serde(rename = "originalId")]
    pub original_id: Option<String>,
    #[serde(rename = "musicbrainzId")]
    pub musicbrainz_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ArtistAlias {
    #[serde(rename = "artistId")]
    pub artist_id: PlatformUUID,
    pub name: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ArtistSplitAlias {
    #[serde(rename = "artistId")]
    pub artist_id: PlatformUUID,
    pub name: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Playlist {
    pub id: PlatformUUID,
    pub name: String,
    pub songs: Vec<PlatformUUID>,
    #[serde(rename = "totalDuration")]
    pub total_duration: i64,
    #[serde(rename = "imageId")]
    pub image_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct UserPlaylist {
    pub id: PlatformUUID,
    pub name: String,
    pub songs: Vec<PlatformUUID>,
    #[serde(rename = "songEntries")]
    pub song_entries: Option<Vec<UserPlaylistSong>>,
    #[serde(rename = "totalDuration")]
    pub total_duration: i64,
    #[serde(rename = "imageId")]
    pub image_id: Option<PlatformUUID>,
    pub creator: PlatformUUID,
    pub description: String,
    pub origin: Option<String>,
    #[serde(rename = "modifiedAt")]
    pub modified_at: Option<PlatformDate>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct UserPlaylistSong {
    #[serde(rename = "songId")]
    pub song_id: PlatformUUID,
    #[serde(rename = "addedAt")]
    pub added_at: i64,
    #[serde(rename = "musicBrainzId")]
    pub music_brainz_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Image {
    pub id: PlatformUUID,
    pub path: String,
    #[serde(rename = "imageHash")]
    pub image_hash: String,
    pub origin: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct User {
    pub id: PlatformUUID,
    pub username: String,
    #[serde(rename = "displayName")]
    pub display_name: Option<String>,
    #[serde(rename = "passwordHash")]
    pub password_hash: String,
    #[serde(rename = "isAdmin")]
    pub is_admin: bool,
    #[serde(rename = "profileImageId")]
    pub profile_image_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct BackupInfo {
    pub name: String,
    pub size: i64,
    pub date: i64,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct UserPlaylistBackup {
    #[serde(rename = "userId")]
    pub user_id: PlatformUUID,
    pub playlists: Vec<UserPlaylist>,
    pub images: Option<Vec<BackupImage>>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct BackupImage {
    pub image: Image,
    pub data: serde_bytes::ByteBuf,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct UserSong {
    pub id: PlatformUUID,
    pub title: String,
    pub artists: Vec<Artist>,
    pub album: Option<Album>,
    pub duration: i64,
    pub explicit: bool,
    #[serde(rename = "releaseDate")]
    pub release_date: Option<PlatformDateTime>,
    pub lyrics: String,
    pub path: String,
    #[serde(rename = "originalUrl")]
    pub original_url: String,
    #[serde(rename = "trackNumber")]
    pub track_number: i32,
    #[serde(rename = "discNumber")]
    pub disc_number: i32,
    pub copyright: String,
    #[serde(rename = "sampleRate")]
    pub sample_rate: i32,
    #[serde(rename = "bitsPerSample")]
    pub bits_per_sample: i32,
    #[serde(rename = "bitRate")]
    pub bit_rate: i64,
    #[serde(rename = "fileSize")]
    pub file_size: i64,
    #[serde(rename = "coverId")]
    pub cover_id: Option<PlatformUUID>,
    #[serde(rename = "musicBrainzId")]
    pub music_brainz_id: Option<PlatformUUID>,
    pub genres: Vec<Genre>,
    #[serde(rename = "isFavourite")]
    pub is_favourite: Option<bool>,
    #[serde(rename = "userSongCreatedAt")]
    pub user_song_created_at: Option<PlatformDate>,
    #[serde(rename = "userSongUpdatedAt")]
    pub user_song_updated_at: Option<PlatformDate>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct PlaylistEntry {
    pub id: PlatformUUID,
    pub name: String,
    pub duration: i64,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct PaginatedResponse<T> {
    pub data: Vec<T>,
    pub page: i32,
    pub total: i32,
    #[serde(rename = "pageSize")]
    pub page_size: i32,
    #[serde(rename = "hasNextPage")]
    pub has_next_page: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub enum SyncServiceType {
    #[serde(rename = "tidal")]
    Tidal,
    #[serde(rename = "unknown")]
    Unknown,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct FavSync {
    #[serde(rename = "userId")]
    pub user_id: PlatformUUID,
    pub service: SyncServiceType,
    #[serde(rename = "syncedAt")]
    pub synced_at: PlatformDate,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct InsertablePlaylist {
    pub name: String,
    pub description: String,
    #[serde(rename = "songPaths")]
    pub song_paths: Vec<String>,
    #[serde(rename = "imageHash")]
    pub image_hash: Option<String>,
    pub origin: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MetadataType {
    pub value: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceArtist {
    pub id: String,
    pub name: String,
    pub popularity: Float,
    pub url: Option<String>,
    pub images: Vec<IMetadataServiceImage>,
    pub biography: Option<String>,
    pub styles: Vec<String>,
    pub genres: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceImage {
    pub url: String,
    pub width: i32,
    pub height: i32,
    pub animated: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Track {
    pub id: String,
    pub title: String,
    pub artists: Vec<String>,
    pub duration: String,
    #[serde(rename = "createdAt")]
    pub created_at: Option<PlatformDateTime>,
    #[serde(rename = "addedAt")]
    pub added_at: Option<PlatformDateTime>,
    #[serde(rename = "trackNumber")]
    pub track_number: Option<i32>,
    #[serde(rename = "discNumber")]
    pub disc_number: Option<i32>,
    pub images: Vec<IMetadataServiceImage>,
    pub genres: Vec<String>,
    #[serde(rename = "albumId")]
    pub album_id: Option<String>,
    #[serde(rename = "albumTitle")]
    pub album_title: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceAlbum {
    pub id: String,
    pub title: String,
    pub artists: Vec<String>,
    pub duration: String,
    #[serde(rename = "trackCount")]
    pub track_count: i32,
    #[serde(rename = "discCount")]
    pub disc_count: i32,
    #[serde(rename = "releaseDate")]
    pub release_date: Option<PlatformDateTime>,
    pub images: Vec<IMetadataServiceImage>,
    pub genres: Vec<String>,
    #[serde(rename = "additionalTitles")]
    pub additional_titles: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzArtist {
    pub id: PlatformUUID,
    pub name: Option<String>,
    #[serde(rename = "type")]
    pub r#type: Option<ArtistType>,
    pub gender: Option<String>,
    pub country: Option<String>,
    #[serde(rename = "sortName")]
    pub sort_name: Option<String>,
    pub disambiguation: Option<String>,
    #[serde(rename = "lifeSpan")]
    pub life_span: Option<MusicBrainzLifeSpan>,
    pub area: Option<MusicBrainzArea>,
    #[serde(rename = "beginArea")]
    pub begin_area: Option<MusicBrainzArea>,
    #[serde(rename = "endArea")]
    pub end_area: Option<MusicBrainzArea>,
    pub tags: Option<Vec<MusicBrainzTag>>,
    pub genres: Option<Vec<MusicBrainzGenre>>,
    pub aliases: Option<Vec<MusicBrainzAlias>>,
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub enum ArtistType {
    #[serde(rename = "PERSON")]
    Person,
    #[serde(rename = "GROUP")]
    Group,
    #[serde(rename = "ORCHESTRA")]
    Orchestra,
    #[serde(rename = "CHOIR")]
    Choir,
    #[serde(rename = "CHARACTER")]
    Character,
    #[serde(rename = "OTHER")]
    Other,
    #[serde(rename = "UNKNOWN")]
    Unknown,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzLifeSpan {
    pub begin: Option<String>,
    pub end: Option<String>,
    pub ended: Option<bool>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzArea {
    pub id: PlatformUUID,
    pub name: Option<String>,
    #[serde(rename = "sortName")]
    pub sort_name: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzTag {
    pub count: i32,
    pub name: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzGenre {
    pub id: PlatformUUID,
    pub name: String,
    pub count: Option<i32>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzAlias {
    pub name: String,
    #[serde(rename = "sortName")]
    pub sort_name: String,
    pub locale: Option<String>,
    #[serde(rename = "type")]
    pub r#type: Option<String>,
    pub primary: Option<bool>,
    #[serde(rename = "beginDate")]
    pub begin_date: Option<String>,
    #[serde(rename = "endDate")]
    pub end_date: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzRecording {
    pub id: PlatformUUID,
    pub title: Option<String>,
    #[serde(rename = "artistCredit")]
    pub artist_credit: Option<Vec<MusicBrainzArtistCredit>>,
    pub releases: Option<Vec<MusicBrainzRelease>>,
    pub length: Option<i64>,
    pub tags: Option<Vec<MusicBrainzTag>>,
    pub genres: Option<Vec<MusicBrainzGenre>>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzArtistCredit {
    pub name: Option<String>,
    pub joinphrase: Option<String>,
    pub artist: Option<MusicBrainzArtist>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzRelease {
    pub id: PlatformUUID,
    pub title: Option<String>,
    pub status: Option<String>,
    pub quality: Option<String>,
    pub barcode: Option<String>,
    pub country: Option<String>,
    pub date: Option<String>,
    pub disambiguation: Option<String>,
    #[serde(rename = "releaseGroup")]
    pub release_group: Option<MusicBrainzReleaseGroup>,
    pub relations: Option<Vec<MusicBrainzRelation>>,
    pub tags: Option<Vec<MusicBrainzTag>>,
    pub genres: Option<Vec<MusicBrainzGenre>>,
    #[serde(rename = "artistCredit")]
    pub artist_credit: Option<Vec<MusicBrainzArtistCredit>>,
    pub media: Option<Vec<MusicBrainzMedia>>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzReleaseGroup {
    pub id: PlatformUUID,
    pub title: String,
    #[serde(rename = "primaryType")]
    pub primary_type: Option<String>,
    #[serde(rename = "firstReleaseDate")]
    pub first_release_date: Option<String>,
    pub relations: Option<Vec<MusicBrainzRelation>>,
    pub tags: Option<Vec<MusicBrainzTag>>,
    pub genres: Option<Vec<MusicBrainzGenre>>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzRelation {
    #[serde(rename = "type")]
    pub r#type: Option<String>,
    pub url: Option<MusicBrainzRelationUrl>,
    #[serde(rename = "releaseGroup")]
    pub release_group: Option<MusicBrainzReleaseGroup>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzRelationUrl {
    pub id: PlatformUUID,
    pub resource: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzMedia {
    pub format: Option<String>,
    #[serde(rename = "trackCount")]
    pub track_count: Option<i32>,
    pub tracks: Option<Vec<MusicBrainzTrack>>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MusicBrainzTrack {
    pub id: PlatformUUID,
    pub position: Option<i32>,
    pub number: Option<String>,
    pub title: Option<String>,
    pub recording: Option<MusicBrainzRecording>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct SyncedLyrics {
    pub lines: Vec<LyricLine>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct LyricLine {
    #[serde(rename = "startTime")]
    pub start_time: String,
    #[serde(rename = "endTime")]
    pub end_time: String,
    pub words: Vec<LyricWord>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct LyricWord {
    pub text: String,
    #[serde(rename = "startTime")]
    pub start_time: String,
    #[serde(rename = "endTime")]
    pub end_time: String,
    pub chars: Option<Vec<LyricChar>>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct LyricChar {
    pub char: String,
    #[serde(rename = "startTime")]
    pub start_time: String,
    #[serde(rename = "endTime")]
    pub end_time: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ScheduledTaskLog {
    pub id: PlatformUUID,
    #[serde(rename = "taskName")]
    pub task_name: String,
    #[serde(rename = "startTime")]
    pub start_time: i64,
    #[serde(rename = "endTime")]
    pub end_time: i64,
    pub status: TaskStatus,
    pub message: Option<String>,
    pub details: Option<std::collections::HashMap<String, String>>,
    pub progress: Double,
    pub logs: Vec<String>,
    #[serde(rename = "logTime")]
    pub log_time: i64,
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub enum TaskStatus {
    #[serde(rename = "SUCCESS")]
    Success,
    #[serde(rename = "FAILURE")]
    Failure,
    #[serde(rename = "RUNNING")]
    Running,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct CustomMetadata {
    pub title: Option<String>,
    pub artists: Option<Vec<String>>,
    pub album: Option<String>,
    pub year: Option<String>,
    pub genre: Option<String>,
    #[serde(rename = "coverData")]
    pub cover_data: Option<serde_bytes::ByteBuf>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct PlaybackState {
    pub queue: Vec<QueueEntry>,
    #[serde(rename = "currentIndex")]
    pub current_index: i32,
    #[serde(rename = "isPlaying")]
    pub is_playing: bool,
    #[serde(rename = "positionMs")]
    pub position_ms: i64,
    #[serde(rename = "shuffleMode")]
    pub shuffle_mode: bool,
    #[serde(rename = "repeatMode")]
    pub repeat_mode: RepeatMode,
    #[serde(rename = "sourceId")]
    pub source_id: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct QueueEntry {
    #[serde(rename = "queueId")]
    pub queue_id: i64,
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub enum RepeatMode {
    #[serde(rename = "OFF")]
    Off,
    #[serde(rename = "ALL")]
    All,
    #[serde(rename = "ONE")]
    One,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Session {
    pub id: PlatformUUID,
    #[serde(rename = "userAgent")]
    pub user_agent: String,
    #[serde(rename = "ipAddress")]
    pub ip_address: String,
    #[serde(rename = "lastActive")]
    pub last_active: i64,
    #[serde(rename = "isActive")]
    pub is_active: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MergeArtists {
    pub name: String,
    pub image: Option<String>,
    #[serde(rename = "artistIds")]
    pub artist_ids: Vec<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct SplitArtist {
    #[serde(rename = "artistId")]
    pub artist_id: PlatformUUID,
    #[serde(rename = "newArtists")]
    pub new_artists: std::collections::HashMap<String, Option<PlatformUUID>>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct AuthenticationResponse {
    pub token: String,
    #[serde(rename = "refreshToken")]
    pub refresh_token: String,
    #[serde(rename = "expiresAt")]
    pub expires_at: PlatformDate,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct SongAudioData {
    pub bpm: Option<Double>,
    pub key: Option<String>,
    pub scale: Option<AudioScale>,
    pub loudness: Option<Double>,
    pub energy: Option<Double>,
    pub valence: Option<Double>,
    pub danceability: Option<Double>,
    pub acousticness: Option<Double>,
    pub instrumentalness: Option<Double>,
    pub speechiness: Option<Double>,
    pub composer: Option<Vec<String>>,
    pub lyricist: Option<Vec<String>>,
    pub producers: Option<Vec<String>>,
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub enum AudioScale {
    Major,
    Minor,
    MajMin,
    Unknown,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct FollowedArtist {
    #[serde(rename = "artistId")]
    pub artist_id: PlatformUUID,
    pub name: String,
    #[serde(rename = "imageId")]
    pub image_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct RecentRelease {
    #[serde(rename = "releaseId")]
    pub release_id: PlatformUUID,
    #[serde(rename = "artistId")]
    pub artist_id: PlatformUUID,
    #[serde(rename = "artistName")]
    pub artist_name: String,
    pub title: String,
    #[serde(rename = "releaseDate")]
    pub release_date: Option<PlatformDate>,
    #[serde(rename = "type")]
    pub r#type: ReleaseType,
    #[serde(rename = "imageId")]
    pub image_id: Option<PlatformUUID>,
    pub links: Vec<String>,
    #[serde(rename = "albumId")]
    pub album_id: Option<PlatformUUID>,
    #[serde(rename = "songId")]
    pub song_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub enum ReleaseType {
    Album,
    Single,
    #[serde(rename = "EP")]
    Ep,
    Broadcast,
    Other,
    Unknown,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct InsertableImage {
    pub data: serde_bytes::ByteBuf,
    #[serde(rename = "imageHash")]
    pub image_hash: String,
    pub origin: String,
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub enum SongTag {
    #[serde(rename = "Q_44_48")]
    Q4448,
    #[serde(rename = "Q_96")]
    Q96,
    #[serde(rename = "Q_192")]
    Q192,
    #[serde(rename = "B_16")]
    B16,
    #[serde(rename = "B_24")]
    B24,
    #[serde(rename = "HAS_LYRICS")]
    HasLyrics,
    #[serde(rename = "CUSTOM_UPLOAD")]
    CustomUpload,
    #[serde(rename = "HAS_MUSICBRAINZ_ID")]
    HasMusicbrainzId,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ServerStats {
    #[serde(rename = "songCount")]
    pub song_count: i32,
    #[serde(rename = "albumCount")]
    pub album_count: i32,
    #[serde(rename = "artistCount")]
    pub artist_count: i32,
    #[serde(rename = "imagesCount")]
    pub images_count: i32,
    #[serde(rename = "playlistCount")]
    pub playlist_count: i32,
    #[serde(rename = "totalFileSize")]
    pub total_file_size: i64,
    #[serde(rename = "indexedFileSize")]
    pub indexed_file_size: i64,
    #[serde(rename = "averageSizePerSong")]
    pub average_size_per_song: i64,
    #[serde(rename = "totalDuration")]
    pub total_duration: i64,
    pub version: Version,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Version {
    pub version: String,
    #[serde(rename = "buildTime")]
    pub build_time: String,
    #[serde(rename = "commitHash")]
    pub commit_hash: String,
    pub runtime: String,
    pub kernel: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ProxyInfo {
    pub host: String,
    #[serde(rename = "controlPort")]
    pub control_port: i32,
    pub ssl: bool,
    pub id: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct BackupResult {
    #[serde(rename = "fileName")]
    pub file_name: String,
    pub size: i64,
    #[serde(rename = "imageCount")]
    pub image_count: i32,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct RemoteServerConfig {
    pub host: String,
    pub port: i32,
    pub username: String,
    pub password: String,
    pub secure: bool,
    pub quality: i32,
    #[serde(rename = "playlistIds")]
    pub playlist_ids: Option<Vec<PlatformUUID>>,
    #[serde(rename = "userPlaylistIds")]
    pub user_playlist_ids: Option<Vec<PlatformUUID>>,
    #[serde(rename = "likedByUserIds")]
    pub liked_by_user_ids: Option<Vec<PlatformUUID>>,
    #[serde(rename = "useProxy")]
    pub use_proxy: bool,
    #[serde(rename = "proxyInstanceId")]
    pub proxy_instance_id: Option<String>,
    #[serde(rename = "targetUserId")]
    pub target_user_id: Option<PlatformUUID>,
    #[serde(rename = "isImport")]
    pub is_import: bool,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MirrorProgress {
    #[serde(rename = "currentTask")]
    pub current_task: String,
    #[serde(rename = "processedItems")]
    pub processed_items: i32,
    #[serde(rename = "totalItems")]
    pub total_items: i32,
    #[serde(rename = "isFinished")]
    pub is_finished: bool,
    pub error: Option<String>,
    #[serde(rename = "currentItem")]
    pub current_item: Option<String>,
    #[serde(rename = "currentItemProgress")]
    pub current_item_progress: Option<Float>,
    pub speed: Option<String>,
    pub eta: Option<String>,
    #[serde(rename = "statusMessage")]
    pub status_message: Option<String>,
    #[serde(rename = "syncBreakdown")]
    pub sync_breakdown: Option<SyncBreakdown>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct SyncBreakdown {
    pub songs: i32,
    #[serde(rename = "existingSongs")]
    pub existing_songs: i32,
    pub artists: i32,
    #[serde(rename = "existingArtists")]
    pub existing_artists: i32,
    pub albums: i32,
    #[serde(rename = "existingAlbums")]
    pub existing_albums: i32,
    pub images: i32,
    #[serde(rename = "existingImages")]
    pub existing_images: i32,
    pub playlists: i32,
    #[serde(rename = "existingPlaylists")]
    pub existing_playlists: i32,
    #[serde(rename = "userPlaylists")]
    pub user_playlists: i32,
    #[serde(rename = "existingUserPlaylists")]
    pub existing_user_playlists: i32,
    pub errors: i32,
    #[serde(rename = "failedItems")]
    pub failed_items: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ProxyInstanceInfo {
    pub id: String,
    pub name: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct LogLine {
    #[serde(rename = "queueEntry")]
    pub queue_entry: DownloadQueueEntry,
    pub line: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct DownloadQueueEntry {
    #[serde(rename = "type")]
    pub r#type: Option<Type>,
    #[serde(rename = "maxRetries")]
    pub max_retries: i32,
    #[serde(rename = "byUser")]
    pub by_user: Option<PlatformUUID>,
    #[serde(skip)]
    pub callback: SuspendFunction0,
}

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub enum Type {
    #[serde(rename = "MIX")]
    Mix,
    #[serde(rename = "SONG")]
    Song,
    #[serde(rename = "ALBUM")]
    Album,
    #[serde(rename = "PLAYLIST")]
    Playlist,
    #[serde(rename = "ARTIST")]
    Artist,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct FinishedDownloadQueueEntry {
    #[serde(rename = "downloadQueueEntry")]
    pub download_queue_entry: DownloadQueueEntry,
    pub result: ProcessExecutionResult,
    pub logs: Vec<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ProcessExecutionResult {
    #[serde(rename = "exitCode")]
    pub exit_code: i32,
    #[serde(rename = "fullOutput")]
    pub full_output: String,
    pub error: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct DownloadBackend {
    pub id: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct DownloadSong {
    pub id: String,
    pub title: String,
    pub artists: Vec<String>,
    pub cover: std::collections::HashMap<i32, String>,
}

pub trait IIndexer {
    fn start(&self, ) -> RpcStream<String>;
}

pub trait IMirrorService {
    fn get_server_paths<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<RemoteServerPaths, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_songs(&self, ) -> RpcStream<Song>;
    fn get_artists(&self, ) -> RpcStream<Artist>;
    fn get_artist_aliases(&self, ) -> RpcStream<ArtistAlias>;
    fn get_artist_split_aliases(&self, ) -> RpcStream<ArtistSplitAlias>;
    fn get_albums(&self, ) -> RpcStream<Album>;
    fn get_playlists(&self, ) -> RpcStream<Playlist>;
    fn get_user_playlists(&self, ) -> RpcStream<UserPlaylist>;
    fn get_image_metadata(&self, ) -> RpcStream<Image>;
    fn get_song_data(&self, song_id: PlatformUUID, quality: i32, chunk_size: i32, force: bool) -> RpcStream<serde_bytes::ByteBuf>;
    fn get_users(&self, ) -> RpcStream<User>;
    fn get_songs_by_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<Song>;
    fn get_songs_by_user_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<Song>;
    fn get_liked_songs(&self, user_id: PlatformUUID) -> RpcStream<Song>;
}

pub trait IUserPlaylistBackupService {
    fn create_backup<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn list_backups<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<BackupInfo>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn restore_backup<'life0, 'async_trait>(&'life0 self, file_name: Option<String>) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_backup_content<'life0, 'async_trait>(&'life0 self, file_name: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserPlaylistBackup>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn delete_backup<'life0, 'async_trait>(&'life0 self, file_name: String) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IDiscoveryService {
    fn get_similar_songs<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_similar_songs_by_playlist<'life0, 'async_trait>(&'life0 self, playlist_id: PlatformUUID, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_similar_songs_by_bpm<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_similar_songs_by_energy<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_similar_songs_by_mood<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_songs_by_same_composers<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_songs_by_same_lyricists<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_songs_by_same_producers<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait ILyricsSearch {
    fn search_lyrics<'life0, 'async_trait>(&'life0 self, artist: String, title: String, synced_only: bool) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<String>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait ISyncService {
}

pub trait IPlaylistService {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_id_full<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<(String, Vec<PlaylistEntry>)>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_name<'life0, 'async_trait>(&'life0 self, name: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_playlists<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn delete<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IFavSyncService {
    fn get_latest_fav_sync<'life0, 'async_trait>(&'life0 self, service: SyncServiceType) -> Pin<Box<dyn std::future::Future<Output = Result<Option<FavSync>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn insert_fav_sync<'life0, 'async_trait>(&'life0 self, service: SyncServiceType, synced_at: PlatformDate) -> Pin<Box<dyn std::future::Future<Output = Result<i32, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IUserPlaylistService {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn ranked_search<'life0, 'async_trait>(&'life0 self, creator: Option<PlatformUUID>, page: i32, page_size: i32, query: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_playlists<'life0, 'async_trait>(&'life0 self, creator: Option<PlatformUUID>, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn delete<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_or_add_playlist<'life0, 'async_trait>(&'life0 self, user: User, custom_identifier: Option<String>, playlist: InsertablePlaylist) -> Pin<Box<dyn std::future::Future<Output = Result<PlatformUUID, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn add_to_playlist<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, song_ids: Vec<(i64, PlatformUUID)>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn remove_from_playlist<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, song_ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<i32, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_playlist_image<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, image_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IMetadataService {
    fn search_artists<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, query: String, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn search<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, query: String, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Track>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn search_albums<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, query: String, limit: i32, include_tracks: bool) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceAlbum>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_album_id_by_track_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, track_id: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<String>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_image_url_by_album_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, album_id: String) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceImage>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_artist_by_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<IMetadataServiceArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_album_by_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<IMetadataServiceAlbum>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_track_by_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Track>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_image_url_by_artist_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceImage>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_image_url_by_album_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceImage>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_image_url_by_track_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceImage>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_image_urls_by_album_ids<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, album_ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<std::collections::HashMap<String, Vec<IMetadataServiceImage>>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_image_url_by_image_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, image_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<String>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_track_by_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, track_id: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Track>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_tracks_by_ids<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, track_ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Track>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_albums_by_ids<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, album_ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceAlbum>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn album_exists_by_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, album_id: String) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_artists_by_ids<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, artist_ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IMusicBrainzService {
    fn get_artist<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_recording<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzRecording>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_release<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzRelease>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_release_group<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzReleaseGroup>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn search_recording<'life0, 'async_trait>(&'life0 self, title: String, artists: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzRecording>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn search_release<'life0, 'async_trait>(&'life0 self, title: String, artists: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzRelease>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait ILyricsService {
    fn get_synced_lyrics<'life0, 'async_trait>(&'life0 self, song_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<SyncedLyrics>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn transcribe_lyrics<'life0, 'async_trait>(&'life0 self, song_id: PlatformUUID, lyrics: Option<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<SyncedLyrics>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn start_sync_worker<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IScheduledTaskLogService {
    fn get_grouped_logs<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<std::collections::HashMap<String, Vec<ScheduledTaskLog>>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_grouped_logs_flow(&self, ) -> RpcStream<std::collections::HashMap<String, Vec<ScheduledTaskLog>>>;
}

pub trait ICustomAudioService {
    fn upload_custom_audio<'life0, 'async_trait>(&'life0 self, file_data: serde_bytes::ByteBuf, file_name: String, metadata: Option<CustomMetadata>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IStorageService {
    fn get_total_storage<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<i64, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IUserService {
    fn find_user_by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn find_user_by_username<'life0, 'async_trait>(&'life0 self, username: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn me<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<User, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_all_users<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_profile_image<'life0, 'async_trait>(&'life0 self, bytes: serde_bytes::ByteBuf) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_display_name<'life0, 'async_trait>(&'life0 self, name: Option<String>) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IPlaybackService {
    fn get_playback_state<'life0, 'async_trait>(&'life0 self, session_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<PlaybackState>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_playback_state<'life0, 'async_trait>(&'life0 self, session_id: PlatformUUID, state: PlaybackState) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn observe_playback_state(&self, session_id: PlatformUUID) -> RpcStream<PlaybackState>;
}

pub trait ISessionService {
    fn deactivate_session<'life0, 'async_trait>(&'life0 self, session_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_sessions<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Session>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IArtistService {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_group<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, artist_ids: Option<Vec<PlatformUUID>>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_group<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, group_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn merge_artists<'life0, 'async_trait>(&'life0 self, merge_artists: MergeArtists) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn split_artist<'life0, 'async_trait>(&'life0 self, split_artist: SplitArtist) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_artists<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn create_artist<'life0, 'async_trait>(&'life0 self, name: String, is_group: bool, about: String, music_brainz_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Artist, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn search_artist_on_music_brainz<'life0, 'async_trait>(&'life0 self, query: String, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<MusicBrainzArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn fetch_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, music_brainz_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn artists_without_music_brainz_id_flow(&self, ) -> RpcStream<Artist>;
    fn artist_ids_without_music_brainz_id(&self, ) -> RpcStream<PlatformUUID>;
}

pub trait IAuthService {
    fn authenticate<'life0, 'async_trait>(&'life0 self, username: String, password: String) -> Pin<Box<dyn std::future::Future<Output = Result<AuthenticationResponse, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn refresh_token<'life0, 'async_trait>(&'life0 self, refresh_token: String) -> Pin<Box<dyn std::future::Future<Output = Result<AuthenticationResponse, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IAudioAnalysisService {
    fn get_audio_data<'life0, 'async_trait>(&'life0 self, song_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<SongAudioData>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn analyze_song<'life0, 'async_trait>(&'life0 self, song_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IReleaseService {
    fn follow_artist<'life0, 'async_trait>(&'life0 self, music_brainz_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn unfollow_artist<'life0, 'async_trait>(&'life0 self, artist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_followed_artists<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<FollowedArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_recent_releases<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<RecentRelease>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IAlbumService {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn versions<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_name<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, name: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_albums<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn update_album<'life0, 'async_trait>(&'life0 self, album: Album) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn delete_albums<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn fetch_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, music_brainz_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_artist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, artist_id: PlatformUUID, singles: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IDbManagementService {
    fn export_data<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<serde_bytes::ByteBuf, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn import_data<'life0, 'async_trait>(&'life0 self, data: serde_bytes::ByteBuf) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IImageService {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Image>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_hash<'life0, 'async_trait>(&'life0 self, hash: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Image>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_cover_hashes<'life0, 'async_trait>(&'life0 self, hashes: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<std::collections::HashMap<String, PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_image_data<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Option<serde_bytes::ByteBuf>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn create_image<'life0, 'async_trait>(&'life0 self, bytes: serde_bytes::ByteBuf, origin: String) -> Pin<Box<dyn std::future::Future<Output = Result<PlatformUUID, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn create_batch<'life0, 'async_trait>(&'life0 self, images: Vec<InsertableImage>) -> Pin<Box<dyn std::future::Future<Output = Result<std::collections::HashMap<String, PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn move_images<'life0, 'async_trait>(&'life0 self, old_path: String, new_path: String) -> Pin<Box<dyn std::future::Future<Output = Result<i32, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait ISongService {
    fn set_liked<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, liked: bool, added_at: Option<PlatformDateTime>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_lyrics<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, lyrics: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_artists<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, artist_ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, music_brainz_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn fetch_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_music_brainz_id<'life0, 'async_trait>(&'life0 self, music_brainz_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_title<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, title: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_artist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, artist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn liked_by_artist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, artist_id: PlatformUUID, explicit: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_album<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, album_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_playlist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, playlist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_user_playlist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, playlist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_original_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_original_tracks<'life0, 'async_trait>(&'life0 self, tracks: Vec<Track>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn liked_songs<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, explicit: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_songs<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, explicit: bool, tags: Vec<SongTag>, invert_tags: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn delete_songs<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String, explicit: bool, liked: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn stream_song(&self, id: PlatformUUID, offset: i64, chunk_size: i32) -> RpcStream<serde_bytes::ByteBuf>;
    fn download_song(&self, id: PlatformUUID, quality: i32, offset: i64, chunk_size: i32, force: bool) -> RpcStream<serde_bytes::ByteBuf>;
    fn get_stream_size<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<i64, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_download_size<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, quality: i32, force: bool) -> Pin<Box<dyn std::future::Future<Output = Result<i64, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_song_ids(&self, explicit: bool, tags: Vec<SongTag>, invert_tags: bool) -> RpcStream<PlatformUUID>;
    fn liked_song_ids(&self, explicit: bool) -> RpcStream<PlatformUUID>;
    fn song_ids_by_artist(&self, artist_id: PlatformUUID) -> RpcStream<PlatformUUID>;
    fn song_ids_by_album(&self, album_id: PlatformUUID) -> RpcStream<PlatformUUID>;
    fn song_ids_by_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<PlatformUUID>;
    fn song_ids_by_user_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<PlatformUUID>;
    fn move_songs<'life0, 'async_trait>(&'life0 self, old_path: String, new_path: String, original_id_prefix: Option<String>) -> Pin<Box<dyn std::future::Future<Output = Result<i32, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IServerStatsService {
    fn get_stats<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<ServerStats, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn health<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_proxy_info<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Option<ProxyInfo>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IBackupService {
    fn list_backups<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<BackupInfo>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn load_backup<'life0, 'async_trait>(&'life0 self, file_name: String) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn delete_backup<'life0, 'async_trait>(&'life0 self, file_name: String) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn create_backup<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<BackupResult, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IRemoteMirrorService {
    fn get_remote_stats<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<ServerStats, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn start_mirror<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn stop_mirror<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn reset_mirror<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_active_mirror_progress(&self, ) -> RpcStream<MirrorProgress>;
    fn get_remote_users<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_remote_playlists<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_remote_user_playlists<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_proxy_instances<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<ProxyInstanceInfo>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_remote_image_data<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig, image_id: PlatformUUID, size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Option<serde_bytes::ByteBuf>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IDownloadService {
    fn logs(&self, ) -> RpcStream<LogLine>;
    fn current_download<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Option<DownloadQueueEntry>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn download_queue<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<DownloadQueueEntry>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn finished_downloads<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<FinishedDownloadQueueEntry>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn sync_favourites_available<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn sync_favourites<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn download_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PrefixedId>, r#type: Type, downloader: Option<DownloadBackend>) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn download_urls<'life0, 'async_trait>(&'life0 self, urls: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_downloader_for_url<'life0, 'async_trait>(&'life0 self, url: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<DownloadBackend>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn exists_by_original_id<'life0, 'async_trait>(&'life0 self, id: PrefixedId, r#type: Type) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_download_service<'life0, 'async_trait>(&'life0 self, service: DownloadBackend) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_download_service<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<DownloadBackend, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_all_download_services<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<DownloadBackend>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn download_authorized<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn download_login(&self, ) -> RpcStream<String>;
    fn tidal_sync_authorized<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_auth_url<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<String, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn kill_all_child_processes<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn search<'life0, 'async_trait>(&'life0 self, query: Option<String>, title: Option<String>, artist: Option<String>, count: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<DownloadSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub struct RpcClient { pub manager: *mut NativeRpcManager }
unsafe impl Send for RpcClient {}
unsafe impl Sync for RpcClient {}
impl RpcClient {
    pub async fn call<T: Serialize, R: for<'de> Deserialize<'de>>(&self, service: &str, method: &str, args: &T) -> Result<R, String> {
        let s_c = CString::new(service).unwrap(); let m_c = CString::new(method).unwrap();
        let arg_bytes = serde_cbor::to_vec(args).map_err(|e| e.to_string())?;
        let mut out_len: c_int = 0;
        let res_ptr = unsafe { common_rpc_call(self.manager, s_c.as_ptr(), m_c.as_ptr(), arg_bytes.as_ptr(), arg_bytes.len() as c_int, &mut out_len) };
        if res_ptr.is_null() { return Err("RPC Error".into()); }
        let res = unsafe { std::slice::from_raw_parts(res_ptr, out_len as usize) };
        let envelope: RpcEnvelope = serde_cbor::from_slice(res).map_err(|e| e.to_string())?;
        unsafe { common_rpc_free_buffer(res_ptr) };
        if let Some(err) = envelope.error { return Err(err); }
        let data = envelope.data.ok_or("No data in envelope")?;
        let val = serde_cbor::from_slice(&data).map_err(|e| e.to_string())?;
        Ok(val)
    }

    pub fn subscribe<T: Serialize, R: for<'de> Deserialize<'de> + Send + 'static>(&self, service: &str, method: &str, args: &T) -> RpcStream<R> {
        let (tx, rx) = mpsc::unbounded_channel();
        let tx_box = Box::new(tx);
        let s_c = CString::new(service).unwrap(); let m_c = CString::new(method).unwrap();
        let arg_bytes = serde_cbor::to_vec(args).unwrap();
        let job = unsafe { common_rpc_subscribe(self.manager, s_c.as_ptr(), m_c.as_ptr(), arg_bytes.as_ptr(), arg_bytes.len() as c_int, Box::into_raw(tx_box) as *mut c_void, flow_callback_handler::<R>) };
        RpcStream::new(rx, job)
    }

    pub fn set_url(&self, url: &str) {
        let url_c = CString::new(url).unwrap();
        unsafe { common_rpc_set_url(self.manager, url_c.as_ptr()) };
    }

    pub fn get_url(&self) -> Option<String> {
        let ptr = unsafe { common_rpc_get_url(self.manager) };
        if ptr.is_null() { return None; }
        let s = unsafe { CStr::from_ptr(ptr).to_string_lossy().into_owned() };
        unsafe { common_rpc_free_buffer(ptr as *mut u8) };
        Some(s)
    }

    pub fn validate_server(&self, url: &str) -> bool {
        let url_c = CString::new(url).unwrap();
        unsafe { common_rpc_validate_server(self.manager, url_c.as_ptr()) }
    }

    pub fn update_auth(&self, response: &AuthenticationResponse) {
        let arg_bytes = serde_cbor::to_vec(response).unwrap();
        unsafe { common_rpc_update_auth(self.manager, arg_bytes.as_ptr(), arg_bytes.len() as c_int) };
    }

    pub fn is_authenticated(&self) -> bool {
        unsafe { common_rpc_is_authenticated(self.manager) }
    }

    pub fn get_auth_token(&self) -> Option<String> {
        let ptr = unsafe { common_rpc_get_auth_token(self.manager) };
        if ptr.is_null() { return None; }
        let s = unsafe { CStr::from_ptr(ptr).to_string_lossy().into_owned() };
        unsafe { common_rpc_free_buffer(ptr as *mut u8) };
        Some(s)
    }

    pub fn get_refresh_token(&self) -> Option<String> {
        let ptr = unsafe { common_rpc_get_refresh_token(self.manager) };
        if ptr.is_null() { return None; }
        let s = unsafe { CStr::from_ptr(ptr).to_string_lossy().into_owned() };
        unsafe { common_rpc_free_buffer(ptr as *mut u8) };
        Some(s)
    }

    pub fn is_token_expired(&self) -> bool {
        unsafe { common_rpc_is_token_expired(self.manager) }
    }
}
impl IIndexer for RpcClient {
    fn start(&self, ) -> RpcStream<String> {
        self.subscribe("IIndexer", "start", &())
    }
}
impl IMirrorService for RpcClient {
    fn get_server_paths<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<RemoteServerPaths, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IMirrorService", "getServerPaths", &()).await
        })
    }
    fn get_songs(&self, ) -> RpcStream<Song> {
        self.subscribe("IMirrorService", "getSongs", &())
    }
    fn get_artists(&self, ) -> RpcStream<Artist> {
        self.subscribe("IMirrorService", "getArtists", &())
    }
    fn get_artist_aliases(&self, ) -> RpcStream<ArtistAlias> {
        self.subscribe("IMirrorService", "getArtistAliases", &())
    }
    fn get_artist_split_aliases(&self, ) -> RpcStream<ArtistSplitAlias> {
        self.subscribe("IMirrorService", "getArtistSplitAliases", &())
    }
    fn get_albums(&self, ) -> RpcStream<Album> {
        self.subscribe("IMirrorService", "getAlbums", &())
    }
    fn get_playlists(&self, ) -> RpcStream<Playlist> {
        self.subscribe("IMirrorService", "getPlaylists", &())
    }
    fn get_user_playlists(&self, ) -> RpcStream<UserPlaylist> {
        self.subscribe("IMirrorService", "getUserPlaylists", &())
    }
    fn get_image_metadata(&self, ) -> RpcStream<Image> {
        self.subscribe("IMirrorService", "getImageMetadata", &())
    }
    fn get_song_data(&self, song_id: PlatformUUID, quality: i32, chunk_size: i32, force: bool) -> RpcStream<serde_bytes::ByteBuf> {
        let args = IMirrorServiceGetSongDataArgs { song_id, quality, chunk_size, force };
        self.subscribe("IMirrorService", "getSongData", &args)
    }
    fn get_users(&self, ) -> RpcStream<User> {
        self.subscribe("IMirrorService", "getUsers", &())
    }
    fn get_songs_by_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<Song> {
        self.subscribe("IMirrorService", "getSongsByPlaylist", &playlist_id)
    }
    fn get_songs_by_user_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<Song> {
        self.subscribe("IMirrorService", "getSongsByUserPlaylist", &playlist_id)
    }
    fn get_liked_songs(&self, user_id: PlatformUUID) -> RpcStream<Song> {
        self.subscribe("IMirrorService", "getLikedSongs", &user_id)
    }
}
impl IUserPlaylistBackupService for RpcClient {
    fn create_backup<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserPlaylistBackupService", "createBackup", &()).await
        })
    }
    fn list_backups<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<BackupInfo>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserPlaylistBackupService", "listBackups", &()).await
        })
    }
    fn restore_backup<'life0, 'async_trait>(&'life0 self, file_name: Option<String>) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserPlaylistBackupService", "restoreBackup", &file_name).await
        })
    }
    fn get_backup_content<'life0, 'async_trait>(&'life0 self, file_name: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserPlaylistBackup>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserPlaylistBackupService", "getBackupContent", &file_name).await
        })
    }
    fn delete_backup<'life0, 'async_trait>(&'life0 self, file_name: String) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserPlaylistBackupService", "deleteBackup", &file_name).await
        })
    }
}
impl IDiscoveryService for RpcClient {
    fn get_similar_songs<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDiscoveryServiceGetSimilarSongsArgs { seed_song_ids, limit };
            self.call("IDiscoveryService", "getSimilarSongs", &args).await
        })
    }
    fn get_similar_songs_by_playlist<'life0, 'async_trait>(&'life0 self, playlist_id: PlatformUUID, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDiscoveryServiceGetSimilarSongsByPlaylistArgs { playlist_id, limit };
            self.call("IDiscoveryService", "getSimilarSongsByPlaylist", &args).await
        })
    }
    fn get_similar_songs_by_bpm<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDiscoveryServiceGetSimilarSongsByBpmArgs { seed_song_ids, limit };
            self.call("IDiscoveryService", "getSimilarSongsByBpm", &args).await
        })
    }
    fn get_similar_songs_by_energy<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDiscoveryServiceGetSimilarSongsByEnergyArgs { seed_song_ids, limit };
            self.call("IDiscoveryService", "getSimilarSongsByEnergy", &args).await
        })
    }
    fn get_similar_songs_by_mood<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDiscoveryServiceGetSimilarSongsByMoodArgs { seed_song_ids, limit };
            self.call("IDiscoveryService", "getSimilarSongsByMood", &args).await
        })
    }
    fn get_songs_by_same_composers<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDiscoveryServiceGetSongsBySameComposersArgs { seed_song_ids, limit };
            self.call("IDiscoveryService", "getSongsBySameComposers", &args).await
        })
    }
    fn get_songs_by_same_lyricists<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDiscoveryServiceGetSongsBySameLyricistsArgs { seed_song_ids, limit };
            self.call("IDiscoveryService", "getSongsBySameLyricists", &args).await
        })
    }
    fn get_songs_by_same_producers<'life0, 'async_trait>(&'life0 self, seed_song_ids: Vec<PlatformUUID>, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDiscoveryServiceGetSongsBySameProducersArgs { seed_song_ids, limit };
            self.call("IDiscoveryService", "getSongsBySameProducers", &args).await
        })
    }
}
impl ILyricsSearch for RpcClient {
    fn search_lyrics<'life0, 'async_trait>(&'life0 self, artist: String, title: String, synced_only: bool) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<String>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ILyricsSearchSearchLyricsArgs { artist, title, synced_only };
            self.call("ILyricsSearch", "searchLyrics", &args).await
        })
    }
}
impl ISyncService for RpcClient {
}
impl IPlaylistService for RpcClient {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IPlaylistService", "byId", &id).await
        })
    }
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IPlaylistService", "byIds", &ids).await
        })
    }
    fn by_id_full<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<(String, Vec<PlaylistEntry>)>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IPlaylistService", "byIdFull", &id).await
        })
    }
    fn by_name<'life0, 'async_trait>(&'life0 self, name: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IPlaylistService", "byName", &name).await
        })
    }
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IPlaylistServiceRankedSearchArgs { page, page_size, query };
            self.call("IPlaylistService", "rankedSearch", &args).await
        })
    }
    fn all_playlists<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IPlaylistServiceAllPlaylistsArgs { page, page_size };
            self.call("IPlaylistService", "allPlaylists", &args).await
        })
    }
    fn delete<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IPlaylistService", "delete", &id).await
        })
    }
}
impl IFavSyncService for RpcClient {
    fn get_latest_fav_sync<'life0, 'async_trait>(&'life0 self, service: SyncServiceType) -> Pin<Box<dyn std::future::Future<Output = Result<Option<FavSync>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IFavSyncService", "getLatestFavSync", &service).await
        })
    }
    fn insert_fav_sync<'life0, 'async_trait>(&'life0 self, service: SyncServiceType, synced_at: PlatformDate) -> Pin<Box<dyn std::future::Future<Output = Result<i32, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IFavSyncServiceInsertFavSyncArgs { service, synced_at };
            self.call("IFavSyncService", "insertFavSync", &args).await
        })
    }
}
impl IUserPlaylistService for RpcClient {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserPlaylistService", "byId", &id).await
        })
    }
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserPlaylistService", "byIds", &ids).await
        })
    }
    fn ranked_search<'life0, 'async_trait>(&'life0 self, creator: Option<PlatformUUID>, page: i32, page_size: i32, query: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IUserPlaylistServiceRankedSearchArgs { creator, page, page_size, query };
            self.call("IUserPlaylistService", "rankedSearch", &args).await
        })
    }
    fn all_playlists<'life0, 'async_trait>(&'life0 self, creator: Option<PlatformUUID>, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IUserPlaylistServiceAllPlaylistsArgs { creator, page, page_size };
            self.call("IUserPlaylistService", "allPlaylists", &args).await
        })
    }
    fn delete<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserPlaylistService", "delete", &id).await
        })
    }
    fn get_or_add_playlist<'life0, 'async_trait>(&'life0 self, user: User, custom_identifier: Option<String>, playlist: InsertablePlaylist) -> Pin<Box<dyn std::future::Future<Output = Result<PlatformUUID, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IUserPlaylistServiceGetOrAddPlaylistArgs { user, custom_identifier, playlist };
            self.call("IUserPlaylistService", "getOrAddPlaylist", &args).await
        })
    }
    fn add_to_playlist<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, song_ids: Vec<(i64, PlatformUUID)>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IUserPlaylistServiceAddToPlaylistArgs { id, song_ids };
            self.call("IUserPlaylistService", "addToPlaylist", &args).await
        })
    }
    fn remove_from_playlist<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, song_ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<i32, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IUserPlaylistServiceRemoveFromPlaylistArgs { id, song_ids };
            self.call("IUserPlaylistService", "removeFromPlaylist", &args).await
        })
    }
    fn set_playlist_image<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, image_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IUserPlaylistServiceSetPlaylistImageArgs { id, image_id };
            self.call("IUserPlaylistService", "setPlaylistImage", &args).await
        })
    }
}
impl IMetadataService for RpcClient {
    fn search_artists<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, query: String, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceSearchArtistsArgs { r#type, query, limit };
            self.call("IMetadataService", "searchArtists", &args).await
        })
    }
    fn search<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, query: String, limit: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Track>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceSearchArgs { r#type, query, limit };
            self.call("IMetadataService", "search", &args).await
        })
    }
    fn search_albums<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, query: String, limit: i32, include_tracks: bool) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceAlbum>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceSearchAlbumsArgs { r#type, query, limit, include_tracks };
            self.call("IMetadataService", "searchAlbums", &args).await
        })
    }
    fn get_album_id_by_track_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, track_id: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<String>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetAlbumIdByTrackIdArgs { r#type, track_id };
            self.call("IMetadataService", "getAlbumIdByTrackId", &args).await
        })
    }
    fn get_image_url_by_album_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, album_id: String) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceImage>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetImageUrlByAlbumIdArgs { r#type, album_id };
            self.call("IMetadataService", "getImageUrlByAlbumId", &args).await
        })
    }
    fn get_artist_by_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<IMetadataServiceArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetArtistByMbIdArgs { r#type, mb_id };
            self.call("IMetadataService", "getArtistByMbId", &args).await
        })
    }
    fn get_album_by_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<IMetadataServiceAlbum>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetAlbumByMbIdArgs { r#type, mb_id };
            self.call("IMetadataService", "getAlbumByMbId", &args).await
        })
    }
    fn get_track_by_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Track>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetTrackByMbIdArgs { r#type, mb_id };
            self.call("IMetadataService", "getTrackByMbId", &args).await
        })
    }
    fn get_image_url_by_artist_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceImage>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetImageUrlByArtistMbIdArgs { r#type, mb_id };
            self.call("IMetadataService", "getImageUrlByArtistMbId", &args).await
        })
    }
    fn get_image_url_by_album_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceImage>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetImageUrlByAlbumMbIdArgs { r#type, mb_id };
            self.call("IMetadataService", "getImageUrlByAlbumMbId", &args).await
        })
    }
    fn get_image_url_by_track_mb_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, mb_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceImage>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetImageUrlByTrackMbIdArgs { r#type, mb_id };
            self.call("IMetadataService", "getImageUrlByTrackMbId", &args).await
        })
    }
    fn get_image_urls_by_album_ids<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, album_ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<std::collections::HashMap<String, Vec<IMetadataServiceImage>>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetImageUrlsByAlbumIdsArgs { r#type, album_ids };
            self.call("IMetadataService", "getImageUrlsByAlbumIds", &args).await
        })
    }
    fn get_image_url_by_image_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, image_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<String>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetImageUrlByImageIdArgs { r#type, image_id };
            self.call("IMetadataService", "getImageUrlByImageId", &args).await
        })
    }
    fn get_track_by_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, track_id: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Track>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetTrackByIdArgs { r#type, track_id };
            self.call("IMetadataService", "getTrackById", &args).await
        })
    }
    fn get_tracks_by_ids<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, track_ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Track>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetTracksByIdsArgs { r#type, track_ids };
            self.call("IMetadataService", "getTracksByIds", &args).await
        })
    }
    fn get_albums_by_ids<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, album_ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceAlbum>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetAlbumsByIdsArgs { r#type, album_ids };
            self.call("IMetadataService", "getAlbumsByIds", &args).await
        })
    }
    fn album_exists_by_id<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, album_id: String) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceAlbumExistsByIdArgs { r#type, album_id };
            self.call("IMetadataService", "albumExistsById", &args).await
        })
    }
    fn get_artists_by_ids<'life0, 'async_trait>(&'life0 self, r#type: MetadataType, artist_ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<IMetadataServiceArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMetadataServiceGetArtistsByIdsArgs { r#type, artist_ids };
            self.call("IMetadataService", "getArtistsByIds", &args).await
        })
    }
}
impl IMusicBrainzService for RpcClient {
    fn get_artist<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IMusicBrainzService", "getArtist", &id).await
        })
    }
    fn get_recording<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzRecording>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IMusicBrainzService", "getRecording", &id).await
        })
    }
    fn get_release<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzRelease>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IMusicBrainzService", "getRelease", &id).await
        })
    }
    fn get_release_group<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzReleaseGroup>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IMusicBrainzService", "getReleaseGroup", &id).await
        })
    }
    fn search_recording<'life0, 'async_trait>(&'life0 self, title: String, artists: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzRecording>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMusicBrainzServiceSearchRecordingArgs { title, artists };
            self.call("IMusicBrainzService", "searchRecording", &args).await
        })
    }
    fn search_release<'life0, 'async_trait>(&'life0 self, title: String, artists: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<MusicBrainzRelease>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IMusicBrainzServiceSearchReleaseArgs { title, artists };
            self.call("IMusicBrainzService", "searchRelease", &args).await
        })
    }
}
impl ILyricsService for RpcClient {
    fn get_synced_lyrics<'life0, 'async_trait>(&'life0 self, song_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<SyncedLyrics>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ILyricsService", "getSyncedLyrics", &song_id).await
        })
    }
    fn transcribe_lyrics<'life0, 'async_trait>(&'life0 self, song_id: PlatformUUID, lyrics: Option<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<SyncedLyrics>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ILyricsServiceTranscribeLyricsArgs { song_id, lyrics };
            self.call("ILyricsService", "transcribeLyrics", &args).await
        })
    }
    fn start_sync_worker<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ILyricsService", "startSyncWorker", &()).await
        })
    }
}
impl IScheduledTaskLogService for RpcClient {
    fn get_grouped_logs<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<std::collections::HashMap<String, Vec<ScheduledTaskLog>>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IScheduledTaskLogService", "getGroupedLogs", &()).await
        })
    }
    fn get_grouped_logs_flow(&self, ) -> RpcStream<std::collections::HashMap<String, Vec<ScheduledTaskLog>>> {
        self.subscribe("IScheduledTaskLogService", "getGroupedLogsFlow", &())
    }
}
impl ICustomAudioService for RpcClient {
    fn upload_custom_audio<'life0, 'async_trait>(&'life0 self, file_data: serde_bytes::ByteBuf, file_name: String, metadata: Option<CustomMetadata>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ICustomAudioServiceUploadCustomAudioArgs { file_data, file_name, metadata };
            self.call("ICustomAudioService", "uploadCustomAudio", &args).await
        })
    }
}
impl IStorageService for RpcClient {
    fn get_total_storage<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<i64, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IStorageService", "getTotalStorage", &()).await
        })
    }
}
impl IUserService for RpcClient {
    fn find_user_by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserService", "findUserById", &id).await
        })
    }
    fn find_user_by_username<'life0, 'async_trait>(&'life0 self, username: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserService", "findUserByUsername", &username).await
        })
    }
    fn me<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<User, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserService", "me", &()).await
        })
    }
    fn get_all_users<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserService", "getAllUsers", &()).await
        })
    }
    fn set_profile_image<'life0, 'async_trait>(&'life0 self, bytes: serde_bytes::ByteBuf) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserService", "setProfileImage", &serde_bytes::Bytes::new(&bytes)).await
        })
    }
    fn set_display_name<'life0, 'async_trait>(&'life0 self, name: Option<String>) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IUserService", "setDisplayName", &name).await
        })
    }
}
impl IPlaybackService for RpcClient {
    fn get_playback_state<'life0, 'async_trait>(&'life0 self, session_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<PlaybackState>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IPlaybackService", "getPlaybackState", &session_id).await
        })
    }
    fn set_playback_state<'life0, 'async_trait>(&'life0 self, session_id: PlatformUUID, state: PlaybackState) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IPlaybackServiceSetPlaybackStateArgs { session_id, state };
            self.call("IPlaybackService", "setPlaybackState", &args).await
        })
    }
    fn observe_playback_state(&self, session_id: PlatformUUID) -> RpcStream<PlaybackState> {
        self.subscribe("IPlaybackService", "observePlaybackState", &session_id)
    }
}
impl ISessionService for RpcClient {
    fn deactivate_session<'life0, 'async_trait>(&'life0 self, session_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISessionService", "deactivateSession", &session_id).await
        })
    }
    fn get_sessions<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Session>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISessionService", "getSessions", &()).await
        })
    }
}
impl IArtistService for RpcClient {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IArtistService", "byId", &id).await
        })
    }
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IArtistService", "byIds", &ids).await
        })
    }
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IArtistServiceRankedSearchArgs { page, page_size, query };
            self.call("IArtistService", "rankedSearch", &args).await
        })
    }
    fn set_group<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, artist_ids: Option<Vec<PlatformUUID>>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IArtistServiceSetGroupArgs { id, artist_ids };
            self.call("IArtistService", "setGroup", &args).await
        })
    }
    fn by_group<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, group_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IArtistServiceByGroupArgs { page, page_size, group_id };
            self.call("IArtistService", "byGroup", &args).await
        })
    }
    fn merge_artists<'life0, 'async_trait>(&'life0 self, merge_artists: MergeArtists) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IArtistService", "mergeArtists", &merge_artists).await
        })
    }
    fn split_artist<'life0, 'async_trait>(&'life0 self, split_artist: SplitArtist) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IArtistService", "splitArtist", &split_artist).await
        })
    }
    fn all_artists<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IArtistServiceAllArtistsArgs { page, page_size };
            self.call("IArtistService", "allArtists", &args).await
        })
    }
    fn create_artist<'life0, 'async_trait>(&'life0 self, name: String, is_group: bool, about: String, music_brainz_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Artist, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IArtistServiceCreateArtistArgs { name, is_group, about, music_brainz_id };
            self.call("IArtistService", "createArtist", &args).await
        })
    }
    fn search_artist_on_music_brainz<'life0, 'async_trait>(&'life0 self, query: String, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<MusicBrainzArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IArtistServiceSearchArtistOnMusicBrainzArgs { query, page, page_size };
            self.call("IArtistService", "searchArtistOnMusicBrainz", &args).await
        })
    }
    fn fetch_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IArtistService", "fetchMusicBrainzId", &id).await
        })
    }
    fn set_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, music_brainz_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IArtistServiceSetMusicBrainzIdArgs { id, music_brainz_id };
            self.call("IArtistService", "setMusicBrainzId", &args).await
        })
    }
    fn artists_without_music_brainz_id_flow(&self, ) -> RpcStream<Artist> {
        self.subscribe("IArtistService", "artistsWithoutMusicBrainzIdFlow", &())
    }
    fn artist_ids_without_music_brainz_id(&self, ) -> RpcStream<PlatformUUID> {
        self.subscribe("IArtistService", "artistIdsWithoutMusicBrainzId", &())
    }
}
impl IAuthService for RpcClient {
    fn authenticate<'life0, 'async_trait>(&'life0 self, username: String, password: String) -> Pin<Box<dyn std::future::Future<Output = Result<AuthenticationResponse, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IAuthServiceAuthenticateArgs { username, password };
            self.call("IAuthService", "authenticate", &args).await
        })
    }
    fn refresh_token<'life0, 'async_trait>(&'life0 self, refresh_token: String) -> Pin<Box<dyn std::future::Future<Output = Result<AuthenticationResponse, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAuthService", "refreshToken", &refresh_token).await
        })
    }
}
impl IAudioAnalysisService for RpcClient {
    fn get_audio_data<'life0, 'async_trait>(&'life0 self, song_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<SongAudioData>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAudioAnalysisService", "getAudioData", &song_id).await
        })
    }
    fn analyze_song<'life0, 'async_trait>(&'life0 self, song_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAudioAnalysisService", "analyzeSong", &song_id).await
        })
    }
}
impl IReleaseService for RpcClient {
    fn follow_artist<'life0, 'async_trait>(&'life0 self, music_brainz_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IReleaseService", "followArtist", &music_brainz_id).await
        })
    }
    fn unfollow_artist<'life0, 'async_trait>(&'life0 self, artist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IReleaseService", "unfollowArtist", &artist_id).await
        })
    }
    fn get_followed_artists<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<FollowedArtist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IReleaseService", "getFollowedArtists", &()).await
        })
    }
    fn get_recent_releases<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<RecentRelease>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IReleaseServiceGetRecentReleasesArgs { page, page_size };
            self.call("IReleaseService", "getRecentReleases", &args).await
        })
    }
}
impl IAlbumService for RpcClient {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAlbumService", "byId", &id).await
        })
    }
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAlbumService", "byIds", &ids).await
        })
    }
    fn versions<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAlbumService", "versions", &id).await
        })
    }
    fn by_name<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, name: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IAlbumServiceByNameArgs { page, page_size, name };
            self.call("IAlbumService", "byName", &args).await
        })
    }
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IAlbumServiceRankedSearchArgs { page, page_size, query };
            self.call("IAlbumService", "rankedSearch", &args).await
        })
    }
    fn all_albums<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IAlbumServiceAllAlbumsArgs { page, page_size };
            self.call("IAlbumService", "allAlbums", &args).await
        })
    }
    fn update_album<'life0, 'async_trait>(&'life0 self, album: Album) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAlbumService", "updateAlbum", &album).await
        })
    }
    fn delete_albums<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAlbumService", "deleteAlbums", &ids).await
        })
    }
    fn fetch_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAlbumService", "fetchMusicBrainzId", &id).await
        })
    }
    fn set_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, music_brainz_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IAlbumServiceSetMusicBrainzIdArgs { id, music_brainz_id };
            self.call("IAlbumService", "setMusicBrainzId", &args).await
        })
    }
    fn by_artist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, artist_id: PlatformUUID, singles: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IAlbumServiceByArtistArgs { page, page_size, artist_id, singles };
            self.call("IAlbumService", "byArtist", &args).await
        })
    }
}
impl IDbManagementService for RpcClient {
    fn export_data<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<serde_bytes::ByteBuf, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDbManagementService", "exportData", &()).await
        })
    }
    fn import_data<'life0, 'async_trait>(&'life0 self, data: serde_bytes::ByteBuf) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDbManagementService", "importData", &serde_bytes::Bytes::new(&data)).await
        })
    }
}
impl IImageService for RpcClient {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Image>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IImageService", "byId", &id).await
        })
    }
    fn by_hash<'life0, 'async_trait>(&'life0 self, hash: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Image>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IImageService", "byHash", &hash).await
        })
    }
    fn get_cover_hashes<'life0, 'async_trait>(&'life0 self, hashes: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<std::collections::HashMap<String, PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IImageService", "getCoverHashes", &hashes).await
        })
    }
    fn get_image_data<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Option<serde_bytes::ByteBuf>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IImageServiceGetImageDataArgs { id, size };
            self.call("IImageService", "getImageData", &args).await
        })
    }
    fn create_image<'life0, 'async_trait>(&'life0 self, bytes: serde_bytes::ByteBuf, origin: String) -> Pin<Box<dyn std::future::Future<Output = Result<PlatformUUID, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IImageServiceCreateImageArgs { bytes, origin };
            self.call("IImageService", "createImage", &args).await
        })
    }
    fn create_batch<'life0, 'async_trait>(&'life0 self, images: Vec<InsertableImage>) -> Pin<Box<dyn std::future::Future<Output = Result<std::collections::HashMap<String, PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IImageService", "createBatch", &images).await
        })
    }
    fn move_images<'life0, 'async_trait>(&'life0 self, old_path: String, new_path: String) -> Pin<Box<dyn std::future::Future<Output = Result<i32, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IImageServiceMoveImagesArgs { old_path, new_path };
            self.call("IImageService", "moveImages", &args).await
        })
    }
}
impl ISongService for RpcClient {
    fn set_liked<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, liked: bool, added_at: Option<PlatformDateTime>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceSetLikedArgs { id, liked, added_at };
            self.call("ISongService", "setLiked", &args).await
        })
    }
    fn set_lyrics<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, lyrics: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceSetLyricsArgs { id, lyrics };
            self.call("ISongService", "setLyrics", &args).await
        })
    }
    fn set_artists<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, artist_ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceSetArtistsArgs { id, artist_ids };
            self.call("ISongService", "setArtists", &args).await
        })
    }
    fn set_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, music_brainz_id: Option<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceSetMusicBrainzIdArgs { id, music_brainz_id };
            self.call("ISongService", "setMusicBrainzId", &args).await
        })
    }
    fn fetch_music_brainz_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "fetchMusicBrainzId", &id).await
        })
    }
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "byId", &id).await
        })
    }
    fn by_music_brainz_id<'life0, 'async_trait>(&'life0 self, music_brainz_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "byMusicBrainzId", &music_brainz_id).await
        })
    }
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "byIds", &ids).await
        })
    }
    fn by_title<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, title: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceByTitleArgs { page, page_size, title };
            self.call("ISongService", "byTitle", &args).await
        })
    }
    fn by_artist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, artist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceByArtistArgs { page, page_size, artist_id };
            self.call("ISongService", "byArtist", &args).await
        })
    }
    fn liked_by_artist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, artist_id: PlatformUUID, explicit: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceLikedByArtistArgs { page, page_size, artist_id, explicit };
            self.call("ISongService", "likedByArtist", &args).await
        })
    }
    fn by_album<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, album_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceByAlbumArgs { page, page_size, album_id };
            self.call("ISongService", "byAlbum", &args).await
        })
    }
    fn by_playlist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, playlist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceByPlaylistArgs { page, page_size, playlist_id };
            self.call("ISongService", "byPlaylist", &args).await
        })
    }
    fn by_user_playlist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, playlist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceByUserPlaylistArgs { page, page_size, playlist_id };
            self.call("ISongService", "byUserPlaylist", &args).await
        })
    }
    fn by_original_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "byOriginalIds", &ids).await
        })
    }
    fn by_original_tracks<'life0, 'async_trait>(&'life0 self, tracks: Vec<Track>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "byOriginalTracks", &tracks).await
        })
    }
    fn liked_songs<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, explicit: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceLikedSongsArgs { page, page_size, explicit };
            self.call("ISongService", "likedSongs", &args).await
        })
    }
    fn all_songs<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, explicit: bool, tags: Vec<SongTag>, invert_tags: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceAllSongsArgs { page, page_size, explicit, tags, invert_tags };
            self.call("ISongService", "allSongs", &args).await
        })
    }
    fn delete_songs<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "deleteSongs", &ids).await
        })
    }
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String, explicit: bool, liked: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceRankedSearchArgs { page, page_size, query, explicit, liked };
            self.call("ISongService", "rankedSearch", &args).await
        })
    }
    fn stream_song(&self, id: PlatformUUID, offset: i64, chunk_size: i32) -> RpcStream<serde_bytes::ByteBuf> {
        let args = ISongServiceStreamSongArgs { id, offset, chunk_size };
        self.subscribe("ISongService", "streamSong", &args)
    }
    fn download_song(&self, id: PlatformUUID, quality: i32, offset: i64, chunk_size: i32, force: bool) -> RpcStream<serde_bytes::ByteBuf> {
        let args = ISongServiceDownloadSongArgs { id, quality, offset, chunk_size, force };
        self.subscribe("ISongService", "downloadSong", &args)
    }
    fn get_stream_size<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<i64, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "getStreamSize", &id).await
        })
    }
    fn get_download_size<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, quality: i32, force: bool) -> Pin<Box<dyn std::future::Future<Output = Result<i64, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceGetDownloadSizeArgs { id, quality, force };
            self.call("ISongService", "getDownloadSize", &args).await
        })
    }
    fn all_song_ids(&self, explicit: bool, tags: Vec<SongTag>, invert_tags: bool) -> RpcStream<PlatformUUID> {
        let args = ISongServiceAllSongIdsArgs { explicit, tags, invert_tags };
        self.subscribe("ISongService", "allSongIds", &args)
    }
    fn liked_song_ids(&self, explicit: bool) -> RpcStream<PlatformUUID> {
        self.subscribe("ISongService", "likedSongIds", &explicit)
    }
    fn song_ids_by_artist(&self, artist_id: PlatformUUID) -> RpcStream<PlatformUUID> {
        self.subscribe("ISongService", "songIdsByArtist", &artist_id)
    }
    fn song_ids_by_album(&self, album_id: PlatformUUID) -> RpcStream<PlatformUUID> {
        self.subscribe("ISongService", "songIdsByAlbum", &album_id)
    }
    fn song_ids_by_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<PlatformUUID> {
        self.subscribe("ISongService", "songIdsByPlaylist", &playlist_id)
    }
    fn song_ids_by_user_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<PlatformUUID> {
        self.subscribe("ISongService", "songIdsByUserPlaylist", &playlist_id)
    }
    fn move_songs<'life0, 'async_trait>(&'life0 self, old_path: String, new_path: String, original_id_prefix: Option<String>) -> Pin<Box<dyn std::future::Future<Output = Result<i32, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceMoveSongsArgs { old_path, new_path, original_id_prefix };
            self.call("ISongService", "moveSongs", &args).await
        })
    }
}
impl IServerStatsService for RpcClient {
    fn get_stats<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<ServerStats, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IServerStatsService", "getStats", &()).await
        })
    }
    fn health<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IServerStatsService", "health", &()).await
        })
    }
    fn get_proxy_info<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Option<ProxyInfo>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IServerStatsService", "getProxyInfo", &()).await
        })
    }
}
impl IBackupService for RpcClient {
    fn list_backups<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<BackupInfo>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IBackupService", "listBackups", &()).await
        })
    }
    fn load_backup<'life0, 'async_trait>(&'life0 self, file_name: String) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IBackupService", "loadBackup", &file_name).await
        })
    }
    fn delete_backup<'life0, 'async_trait>(&'life0 self, file_name: String) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IBackupService", "deleteBackup", &file_name).await
        })
    }
    fn create_backup<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<BackupResult, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IBackupService", "createBackup", &()).await
        })
    }
}
impl IRemoteMirrorService for RpcClient {
    fn get_remote_stats<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<ServerStats, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IRemoteMirrorService", "getRemoteStats", &config).await
        })
    }
    fn start_mirror<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IRemoteMirrorService", "startMirror", &config).await
        })
    }
    fn stop_mirror<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IRemoteMirrorService", "stopMirror", &()).await
        })
    }
    fn reset_mirror<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IRemoteMirrorService", "resetMirror", &()).await
        })
    }
    fn get_active_mirror_progress(&self, ) -> RpcStream<MirrorProgress> {
        self.subscribe("IRemoteMirrorService", "getActiveMirrorProgress", &())
    }
    fn get_remote_users<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IRemoteMirrorService", "getRemoteUsers", &config).await
        })
    }
    fn get_remote_playlists<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Playlist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IRemoteMirrorService", "getRemotePlaylists", &config).await
        })
    }
    fn get_remote_user_playlists<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserPlaylist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IRemoteMirrorService", "getRemoteUserPlaylists", &config).await
        })
    }
    fn get_proxy_instances<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<ProxyInstanceInfo>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IRemoteMirrorService", "getProxyInstances", &config).await
        })
    }
    fn get_remote_image_data<'life0, 'async_trait>(&'life0 self, config: RemoteServerConfig, image_id: PlatformUUID, size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Option<serde_bytes::ByteBuf>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IRemoteMirrorServiceGetRemoteImageDataArgs { config, image_id, size };
            self.call("IRemoteMirrorService", "getRemoteImageData", &args).await
        })
    }
}
impl IDownloadService for RpcClient {
    fn logs(&self, ) -> RpcStream<LogLine> {
        self.subscribe("IDownloadService", "logs", &())
    }
    fn current_download<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Option<DownloadQueueEntry>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "currentDownload", &()).await
        })
    }
    fn download_queue<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<DownloadQueueEntry>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "downloadQueue", &()).await
        })
    }
    fn finished_downloads<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<FinishedDownloadQueueEntry>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "finishedDownloads", &()).await
        })
    }
    fn sync_favourites_available<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "syncFavouritesAvailable", &()).await
        })
    }
    fn sync_favourites<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "syncFavourites", &()).await
        })
    }
    fn download_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PrefixedId>, r#type: Type, downloader: Option<DownloadBackend>) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDownloadServiceDownloadIdsArgs { ids, r#type, downloader };
            self.call("IDownloadService", "downloadIds", &args).await
        })
    }
    fn download_urls<'life0, 'async_trait>(&'life0 self, urls: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "downloadUrls", &urls).await
        })
    }
    fn get_downloader_for_url<'life0, 'async_trait>(&'life0 self, url: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<DownloadBackend>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "getDownloaderForUrl", &url).await
        })
    }
    fn exists_by_original_id<'life0, 'async_trait>(&'life0 self, id: PrefixedId, r#type: Type) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDownloadServiceExistsByOriginalIdArgs { id, r#type };
            self.call("IDownloadService", "existsByOriginalId", &args).await
        })
    }
    fn set_download_service<'life0, 'async_trait>(&'life0 self, service: DownloadBackend) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "setDownloadService", &service).await
        })
    }
    fn get_download_service<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<DownloadBackend, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "getDownloadService", &()).await
        })
    }
    fn get_all_download_services<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<DownloadBackend>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "getAllDownloadServices", &()).await
        })
    }
    fn download_authorized<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "downloadAuthorized", &()).await
        })
    }
    fn download_login(&self, ) -> RpcStream<String> {
        self.subscribe("IDownloadService", "downloadLogin", &())
    }
    fn tidal_sync_authorized<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "tidalSyncAuthorized", &()).await
        })
    }
    fn get_auth_url<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<String, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "getAuthUrl", &()).await
        })
    }
    fn kill_all_child_processes<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "killAllChildProcesses", &()).await
        })
    }
    fn search<'life0, 'async_trait>(&'life0 self, query: Option<String>, title: Option<String>, artist: Option<String>, count: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<DownloadSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDownloadServiceSearchArgs { query, title, artist, count };
            self.call("IDownloadService", "search", &args).await
        })
    }
}
