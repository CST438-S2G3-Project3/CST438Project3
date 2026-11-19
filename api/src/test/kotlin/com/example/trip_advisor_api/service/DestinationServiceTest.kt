package com.example.trip_advisor_api.service

import com.example.trip_advisor_api.dto.CreateDestinationRequest
import com.example.trip_advisor_api.dto.UpdateDestinationRequest
import com.example.trip_advisor_api.model.Destination
import com.example.trip_advisor_api.model.Trip
import com.example.trip_advisor_api.repository.ActivityRepository
import com.example.trip_advisor_api.repository.DestinationRepository
import com.example.trip_advisor_api.repository.TripRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.time.OffsetDateTime
import java.util.Optional
import java.util.UUID

class DestinationServiceTest {
    private class Fixture {
        val trips = mock(TripRepository::class.java)
        val destinations = mock(DestinationRepository::class.java)
        val activities = mock(ActivityRepository::class.java)
        val service = DestinationService(trips, destinations, activities)
        val tripId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val destinationId = UUID.randomUUID()
        val trip = Trip(id = tripId, userId = ownerId, name = "Weekend Trip")
        val destination = Destination(
            id = destinationId,
            tripId = tripId,
            name = "Golden Gate Park",
            location = "San Francisco",
            createdAt = OffsetDateTime.parse("2026-11-01T12:00:00Z")
        )

        fun ownTrip() {
            `when`(trips.findById(tripId)).thenReturn(Optional.of(trip))
        }

        fun existingDestination() {
            ownTrip()
            `when`(destinations.findForUpdate(destinationId)).thenReturn(destination)
        }
    }

    private val createRequest = CreateDestinationRequest("Golden Gate Park", "San Francisco")
    private val updateRequest = UpdateDestinationRequest("Museum", "San Jose")

    private fun expectStatus(status: HttpStatus, action: () -> Unit) {
        val error = assertThrows(ResponseStatusException::class.java) { action() }
        assertEquals(status, error.statusCode)
    }

    private fun operations(f: Fixture, caller: UUID): List<() -> Unit> = listOf(
        { f.service.createDestination(f.tripId, caller, createRequest); Unit },
        { f.service.listDestinations(f.tripId, caller); Unit },
        { f.service.updateDestination(f.tripId, f.destinationId, caller, updateRequest); Unit },
        { f.service.deleteDestination(f.tripId, f.destinationId, caller) }
    )

    @Test
    fun `owner creates destination with trimmed details`() {
        val f = Fixture()
        f.ownTrip()
        val newDestination = Destination(
            tripId = f.tripId, name = "Golden Gate Park", location = "San Francisco"
        )
        `when`(f.destinations.save(newDestination)).thenReturn(f.destination)

        val result = f.service.createDestination(
            f.tripId, f.ownerId,
            CreateDestinationRequest("  Golden Gate Park  ", " San Francisco\n")
        )

        assertEquals(f.destination, result)
        verify(f.destinations).save(newDestination)
        verifyNoInteractions(f.activities)
    }

    @Test
    fun `create rejects missing empty and whitespace details without saving`() {
        for (invalid in listOf(null, "", " ", "\t\n")) {
            for (request in listOf(
                createRequest.copy(name = invalid),
                createRequest.copy(location = invalid)
            )) {
                val f = Fixture()
                expectStatus(HttpStatus.BAD_REQUEST) {
                    f.service.createDestination(f.tripId, f.ownerId, request)
                }
                verifyNoInteractions(f.destinations, f.activities)
            }
        }
    }

    @Test
    fun `all operations reject another user's trip before destination access`() {
        for (index in 0..3) {
            val f = Fixture()
            f.ownTrip()
            expectStatus(HttpStatus.NOT_FOUND, operations(f, UUID.randomUUID())[index])
            verifyNoInteractions(f.destinations, f.activities)
        }
    }

    @Test
    fun `all operations reject a missing trip`() {
        for (index in 0..3) {
            val f = Fixture()
            `when`(f.trips.findById(f.tripId)).thenReturn(Optional.empty())
            expectStatus(HttpStatus.NOT_FOUND, operations(f, f.ownerId)[index])
            verifyNoInteractions(f.destinations, f.activities)
        }
    }

