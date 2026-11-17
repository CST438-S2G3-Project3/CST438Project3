package com.example.trip_advisor_api.dto

import java.util.UUID
import java.time.OffsetDateTime

data class CreateActivityRequest(
    val destinationId: UUID,
    val title: String,
    val scheduledAt: OffsetDateTime? = null
)