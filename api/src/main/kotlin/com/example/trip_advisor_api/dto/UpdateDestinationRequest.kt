package com.example.trip_advisor_api.dto

/** Replacement details for an existing destination; both fields are required by the service. */
data class UpdateDestinationRequest(
    val name: String? = null,
    val location: String? = null
)
