package dev.dertyp.rpc.compiler

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

class RpcProcessor(
    private val codeGenerator: CodeGenerator
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
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
            out.writeLine("@Suppress(\"UNCHECKED_CAST\")")
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
            out.writeLine("@Suppress(\"UNCHECKED_CAST\")")
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
        val allFunctions = symbol.getAllFunctions().filter { it.isPublic() && it.simpleName.asString() != "<init>" }.toList()

        allFunctions.filter { it.parameters.size > 1 }.forEach { func ->
            val funcName = func.simpleName.asString().replaceFirstChar { it.uppercase() }
            out.writeLine("@Serializable")
            out.writeLine("data class ${name}${funcName}Args(")
            func.parameters.forEachIndexed { index, param ->
                val pName = param.name?.asString() ?: "arg$index"
                val pType = param.type.toTypeString()
                val comma = if (index == func.parameters.size - 1) "" else ","
                val annotation = getSerializerAnnotation(pType)
                out.writeLine("    $annotation val $pName: $pType$comma")
            }
            out.writeLine(")")
            out.writeLine("")
        }

        out.writeLine("")
        out.writeLine("@Suppress(\"UNCHECKED_CAST\")")
        out.writeLine("private suspend fun dispatch$name(manager: BaseRpcServiceManager, method: String, args: ByteArray): ByteArray {")
        out.writeLine("    val s = manager.getService<$name>()")
        out.writeLine("    return when (method) {")
        allFunctions.filter { it.modifiers.contains(Modifier.SUSPEND) }.forEach { func ->
            val funcName = func.simpleName.asString()
            out.writeLine("        \"$funcName\" -> {")
            when {
                func.parameters.isEmpty() -> {
                    val returnType = func.returnType?.toTypeString() ?: "Unit"
                    val serializer = getSerializerForType(returnType)
                    if (serializer != null) {
                        out.writeLine("            AppCbor.encodeToByteArray($serializer as KSerializer<Any>, s.$funcName() as Any)")
                    } else {
                        out.writeLine("            AppCbor.encodeToByteArray(s.$funcName())")
                    }
                }
                func.parameters.size == 1 -> {
                    val paramType = func.parameters[0].type.toTypeString()
                    val paramSerializer = getSerializerForType(paramType)
                    val decodeCall = if (paramSerializer != null) {
                        "AppCbor.decodeFromByteArray($paramSerializer as KSerializer<Any>, args) as $paramType"
                    } else {
                        "AppCbor.decodeFromByteArray<$paramType>(args)"
                    }
                    val returnType = func.returnType?.toTypeString() ?: "Unit"
                    val returnSerializer = getSerializerForType(returnType)
                    if (returnSerializer != null) {
                        out.writeLine("            AppCbor.encodeToByteArray($returnSerializer as KSerializer<Any>, s.$funcName($decodeCall) as Any)")
                    } else {
                        out.writeLine("            AppCbor.encodeToByteArray(s.$funcName($decodeCall))")
                    }
                }
                else -> {
                    val argsClassName = "${name}${funcName.replaceFirstChar { it.uppercase() }}Args"
                    out.writeLine("            val a = AppCbor.decodeFromByteArray<$argsClassName>(args)")
                    val callArgs = func.parameters.mapIndexed { index, param ->
                        "a.${param.name?.asString() ?: "arg$index"}"
                    }.joinToString(", ")
                    val returnType = func.returnType?.toTypeString() ?: "Unit"
                    val returnSerializer = getSerializerForType(returnType)
                    if (returnSerializer != null) {
                        out.writeLine("            AppCbor.encodeToByteArray($returnSerializer as KSerializer<Any>, s.$funcName($callArgs) as Any)")
                    } else {
                        out.writeLine("            AppCbor.encodeToByteArray(s.$funcName($callArgs))")
                    }
                }
            }
            out.writeLine("        }")
        }
        out.writeLine($$"        else -> throw IllegalArgumentException(\"Unknown method: $method\")")
        out.writeLine("    }")
        out.writeLine("}")

        out.writeLine("")
        out.writeLine("@Suppress(\"UNCHECKED_CAST\")")
        out.writeLine("private fun subscribe$name(scope: CoroutineScope, manager: BaseRpcServiceManager, method: String, args: ByteArray, onEach: (ByteArray) -> Unit): Job {")
        out.writeLine("    val s = manager.getService<$name>()")
        out.writeLine("    return scope.launch {")
        out.writeLine("        val flow = when (method) {")
        allFunctions.filter { !it.modifiers.contains(Modifier.SUSPEND) && it.returnType?.resolve()?.declaration?.simpleName?.asString() == "Flow" }.forEach { func ->
            val funcName = func.simpleName.asString()
            out.writeLine("            \"$funcName\" -> {")
            when {
                func.parameters.isEmpty() -> out.writeLine("                s.$funcName()")
                func.parameters.size == 1 -> {
                    val paramType = func.parameters[0].type.toTypeString()
                    val paramSerializer = getSerializerForType(paramType)
                    val decodeCall = if (paramSerializer != null) {
                        "AppCbor.decodeFromByteArray($paramSerializer as KSerializer<Any>, args) as $paramType"
                    } else {
                        "AppCbor.decodeFromByteArray<$paramType>(args)"
                    }
                    out.writeLine("                s.$funcName($decodeCall)")
                }
                else -> {
                    val argsClassName = "${name}${funcName.replaceFirstChar { it.uppercase() }}Args"
                    out.writeLine("                val a = AppCbor.decodeFromByteArray<$argsClassName>(args)")
                    val callArgs = func.parameters.mapIndexed { index, param -> "a.${param.name?.asString() ?: "arg$index"}" }.joinToString(", ")
                    out.writeLine("                s.$funcName($callArgs)")
                }
            }
            out.writeLine("            }")
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
    }

    private fun generateRustBridge(resolver: Resolver, symbols: List<KSClassDeclaration>, dependencies: Dependencies) {
        val file = codeGenerator.createNewFile(
            dependencies,
            "dev.dertyp.rpc",
            "RustBridge",
            "rs"
        )

        val modelsToGenerate = mutableSetOf<KSClassDeclaration>()

        file.use { out ->
            out.writeLine("use serde::{Deserialize, Serialize};")
            out.writeLine("use std::ffi::{CStr, CString};")
            out.writeLine("use std::os::raw::{c_char, c_int, c_void};")
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
            out.writeLine("#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)] #[serde(transparent)] pub struct PlatformUUID(pub Vec<u8>);")
            out.writeLine("#[derive(Serialize, Deserialize, Debug, Clone, PartialEq)] #[serde(transparent)] pub struct PlatformDate(pub i64);")
            out.writeLine("")

            // Collect and generate models
            symbols.forEach { symbol ->
                symbol.getAllFunctions().filter { it.isPublic() && it.simpleName.asString() != "<init>" }.forEach { func ->
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

            // Traits and Implementation
            out.writeLine("#[async_trait::async_trait]")
            out.writeLine("pub trait RpcService {")
            symbols.forEach { symbol ->
                val name = symbol.simpleName.asString()
                out.writeLine("    fn ${toSnakeCase(name)}(&self) -> Box<dyn $name + Send + Sync>;")
            }
            out.writeLine("}")
            out.writeLine("")

            symbols.forEach { symbol ->
                val name = symbol.simpleName.asString()
                out.writeLine("#[async_trait::async_trait]")
                out.writeLine("pub trait $name {")
                symbol.getAllFunctions().filter { it.isPublic() && it.simpleName.asString() != "<init>" }.forEach { func ->
                    val fName = toSnakeCase(func.simpleName.asString())
                    val params = func.parameters.joinToString(", ") { "${toSnakeCase(it.name?.asString() ?: "arg")}: ${toRustType(it.type.resolve())}" }
                    val ret = toRustType(func.returnType?.resolve() ?: resolver.builtIns.unitType)
                    out.writeLine("    async fn $fName(&self, $params) -> Result<$ret, String>;")
                }
                out.writeLine("}")
                out.writeLine("")
            }

            // Client implementation
            out.writeLine("pub struct RpcClient { manager: *mut NativeRpcManager }")
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
            out.writeLine("}")

            symbols.forEach { symbol ->
                val name = symbol.simpleName.asString()
                out.writeLine("#[async_trait::async_trait]")
                out.writeLine("impl $name for RpcClient {")
                symbol.getAllFunctions().filter { it.isPublic() && it.simpleName.asString() != "<init>" }.forEach { func ->
                    val fName = toSnakeCase(func.simpleName.asString())
                    val params = func.parameters.joinToString(", ") { "${toSnakeCase(it.name?.asString() ?: "arg")}: ${toRustType(it.type.resolve())}" }
                    val paramNames = func.parameters.joinToString(", ") { toSnakeCase(it.name?.asString() ?: "arg") }
                    val ret = toRustType(func.returnType?.resolve() ?: resolver.builtIns.unitType)
                    out.writeLine("    async fn $fName(&self, $params) -> Result<$ret, String> {")
                    if (func.parameters.isEmpty()) out.writeLine("        self.call(\"$name\", \"${func.simpleName.asString()}\", &()).await")
                    else if (func.parameters.size == 1) out.writeLine("        self.call(\"$name\", \"${func.simpleName.asString()}\", &$paramNames).await")
                    else {
                        val argsClassName = "${name}${func.simpleName.asString().replaceFirstChar { it.uppercase() }}Args"
                        val argsFields = func.parameters.joinToString(", ") { toSnakeCase(it.name?.asString() ?: "arg") }
                        out.writeLine("        let args = $argsClassName { $argsFields };")
                        out.writeLine("        self.call(\"$name\", \"${func.simpleName.asString()}\", &args).await")
                    }
                    out.writeLine("    }")
                }
                out.writeLine("}")
            }
        }
    }

    private fun collectModels(type: KSType, set: MutableSet<KSClassDeclaration>) {
        val decl = type.declaration
        if (decl is KSClassDeclaration && decl.classKind == ClassKind.CLASS && !decl.qualifiedName?.asString()?.startsWith("kotlin.")!!) {
            if (decl.qualifiedName?.asString() == "dev.dertyp.PlatformUUID") return
            if (set.contains(decl)) return
            set.add(decl)
            decl.getAllProperties().forEach { collectModels(it.type.resolve(), set) }
        }
        type.arguments.forEach { argument -> argument.type?.resolve()?.let { collectModels(it, set) } }
    }

    private fun generateRustStruct(out: OutputStream, decl: KSClassDeclaration, generated: MutableSet<String>) {
        val qName = decl.qualifiedName?.asString() ?: return
        if (generated.contains(qName)) return
        generated.add(qName)
        
        val typeParams = if (decl.typeParameters.isNotEmpty()) "<" + decl.typeParameters.joinToString(", ") { it.name.asString() } + ">" else ""
        out.writeLine("#[derive(Serialize, Deserialize, Debug, Clone)]")
        out.writeLine("pub struct ${decl.simpleName.asString()}$typeParams {")
        decl.getAllProperties().forEach { prop ->
            val name = toSnakeCase(prop.simpleName.asString())
            val type = toRustType(prop.type.resolve())
            if (name != prop.simpleName.asString()) out.writeLine("    #[serde(rename = \"${prop.simpleName.asString()}\")]")
            out.writeLine("    pub $name: $type,")
        }
        out.writeLine("}")
        out.writeLine("")
    }

    private fun toRustType(type: KSType): String {
        val qName = type.declaration.qualifiedName?.asString()
        val base = when (qName) {
            "kotlin.String" -> "String"
            "kotlin.Int" -> "i32"
            "kotlin.Long" -> "i64"
            "kotlin.Boolean" -> "bool"
            "kotlin.ByteArray" -> "Vec<u8>"
            "dev.dertyp.PlatformUUID" -> "PlatformUUID"
            "dev.dertyp.PlatformDate", "dev.dertyp.PlatformInstant" -> "PlatformDate"
            "kotlin.collections.List", "kotlin.collections.Collection", "kotlin.collections.Set" -> {
                val arg = type.arguments.firstOrNull()?.type?.resolve()
                "Vec<${arg?.let { toRustType(it) } ?: "serde_json::Value"}>"
            }
            "dev.dertyp.data.PaginatedResponse" -> {
                val arg = type.arguments.firstOrNull()?.type?.resolve()
                "PaginatedResponse<${arg?.let { toRustType(it) } ?: "serde_json::Value"}>"
            }
            else -> type.declaration.simpleName.asString()
        }
        return if (type.isMarkedNullable) "Option<$base>" else base
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
