package com.openisle.android.data.repository

import com.openisle.android.data.remote.AuthApiService
import com.openisle.android.data.remote.dto.AuthResponse
import com.openisle.android.data.remote.dto.GoogleLoginRequest
import com.openisle.android.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService
) : AuthRepository {

    override suspend fun socialLogin(request: GoogleLoginRequest): AuthResponse {
        return apiService.socialLogin(request)
    }
}