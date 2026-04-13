package com.stuf.classroom.course.components.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.stuf.domain.model.AnnouncementPost
import com.stuf.domain.model.MaterialPost
import com.stuf.domain.model.Post
import com.stuf.domain.model.PostId
import com.stuf.domain.model.TaskPost
import com.stuf.domain.model.TeamTaskPost

@Composable
internal fun CourseFeedPostItem(
    post: Post,
    onPostClick: (PostId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (post) {
        is AnnouncementPost ->
            CourseAnnouncementPostCard(post = post, onClick = { onPostClick(post.id) }, modifier = modifier)
        is MaterialPost ->
            CourseMaterialPostCard(post = post, onClick = { onPostClick(post.id) }, modifier = modifier)
        is TaskPost ->
            CourseTaskPostCard(post = post, onClick = { onPostClick(post.id) }, modifier = modifier)
        is TeamTaskPost ->
            CourseTeamTaskPostCard(post = post, onClick = { onPostClick(post.id) }, modifier = modifier)
    }
}
