package com.example.trip_advisor_api.controller

import com.example.trip_advisor_api.dto.CreateDestinationRequest
import com.example.trip_advisor_api.dto.UpdateDestinationRequest
import com.example.trip_advisor_api.model.Destination
import com.example.trip_advisor_api.service.DestinationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.security.Principal
import java.util.UUID

/**
 * Destination HTTP operations. Authentication must be supplied by the team's
 * Resource Server, with Principal.name mapped to the application's user UUID.
 * A bearer header alone never supplies an identity to this controller.
 */
@RestController
@RequestMapping("/api/v1/trips/{tripId}/destinations")
class DestinationController(private val service: DestinationService) {
    /** Lists destinations in a trip owned by the authenticated caller. */
    @GetMapping
    fun list(@PathVariable("tripId") tripId: UUID, principal: Principal?): List<Destination> =
        service.listDestinations(tripId, userId(principal))

    /** Creates a destination using the caller's verified identity. */
    @PostMapping
    fun create(
        @PathVariable("tripId") tripId: UUID,
        @RequestBody request: CreateDestinationRequest,
        principal: Principal?
    ): ResponseEntity<Destination> = ResponseEntity.status(HttpStatus.CREATED).body(
        service.createDestination(tripId, userId(principal), request)
    )

    /** Updates a destination only within its owning trip. */
    @PutMapping("/{destinationId}")
    fun update(
        @PathVariable("tripId") tripId: UUID,
        @PathVariable("destinationId") destinationId: UUID,
        @RequestBody request: UpdateDestinationRequest,
        principal: Principal?
    ): Destination = service.updateDestination(tripId, destinationId, userId(principal), request)

    /** Deletes a destination when ownership and activity checks permit it. */
    @DeleteMapping("/{destinationId}")
    fun delete(
        @PathVariable("tripId") tripId: UUID,
        @PathVariable("destinationId") destinationId: UUID,
        principal: Principal?
    ): ResponseEntity<Void> {
        service.deleteDestination(tripId, destinationId, userId(principal))
        return ResponseEntity.noContent().build()
    }

    private fun userId(principal: Principal?): UUID {
        val name = principal?.name
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to manage destinations")
        return try {
            UUID.fromString(name)
        } catch (_: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "A valid user identity is required")
        }
    }
}
