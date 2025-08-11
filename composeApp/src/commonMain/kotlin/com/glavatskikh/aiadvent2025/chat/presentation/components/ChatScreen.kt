package com.glavatskikh.aiadvent2025.chat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.glavatskikh.aiadvent2025.chat.data.models.MessageRole
import com.glavatskikh.aiadvent2025.chat.presentation.ChatViewModel
import com.glavatskikh.aiadvent2025.chat.presentation.prompt.PromptEditorDialog
import com.glavatskikh.aiadvent2025.chat.presentation.prompt.PromptEditorViewModel
import com.glavatskikh.aiadvent2025.theme.LocalThemeManager
import com.glavatskikh.aiadvent2025.theme.ThemeMode
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel { ChatViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val promptEditorViewModel = viewModel<PromptEditorViewModel> {
        PromptEditorViewModel(viewModel.promptManager)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }
    
    if (uiState.isPromptEditorOpen) {
        LaunchedEffect(Unit) {
            promptEditorViewModel.openEditor()
        }
        PromptEditorDialog(
            viewModel = promptEditorViewModel,
            onDismiss = viewModel::closePromptEditor
        )
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                onClearChat = viewModel::clearChat,
                onConfigurePrompt = viewModel::openPromptEditor
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 900.dp)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.messages,
                        key = { it.id }
                    ) { message ->
                        MessageBubble(
                            message = message,
                            modifier = Modifier.animateItem()
                        )
                    }

                    if (uiState.isLoading) {
                        item {
                            LoadingIndicator()
                        }
                    }
                }

                uiState.errorMessage?.let { error ->
                    ErrorMessage(
                        message = error,
                        onDismiss = viewModel::dismissError
                    )
                }

                ChatInputSection(
                    inputText = uiState.inputText,
                    onInputChange = viewModel::updateInputText,
                    onSendClick = viewModel::sendMessage,
                    isLoading = uiState.isLoading,
                    selectedModel = uiState.selectedModel,
                    availableModels = uiState.availableModels,
                    onModelSelect = viewModel::selectModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    onClearChat: () -> Unit,
    onConfigurePrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeManager = LocalThemeManager.current
    val isDarkTheme = themeManager.isDarkTheme()

    TopAppBar(
        title = { Text("Gemini AI Chat") },
        actions = {
            IconButton(
                onClick = onConfigurePrompt
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configure Prompt"
                )
            }
            IconButton(
                onClick = {
                    val nextMode = if (isDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK
                    themeManager.setThemeMode(nextMode)
                }
            ) {
                val icon = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode
                Icon(
                    imageVector = icon,
                    contentDescription = if (isDarkTheme) "Dark Mode" else "Light Mode"
                )
            }
            IconButton(onClick = onClearChat) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Chat")
            }
        },
        modifier = modifier
    )
}

@Composable
fun MessageBubble(
    message: com.glavatskikh.aiadvent2025.chat.data.models.ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER
    val backgroundColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = if (isUser) "You" else "Gemini",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                ChatMarkdownText(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .time.toString().substringBeforeLast(":"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    message.tokenUsage?.let { usage ->
                        Text(
                            text = "${usage.totalTokens} tokens",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Typing...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}