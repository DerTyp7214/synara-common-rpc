use serde::{Deserialize, Serialize};
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_int, c_void};
use futures_util::Stream;
use std::pin::Pin;
use std::task::{Context, Poll};
use std::sync::Arc;
use tokio::sync::mpsc;

pub type FlowCallback = extern "C" fn(*mut c_void, *const u8, c_int);

#[repr(C)]
pub struct NativeRpcManager { _unused: [u8; 0] }

extern "C" {
    pub fn common_rpc_manager_create() -> *mut NativeRpcManager;
    pub fn common_rpc_manager_release(ptr: *mut NativeRpcManager);
    pub fn common_rpc_free_buffer(ptr: *mut u8);
    pub fn common_rpc_call(ptr: *mut NativeRpcManager, service: *const c_char, method: *const c_char, args: *const u8, len: c_int, out_len: *mut c_int) -> *mut u8;
    pub fn common_rpc_subscribe(ptr: *mut NativeRpcManager, service: *const c_char, method: *const c_char, args: *const u8, len: c_int, ctx: *mut c_void, cb: FlowCallback) -> *mut c_void;
    pub fn common_rpc_unsubscribe(job: *mut c_void);
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

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)] #[serde(transparent)] pub struct PlatformUUID(pub Vec<u8>);
#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)] #[serde(transparent)] pub struct PlatformDate(pub i64);
#[derive(Serialize, Deserialize, Debug, Clone, PartialEq, Default)] #[serde(transparent)] pub struct SuspendFunction0(pub serde_json::Value);

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
pub struct ICustomAudioServiceUploadCustomAudioArgs {
    #[serde(rename = "fileData")]
    pub file_data: Vec<u8>,
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
pub struct IAuthServiceAuthenticateArgs {
    pub username: String,
    pub password: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDownloadServiceDownloadTidalIdsArgs {
    pub ids: Vec<String>,
    #[serde(rename = "type")]
    pub r#type: Type,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDownloadServiceExistsByTidalIdArgs {
    pub id: String,
    #[serde(rename = "type")]
    pub r#type: Type,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IDownloadServiceSearchTidalArgs {
    pub query: Option<String>,
    pub title: Option<String>,
    pub artist: Option<String>,
    pub count: i32,
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
    pub bytes: Vec<u8>,
    pub origin: String,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceSetLikedArgs {
    pub id: PlatformUUID,
    pub liked: bool,
    #[serde(rename = "addedAt")]
    pub added_at: Option<PlatformDate>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct ISongServiceSetLyricsArgs {
    pub id: PlatformUUID,
    pub lyrics: Vec<String>,
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
pub struct UserPlaylist {
    pub id: PlatformUUID,
    pub name: String,
    pub songs: Vec<PlatformUUID>,
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
pub struct User {
    pub id: PlatformUUID,
    pub username: String,
    #[serde(rename = "passwordHash")]
    pub password_hash: String,
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
pub struct CustomMetadata {
    pub title: Option<String>,
    pub artists: Option<Vec<String>>,
    pub album: Option<String>,
    pub year: Option<String>,
    pub genre: Option<String>,
    #[serde(rename = "coverData")]
    pub cover_data: Option<Vec<u8>>,
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
    pub repeat_mode: String,
    #[serde(rename = "sourceId")]
    pub source_id: Option<String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct QueueEntry {
    #[serde(rename = "queueId")]
    pub queue_id: i64,
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
pub struct Artist {
    pub id: PlatformUUID,
    pub name: String,
    #[serde(rename = "isGroup")]
    pub is_group: bool,
    pub artists: Vec<Artist>,
    pub about: String,
    #[serde(rename = "imageId")]
    pub image_id: Option<PlatformUUID>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct MergeArtists {
    pub name: String,
    pub image: Option<String>,
    #[serde(rename = "artistIds")]
    pub artist_ids: Vec<PlatformUUID>,
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

#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]
pub enum TidalDownloadService {
    Tdn,
    Tiddl,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct TidalSong {
    pub id: String,
    pub title: String,
    pub artists: Vec<String>,
    pub cover: std::collections::HashMap<i32, String>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Album {
    pub id: PlatformUUID,
    pub name: String,
    pub artists: Vec<Artist>,
    #[serde(rename = "songCount")]
    pub song_count: i32,
    #[serde(rename = "releaseDate")]
    pub release_date: Option<PlatformDate>,
    #[serde(rename = "totalDuration")]
    pub total_duration: i64,
    #[serde(rename = "totalSize")]
    pub total_size: i64,
    #[serde(rename = "coverId")]
    pub cover_id: Option<PlatformUUID>,
    #[serde(rename = "originalId")]
    pub original_id: Option<String>,
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
pub struct UserSong {
    pub id: PlatformUUID,
    pub title: String,
    pub artists: Vec<Artist>,
    pub album: Option<Album>,
    pub duration: i64,
    pub explicit: bool,
    #[serde(rename = "releaseDate")]
    pub release_date: Option<PlatformDate>,
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
    #[serde(rename = "isFavourite")]
    pub is_favourite: Option<bool>,
    #[serde(rename = "userSongCreatedAt")]
    pub user_song_created_at: Option<PlatformDate>,
    #[serde(rename = "userSongUpdatedAt")]
    pub user_song_updated_at: Option<PlatformDate>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct Track {
    pub id: String,
    pub title: String,
    pub artists: Vec<String>,
    pub duration: String,
    #[serde(rename = "createdAt")]
    pub created_at: Option<PlatformDate>,
    #[serde(rename = "addedAt")]
    pub added_at: Option<PlatformDate>,
    #[serde(rename = "trackNumber")]
    pub track_number: Option<i32>,
    #[serde(rename = "discNumber")]
    pub disc_number: Option<i32>,
    pub images: Vec<IMetadataServiceImage>,
}

#[derive(Serialize, Deserialize, Debug, Clone)]
pub struct IMetadataServiceImage {
    pub url: String,
    pub width: i32,
    pub height: i32,
    pub animated: bool,
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

pub trait IIndexer {
    fn start(&self, ) -> RpcStream<String>;
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

pub trait ICustomAudioService {
    fn upload_custom_audio<'life0, 'async_trait>(&'life0 self, file_data: Vec<u8>, file_name: String, metadata: Option<CustomMetadata>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IStorageService {
    fn get_total_storage<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<i64, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IUserService {
    fn find_user_by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn find_user_by_username<'life0, 'async_trait>(&'life0 self, username: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<User>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn me<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<User, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
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
    fn by_group<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, group_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn merge_artists<'life0, 'async_trait>(&'life0 self, merge_artists: MergeArtists) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_artists<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IAuthService {
    fn authenticate<'life0, 'async_trait>(&'life0 self, username: String, password: String) -> Pin<Box<dyn std::future::Future<Output = Result<AuthenticationResponse, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn refresh_token<'life0, 'async_trait>(&'life0 self, refresh_token: String) -> Pin<Box<dyn std::future::Future<Output = Result<AuthenticationResponse, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IDownloadService {
    fn logs(&self, ) -> RpcStream<LogLine>;
    fn current_download<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Option<DownloadQueueEntry>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn download_queue<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<DownloadQueueEntry>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn finished_downloads<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<FinishedDownloadQueueEntry>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn sync_favourites_available<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn sync_favourites<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn download_tidal_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<String>, r#type: Type) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn exists_by_tidal_id<'life0, 'async_trait>(&'life0 self, id: String, r#type: Type) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_tidal_download_service<'life0, 'async_trait>(&'life0 self, service: TidalDownloadService) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_tidal_download_service<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<TidalDownloadService, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn tidal_download_authorized<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn tidal_download_login(&self, ) -> RpcStream<String>;
    fn tidal_sync_authorized<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_auth_url<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<String, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn kill_all_child_processes<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn search_tidal<'life0, 'async_trait>(&'life0 self, query: Option<String>, title: Option<String>, artist: Option<String>, count: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<TidalSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IAlbumService {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn versions<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_name<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, name: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_albums<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn delete_albums<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_artist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, artist_id: PlatformUUID, singles: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait IImageService {
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Image>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_hash<'life0, 'async_trait>(&'life0 self, hash: String) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Image>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_cover_hashes<'life0, 'async_trait>(&'life0 self, hashes: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<std::collections::HashMap<String, PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn get_image_data<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Vec<u8>>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn create_image<'life0, 'async_trait>(&'life0 self, bytes: Vec<u8>, origin: String) -> Pin<Box<dyn std::future::Future<Output = Result<PlatformUUID, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub trait ISongService {
    fn set_liked<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, liked: bool, added_at: Option<PlatformDate>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn set_lyrics<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, lyrics: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_title<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, title: String) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_artist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, artist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_album<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, album_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_playlist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, playlist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_user_playlist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, playlist_id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_tidal_track_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn by_tidal_tracks<'life0, 'async_trait>(&'life0 self, tracks: Vec<Track>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn liked_songs<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, explicit: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_songs<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, explicit: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn delete_songs<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn ranked_search<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, query: String, explicit: bool, liked: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn stream_song(&self, id: PlatformUUID, offset: i64) -> RpcStream<Vec<u8>>;
    fn get_stream_size<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<i64, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn all_song_ids(&self, explicit: bool) -> RpcStream<PlatformUUID>;
    fn liked_song_ids(&self, explicit: bool) -> RpcStream<PlatformUUID>;
    fn song_ids_by_artist(&self, artist_id: PlatformUUID) -> RpcStream<PlatformUUID>;
    fn song_ids_by_album(&self, album_id: PlatformUUID) -> RpcStream<PlatformUUID>;
    fn song_ids_by_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<PlatformUUID>;
    fn song_ids_by_user_playlist(&self, playlist_id: PlatformUUID) -> RpcStream<PlatformUUID>;
}

pub trait IServerStatsService {
    fn get_stats<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<ServerStats, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
    fn health<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
}

pub struct RpcClient { manager: *mut NativeRpcManager }
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
        let val = serde_cbor::from_slice(res).map_err(|e| e.to_string())?;
        unsafe { common_rpc_free_buffer(res_ptr) }; Ok(val)
    }

    pub fn subscribe<T: Serialize, R: for<'de> Deserialize<'de> + Send + 'static>(&self, service: &str, method: &str, args: &T) -> RpcStream<R> {
        let (tx, rx) = mpsc::unbounded_channel();
        let tx_box = Box::new(tx);
        let s_c = CString::new(service).unwrap(); let m_c = CString::new(method).unwrap();
        let arg_bytes = serde_cbor::to_vec(args).unwrap();
        let job = unsafe { common_rpc_subscribe(self.manager, s_c.as_ptr(), m_c.as_ptr(), arg_bytes.as_ptr(), arg_bytes.len() as c_int, Box::into_raw(tx_box) as *mut c_void, flow_callback_handler::<R>) };
        RpcStream::new(rx, job)
    }
}
impl IIndexer for RpcClient {
    fn start(&self, ) -> RpcStream<String> {
        self.subscribe("IIndexer", "start", &())
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
impl ICustomAudioService for RpcClient {
    fn upload_custom_audio<'life0, 'async_trait>(&'life0 self, file_data: Vec<u8>, file_name: String, metadata: Option<CustomMetadata>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<PlatformUUID>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
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
    fn all_artists<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Artist>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IArtistServiceAllArtistsArgs { page, page_size };
            self.call("IArtistService", "allArtists", &args).await
        })
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
    fn download_tidal_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<String>, r#type: Type) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDownloadServiceDownloadTidalIdsArgs { ids, r#type };
            self.call("IDownloadService", "downloadTidalIds", &args).await
        })
    }
    fn exists_by_tidal_id<'life0, 'async_trait>(&'life0 self, id: String, r#type: Type) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDownloadServiceExistsByTidalIdArgs { id, r#type };
            self.call("IDownloadService", "existsByTidalId", &args).await
        })
    }
    fn set_tidal_download_service<'life0, 'async_trait>(&'life0 self, service: TidalDownloadService) -> Pin<Box<dyn std::future::Future<Output = Result<(), String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "setTidalDownloadService", &service).await
        })
    }
    fn get_tidal_download_service<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<TidalDownloadService, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "getTidalDownloadService", &()).await
        })
    }
    fn tidal_download_authorized<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IDownloadService", "tidalDownloadAuthorized", &()).await
        })
    }
    fn tidal_download_login(&self, ) -> RpcStream<String> {
        self.subscribe("IDownloadService", "tidalDownloadLogin", &())
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
    fn search_tidal<'life0, 'async_trait>(&'life0 self, query: Option<String>, title: Option<String>, artist: Option<String>, count: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<TidalSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IDownloadServiceSearchTidalArgs { query, title, artist, count };
            self.call("IDownloadService", "searchTidal", &args).await
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
    fn delete_albums<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<bool, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IAlbumService", "deleteAlbums", &ids).await
        })
    }
    fn by_artist<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, artist_id: PlatformUUID, singles: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<Album>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IAlbumServiceByArtistArgs { page, page_size, artist_id, singles };
            self.call("IAlbumService", "byArtist", &args).await
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
    fn get_image_data<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, size: i32) -> Pin<Box<dyn std::future::Future<Output = Result<Option<Vec<u8>>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IImageServiceGetImageDataArgs { id, size };
            self.call("IImageService", "getImageData", &args).await
        })
    }
    fn create_image<'life0, 'async_trait>(&'life0 self, bytes: Vec<u8>, origin: String) -> Pin<Box<dyn std::future::Future<Output = Result<PlatformUUID, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = IImageServiceCreateImageArgs { bytes, origin };
            self.call("IImageService", "createImage", &args).await
        })
    }
}
impl ISongService for RpcClient {
    fn set_liked<'life0, 'async_trait>(&'life0 self, id: PlatformUUID, liked: bool, added_at: Option<PlatformDate>) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
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
    fn by_id<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<Option<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "byId", &id).await
        })
    }
    fn by_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<PlatformUUID>) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
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
    fn by_tidal_track_ids<'life0, 'async_trait>(&'life0 self, ids: Vec<String>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "byTidalTrackIds", &ids).await
        })
    }
    fn by_tidal_tracks<'life0, 'async_trait>(&'life0 self, tracks: Vec<Track>) -> Pin<Box<dyn std::future::Future<Output = Result<Vec<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "byTidalTracks", &tracks).await
        })
    }
    fn liked_songs<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, explicit: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceLikedSongsArgs { page, page_size, explicit };
            self.call("ISongService", "likedSongs", &args).await
        })
    }
    fn all_songs<'life0, 'async_trait>(&'life0 self, page: i32, page_size: i32, explicit: bool) -> Pin<Box<dyn std::future::Future<Output = Result<PaginatedResponse<UserSong>, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            let args = ISongServiceAllSongsArgs { page, page_size, explicit };
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
    fn stream_song(&self, id: PlatformUUID, offset: i64) -> RpcStream<Vec<u8>> {
        let args = ISongServiceStreamSongArgs { id, offset };
        self.subscribe("ISongService", "streamSong", &args)
    }
    fn get_stream_size<'life0, 'async_trait>(&'life0 self, id: PlatformUUID) -> Pin<Box<dyn std::future::Future<Output = Result<i64, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("ISongService", "getStreamSize", &id).await
        })
    }
    fn all_song_ids(&self, explicit: bool) -> RpcStream<PlatformUUID> {
        self.subscribe("ISongService", "allSongIds", &explicit)
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
}
