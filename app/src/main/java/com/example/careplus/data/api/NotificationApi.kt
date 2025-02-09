package com.example.careplus.data.api
import com.example.careplus.data.model.notification.DeactivateTokenRequest
import com.example.careplus.data.model.notification.DeactivateTokenResponse
import com.example.careplus.data.model.notification.DeviceTokenResponse
import com.example.careplus.data.model.notification.TokenRegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PATCH
interface NotificationApi {
    @POST("notification/register-token")
    suspend fun registerToken(@Body request:TokenRegisterRequest): DeviceTokenResponse

    @POST("notification/deactivate-token")
    suspend fun deactivateToken(@Body request: DeactivateTokenRequest): DeactivateTokenResponse
}