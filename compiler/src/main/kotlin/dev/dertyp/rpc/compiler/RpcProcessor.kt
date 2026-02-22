package dev.dertyp.rpc.compiler

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.File
import java.io.OutputStream

class RpcProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    private val simpleToQualifiedName = mutableMapOf<String, String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        simpleToQualifiedName.clear()
        val symbols = resolver.getSymbolsWithAnnotation("kotlinx.rpc.annotations.Rpc")
            .filterIsInstance<KSClassDeclaration>()
            .toList()

        if (symbols.isEmpty()) return emptyList()

        val sourceFiles = symbols.mapNotNull { it.containingFile }.toTypedArray()
        val dependencies = Dependencies(false, *sourceFiles)

        generateKotlinDispatchers(symbols, dependencies)
        generateRustBridge(resolver, symbols, dependencies)

        return symbols.filterNot { it.validate() }
    }

    private fun generateKotlinDispatchers(symbols: List<KSClassDeclaration>, dependencies: Dependencies) {
        val file = codeGenerator.createNewFile(
            dependencies,
            "dev.dertyp.rpc",
            "NativeDispatchers"
        )

        file.use { out ->
            out.writeLine("@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)")
            out.writeLine("@file:kotlinx.serialization.UseContextualSerialization(dev.dertyp.PlatformUUID::class)")
            out.writeLine("@file:Suppress(\"USELESS_CAST\", \"KotlinUnreachableCode\", \"unused\", \"UNCHECKED_CAST\")")
            out.writeLine("")
            out.writeLine("package dev.dertyp.rpc")
            out.writeLine("")
            out.writeLine("import dev.dertyp.rpc.BaseRpcServiceManager")
            out.writeLine("import dev.dertyp.serializers.*")
            out.writeLine("import kotlinx.serialization.*")
            out.writeLine("import kotlinx.serialization.builtins.*")
            out.writeLine("import kotlinx.coroutines.flow.Flow")
            out.writeLine("import kotlinx.coroutines.Job")
            out.writeLine("import kotlinx.coroutines.launch")
            out.writeLine("import kotlinx.coroutines.CoroutineScope")
            out.writeLine("import dev.dertyp.*")
            out.writeLine("import kotlinx.serialization.ExperimentalSerializationApi")
            out.writeLine("import kotlinx.serialization.serializer")
            out.writeLine("import kotlin.time.Duration")

            symbols.forEach { symbol ->
                out.writeLine("import ${symbol.qualifiedName?.asString()}")
            }

            out.writeLine("")
            out.writeLine("private object PlatformDateListSerializer : KSerializer<List<PlatformDate>> by ListSerializer(DateSerializer)")
            out.writeLine("private object PlatformLocalDateListSerializer : KSerializer<List<PlatformLocalDate>> by ListSerializer(LocalDateSerializer)")
            out.writeLine("private object PlatformLocalDateTimeListSerializer : KSerializer<List<PlatformLocalDateTime>> by ListSerializer(LocalDateTimeSerializer)")
            out.writeLine("private object PlatformOffsetDateTimeListSerializer : KSerializer<List<PlatformOffsetDateTime>> by ListSerializer(OffsetDateTimeSerializer)")
            out.writeLine("private object DurationListSerializer : KSerializer<List<Duration>> by ListSerializer(DurationSerializer)")
            out.writeLine("private object PlatformInstantListSerializer : KSerializer<List<PlatformInstant>> by ListSerializer(InstantSerializer)")

            out.writeLine("")
            out.writeLine("suspend fun dispatchService(manager: BaseRpcServiceManager, service: String, method: String, args: ByteArray): ByteArray {")
            out.writeLine("    return when (service) {")
            symbols.forEach { symbol ->
                val name = symbol.simpleName.asString()
                out.writeLine("        \"$name\" -> dispatch$name(manager, method, args)")
            }
            out.writeLine($$"        else -> throw IllegalArgumentException(\"Unknown service: $service\")")
            out.writeLine("    }")
            out.writeLine("}")

            out.writeLine("")
            out.writeLine("fun subscribeService(scope: CoroutineScope, manager: BaseRpcServiceManager, service: String, method: String, args: ByteArray, onEach: (ByteArray) -> Unit): Job {")
            out.writeLine("    return when (service) {")
            symbols.forEach { symbol ->
                val name = symbol.simpleName.asString()
                out.writeLine("        \"$name\" -> subscribe$name(scope, manager, method, args, onEach)")
            }
            out.writeLine($$"        else -> throw IllegalArgumentException(\"Unknown service: $service\")")
            out.writeLine("    }")
            out.writeLine("}")

            symbols.forEach { symbol ->
                generateServiceDispatcher(out, symbol)
            }
        }
    }

    private fun generateServiceDispatcher(out: OutputStream, symbol: KSClassDeclaration) {
        val name = symbol.simpleName.asString()
        val allFunctions = symbol.getAllFunctions().filter {
            it.isPublic() && it.simpleName.asString() !in listOf("<init>", "equals", "hashCode", "toString")
        }.toList()

        out.writeLine("")
        out.writeLine("private suspend fun dispatch$name(manager: BaseRpcServiceManager, method: String, args: ByteArray): ByteArray {")
        out.writeLine("    val service = manager.getService<$name>()")
        out.writeLine("    return when (method) {")
        allFunctions.filter { it.modifiers.contains(Modifier.SUSPEND) }.forEach { func ->
            val funcName = func.simpleName.asString()
            val params = func.parameters.map { (it.name?.asString() ?: "arg") to it.type.toTypeString() }

            if (params.isEmpty()) {
                out.writeLine("        \"$funcName\" -> {")
                out.writeLine("            val res = service.$funcName()")
                val retType = func.returnType?.resolve()?.toTypeString() ?: "Unit"
                val serializer = getSerializerForType(retType)
                if (serializer != null) out.writeLine("            AppCbor.encodeToByteArray($serializer, res)")
                else out.writeLine("            AppCbor.encodeToByteArray(res)")
                out.writeLine("        }")
            } else if (params.size == 1) {
                out.writeLine("        \"$funcName\" -> {")
                val (_, pType) = params[0]
                val serializer = getSerializerForType(pType)
                if (serializer != null) out.writeLine("            val a = AppCbor.decodeFromByteArray($serializer, args)")
                else out.writeLine("            val a = AppCbor.decodeFromByteArray<$pType>(args)")
                out.writeLine("            val res = service.$funcName(a)")
                val retType = func.returnType?.resolve()?.toTypeString() ?: "Unit"
                val retSerializer = getSerializerForType(retType)
                if (retSerializer != null) out.writeLine("            AppCbor.encodeToByteArray($retSerializer, res)")
                else out.writeLine("            AppCbor.encodeToByteArray(res)")
                out.writeLine("        }")
            } else {
                val argsClassName = "${name}${funcName.replaceFirstChar { it.uppercase() }}Args"
                out.writeLine("        \"$funcName\" -> {")
                out.writeLine("            val a = AppCbor.decodeFromByteArray<$argsClassName>(args)")
                val callParams = params.joinToString(", ") { "a.${it.first}" }
                out.writeLine("            val res = service.$funcName($callParams)")
                val retType = func.returnType?.resolve()?.toTypeString() ?: "Unit"
                val retSerializer = getSerializerForType(retType)
                if (retSerializer != null) out.writeLine("            AppCbor.encodeToByteArray($retSerializer, res)")
                else out.writeLine("            AppCbor.encodeToByteArray(res)")
                out.writeLine("        }")
            }
        }
        out.writeLine($$"        else -> throw IllegalArgumentException(\"Unknown method: $method\")")
        out.writeLine("    }")
        out.writeLine("}")

        out.writeLine("")
        out.writeLine("private fun subscribe$name(scope: CoroutineScope, manager: BaseRpcServiceManager, method: String, args: ByteArray, onEach: (ByteArray) -> Unit): Job {")
        out.writeLine("    val service = manager.getService<$name>()")
        out.writeLine("    return scope.launch {")
        out.writeLine("        val flow = when (method) {")
        allFunctions.filter { !it.modifiers.contains(Modifier.SUSPEND) && it.returnType?.resolve()?.declaration?.simpleName?.asString() == "Flow" }.forEach { func ->
            val funcName = func.simpleName.asString()
            val params = func.parameters.map { (it.name?.asString() ?: "arg") to it.type.toTypeString() }
            if (params.isEmpty()) {
                out.writeLine("            \"$funcName\" -> service.$funcName()")
            } else if (params.size == 1) {
                val (_, pType) = params[0]
                val serializer = getSerializerForType(pType)
                if (serializer != null) out.writeLine("            \"$funcName\" -> service.$funcName(AppCbor.decodeFromByteArray($serializer, args))")
                else out.writeLine("            \"$funcName\" -> service.$funcName(AppCbor.decodeFromByteArray<$pType>(args))")
            } else {
                val argsClassName = "${name}${funcName.replaceFirstChar { it.uppercase() }}Args"
                val callParams = params.joinToString(", ") { "a.${it.first}" }
                out.writeLine("            \"$funcName\" -> {")
                out.writeLine("                val a = AppCbor.decodeFromByteArray<$argsClassName>(args)")
                out.writeLine("                service.$funcName($callParams)")
                out.writeLine("            }")
            }
        }
        out.writeLine($$"            else -> throw IllegalArgumentException(\"Unknown method: $method\")")
        out.writeLine("        } as Flow<Any>")
        out.writeLine("        flow.collect { item ->")
        out.writeLine("            val serializer = when (method) {")
        allFunctions.filter { !it.modifiers.contains(Modifier.SUSPEND) && it.returnType?.resolve()?.declaration?.simpleName?.asString() == "Flow" }.forEach { func ->
            val funcName = func.simpleName.asString()
            val flowType = func.returnType?.resolve()?.arguments?.firstOrNull()?.type?.toTypeString() ?: "Any"
            val serializer = getSerializerForType(flowType)
            if (serializer != null) out.writeLine("                \"$funcName\" -> $serializer")
            else out.writeLine("                \"$funcName\" -> AppCbor.serializersModule.serializer<$flowType>()")
        }
        out.writeLine("                else -> AppCbor.serializersModule.serializer<Any>()")
        out.writeLine("            } as KSerializer<Any>")
        out.writeLine("            onEach(AppCbor.encodeToByteArray(serializer, item))")
        out.writeLine("        }")
        out.writeLine("    }")
        out.writeLine("}")

        // Generate Args classes for multi-param methods
        allFunctions.forEach { func ->
            val params = func.parameters.map { (it.name?.asString() ?: "arg") to it.type.toTypeString() }
            if (params.size > 1) {
                val funcName = func.simpleName.asString()
                val argsClassName = "${name}${funcName.replaceFirstChar { it.uppercase() }}Args"
                out.writeLine("")
                out.writeLine("@Serializable")
                out.writeLine("data class $argsClassName(")
                params.forEachIndexed { index, (pName, pType) ->
                    val comma = if (index < params.size - 1) "," else ""
                    val ann = getSerializerAnnotation(pType)
                    out.writeLine("    $ann val $pName: $pType$comma")
                }
                
                val hasArray = params.any { it.second.contains("Array") }
                if (hasArray) {
                    out.writeLine(") {")
                    out.writeLine("    override fun equals(other: Any?): Boolean {")
                    out.writeLine("        if (this === other) return true")
                    out.writeLine("        if (other !is $argsClassName) return false")
                    params.forEach { (pName, pType) ->
                        if (pType.contains("Array")) {
                            if (pType.endsWith("?")) {
                                out.writeLine("        if (!(this.$pName?.contentEquals(other.$pName) ?: (other.$pName == null))) return false")
                            } else {
                                out.writeLine("        if (!this.$pName.contentEquals(other.$pName)) return false")
                            }
                        } else {
                            out.writeLine("        if (this.$pName != other.$pName) return false")
                        }
                    }
                    out.writeLine("        return true")
                    out.writeLine("    }")
                    out.writeLine("")
                    out.writeLine("    override fun hashCode(): Int {")
                    params.forEachIndexed { index, (pName, pType) ->
                        val hash = if (pType.contains("Array")) {
                            if (pType.endsWith("?")) "this.$pName?.contentHashCode() ?: 0" else "this.$pName.contentHashCode()"
                        } else {
                            if (pType.endsWith("?")) "this.$pName?.hashCode() ?: 0" else "this.$pName.hashCode()"
                        }
                        if (index == 0) {
                            out.writeLine("        var result = $hash")
                        } else {
                            out.writeLine("        result = 31 * result + ($hash)")
                        }
                    }
                    out.writeLine("        return result")
                    out.writeLine("    }")
                    out.writeLine("}")
                } else {
                    out.writeLine(")")
                }
            }
        }
    }

    private fun generateRustBridge(resolver: Resolver, symbols: List<KSClassDeclaration>, dependencies: Dependencies) {
        val anySourceFile = symbols.firstOrNull()?.containingFile?.filePath
        val rustBridgePath = if (anySourceFile != null) {
            val moduleRoot = File(anySourceFile).absolutePath.substringBefore("/src/")
            File(moduleRoot, "rust-bridge/src/lib.rs")
        } else {
            File("rust-bridge/src/lib.rs")
        }

        val file = if (rustBridgePath.parentFile?.exists() == true) {
            rustBridgePath.outputStream()
        } else {
             codeGenerator.createNewFile(
                dependencies,
                "dev.dertyp.rpc",
                "RustBridge",
                "rs"
            )
        }

        val modelsToGenerate = mutableSetOf<KSClassDeclaration>()

        file.use { out ->
            out.writeLine("use serde::{Deserialize, Serialize};")
            out.writeLine("use std::ffi::{CStr, CString};")
            out.writeLine("use std::os::raw::{c_char, c_int, c_void};")
            out.writeLine("use futures_util::Stream;")
            out.writeLine("use std::pin::Pin;")
            out.writeLine("use std::task::{Context, Poll};")
            out.writeLine("use std::sync::Arc;")
            out.writeLine("use tokio::sync::mpsc;")
            out.writeLine("")
            out.writeLine("pub type FlowCallback = extern \"C\" fn(*mut c_void, *const u8, c_int);")
            out.writeLine("")
            out.writeLine("#[repr(C)]")
            out.writeLine("pub struct NativeRpcManager { _unused: [u8; 0] }")
            out.writeLine("")
            out.writeLine("extern \"C\" {")
            out.writeLine("    pub fn common_rpc_manager_create() -> *mut NativeRpcManager;")
            out.writeLine("    pub fn common_rpc_manager_release(ptr: *mut NativeRpcManager);")
            out.writeLine("    pub fn common_rpc_free_buffer(ptr: *mut u8);")
            out.writeLine("    pub fn common_rpc_call(ptr: *mut NativeRpcManager, service: *const c_char, method: *const c_char, args: *const u8, len: c_int, out_len: *mut c_int) -> *mut u8;")
            out.writeLine("    pub fn common_rpc_subscribe(ptr: *mut NativeRpcManager, service: *const c_char, method: *const c_char, args: *const u8, len: c_int, ctx: *mut c_void, cb: FlowCallback) -> *mut c_void;")
            out.writeLine("    pub fn common_rpc_unsubscribe(job: *mut c_void);")
            out.writeLine("}")
            out.writeLine("")
            out.writeLine("pub struct RpcStream<T> {")
            out.writeLine("    rx: mpsc::UnboundedReceiver<T>,")
            out.writeLine("    job: *mut c_void,")
            out.writeLine("}")
            out.writeLine("")
            out.writeLine("impl<T> RpcStream<T> {")
            out.writeLine("    pub fn new(rx: mpsc::UnboundedReceiver<T>, job: *mut c_void) -> Self {")
            out.writeLine("        Self { rx, job }")
            out.writeLine("    }")
            out.writeLine("}")
            out.writeLine("")
            out.writeLine("impl<T> Stream for RpcStream<T> {")
            out.writeLine("    type Item = T;")
            out.writeLine("    fn poll_next(mut self: Pin<&mut Self>, cx: &mut Context<'_>) -> Poll<Option<Self::Item>> {")
            out.writeLine("        self.rx.poll_recv(cx)")
            out.writeLine("    }")
            out.writeLine("}")
            out.writeLine("")
            out.writeLine("impl<T> Drop for RpcStream<T> {")
            out.writeLine("    fn drop(&mut self) {")
            out.writeLine("        unsafe { common_rpc_unsubscribe(self.job) };")
            out.writeLine("    }")
            out.writeLine("}")
            out.writeLine("unsafe impl<T> Send for RpcStream<T> {}")
            out.writeLine("")
            out.writeLine("extern \"C\" fn flow_callback_handler<T: for<'de> Deserialize<'de> + Send + 'static>(ctx: *mut c_void, data: *const u8, len: c_int) {")
            out.writeLine("    let tx = unsafe { &*(ctx as *const mpsc::UnboundedSender<T>) };")
            out.writeLine("    let bytes = unsafe { std::slice::from_raw_parts(data, len as usize) };")
            out.writeLine("    if let Ok(val) = serde_cbor::from_slice(bytes) {")
            out.writeLine("        let _ = tx.send(val);")
            out.writeLine("    }")
            out.writeLine("}")
            out.writeLine("")
            out.writeLine("#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)] #[serde(transparent)] pub struct PlatformUUID(pub Vec<u8>);")
            out.writeLine("#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)] #[serde(transparent)] pub struct PlatformDate(pub i64);")
            out.writeLine("#[derive(Serialize, Deserialize, Debug, Clone, PartialEq, Default)] #[serde(transparent)] pub struct SuspendFunction0(pub serde_json::Value);")
            out.writeLine("")

            // Collect and generate models
            symbols.forEach { symbol ->
                symbol.getAllFunctions().filter { 
                    it.isPublic() && it.simpleName.asString() !in listOf("<init>", "equals", "hashCode", "toString") 
                }.forEach { func ->
                    func.parameters.forEach { collectModels(it.type.resolve(), modelsToGenerate) }
                    func.returnType?.resolve()?.let { collectModels(it, modelsToGenerate) }
                    
                    if (func.parameters.size > 1) {
                        val argsClassName = "${symbol.simpleName.asString()}${func.simpleName.asString().replaceFirstChar { it.uppercase() }}Args"
                        out.writeLine("#[derive(Serialize, Deserialize, Debug, Clone)]")
                        out.writeLine("pub struct $argsClassName {")
                        func.parameters.forEach { param ->
                            val name = toSnakeCase(param.name?.asString() ?: "arg")
                            val type = toRustType(param.type.resolve())
                            if (name != param.name?.asString()) out.writeLine("    #[serde(rename = \"${param.name?.asString()}\")]")
                            out.writeLine("    pub $name: $type,")
                        }
                        out.writeLine("}")
                        out.writeLine("")
                    }
                }
            }

            val generated = mutableSetOf<String>()
            modelsToGenerate.forEach { generateRustStruct(out, it, generated) }

            symbols.forEach { symbol ->
                val name = symbol.simpleName.asString()
                out.writeLine("pub trait $name {")
                symbol.getAllFunctions().filter { 
                    it.isPublic() && it.simpleName.asString() !in listOf("<init>", "equals", "hashCode", "toString") 
                }.forEach { func ->
                    val fName = toSnakeCase(func.simpleName.asString())
                    val params = func.parameters.joinToString(", ") { "${toSnakeCase(it.name?.asString() ?: "arg")}: ${toRustType(it.type.resolve())}" }
                    val resolvedRet = func.returnType?.resolve()
                    val isFlow = resolvedRet?.declaration?.qualifiedName?.asString() == "kotlinx.coroutines.flow.Flow"
                    if (isFlow) {
                        val flowType = resolvedRet.arguments.firstOrNull()?.type?.resolve()?.let { toRustType(it) } ?: "serde_json::Value"
                        out.writeLine("    fn $fName(&self, $params) -> RpcStream<$flowType>;")
                    } else {
                        val ret = toRustType(resolvedRet ?: resolver.builtIns.unitType)
                        out.writeLine("    fn $fName<'life0, 'async_trait>(&'life0 self, $params) -> Pin<Box<dyn std::future::Future<Output = Result<$ret, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait;")
                    }
                }
                out.writeLine("}")
                out.writeLine("")
            }

            // Client implementation
            out.writeLine("pub struct RpcClient { manager: *mut NativeRpcManager }")
            out.writeLine("unsafe impl Send for RpcClient {}")
            out.writeLine("unsafe impl Sync for RpcClient {}")
            out.writeLine("impl RpcClient {")
            out.writeLine("    pub async fn call<T: Serialize, R: for<'de> Deserialize<'de>>(&self, service: &str, method: &str, args: &T) -> Result<R, String> {")
            out.writeLine("        let s_c = CString::new(service).unwrap(); let m_c = CString::new(method).unwrap();")
            out.writeLine("        let arg_bytes = serde_cbor::to_vec(args).map_err(|e| e.to_string())?;")
            out.writeLine("        let mut out_len: c_int = 0;")
            out.writeLine("        let res_ptr = unsafe { common_rpc_call(self.manager, s_c.as_ptr(), m_c.as_ptr(), arg_bytes.as_ptr(), arg_bytes.len() as c_int, &mut out_len) };")
            out.writeLine("        if res_ptr.is_null() { return Err(\"RPC Error\".into()); }")
            out.writeLine("        let res = unsafe { std::slice::from_raw_parts(res_ptr, out_len as usize) };")
            out.writeLine("        let val = serde_cbor::from_slice(res).map_err(|e| e.to_string())?;")
            out.writeLine("        unsafe { common_rpc_free_buffer(res_ptr) }; Ok(val)")
            out.writeLine("    }")
            out.writeLine("")
            out.writeLine("    pub fn subscribe<T: Serialize, R: for<'de> Deserialize<'de> + Send + 'static>(&self, service: &str, method: &str, args: &T) -> RpcStream<R> {")
            out.writeLine("        let (tx, rx) = mpsc::unbounded_channel();")
            out.writeLine("        let tx_box = Box::new(tx);")
            out.writeLine("        let s_c = CString::new(service).unwrap(); let m_c = CString::new(method).unwrap();")
            out.writeLine("        let arg_bytes = serde_cbor::to_vec(args).unwrap();")
            out.writeLine("        let job = unsafe { common_rpc_subscribe(self.manager, s_c.as_ptr(), m_c.as_ptr(), arg_bytes.as_ptr(), arg_bytes.len() as c_int, Box::into_raw(tx_box) as *mut c_void, flow_callback_handler::<R>) };")
            out.writeLine("        RpcStream::new(rx, job)")
            out.writeLine("    }")
            out.writeLine("}")

            symbols.forEach { symbol ->
                val name = symbol.simpleName.asString()
                out.writeLine("impl $name for RpcClient {")
                symbol.getAllFunctions().filter { 
                    it.isPublic() && it.simpleName.asString() !in listOf("<init>", "equals", "hashCode", "toString") 
                }.forEach { func ->
                    val fName = toSnakeCase(func.simpleName.asString())
                    val params = func.parameters.joinToString(", ") { "${toSnakeCase(it.name?.asString() ?: "arg")}: ${toRustType(it.type.resolve())}" }
                    val paramNames = func.parameters.joinToString(", ") { toSnakeCase(it.name?.asString() ?: "arg") }
                    val resolvedRet = func.returnType?.resolve()
                    val isFlow = resolvedRet?.declaration?.qualifiedName?.asString() == "kotlinx.coroutines.flow.Flow"
                    
                    if (isFlow) {
                        val flowType = resolvedRet.arguments.firstOrNull()?.type?.resolve()?.let { toRustType(it) } ?: "serde_json::Value"
                        out.writeLine("    fn $fName(&self, $params) -> RpcStream<$flowType> {")
                        if (func.parameters.isEmpty()) out.writeLine("        self.subscribe(\"$name\", \"${func.simpleName.asString()}\", &())")
                        else if (func.parameters.size == 1) out.writeLine("        self.subscribe(\"$name\", \"${func.simpleName.asString()}\", &$paramNames)")
                        else {
                            val argsClassName = "${name}${func.simpleName.asString().replaceFirstChar { it.uppercase() }}Args"
                            val argsFields = func.parameters.joinToString(", ") { toSnakeCase(it.name?.asString() ?: "arg") }
                            out.writeLine("        let args = $argsClassName { $argsFields };")
                            out.writeLine("        self.subscribe(\"$name\", \"${func.simpleName.asString()}\", &args)")
                        }
                    } else {
                        val ret = toRustType(resolvedRet ?: resolver.builtIns.unitType)
                        out.writeLine("    fn $fName<'life0, 'async_trait>(&'life0 self, $params) -> Pin<Box<dyn std::future::Future<Output = Result<$ret, String>> + Send + 'async_trait>> where 'life0: 'async_trait, Self: 'async_trait {")
                        out.writeLine("        Box::pin(async move {")
                        if (func.parameters.isEmpty()) out.writeLine("            self.call(\"$name\", \"${func.simpleName.asString()}\", &()).await")
                        else if (func.parameters.size == 1) out.writeLine("            self.call(\"$name\", \"${func.simpleName.asString()}\", &$paramNames).await")
                        else {
                            val argsClassName = "${name}${func.simpleName.asString().replaceFirstChar { it.uppercase() }}Args"
                            val argsFields = func.parameters.joinToString(", ") { toSnakeCase(it.name?.asString() ?: "arg") }
                            out.writeLine("            let args = $argsClassName { $argsFields };")
                            out.writeLine("            self.call(\"$name\", \"${func.simpleName.asString()}\", &args).await")
                        }
                        out.writeLine("        })")
                    }
                    out.writeLine("    }")
                }
                out.writeLine("}")
            }
        }
    }

    private fun getUniqueName(decl: KSDeclaration): String {
        val simpleName = decl.simpleName.asString()
        val qName = decl.qualifiedName?.asString() ?: ""
        val existingQName = simpleToQualifiedName[simpleName]
        if (existingQName == null || existingQName == qName) {
            simpleToQualifiedName[simpleName] = qName
            return simpleName
        }
        val pkg = qName.substringBeforeLast(".", "").substringAfterLast(".")
        return if (pkg.isNotEmpty()) "${pkg.replaceFirstChar { it.uppercase() }}$simpleName" else qName.replace(".", "")
    }

    private fun collectModels(type: KSType, set: MutableSet<KSClassDeclaration>) {
        val decl = type.declaration
        if (decl is KSClassDeclaration && (decl.classKind == ClassKind.CLASS || decl.classKind == ClassKind.ENUM_CLASS)) {
            val qName = decl.qualifiedName?.asString() ?: ""
            val isBuiltIn = qName.startsWith("kotlin.") ||
                    qName.startsWith("kotlinx.coroutines.") ||
                    qName == "dev.dertyp.PlatformUUID" ||
                    qName == "dev.dertyp.PlatformDate" ||
                    qName == "dev.dertyp.PlatformInstant" ||
                    qName == "dev.dertyp.PlatformLocalDate" ||
                    qName == "dev.dertyp.PlatformLocalDateTime" ||
                    qName == "dev.dertyp.PlatformOffsetDateTime"

            if (!isBuiltIn && !set.contains(decl)) {
                set.add(decl)
                decl.getAllProperties().forEach { collectModels(it.type.resolve(), set) }
            }
        }
        type.arguments.forEach { argument -> argument.type?.resolve()?.let { collectModels(it, set) } }
    }

    private fun generateRustStruct(out: OutputStream, decl: KSClassDeclaration, generated: MutableSet<String>) {
        val qName = decl.qualifiedName?.asString() ?: return
        if (generated.contains(qName)) return
        generated.add(qName)
        
        val name = getUniqueName(decl)
        
        if (decl.classKind == ClassKind.ENUM_CLASS) {
            out.writeLine("#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)]")
            out.writeLine("pub enum $name {")
            decl.declarations.filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .forEach { entry ->
                    out.writeLine("    ${entry.simpleName.asString()},")
                }
            out.writeLine("}")
        } else {
            val typeParams = if (decl.typeParameters.isNotEmpty()) "<" + decl.typeParameters.joinToString(", ") { it.name.asString() } + ">" else ""
            out.writeLine("#[derive(Serialize, Deserialize, Debug, Clone)]")
            out.writeLine("pub struct $name$typeParams {")
            decl.getAllProperties().forEach { prop ->
                if (prop.annotations.any { it.shortName.asString() == "Transient" }) return@forEach
                
                val pName = toSnakeCase(prop.simpleName.asString())
                val pType = toRustType(prop.type.resolve())
                if (pName != prop.simpleName.asString()) out.writeLine("    #[serde(rename = \"${prop.simpleName.asString()}\")]")
                
                if (pType == "SuspendFunction0") out.writeLine("    #[serde(skip)]")
                
                out.writeLine("    pub $pName: $pType,")
            }
            out.writeLine("}")
        }
        out.writeLine("")
    }

    private fun toRustType(type: KSType): String {
        val decl = type.declaration
        val qName = decl.qualifiedName?.asString()
        val base = when {
            qName == "kotlin.String" -> "String"
            qName == "kotlin.Int" -> "i32"
            qName == "kotlin.Long" -> "i64"
            qName == "kotlin.Boolean" -> "bool"
            qName == "kotlin.ByteArray" -> "Vec<u8>"
            qName == "kotlin.Unit" -> "()"
            qName == "kotlin.Any" -> "serde_json::Value"
            qName == "dev.dertyp.PlatformUUID" -> "PlatformUUID"
            qName == "dev.dertyp.PlatformDate" || qName == "dev.dertyp.PlatformInstant" || qName == "dev.dertyp.PlatformLocalDate" || qName == "dev.dertyp.PlatformLocalDateTime" || qName == "dev.dertyp.PlatformOffsetDateTime" -> "PlatformDate"
            qName == "kotlin.time.Duration" -> "String"
            qName?.startsWith("kotlin.Function") == true || qName?.startsWith("kotlin.coroutines.SuspendFunction") == true -> "SuspendFunction0"
            qName == "kotlin.collections.List" || qName == "kotlin.collections.Collection" || qName == "kotlin.collections.Set" -> {
                val arg = type.arguments.firstOrNull()?.type?.resolve()
                "Vec<${arg?.let { toRustType(it) } ?: "serde_json::Value"}>"
            }
            qName == "kotlin.collections.Map" -> {
                val k = type.arguments.getOrNull(0)?.type?.resolve()
                val v = type.arguments.getOrNull(1)?.type?.resolve()
                "std::collections::HashMap<${k?.let { toRustType(it) } ?: "String"}, ${v?.let { toRustType(it) } ?: "serde_json::Value"}>"
            }
            qName == "kotlin.Pair" -> {
                val t1 = type.arguments.getOrNull(0)?.type?.resolve()
                val t2 = type.arguments.getOrNull(1)?.type?.resolve()
                "(${t1?.let { toRustType(t1) } ?: "serde_json::Value"}, ${t2?.let { toRustType(t2) } ?: "serde_json::Value"})"
            }
            qName == "kotlinx.coroutines.flow.Flow" -> "()"
            qName == "dev.dertyp.data.PaginatedResponse" -> {
                val arg = type.arguments.firstOrNull()?.type?.resolve()
                "PaginatedResponse<${arg?.let { toRustType(it) } ?: "serde_json::Value"}>"
            }
            else -> {
                if (decl is KSTypeParameter) decl.name.asString()
                else getUniqueName(decl)
            }
        }
        return if (type.isMarkedNullable && base != "()") "Option<$base>" else base
    }

    private fun toSnakeCase(name: String): String {
        val snake = name.replace(Regex("([a-z])([A-Z]+)"), "$1_$2").lowercase()
        return if (snake == "type") "r#type" else snake
    }

    private fun getSerializerAnnotation(type: String) = getSerializerForType(type)?.let { "@Serializable(with = $it::class) " } ?: ""
    private fun getSerializerForType(type: String): String? {
        return when (val base = type.removeSuffix("?").substringAfterLast(".")) {
            "PlatformDate" -> "DateSerializer"
            "PlatformLocalDate" -> "LocalDateSerializer"
            "PlatformLocalDateTime" -> "LocalDateTimeSerializer"
            "PlatformOffsetDateTime" -> "OffsetDateTimeSerializer"
            "Duration" -> "DurationSerializer"
            "PlatformInstant" -> "InstantSerializer"
            else -> if (type.contains("List<Platform")) "${base}ListSerializer" else null
        }
    }

    private fun OutputStream.writeLine(str: String) = this.write((str + "\n").toByteArray())
    private fun KSTypeReference.toTypeString(): String = resolve().toTypeString()
    private fun KSType.toTypeString(): String {
        val name = declaration.qualifiedName?.asString() ?: return "Any"
        val nullability = if (isMarkedNullable) "?" else ""
        if (arguments.isEmpty()) return "$name$nullability"
        val args = arguments.joinToString(", ") { it.type?.resolve()?.toTypeString() ?: "Any" }
        return "$name<$args>$nullability"
    }
}

class RpcProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = RpcProcessor(environment.codeGenerator)
}
