package dev.dertyp.rpc.rest

enum class RuleMatch { PREFIX_WORD, INFIX }

data class NameRule(
    val token: String,
    val match: RuleMatch,
    val method: String,
    val stripPrefix: Boolean
)

object RestRules {

    const val DEFAULT_METHOD = "POST"

    val nameRules: List<NameRule> = listOf(
        prefix("by", "GET"),
        prefix("all", "GET"),
        prefix("liked", "GET"),
        prefix("stream", "GET"),
        prefix("import", "GET"),
        prefix("download", "GET"),
        prefix("get", "GET", stripPrefix = true),
        prefix("list", "GET", stripPrefix = true),
        prefix("find", "GET", stripPrefix = true),
        prefix("fetch", "GET", stripPrefix = true),
        prefix("search", "GET", stripPrefix = true),
        prefix("ranked", "GET", stripPrefix = true),
        prefix("exists", "GET"),
        NameRule("Exists", RuleMatch.INFIX, "GET", stripPrefix = false),
        prefix("add", "POST"),
        prefix("post", "POST", stripPrefix = true),
        prefix("create", "POST", stripPrefix = true),
        prefix("put", "PUT", stripPrefix = true),
        prefix("set", "PUT", stripPrefix = true),
        prefix("update", "PUT", stripPrefix = true),
        prefix("delete", "DELETE", stripPrefix = true),
        prefix("remove", "DELETE", stripPrefix = true)
    )

    private fun prefix(token: String, method: String, stripPrefix: Boolean = false) =
        NameRule(token, RuleMatch.PREFIX_WORD, method, stripPrefix)
}
