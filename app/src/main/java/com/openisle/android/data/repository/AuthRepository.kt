package com.openisle.android.domain.repository

import com.openisle.android.data.remote.dto.AuthResponse
import com.openisle.android.data.remote.dto.GoogleLoginRequest

interface AuthRepository {
    suspend fun socialLogin(request: GoogleLoginRequest): AuthResponse
}