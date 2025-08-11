@file:Suppress("RegExpRedundantEscape")

package com.glavatskikh.aiadvent2025.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- Block model for parsed Markdown ----------
private sealed interface Block {
    data class Heading(val level: Int, val content: String) : Block
    data class CodeBlock(val lang: String?, val code: String) : Block
    data class Quote(val content: String) : Block
    data class BulletedList(val items: List<String>) : Block
    data class OrderedList(val items: List<String>) : Block
    data class Paragraph(val content: String) : Block
}

// ---------- Inline style configuration ----------
data class MarkdownInlineStyle(
    val linkColor: Color,
    val inlineCodeBackground: Color,
    val inlineCodeFont: FontFamily = FontFamily.Monospace,
    val linkFontWeight: FontWeight = FontWeight.SemiBold,
    val inlineCodeFontWeight: FontWeight = FontWeight.Medium
)

// Provide inline style values from the current theme (called in a Composable context)
@Composable
fun rememberMarkdownInlineStyle(): MarkdownInlineStyle {
    val scheme = MaterialTheme.colorScheme
    return MarkdownInlineStyle(
        linkColor = scheme.primary,
        inlineCodeBackground = scheme.surfaceVariant
    )
}

// ---------- Markdown block parser ----------
// Supports headings, fenced code blocks, blockquotes, bulleted lists, ordered lists, and paragraphs
private fun parseMarkdownToBlocks(input: String): List<Block> {
    val lines = input.replace("\r\n", "\n").split("\n")
    val blocks = mutableListOf<Block>()

    var i = 0
    while (i < lines.size) {
        val line = lines[i]

        // Fenced code block ```lang
        if (line.trim().startsWith("```")) {
            val fence = line.trim()
            val lang = fence.removePrefix("```").trim().ifEmpty { null }
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines += lines[i]
                i++
            }
            blocks += Block.CodeBlock(lang, codeLines.joinToString("\n"))
            // Skip the closing ```
            if (i < lines.size) i++
            continue
        }
        
        // Auto-detect XML/HTML content
        if (line.trim().startsWith("<?xml") || line.trim().startsWith("<html") || 
            (line.trim().startsWith("<") && line.trim().endsWith(">") && 
             (line.contains("=\"") || line.contains("='/")))) {
            val xmlLines = mutableListOf<String>()
            // Collect all XML lines until we hit a non-XML line
            while (i < lines.size) {
                val currentLine = lines[i].trim()
                if (currentLine.isEmpty() && xmlLines.isNotEmpty() && 
                    lines.getOrNull(i + 1)?.trim()?.startsWith("<") != true) {
                    break
                }
                if (currentLine.isNotEmpty() || (xmlLines.isNotEmpty() && i + 1 < lines.size && 
                    lines[i + 1].trim().startsWith("<"))) {
                    xmlLines += lines[i]
                }
                i++
                // Check if we've reached the end of XML content
                if (currentLine.endsWith("</response>") || currentLine.endsWith("</html>") ||
                    currentLine.endsWith("</root>")) {
                    break
                }
            }
            if (xmlLines.isNotEmpty()) {
                blocks += Block.CodeBlock("xml", xmlLines.joinToString("\n"))
            }
            continue
        }

        // Heading (#, ##, ###...)
        val headingMatch = Regex("""^(#{1,6})\s+(.*)$""").matchEntire(line)
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val content = headingMatch.groupValues[2]
            blocks += Block.Heading(level, content)
            i++
            continue
        }

        // Quote block (> ...)
        if (line.trim().startsWith(">")) {
            val quoteLines = mutableListOf(line.trim().removePrefix(">").trim())
            i++
            while (i < lines.size && lines[i].trim().startsWith(">")) {
                quoteLines += lines[i].trim().removePrefix(">").trim()
                i++
            }
            blocks += Block.Quote(quoteLines.joinToString("\n"))
            continue
        }

        // Bulleted list
        if (line.trim().matches(Regex("""^[-*]\s+.+$"""))) {
            val items = mutableListOf(line.trim().replace(Regex("""^[-*]\s+"""), ""))
            i++
            while (i < lines.size && lines[i].trim().matches(Regex("""^[-*]\s+.+$"""))) {
                items += lines[i].trim().replace(Regex("""^[-*]\s+"""), "")
                i++
            }
            blocks += Block.BulletedList(items)
            continue
        }

        // Ordered list
        if (line.trim().matches(Regex("""^\d+\.\s+.+$"""))) {
            val items = mutableListOf(line.trim().replace(Regex("""^\d+\.\s+"""), ""))
            i++
            while (i < lines.size && lines[i].trim().matches(Regex("""^\d+\.\s+.+$"""))) {
                items += lines[i].trim().replace(Regex("""^\d+\.\s+"""), "")
                i++
            }
            blocks += Block.OrderedList(items)
            continue
        }

        // Paragraph: combine consecutive lines until a blank line or another block type
        if (line.isNotBlank()) {
            val para = mutableListOf(line)
            i++
            while (i < lines.size && lines[i].isNotBlank()
                && !lines[i].trim().startsWith("```")
                && !lines[i].trim().startsWith("#")
                && !lines[i].trim().startsWith(">")
                && !lines[i].trim().matches(Regex("""^[-*]\s+.+$"""))
                && !lines[i].trim().matches(Regex("""^\d+\.\s+.+$"""))
            ) {
                para += lines[i]
                i++
            }
            blocks += Block.Paragraph(para.joinToString("\n"))
            continue
        }

        // Skip empty lines
        i++
    }
    return blocks
}

