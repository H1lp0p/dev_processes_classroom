package com.stuf.domain.model

/** Упрощённое представление пользователя (идентификатор и отображаемое имя с API). */
data class UserRef(
    val id: UserId,
    val credentials: String,
)
