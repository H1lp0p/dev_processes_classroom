package com.stuf.classroom.post.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun PostScreenErrorBlock(
    message: String,
    onRetry: () -> Unit,
) {
    Spacer(modifier = Modifier.height(8.dp))
    Column {
        Text(text = message, modifier = Modifier.testTag("post_error"))
        Button(
            onClick = onRetry,
            modifier = Modifier.testTag("post_retry_button"),
        ) {
            Text("Повторить")
        }
    }
}
