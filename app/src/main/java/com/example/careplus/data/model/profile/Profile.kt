package com.example.careplus.data.model.profile

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val id: Int,
    val user_id: Int,
    val gender: String?,
    val date_of_birth: String?,
    val address: String?,
    val phone_number: String?,
    val avatar: String?
) : Parcelable

@Parcelize
data class ProfileGetResponse(
    val error: Boolean,
    val data: ProfileData
): Parcelable

@Parcelize
data class ProfilePatient(
    val id: Int,
    val user_id: Int,
) : Parcelable

@Parcelize
data class ProfileData(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val email_verified_at: String?,
    val patient: ProfilePatient?,
    val profile: UserProfile
) : Parcelable

@Parcelize
data class ProfileUpdateRequest(
    val gender: String?,
    val date_of_birth: String?,
    val address: String?,
    val phone_number: String?,
    val avatar: String?
): Parcelable
@Parcelize
data class ProfileUpdateResponse(
    val error: Boolean,
    val message: String,
    val profile: UserProfile
): Parcelable
@Parcelize
data class ProfileErrorResponse(
    val error: Boolean,
    val message: String
) : Parcelable