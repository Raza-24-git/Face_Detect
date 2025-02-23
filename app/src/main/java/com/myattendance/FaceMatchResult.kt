package com.myattendance

data class FaceMatchResult(
    val isMatched: Boolean,
    val message: String,
    val statusType: FaceStatusType
)

enum class FaceStatusType {
    REGISTERED, MATCHED, NOT_MATCHED
}
