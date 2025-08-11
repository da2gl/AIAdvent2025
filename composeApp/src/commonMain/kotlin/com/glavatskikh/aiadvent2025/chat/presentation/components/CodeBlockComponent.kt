package com.glavatskikh.aiadvent2025.chat.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeBlockComponent(
    code: String,
    language: String?,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val syntaxTheme = rememberSyntaxTheme()
    var showCopiedTooltip by remember { mutableStateOf(false) }
    
    val highlightedCode = remember(code, language) {
        CodeHighlighter.highlight(code, language, syntaxTheme)
    }
    
    Card(
        modifier = modifier.padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = syntaxTheme.background
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Header with language and copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language?.uppercase() ?: "CODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Box {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                            showCopiedTooltip = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (showCopiedTooltip) {
                        LaunchedEffect(showCopiedTooltip) {
                            delay(1500)
                            showCopiedTooltip = false
                        }
                        
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Text("Copied!")
                                }
                            },
                            state = rememberTooltipState(initialIsVisible = true)
                        ) {}
                    }
                }
            }
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            
            // Code content with horizontal scrolling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(
                    text = highlightedCode,
                    style = style.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = style.fontSize * 0.9f
                    ),
                    color = syntaxTheme.plain,
                    modifier = Modifier.padding(12.dp),
                    softWrap = false
                )
            }
        }
    }
}