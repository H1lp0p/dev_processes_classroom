package com.stuf.domain.usecase

import com.stuf.domain.common.DomainResult

interface ChangePassword {
    suspend operator fun invoke(oldPassword: String, newPassword: String): DomainResult<Unit>
}
