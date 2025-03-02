package com.example.careplus.data.model.settings

data class Settings(
    val user_management: UserManagement,
    val emergency_alerts: EmergencyAlerts
)

data class UserManagement(
    val notification_preferences: NotificationPreferences,
    val language_preferences: String,
    val timezone: String,
)

data class NotificationPreferences(
    val email: Boolean,
    val sms: Boolean,
    val push_notifications: Boolean
)

data class EmergencyAlerts(
    val emergency_contacts: List<EmergencyContact>,
    val alert_preferences: AlertPreferences
)

data class EmergencyContact(
    val name: String,
    val email: String,
    val phone: String,
    val address: String
)

data class AlertPreferences(
    val sms: Boolean,
    val email: Boolean
)

data class TimezoneResponse(
    val name: String,
    val utc_offset: String
)

data class UpdateSettingsRequest(
    val settings: Settings
)

data class UpdateSettingsResponse(
    val error: Boolean,
    val message: String,
    val data: Settings
)