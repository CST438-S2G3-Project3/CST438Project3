package com.example.trip_advisor_api.service

import com.example.trip_advisor_api.dto.CreateDestinationRequest
import com.example.trip_advisor_api.dto.UpdateDestinationRequest
import com.example.trip_advisor_api.model.Destination
import com.example.trip_advisor_api.repository.ActivityRepository
import com.example.trip_advisor_api.repository.DestinationRepository
import com.example.trip_advisor_api.repository.TripRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

/**
 * Manages destinations belonging to trips owned by the caller.
 * The HTTP layer must obtain signedInUserId from a verified authenticated identity.
 */
@Service
class DestinationService(
    private val tripRepository: TripRepository,
    private val destinationRepository: DestinationRepository,
    private val activityRepository: ActivityRepository
) {
    /** Creates a destination after validating its details and the trip owner. */
    @Transactional
    fun createDestination(
        tripId: UUID,
        signedInUserId: UUID,
        request: CreateDestinationRequest
    ): Destination {
        val name = requiredText(request.name, "Name")
        val location = requiredText(request.location, "Location")
        requireOwnedTrip(tripId, signedInUserId)

        return destinationRepository.save(
            Destination(tripId = tripId, name = name, location = location)
        )
    }

    /** Returns the owner's destinations ordered by creation time and ID. */
    @Transactional(readOnly = true)
    fun listDestinations(tripId: UUID, signedInUserId: UUID): List<Destination> {
        requireOwnedTrip(tripId, signedInUserId)
        return destinationRepository.findAllByTripIdOrderByCreatedAtAscIdAsc(tripId)
    }

    /** Replaces editable details while preserving the ID, trip, and creation timestamp. */
    @Transactional
    fun updateDestination(
        tripId: UUID,
        destinationId: UUID,
        signedInUserId: UUID,
        request: UpdateDestinationRequest
    ): Destination {
        val name = requiredText(request.name, "Name")
        val location = requiredText(request.location, "Location")
        requireOwnedTrip(tripId, signedInUserId)
        val destination = requireDestinationForUpdate(tripId, destinationId)

        return destinationRepository.save(
            destination.copy(name = name, location = location)
        )
    }

    /** Deletes an owned destination only when it has no activities. */
    @Transactional
    fun deleteDestination(tripId: UUID, destinationId: UUID, signedInUserId: UUID) {
        requireOwnedTrip(tripId, signedInUserId)
        val destination = requireDestinationForUpdate(tripId, destinationId)

        if (activityRepository.existsByDestinationId(destinationId)) {
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "Remove this destination's activities before deleting it"
            )
        }

        destinationRepository.delete(destination)
        destinationRepository.flush()
    }

    private fun requiredText(value: String?, field: String): String =
        value?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "$field is required")

    private fun requireOwnedTrip(tripId: UUID, signedInUserId: UUID) {
        val trip = tripRepository.findById(tripId).orElse(null)
        if (trip == null || trip.userId != signedInUserId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found")
        }
    }

    private fun requireDestinationForUpdate(tripId: UUID, destinationId: UUID): Destination {
        val destination = destinationRepository.findForUpdate(destinationId)
        if (destination == null || destination.tripId != tripId) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Destination not found")
        }
        return destination
    }
}
