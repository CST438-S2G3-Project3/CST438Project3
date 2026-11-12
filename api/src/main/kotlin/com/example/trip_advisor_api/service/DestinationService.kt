package com.example.trip_advisor_api.service

import com.example.trip_advisor_api.dto.CreateDestinationRequest
import com.example.trip_advisor_api.model.Destination
import com.example.trip_advisor_api.repository.DestinationRepository
import com.example.trip_advisor_api.repository.TripRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Service
class DestinationService(
    private val tripRepository: TripRepository,
    private val destinationRepository: DestinationRepository
) {
    fun createDestination(
        tripId: UUID,
        signedInUserId: UUID,
        request: CreateDestinationRequest
    ): Destination {
        val name = request.name?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required")
        val location = request.location?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Location is required")

        val trip = tripRepository.findById(tripId).orElse(null)
        if (trip == null || trip.userId != signedInUserId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found")
        }

        return destinationRepository.save(
            Destination(tripId = tripId, name = name, location = location)
        )
    }
}