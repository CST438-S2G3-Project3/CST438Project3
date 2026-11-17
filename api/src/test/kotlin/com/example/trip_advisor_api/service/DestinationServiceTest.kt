package com.example.trip_advisor_api.service

import com.example.trip_advisor_api.dto.CreateDestinationRequest
import com.example.trip_advisor_api.model.Destination
import com.example.trip_advisor_api.model.Trip
import com.example.trip_advisor_api.repository.DestinationRepository
import com.example.trip_advisor_api.repository.TripRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.Optional
import java.util.UUID

class DestinationServiceTest {
    private val tripRepository = mock(TripRepository::class.java)
    private val destinationRepository = mock(DestinationRepository::class.java)
    private val service = DestinationService(tripRepository, destinationRepository)

    private val tripId = UUID.randomUUID()
    private val ownerId = UUID.randomUUID()
    private val trip = Trip(id = tripId, userId = ownerId, name = "Weekend Trip")
    private val request = CreateDestinationRequest(
        name = "Golden Gate Park",
        location = "San Francisco"
    )

    @Test
    fun `trip owner can save a destination`() {
        val destination = Destination(
            tripId = tripId,
            name = "Golden Gate Park",
            location = "San Francisco"
        )
        val savedDestination = destination.copy(id = UUID.randomUUID())

        `when`(tripRepository.findById(tripId)).thenReturn(Optional.of(trip))
        `when`(destinationRepository.save(destination)).thenReturn(savedDestination)

        val result = service.createDestination(tripId, ownerId, request)

        assertEquals(savedDestination, result)
        verify(destinationRepository).save(destination)
    }

    @Test
    fun `blank location is rejected without saving`() {
        val error = assertThrows(ResponseStatusException::class.java) {
            service.createDestination(tripId, ownerId, request.copy(location = "   "))
        }

        assertEquals(HttpStatus.BAD_REQUEST, error.statusCode)
        verifyNoInteractions(destinationRepository)
    }

    @Test
    fun `another user cannot add a destination to the trip`() {
        `when`(tripRepository.findById(tripId)).thenReturn(Optional.of(trip))

        val error = assertThrows(ResponseStatusException::class.java) {
            service.createDestination(tripId, UUID.randomUUID(), request)
        }

        assertEquals(HttpStatus.NOT_FOUND, error.statusCode)
        verifyNoInteractions(destinationRepository)
    }
}