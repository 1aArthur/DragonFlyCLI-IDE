package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*
import java.util.regex.Pattern

import com.example.utils.performance.NativePerformanceEngine

object SyntaxHighlighter {
    private val KEYWORDS = listOf(
        "fun", "val", "var", "class", "interface", "object", "return", "if", "else",
        "when", "for", "while", "import", "package", "try", "catch", "throw", "null",
        "true", "false", "public", "private", "protected", "override", "suspend",
        "def", "from", "as", "async", "await", "function", "const", "let", "export",
        "fn", "pub", "struct", "enum", "impl", "use", "trait", "match", "select",
        "from", "where", "insert", "update", "delete", "create", "table", "echo",
        "sudo", "apt", "pkg", "git", "chmod", "cd", "ls", "mkdir", "rm", "cat"
    )

    private val TYPES = listOf(
        "String", "Int", "Float", "Double", "Boolean", "Long", "Byte", "Short",
        "List", "Map", "Set", "Flow", "StateFlow", "MutableStateFlow", "State",
        "Modifier", "Composable", "Context", "Intent", "Unit", "Any", "Nothing"
    )

    private val KEYWORD_PATTERN = Pattern.compile("\\b(" + KEYWORDS.joinToString("|") + ")\\b")
    private val TYPE_PATTERN = Pattern.compile("\\b(" + TYPES.joinToString("|") + ")\\b")
    private val ANNOTATION_PATTERN = Pattern.compile("@[A-Za-z0-9_]+")
    private val STRING_PATTERN = Pattern.compile("\".*?\"|'.*?'|`.*?`")
    private val COMMENT_PATTERN = Pattern.compile("//.*|#.*|/\\*.*?\\*/|<!--.*?-->")
    private val NUMBER_PATTERN = Pattern.compile("\\b(0x[0-9a-fA-F]+|\\d+(\\.\\d+)?)\\b")
    private val FUNCTION_CALL_PATTERN = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*(?=\\s*\\()")

    fun highlight(code: String, language: String = "auto"): AnnotatedString {
        return NativePerformanceEngine.getOrComputeSyntax(code, language) {
            buildAnnotatedString {
                append(code)
                val length = code.length

                // 1. Comments
                val commentMatcher = COMMENT_PATTERN.matcher(code)
                while (commentMatcher.find()) {
                    val start = commentMatcher.start()
                    val end = commentMatcher.end()
                    if (start < length && end <= length) {
                        addStyle(SpanStyle(color = TextMuted), start, end)
                    }
                }

                // 2. Keywords
                val keywordMatcher = KEYWORD_PATTERN.matcher(code)
                while (keywordMatcher.find()) {
                    val start = keywordMatcher.start()
                    val end = keywordMatcher.end()
                    if (start < length && end <= length) {
                        addStyle(SpanStyle(color = GlowCyan, fontWeight = FontWeight.Bold), start, end)
                    }
                }

                // 3. Types
                val typeMatcher = TYPE_PATTERN.matcher(code)
                while (typeMatcher.find()) {
                    val start = typeMatcher.start()
                    val end = typeMatcher.end()
                    if (start < length && end <= length) {
                        addStyle(SpanStyle(color = CyberPurple, fontWeight = FontWeight.Medium), start, end)
                    }
                }

                // 4. Annotations
                val annotationMatcher = ANNOTATION_PATTERN.matcher(code)
                while (annotationMatcher.find()) {
                    val start = annotationMatcher.start()
                    val end = annotationMatcher.end()
                    if (start < length && end <= length) {
                        addStyle(SpanStyle(color = ElectricBlue, fontWeight = FontWeight.Bold), start, end)
                    }
                }

                // 5. Function Calls
                val fnMatcher = FUNCTION_CALL_PATTERN.matcher(code)
                while (fnMatcher.find()) {
                    val start = fnMatcher.start()
                    val end = fnMatcher.end()
                    if (start < length && end <= length) {
                        addStyle(SpanStyle(color = Color(0xFFFFD700)), start, end)
                    }
                }

                // 6. Strings
                val stringMatcher = STRING_PATTERN.matcher(code)
                while (stringMatcher.find()) {
                    val start = stringMatcher.start()
                    val end = stringMatcher.end()
                    if (start < length && end <= length) {
                        addStyle(SpanStyle(color = TerminalGreen), start, end)
                    }
                }

                // 7. Numbers
                val numberMatcher = NUMBER_PATTERN.matcher(code)
                while (numberMatcher.find()) {
                    val start = numberMatcher.start()
                    val end = numberMatcher.end()
                    if (start < length && end <= length) {
                        addStyle(SpanStyle(color = TerminalYellow), start, end)
                    }
                }
            }
        }
    }
}
