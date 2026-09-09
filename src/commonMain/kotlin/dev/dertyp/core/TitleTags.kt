@file:JvmName("CommonTitleTags")
@file:Suppress("unused")

package dev.dertyp.core

import dev.dertyp.data.BaseSong
import dev.dertyp.data.InsertableSong
import dev.dertyp.data.Song
import dev.dertyp.data.TitleTag
import dev.dertyp.data.TitleTagKind
import kotlin.jvm.JvmName

data class SplitTitle(val title: String, val tags: List<TitleTag>)

private val bracketSegmentRegex = Regex("""\s*[(\[]([^()\[\]]*)[)\]]""")
private val dashSegmentRegex = Regex("""\s+-\s+([^-()\[\]]+?)\s*$""")
private val trailingCreditRegex = Regex("""\s+\b((?:feat|ft|featuring|prod)\.?\s+.+)$""", RegexOption.IGNORE_CASE)
private val explicitOrCleanRegex = Regex("""^(explicit|clean)$""", RegexOption.IGNORE_CASE)

private class TagRule(val kind: TitleTagKind, vararg patterns: String) {
    val regexes = patterns.map { Regex(it, RegexOption.IGNORE_CASE) }

    fun matches(segment: String): Boolean = regexes.any { it.containsMatchIn(segment) }
}

private const val MIX_QUALIFIERS =
    """extended|club|original|radio|dub|vocal|dance|main|short|long|full|album|single|alternate|alternative|festival|piano|chill|deep|dirty|tv|7"|12""""

private val tagRules = listOf(
    TagRule(TitleTagKind.FEAT, """^(feat|ft|featuring|with)\.?\s+\S"""),
    TagRule(TitleTagKind.PROD, """^prod\.?\s+\S"""),
    TagRule(TitleTagKind.REMIX, """\b(remix|remixed|rework|bootleg)$""", """^remix\b""", """\b(vip|flip)( (mix|edit))?$"""),
    TagRule(TitleTagKind.INSTRUMENTAL, """^instrumental$""", """\binstrumental( (version|mix))?$"""),
    TagRule(TitleTagKind.ACOUSTIC, """^(acoustic|unplugged)$""", """\b(acoustic|unplugged)( version)?$""", """^mtv unplugged\b"""),
    TagRule(TitleTagKind.MIX, """\b($MIX_QUALIFIERS)\s+mix$"""),
    TagRule(TitleTagKind.LIVE, """^live$""", """^live (at|from|in|on|version|\d{4})\b""", """\slive$"""),
    TagRule(TitleTagKind.COVER, """^cover$""", """\scover$""", """^cover (version|by)\b"""),
    TagRule(TitleTagKind.REMASTER, """^(\d{4} )?remaster(ed)?( \d{4})?( version)?$"""),
    TagRule(TitleTagKind.DEMO, """^(\d{4} )?demo( (version|take))?$"""),
    TagRule(TitleTagKind.EDIT, """\bedit$"""),
    TagRule(
        TitleTagKind.VERSION,
        """\bversion$""",
        """^take \d+$""",
        """^sped[ -]?up$""",
        """^slowed( down| \+ reverb)?$""",
        """^bonus track$""",
        """^deluxe( edition)?$""",
    ),
)

fun classifyTitleTag(segment: String): TitleTagKind? {
    val trimmed = segment.trim()
    if (trimmed.isEmpty()) return null
    return tagRules.firstOrNull { it.matches(trimmed) }?.kind
}

private fun isExplicitOrClean(segment: String): Boolean = explicitOrCleanRegex.matches(segment.trim())

fun String.splitTitleTags(): SplitTitle {
    var current = trim()
    val tags = mutableListOf<TitleTag>()

    loop@ while (true) {
        for (match in bracketSegmentRegex.findAll(current)) {
            val content = match.groupValues[1].trim()
            val strippedOnly = isExplicitOrClean(content)
            val kind = if (strippedOnly) null else classifyTitleTag(content)
            if (!strippedOnly && kind == null) continue
            current = current.removeRange(match.range).trim()
            if (kind != null) tags += TitleTag(kind, content)
            continue@loop
        }

        val dashMatch = dashSegmentRegex.find(current)
        if (dashMatch != null) {
            val content = dashMatch.groupValues[1].trim()
            val strippedOnly = isExplicitOrClean(content)
            val kind = if (strippedOnly) null else classifyTitleTag(content)
            if (strippedOnly || kind != null) {
                current = current.removeRange(dashMatch.range).trim()
                if (kind != null) tags += TitleTag(kind, content)
                continue@loop
            }
        }

        val creditMatch = trailingCreditRegex.find(current)
        if (creditMatch != null) {
            val content = creditMatch.groupValues[1].trim()
            val kind = if (content.startsWith("prod", ignoreCase = true)) TitleTagKind.PROD else TitleTagKind.FEAT
            current = current.removeRange(creditMatch.range).trim()
            tags += TitleTag(kind, content)
            continue@loop
        }

        break
    }

    if (current.isBlank()) return SplitTitle(trim(), emptyList())
    return SplitTitle(current, tags.distinctBy { it.kind to it.label.lowercase() })
}

fun String.withTitleTags(tags: List<TitleTag>): String = tags.fold(this) { acc, tag -> "$acc (${tag.label})" }

val BaseSong.fullTitle: String
    get() = title.withTitleTags(tags)

fun List<TitleTag>.mergeTitleTags(other: List<TitleTag>): List<TitleTag> =
    (this + other).distinctBy { it.kind to it.label.lowercase() }

fun InsertableSong.withSplitTitleTags(): InsertableSong {
    val split = title.splitTitleTags()
    if (split.title == title && split.tags.isEmpty()) return this
    return copy(title = split.title, tags = tags.mergeTitleTags(split.tags))
}

fun Song.withSplitTitleTags(): Song {
    val split = title.splitTitleTags()
    if (split.title == title && split.tags.isEmpty()) return this
    return copy(title = split.title, tags = tags.mergeTitleTags(split.tags))
}
