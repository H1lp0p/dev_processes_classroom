package com.stuf.classroom.courses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.stuf.domain.model.CourseRole
import com.stuf.domain.model.UserCourse

/**
 * Карточка в стиле списка курсов: иконка + заголовок + подпись (как в [UserCourseItem]).
 */
@Composable
internal fun UserCourseStyleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    subtitleModifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = titleModifier,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = subtitleModifier,
                )
            }
        }
    }
}

@Composable
internal fun UserCourseItem(
    course: UserCourse,
    modifier: Modifier = Modifier,
) {
    UserCourseStyleCard(
        title = course.title,
        subtitle =
            when (course.role) {
                CourseRole.TEACHER -> "Учитель"
                CourseRole.STUDENT -> "Студент"
            },
        icon =
            when (course.role) {
                CourseRole.TEACHER -> Icons.Outlined.Book
                CourseRole.STUDENT -> Icons.Outlined.School
            },
        modifier = modifier,
    )
}
