package com.glavatskikh.aiadvent2025.chat.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

data class SyntaxTheme(
    val keyword: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val function: Color,
    val type: Color,
    val property: Color,
    val operator: Color,
    val punctuation: Color,
    val background: Color,
    val plain: Color
)

@Composable
fun rememberSyntaxTheme(): SyntaxTheme {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    return if (isDark) {
        SyntaxTheme(
            keyword = Color(0xFF569CD6),       // Blue
            string = Color(0xFFCE9178),        // Orange
            number = Color(0xFFB5CEA8),        // Light green
            comment = Color(0xFF6A9955),       // Green
            function = Color(0xFFDCDCAA),      // Yellow
            type = Color(0xFF4EC9B0),          // Cyan
            property = Color(0xFF9CDCFE),      // Light blue
            operator = Color(0xFFD4D4D4),      // Light gray
            punctuation = Color(0xFFD4D4D4),   // Light gray
            background = MaterialTheme.colorScheme.surfaceVariant,
            plain = MaterialTheme.colorScheme.onSurface
        )
    } else {
        SyntaxTheme(
            keyword = Color(0xFF0000FF),       // Blue
            string = Color(0xFFA31515),        // Red
            number = Color(0xFF098658),        // Green
            comment = Color(0xFF008000),       // Dark green
            function = Color(0xFF795E26),      // Brown
            type = Color(0xFF267F99),          // Teal
            property = Color(0xFF001080),      // Dark blue
            operator = Color(0xFF000000),      // Black
            punctuation = Color(0xFF000000),   // Black
            background = MaterialTheme.colorScheme.surfaceVariant,
            plain = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun Color.luminance(): Float {
    val r = red
    val g = green
    val b = blue
    return 0.299f * r + 0.587f * g + 0.114f * b
}

object CodeHighlighter {

    fun highlight(
        code: String,
        language: String?,
        theme: SyntaxTheme
    ): AnnotatedString {
        return when (language?.lowercase()) {
            "json" -> highlightJson(code, theme)
            "xml", "html" -> highlightXml(code, theme)
            "kotlin", "kt" -> highlightKotlin(code, theme)
            "java" -> highlightJava(code, theme)
            "python", "py" -> highlightPython(code, theme)
            "javascript", "js", "typescript", "ts" -> highlightJavaScript(code, theme)
            "css", "scss", "sass" -> highlightCss(code, theme)
            "sql" -> highlightSql(code, theme)
            "yaml", "yml" -> highlightYaml(code, theme)
            "diff" -> highlightDiff(code, theme)
            else -> AnnotatedString(code)
        }
    }

    private fun highlightJson(code: String, theme: SyntaxTheme): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // String values
            val stringRegex = Regex("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"")
            stringRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.string), match.range.first, match.range.last + 1)
            }

            // Numbers
            val numberRegex = Regex("-?\\b\\d+(\\.\\d+)?\\b")
            numberRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.number), match.range.first, match.range.last + 1)
            }

            // Keywords (true, false, null)
            val keywordRegex = Regex("\\b(true|false|null)\\b")
            keywordRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.keyword), match.range.first, match.range.last + 1)
            }

            // Property names (keys in JSON)
            val propertyRegex = Regex("\"([^\"]+)\"\\s*:")
            propertyRegex.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = theme.property),
                    match.range.first,
                    match.range.first + match.groupValues[0].indexOf(':')
                )
            }
        }
    }

    private fun highlightXml(code: String, theme: SyntaxTheme): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Comments
            val commentRegex = Regex("<!--[\\s\\S]*?-->")
            commentRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.comment), match.range.first, match.range.last + 1)
            }

            // Tag names
            val tagNameRegex = Regex("</?([\\w:]+)")
            tagNameRegex.findAll(code).forEach { match ->
                val tagName = match.groupValues[1]
                val nameStart = match.range.first + 1 + (if (match.value.startsWith("</")) 1 else 0)
                addStyle(
                    SpanStyle(color = theme.type, fontWeight = FontWeight.SemiBold),
                    nameStart,
                    nameStart + tagName.length
                )
            }

            // Attribute names
            val attrNameRegex = Regex("\\s(\\w+)=")
            attrNameRegex.findAll(code).forEach { match ->
                val attrName = match.groupValues[1]
                val nameStart = match.range.first + 1
                addStyle(SpanStyle(color = theme.property), nameStart, nameStart + attrName.length)
            }

            // Attribute values
            val attrValueRegex = Regex("=(\"[^\"]*\"|'[^']*')")
            attrValueRegex.findAll(code).forEach { match ->
                val attrValue = match.groupValues[1]
                val valueStart = match.range.first + 1
                addStyle(SpanStyle(color = theme.string), valueStart, valueStart + attrValue.length)
            }

            // Tag brackets
            val bracketRegex = Regex("[<>]")
            bracketRegex.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = theme.punctuation),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }
    }

    private fun highlightKotlin(code: String, theme: SyntaxTheme): AnnotatedString {
        val keywords = setOf(
            "fun", "val", "var", "if", "else", "when", "for", "while", "do", "return",
            "class", "interface", "object", "package", "import", "data", "sealed", "enum",
            "companion", "const", "lateinit", "inline", "suspend", "override", "private",
            "public", "protected", "internal", "abstract", "final", "open", "annotation",
            "is", "as", "in", "out", "throw", "try", "catch", "finally", "null", "true", "false"
        )

        val types = setOf(
            "Int", "String", "Boolean", "Double", "Float", "Long", "Short", "Byte", "Char",
            "List", "Set", "Map", "Array", "Unit", "Any", "Nothing"
        )

        return buildAnnotatedString {
            append(code)

            // Comments
            val singleLineComment = Regex("//.*$", RegexOption.MULTILINE)
            singleLineComment.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.comment), match.range.first, match.range.last + 1)
            }

            val multiLineComment = Regex("/\\*[\\s\\S]*?\\*/")
            multiLineComment.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.comment), match.range.first, match.range.last + 1)
            }

            // Strings
            val stringRegex = Regex("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(\\\\.[^'\\\\]*)*'")
            stringRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.string), match.range.first, match.range.last + 1)
            }

            // Numbers
            val numberRegex = Regex("\\b\\d+(\\.\\d+)?[fFdDlL]?\\b")
            numberRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.number), match.range.first, match.range.last + 1)
            }

            // Keywords and types
            val wordRegex = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b")
            wordRegex.findAll(code).forEach { match ->
                val word = match.value
                when {
                    word in keywords -> {
                        addStyle(
                            SpanStyle(color = theme.keyword, fontWeight = FontWeight.SemiBold),
                            match.range.first,
                            match.range.last + 1
                        )
                    }

                    word in types -> {
                        addStyle(
                            SpanStyle(color = theme.type),
                            match.range.first,
                            match.range.last + 1
                        )
                    }

                    word[0].isUpperCase() -> {
                        addStyle(
                            SpanStyle(color = theme.type),
                            match.range.first,
                            match.range.last + 1
                        )
                    }
                }
            }

            // Function calls
            val functionRegex = Regex("\\b([a-z][a-zA-Z0-9_]*)\\s*\\(")
            functionRegex.findAll(code).forEach { match ->
                val functionName = match.groupValues[1]
                val nameStart = match.range.first
                addStyle(
                    SpanStyle(color = theme.function),
                    nameStart,
                    nameStart + functionName.length
                )
            }
        }
    }

    private fun highlightJava(code: String, theme: SyntaxTheme): AnnotatedString {
        // Similar to Kotlin but with Java keywords
        val keywords = setOf(
            "public", "private", "protected", "static", "final", "abstract", "synchronized",
            "volatile", "transient", "native", "strictfp", "class", "interface", "enum",
            "extends", "implements", "import", "package", "if", "else", "switch", "case",
            "default", "for", "while", "do", "break", "continue", "return", "throw", "throws",
            "try", "catch", "finally", "new", "this", "super", "void", "null", "true", "false"
        )

        return highlightWithKeywords(code, theme, keywords)
    }

    private fun highlightPython(code: String, theme: SyntaxTheme): AnnotatedString {
        val keywords = setOf(
            "def", "class", "if", "elif", "else", "for", "while", "return", "import", "from",
            "as", "try", "except", "finally", "with", "lambda", "pass", "break", "continue",
            "global", "nonlocal", "assert", "yield", "raise", "del", "and", "or", "not",
            "is", "in", "None", "True", "False", "self", "async", "await"
        )

        return highlightWithKeywords(code, theme, keywords)
    }

    private fun highlightJavaScript(code: String, theme: SyntaxTheme): AnnotatedString {
        val keywords = setOf(
            "function", "var", "let", "const", "if", "else", "for", "while", "do", "return",
            "class", "extends", "import", "export", "from", "async", "await", "try", "catch",
            "finally", "throw", "new", "this", "super", "null", "undefined", "true", "false",
            "typeof", "instanceof", "in", "of", "delete", "void", "yield", "break", "continue"
        )

        return highlightWithKeywords(code, theme, keywords)
    }

    private fun highlightCss(code: String, theme: SyntaxTheme): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Properties
            val propertyRegex = Regex("([a-z-]+)\\s*:", RegexOption.IGNORE_CASE)
            propertyRegex.findAll(code).forEach { match ->
                val propName = match.groupValues[1]
                val nameStart = code.indexOf(propName, match.range.first)
                if (nameStart >= 0) {
                    addStyle(
                        SpanStyle(color = theme.property),
                        nameStart,
                        nameStart + propName.length
                    )
                }
            }

            // Strings/Values in quotes
            val stringRegex = Regex("\"[^\"]*\"|'[^']*'")
            stringRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.string), match.range.first, match.range.last + 1)
            }

            // Comments
            val commentRegex = Regex("/\\*[\\s\\S]*?\\*/")
            commentRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.comment), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightSql(code: String, theme: SyntaxTheme): AnnotatedString {
        val keywords = setOf(
            "SELECT", "FROM", "WHERE", "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON",
            "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE", "CREATE", "TABLE",
            "ALTER", "DROP", "INDEX", "VIEW", "AS", "AND", "OR", "NOT", "NULL", "IS",
            "LIKE", "IN", "EXISTS", "BETWEEN", "ORDER", "BY", "GROUP", "HAVING", "LIMIT"
        )

        return buildAnnotatedString {
            append(code)

            // SQL keywords (case insensitive)
            keywords.forEach { keyword ->
                val regex = Regex("\\b${keyword}\\b", RegexOption.IGNORE_CASE)
                regex.findAll(code).forEach { match ->
                    addStyle(
                        SpanStyle(color = theme.keyword, fontWeight = FontWeight.Bold),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }

            // Strings
            val stringRegex = Regex("'[^']*'")
            stringRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.string), match.range.first, match.range.last + 1)
            }

            // Comments
            val commentRegex = Regex("--.*$", RegexOption.MULTILINE)
            commentRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.comment), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightYaml(code: String, theme: SyntaxTheme): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Keys
            val keyRegex = Regex("^\\s*([a-zA-Z_][a-zA-Z0-9_-]*)\\s*:", RegexOption.MULTILINE)
            keyRegex.findAll(code).forEach { match ->
                val key = match.groupValues[1]
                val keyStart = code.indexOf(key, match.range.first)
                if (keyStart >= 0) {
                    addStyle(
                        SpanStyle(color = theme.property, fontWeight = FontWeight.SemiBold),
                        keyStart,
                        keyStart + key.length
                    )
                }
            }

            // Strings
            val stringRegex = Regex("\"[^\"]*\"|'[^']*'")
            stringRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.string), match.range.first, match.range.last + 1)
            }

            // Comments
            val commentRegex = Regex("#.*$", RegexOption.MULTILINE)
            commentRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.comment), match.range.first, match.range.last + 1)
            }

            // Booleans
            val boolRegex = Regex("\\b(true|false|yes|no|on|off)\\b", RegexOption.IGNORE_CASE)
            boolRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.keyword), match.range.first, match.range.last + 1)
            }
        }
    }

    private fun highlightDiff(code: String, theme: SyntaxTheme): AnnotatedString {
        return buildAnnotatedString {
            code.lines().forEach { line ->
                when {
                    line.startsWith("+") && !line.startsWith("+++") -> {
                        appendLine()
                        addStyle(
                            SpanStyle(
                                color = Color(0xFF22863A),
                                background = Color(0xFFE6FFED)
                            ), length - line.length - 1, length - 1
                        )
                        append(line)
                    }

                    line.startsWith("-") && !line.startsWith("---") -> {
                        appendLine()
                        addStyle(
                            SpanStyle(
                                color = Color(0xFFCB2431),
                                background = Color(0xFFFFEBED)
                            ), length - line.length - 1, length - 1
                        )
                        append(line)
                    }

                    line.startsWith("@@") -> {
                        appendLine()
                        addStyle(
                            SpanStyle(color = theme.type, fontWeight = FontWeight.Bold),
                            length - line.length - 1,
                            length - 1
                        )
                        append(line)
                    }

                    else -> {
                        appendLine()
                        append(line)
                    }
                }
            }
        }
    }

    private fun highlightWithKeywords(
        code: String,
        theme: SyntaxTheme,
        keywords: Set<String>
    ): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            // Comments
            val singleLineComment = Regex("(//|#).*$", RegexOption.MULTILINE)
            singleLineComment.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.comment), match.range.first, match.range.last + 1)
            }

            // Strings
            val stringRegex = Regex("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(\\\\.[^'\\\\]*)*'")
            stringRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.string), match.range.first, match.range.last + 1)
            }

            // Numbers
            val numberRegex = Regex("\\b\\d+(\\.\\d+)?\\b")
            numberRegex.findAll(code).forEach { match ->
                addStyle(SpanStyle(color = theme.number), match.range.first, match.range.last + 1)
            }

            // Keywords
            keywords.forEach { keyword ->
                val regex = Regex("\\b$keyword\\b")
                regex.findAll(code).forEach { match ->
                    addStyle(
                        SpanStyle(color = theme.keyword, fontWeight = FontWeight.SemiBold),
                        match.range.first,
                        match.range.last + 1
                    )
                }
            }
        }
    }
}