    @Test
    fun `owner lists only the requested trip through the ordered query`() {
        val f = Fixture()
        f.ownTrip()
        val expected = listOf(f.destination)
        `when`(f.destinations.findAllByTripIdOrderByCreatedAtAscIdAsc(f.tripId))
            .thenReturn(expected)

        assertEquals(expected, f.service.listDestinations(f.tripId, f.ownerId))
        verify(f.destinations).findAllByTripIdOrderByCreatedAtAscIdAsc(f.tripId)
        verifyNoInteractions(f.activities)
    }

    @Test
    fun `trip with no destinations returns an empty list`() {
        val f = Fixture()
        f.ownTrip()
        `when`(f.destinations.findAllByTripIdOrderByCreatedAtAscIdAsc(f.tripId))
            .thenReturn(emptyList())
        assertEquals(emptyList<Destination>(), f.service.listDestinations(f.tripId, f.ownerId))
    }

    @Test
    fun `update trims details and preserves identity trip and creation timestamp`() {
        val f = Fixture()
        f.existingDestination()
        val expected = f.destination.copy(name = "Museum", location = "San Jose")
        `when`(f.destinations.save(expected)).thenReturn(expected)

        val result = f.service.updateDestination(
            f.tripId, f.destinationId, f.ownerId,
            UpdateDestinationRequest(" Museum ", " San Jose\n")
        )

        assertEquals(expected, result)
        verify(f.destinations).save(expected)
        verifyNoInteractions(f.activities)
    }

    @Test
    fun `update rejects missing empty and whitespace details without saving`() {
        for (invalid in listOf(null, "", " ", "\t\n")) {
            for (request in listOf(
                updateRequest.copy(name = invalid),
                updateRequest.copy(location = invalid)
            )) {
                val f = Fixture()
                expectStatus(HttpStatus.BAD_REQUEST) {
                    f.service.updateDestination(f.tripId, f.destinationId, f.ownerId, request)
                }
                verifyNoInteractions(f.destinations, f.activities)
            }
        }
    }

    @Test
    fun `update and delete reject missing destination`() {
        for (index in 2..3) {
            val f = Fixture()
            f.ownTrip()
            `when`(f.destinations.findForUpdate(f.destinationId)).thenReturn(null)

            expectStatus(HttpStatus.NOT_FOUND, operations(f, f.ownerId)[index])

            verify(f.destinations).findForUpdate(f.destinationId)
            org.mockito.Mockito.verifyNoMoreInteractions(f.destinations)
            verifyNoInteractions(f.activities)
        }
    }

    @Test
    fun `update and delete reject a destination belonging to a different trip`() {
        for (index in 2..3) {
            val f = Fixture()
            f.ownTrip()
            `when`(f.destinations.findForUpdate(f.destinationId))
                .thenReturn(f.destination.copy(tripId = UUID.randomUUID()))

            expectStatus(HttpStatus.NOT_FOUND, operations(f, f.ownerId)[index])

            verify(f.destinations).findForUpdate(f.destinationId)
            org.mockito.Mockito.verifyNoMoreInteractions(f.destinations)
            verifyNoInteractions(f.activities)
        }
    }

    @Test
    fun `owner deletes a destination without activities`() {
        val f = Fixture()
        f.existingDestination()
        `when`(f.activities.existsByDestinationId(f.destinationId)).thenReturn(false)

        f.service.deleteDestination(f.tripId, f.destinationId, f.ownerId)

        val order = org.mockito.Mockito.inOrder(f.destinations, f.activities)
        order.verify(f.destinations).findForUpdate(f.destinationId)
        order.verify(f.activities).existsByDestinationId(f.destinationId)
        order.verify(f.destinations).delete(f.destination)
        order.verify(f.destinations).flush()
    }

    @Test
    fun `destination with activities returns conflict without deleting`() {
        val f = Fixture()
        f.existingDestination()
        `when`(f.activities.existsByDestinationId(f.destinationId)).thenReturn(true)

        expectStatus(HttpStatus.CONFLICT) {
            f.service.deleteDestination(f.tripId, f.destinationId, f.ownerId)
        }

        verify(f.destinations, never()).delete(f.destination)
        verify(f.destinations, never()).flush()
        verify(f.activities).existsByDestinationId(f.destinationId)
        org.mockito.Mockito.verifyNoMoreInteractions(f.activities)
    }
}
