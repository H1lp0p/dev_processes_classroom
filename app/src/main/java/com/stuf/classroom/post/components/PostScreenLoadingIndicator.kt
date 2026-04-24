package com.stuf.classroom.post.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun PostScreenLoadingIndicator() {
    Spacer(modifier = Modifier.height(8.dp))
    CircularProgressIndicator(modifier = Modifier.testTag("post_loading_indicator"))
}
