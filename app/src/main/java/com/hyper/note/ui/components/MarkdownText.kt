package com.hyper.note.ui.components

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val len = markdown.length

        while (currentIndex < len) {
            val char = markdown[currentIndex]
            val remaining = markdown.substring(currentIndex)

            when {
                // Header ## 
                remaining.startsWith("## ") -> {
                    val endLine = remaining.indexOf('\n').takeIf { it != -1 } ?: remaining.length
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp))
                    append(remaining.substring(3, endLine))
                    pop()
                    currentIndex += endLine
                }
                // Header # 
                remaining.startsWith("# ") -> {
                    val endLine = remaining.indexOf('\n').takeIf { it != -1 } ?: remaining.length
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp))
                    append(remaining.substring(2, endLine))
                    pop()
                    currentIndex += endLine
                }
                // Bold **
                remaining.startsWith("**") && remaining.indexOf("**", 2) != -1 -> {
                    val endIdx = remaining.indexOf("**", 2)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(remaining.substring(2, endIdx))
                    pop()
                    currentIndex += endIdx + 2
                }
                // Italic *
                remaining.startsWith("*") && !remaining.startsWith("**") && remaining.indexOf("*", 1) != -1 -> {
                    val endIdx = remaining.indexOf("*", 1)
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(remaining.substring(1, endIdx))
                    pop()
                    currentIndex += endIdx + 1
                }
                // Inline Code `
                remaining.startsWith("`") && remaining.indexOf("`", 1) != -1 -> {
                    val endIdx = remaining.indexOf("`", 1)
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x22FFFFFF)))
                    append(remaining.substring(1, endIdx))
                    pop()
                    currentIndex += endIdx + 1
                }
                else -> {
                    append(char.toString())
                    currentIndex++
                }
            }
        }
    }

    SelectionContainer {
        Text(
            text = annotatedString,
            modifier = modifier,
            color = color,
            style = LocalTextStyle.current
        )
    }
}
