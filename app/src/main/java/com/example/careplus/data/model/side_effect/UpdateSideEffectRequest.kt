data class UpdateSideEffectRequest(
    val side_effect: String,
    val severity: String,
    val duration: Int?,
    val notes: String?,
    val datetime: String
) 