package com.stuf.classroom.post.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
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
import com.stuf.domain.model.CommentId

@Composable
internal fun PostCommentInput(
    replyTo: CommentId?,
    onCancelReply: () -> Unit,
    onCommentSubmit: (text: String, isPrivate: Boolean) -> Unit,
) {
    val textState: MutableState<String> = remember { mutableStateOf("") }

    Column {
        if (replyTo != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Ответ на комментарий")
                TextButton(onClick = onCancelReply) {
                    Text("Отменить")
                }
            }
        }

        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag("post_comment_input"),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = {
                    val text: String = textState.value
                    if (text.isNotBlank()) {
                        onCommentSubmit(text, false)
                        textState.value = ""
                    }
                },
                modifier = Modifier.testTag("post_comment_send_button"),
            ) {
                Text("Отправить")
            }
        }
    }
}
