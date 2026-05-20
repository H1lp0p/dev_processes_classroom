package com.stuf.classroom.post

sealed interface PostTransientUiEvent {
    data class ShowMessage(val message: String) : PostTransientUiEvent
}
