package com.stuf.classroom.post.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun PostScreenCommentComposer(
    isReply: Boolean,
    onDismiss: () -> Unit,
    onCommentSubmit: (text: String, isPrivate: Boolean) -> Unit,
) {
    val textState: MutableState<String> = remember { mutableStateOf("") }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    if (isReply) {
                        "Ответ на комментарий"
                    } else {
                        "Новый комментарий"
                    },
                style = MaterialTheme.typography.titleSmall,
            )
            TextButton(onClick = onDismiss) {
                Text("Отменить")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag("post_comment_input"),
                placeholder = { Text("Текст…") },
                minLines = 1,
                maxLines = 6,
            )
            IconButton(
                onClick = {
                    val text: String = textState.value
                    if (text.isNotBlank()) {
                        onCommentSubmit(text, false)
                        textState.value = ""
                    }
                },
                enabled = textState.value.isNotBlank(),
                modifier = Modifier.testTag("post_comment_send_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Отправить",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
