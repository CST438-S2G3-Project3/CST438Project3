package com.example.trip_advisor_api.dto

import java.time.LocalDate
import java.util.UUID

data class CreateTripRequest(
    val userId: UUID,
    val name: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?
)