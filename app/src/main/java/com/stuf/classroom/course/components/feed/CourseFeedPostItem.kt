package com.stuf.classroom.course.components.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.PostKind

@Composable
internal fun CourseFeedPostItem(
    post: Post,
    onPostClick: (PostId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (post.kind) {
        PostKind.ANNOUNCEMENT ->
            CourseAnnouncementPostCard(post = post, onClick = { onPostClick(post.id) }, modifier = modifier)
        PostKind.MATERIAL ->
            CourseMaterialPostCard(post = post, onClick = { onPostClick(post.id) }, modifier = modifier)
        PostKind.TASK ->
            CourseTaskPostCard(post = post, onClick = { onPostClick(post.id) }, modifier = modifier)
        PostKind.TEAM_TASK ->
            CourseTeamTaskPostCard(post = post, onClick = { onPostClick(post.id) }, modifier = modifier)
    }
}
