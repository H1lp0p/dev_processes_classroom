package com.stuf.domain.model

data class User(
    val id: UserId,
    val credentials: String,
    val email: String,
)
