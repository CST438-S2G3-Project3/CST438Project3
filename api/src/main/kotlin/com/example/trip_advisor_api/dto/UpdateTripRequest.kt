package com.example.trip_advisor_api.dto

import java.time.LocalDate

data class UpdateTripRequest(
    val name: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?
)