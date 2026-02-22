@file:OptIn(
    ExperimentalForeignApi::class,
    ExperimentalNativeApi::class,
    ExperimentalSerializationApi::class
)

package dev.dertyp

import dev.dertyp.data.AuthenticationResponse
import dev.dertyp.rpc.BaseRpcServiceManager
import dev.dertyp.rpc.dispatchService
import dev.dertyp.rpc.subscribeService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.experimental.ExperimentalNativeApi

class NativeRpcManager(client: HttpClient) : BaseRpcServiceManager(client) {
    var rpcUrl: String? = null
    var authToken: String? = null
    var refreshToken: String? = null
    var tokenExpired: Boolean = false
    var authenticated: Boolean = false

    override suspend fun getRpcUrl(): String? = rpcUrl
    override fun getAuthToken(): String? = authToken
    override fun getRefreshToken(): String? = refreshToken
    override fun isTokenExpired(): Boolean = tokenExpired
    override fun isAuthenticated(): Boolean = authenticated

    public override suspend fun updateAuth(response: AuthenticationResponse) {
        authToken = response.token
        refreshToken = response.refreshToken
        authenticated = true
        tokenExpired = false
    }

    override suspend fun handleAuthFailure() {
        authenticated = false
        authToken = null
        refreshToken = null
    }

    val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
}

// Helper to return binary data to Rust
fun ByteArray.toNativeBuffer(outLen: CPointer<IntVar>): CPointer<ByteVar> {
    val ptr = nativeHeap.allocArray<ByteVar>(this.size)
    for (i in this.indices) {
        ptr[i] = this[i]
    }
    outLen.pointed.value = this.size
    return ptr
}

@CName("common_rpc_manager_create")
fun createManager(): COpaquePointer {
    val client = HttpClient(CIO)
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

// --- Generic Dispatching ---

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

// Flow callback type: (context, dataPtr, dataLen)
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

@CName("common_rpc_validate_server")
fun validateServer(ptr: COpaquePointer, url: CPointer<ByteVar>): Boolean {
    val manager = ptr.asStableRef<NativeRpcManager>().get()
    return runBlocking {
        manager.validateServer(url.toKString())
    }
}
