package com.example.trip_advisor_api.dto

data class CreateDestinationRequest(
    val name: String? = null,
    val location: String? = null
)