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
pub struct HandshakeResponse {
    pub secure: bool,
    #[serde(rename = "wssSupported")]
    pub wss_supported: bool,
}

pub trait IHandshakeService {
    fn handshake<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<HandshakeResponse, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;
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
impl IHandshakeService for RpcClient {
    fn handshake<'life0, 'async_trait>(&'life0 self, ) -> Pin<Box<dyn std::future::Future<Output = Result<HandshakeResponse, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {
        Box::pin(async move {
            self.call("IHandshakeService", "handshake", &()).await
        })
    }
}
