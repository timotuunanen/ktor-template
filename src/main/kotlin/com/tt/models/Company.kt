package com.tt.models

import com.tt.plugins.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

typealias BusinessId = String

@Serializable
data class Company(
    val businessId: BusinessId,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Serializable(with = OffsetDateTimeSerializer::class)
    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
    val name: String,
    val status: Status = Status.NO_STATUS
) {
    enum class Status {
        ACTIVE,
        CONDITION_UNKNOWN,
        DISSOLVED,
        BANKRUPT,
        LIQUIDATED,
        RESTRUCTURING,
        ACTIVITY_ENDED,
        ACTIVITY_PAUSED,
        NO_STATUS // null from the source / not received
    }
}
