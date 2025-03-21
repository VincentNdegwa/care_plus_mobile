package com.example.careplus.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.careplus.data.SessionManager
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "https://care.tech360.systems/v1/"
    private lateinit var retrofit: Retrofit

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun create(sessionManager: SessionManager): AuthApi {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(AuthApi::class.java)
    }

    val medicationApi: MedicationApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing medicationApi")
        }
        retrofit.create(MedicationApi::class.java)
    }

    val profileApi: ProfileApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing profileApi")
        }
        retrofit.create(ProfileApi::class.java)
    }

    val dashboardApi: DashboardApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing dashboardApi")
        }
        retrofit.create(DashboardApi::class.java)
    }
    val caregiverApi: CaregiverApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing caregiverApi")
        }
        retrofit.create(CaregiverApi::class.java)
    }
    val notificationApi: NotificationApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing notificationApi")
        }
        retrofit.create(NotificationApi::class.java)
    }

    val sideEffectApi: SideEffectApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing sideEffectApi")
        }
        retrofit.create(SideEffectApi::class.java)
    }
    val fileUploadApi: FileUploadApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing fileUploadApi")
        }
        retrofit.create(FileUploadApi::class.java)
    }

    val diagnosisApi: DiagnosisApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing diagnosisApi")
        }
        retrofit.create(DiagnosisApi::class.java)
    }

    val reportApi: ReportApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing reportApi")
        }
        retrofit.create(ReportApi::class.java)
    }

    val settingsApi: SettingsApi by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("ApiClient must be initialized with create() before accessing settingsApi")
        }
        retrofit.create(SettingsApi::class.java)
    }

}

