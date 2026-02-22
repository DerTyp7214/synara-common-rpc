@file:OptIn(
    ExperimentalForeignApi::class,
    ExperimentalNativeApi::class,
    ExperimentalSerializationApi::class
)
@file:Suppress("unused")

package dev.dertyp

import dev.dertyp.data.AuthenticationResponse
import dev.dertyp.rpc.BaseRpcServiceManager
import dev.dertyp.rpc.dispatchService
import dev.dertyp.rpc.subscribeService
import dev.dertyp.serializers.AppCbor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.websocket.*
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.rpc.krpc.ktor.client.Krpc
import kotlinx.rpc.krpc.serialization.cbor.cbor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlin.experimental.ExperimentalNativeApi
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class NativeRpcManager(client: HttpClient) : BaseRpcServiceManager(client) {
    var rpcUrl: String? = null
    var authToken: String? = null
    var refreshToken: String? = null
    var authenticated: Boolean = false
    var expiresAt: PlatformDate? = null

    public override suspend fun getRpcUrl(): String? = rpcUrl
    public override fun getAuthToken(): String? = authToken
    public override fun getRefreshToken(): String? = refreshToken
    public override fun isTokenExpired(): Boolean = expiresAt?.let {
        it.toEpochMilliseconds() < (currentTimeMillis() + 1.minutes.inWholeMilliseconds)
    } ?: true

    public override fun isAuthenticated(): Boolean = authenticated

    public override suspend fun updateAuth(response: AuthenticationResponse) {
        authToken = response.token
        refreshToken = response.refreshToken
        authenticated = true
        expiresAt = response.expiresAt
    }

    override suspend fun handleAuthFailure() {
        authenticated = false
        authToken = null
        refreshToken = null
    }

    val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
}

fun ByteArray.toNativeBuffer(outLen: CPointer<IntVar>): CPointer<ByteVar> {
    val ptr = nativeHeap.allocArray<ByteVar>(this.size)
    for (i in this.indices) {
        ptr[i] = this[i]
    }
    outLen.pointed.value = this.size
    return ptr
}

fun String.toNativeBuffer(): CPointer<ByteVar> {
    val bytes = this.encodeToByteArray()
    val ptr = nativeHeap.allocArray<ByteVar>(bytes.size + 1)
    for (i in bytes.indices) {
        ptr[i] = bytes[i]
    }
    ptr[bytes.size] = 0
    return ptr
}

@CName("common_rpc_manager_create")
fun createManager(): COpaquePointer {
    val client = HttpClient(CIO) {
        install(UserAgent) {
            agent = "Synara/Rust"
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 30000
        }
        install(WebSockets) {
            pingInterval = 15.seconds
            maxFrameSize = Long.MAX_VALUE
        }
        install(Krpc) {
            serialization {
                cbor(AppCbor)
            }
        }
    }
    val manager = NativeRpcManager(client)
    return StableRef.create(manager).asCPointer()
}

@CName("common_rpc_manager_release")
fun releaseManager(ptr: COpaquePointer) {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    manager.coroutineScope.cancel()
    ptr.asStableRef<NativeRpcManager>().dispose()
}

@CName("common_rpc_free_buffer")
fun freeBuffer(ptr: CPointer<ByteVar>) {
    nativeHeap.free(ptr)
}

@CName("common_rpc_call")
fun commonRpcCall(
    ptr: COpaquePointer,
    serviceName: CPointer<ByteVar>,
    methodName: CPointer<ByteVar>,
    argsPtr: CPointer<ByteVar>?,
    argsLen: Int,
    outLen: CPointer<IntVar>
): CPointer<ByteVar>? {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    val service = serviceName.toKString()
    val method = methodName.toKString()
    val args = argsPtr?.readBytes(argsLen) ?: byteArrayOf()

    return runBlocking {
        try {
            val result = dispatchService(manager, service, method, args)
            result.toNativeBuffer(outLen)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

typealias FlowCallback = CPointer<CFunction<(COpaquePointer?, CPointer<ByteVar>?, Int) -> Unit>>

@CName("common_rpc_subscribe")
fun commonRpcSubscribe(
    ptr: COpaquePointer,
    serviceName: CPointer<ByteVar>,
    methodName: CPointer<ByteVar>,
    argsPtr: CPointer<ByteVar>?,
    argsLen: Int,
    context: COpaquePointer?,
    onEach: FlowCallback
): COpaquePointer {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    val service = serviceName.toKString()
    val method = methodName.toKString()
    val args = argsPtr?.readBytes(argsLen) ?: byteArrayOf()

    val job = subscribeService(manager.coroutineScope, manager, service, method, args) { data: ByteArray ->
        memScoped {
            val dataPtr = data.toCValues().ptr
            onEach.invoke(context, dataPtr, data.size)
        }
    }

    return StableRef.create(job).asCPointer()
}

@CName("common_rpc_unsubscribe")
fun commonRpcUnsubscribe(jobPtr: COpaquePointer) {
    val job = jobPtr.asStableRef<Job>().get()
    job.cancel()
    jobPtr.asStableRef<Job>().dispose()
}

@CName("common_rpc_set_url")
fun setUrl(ptr: COpaquePointer, url: CPointer<ByteVar>) {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    manager.rpcUrl = url.toKString()
}

@CName("common_rpc_get_url")
fun getUrl(ptr: COpaquePointer): CPointer<ByteVar>? {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    return manager.rpcUrl?.toNativeBuffer()
}

@CName("common_rpc_validate_server")
fun validateServer(ptr: COpaquePointer, url: CPointer<ByteVar>): Boolean {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    return runBlocking {
        manager.validateServer(url.toKString())
    }
}

@CName("common_rpc_update_auth")
fun updateAuth(ptr: COpaquePointer, argsPtr: CPointer<ByteVar>, argsLen: Int) {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    val args = argsPtr.readBytes(argsLen)
    val response = AppCbor.decodeFromByteArray<AuthenticationResponse>(args)
    runBlocking {
        manager.updateAuth(response)
    }
}

@CName("common_rpc_is_authenticated")
fun isAuthenticated(ptr: COpaquePointer): Boolean {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    return manager.isAuthenticated()
}

@CName("common_rpc_get_auth_token")
fun getAuthToken(ptr: COpaquePointer): CPointer<ByteVar>? {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    return manager.getAuthToken()?.toNativeBuffer()
}

@CName("common_rpc_get_refresh_token")
fun getRefreshToken(ptr: COpaquePointer): CPointer<ByteVar>? {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    return manager.getRefreshToken()?.toNativeBuffer()
}

@CName("common_rpc_is_token_expired")
fun isTokenExpired(ptr: COpaquePointer): Boolean {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    return manager.isTokenExpired()
}
