package com.stuf.domain.repository

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
)
