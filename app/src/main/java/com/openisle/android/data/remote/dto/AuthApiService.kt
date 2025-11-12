package com.openisle.android.data.remote

import com.openisle.android.data.remote.dto.AuthResponse
import com.openisle.android.data.remote.dto.GoogleLoginRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/google")
    suspend fun socialLogin(@Body request: GoogleLoginRequest): AuthResponse
}