// ---------- Inline annotation builder ----------
// Processes links, inline code, bold, and italic markers
private fun buildInlineAnnotated(
    text: String,
    base: TextStyle,
    inlineStyle: MarkdownInlineStyle
): AnnotatedString {
    val linkRegex = Regex("""\[(.+?)\]\((https?://[^\s)]+)\)""")
    val codeRegex = Regex("""`([^`]+)`""")
    val boldRegex = Regex("""\*\*(.+?)\*\*""")
    val italicRegex = Regex("""(?<!\*)\*(?!\s)(.+?)(?<!\s)\*(?!\*)""") // Avoid list markers

    var work = text
    val builder = AnnotatedString.Builder()

    // Links (process first)
    work = linkRegex.replace(work) { m ->
        val start = builder.length
        builder.append(m.groupValues[1])
        builder.addStyle(
            SpanStyle(color = inlineStyle.linkColor, fontWeight = inlineStyle.linkFontWeight),
            start,
            builder.length
        )
        builder.addStringAnnotation("URL", m.groupValues[2], start, builder.length)
        ""
    }

    // Inline code
    work = codeRegex.replace(work) { m ->
        val start = builder.length
        builder.append(m.groupValues[1])
        builder.addStyle(
            SpanStyle(
                fontFamily = inlineStyle.inlineCodeFont,
                background = inlineStyle.inlineCodeBackground,
                fontWeight = inlineStyle.inlineCodeFontWeight
            ),
            start,
            builder.length
        )
        ""
    }

    // Bold
    work = boldRegex.replace(work) { m ->
        val start = builder.length
        builder.append(m.groupValues[1])
        builder.addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, builder.length)
        ""
    }

    // Italic
    work = italicRegex.replace(work) { m ->
        val start = builder.length
        builder.append(m.groupValues[1])
        builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, builder.length)
        ""
    }

    // Append remaining plain text
    builder.append(work)

    return builder.toAnnotatedString()
}

// ---------- Main Composable ----------
@Composable
fun ChatMarkdownText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val blocks = remember(text) { parseMarkdownToBlocks(text) }
    val inlineStyle = rememberMarkdownInlineStyle()
    val uriHandler = LocalUriHandler.current

    Column(modifier = modifier) {
        blocks.forEach { block ->
            when (block) {
                is Block.Heading -> {
                    val hStyle = when (block.level) {
                        1 -> style.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = style.fontSize * 1.6f
                        )

                        2 -> style.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = style.fontSize * 1.4f
                        )

                        3 -> style.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = style.fontSize * 1.2f
                        )

                        else -> style.copy(fontWeight = FontWeight.Medium)
                    }
                    Text(
                        text = buildInlineAnnotated(block.content, hStyle, inlineStyle),
                        style = hStyle,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                is Block.CodeBlock -> {
                    CodeBlockComponent(
                        code = block.code,
                        language = block.lang,
                        style = style,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is Block.Quote -> {
                    Text(
                        text = buildInlineAnnotated(block.content, style, inlineStyle),
                        style = style.copy(fontStyle = FontStyle.Italic),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(start = 10.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
                    )
                }

                is Block.BulletedList -> {
                    block.items.forEach { item ->
                        val para = buildAnnotatedString {
                            withStyle(
                                ParagraphStyle(
                                    textIndent = TextIndent(restLine = 16.sp, firstLine = 0.sp)
                                )
                            ) {
                                append("• ")
                                append(buildInlineAnnotated(item, style, inlineStyle))
                            }
                        }
                        Text(
                            text = para,
                            style = style,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                is Block.OrderedList -> {
                    block.items.forEachIndexed { idx, item ->
                        val number = "${idx + 1}. "
                        val para = buildAnnotatedString {
                            withStyle(
                                ParagraphStyle(
                                    textIndent = TextIndent(restLine = (number.length * 8).sp)
                                )
                            ) {
                                append(number)
                                append(buildInlineAnnotated(item, style, inlineStyle))
                            }
                        }
                        Text(
                            text = para,
                            style = style,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                is Block.Paragraph -> {
                    val annotated = remember(block.content) {
                        buildInlineAnnotated(block.content, style, inlineStyle)
                    }
                    Text(
                        text = annotated,
                        style = style,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable(
                                enabled = annotated.getStringAnnotations("URL", 0, annotated.length)
                                    .isNotEmpty()
                            ) {
                                annotated.getStringAnnotations("URL", 0, annotated.length)
                                    .firstOrNull()?.let { uriHandler.openUri(it.item) }
                            }
                    )
                }
            }
        }
    }
}
