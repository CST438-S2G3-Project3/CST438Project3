package com.example.trip_advisor_api.controller

import com.example.trip_advisor_api.dto.CreateTripRequest
import com.example.trip_advisor_api.dto.UpdateTripRequest
import com.example.trip_advisor_api.model.Trip
import com.example.trip_advisor_api.repository.TripRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/trips")
class TripController(
    private val tripRepository: TripRepository
) {

    // CREATE
    // POST /api/v1/trips
    @PostMapping
    fun createTrip(
        @RequestBody request: CreateTripRequest
    ): ResponseEntity<Trip> {

        val trip = Trip(
            userId = request.userId,
            name = request.name,
            startDate = request.startDate,
            endDate = request.endDate
        )

        val savedTrip = tripRepository.save(trip)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(savedTrip)
    }

    // READ ALL
    // GET /api/v1/trips
    @GetMapping
    fun getAllTrips(): List<Trip> {
        return tripRepository.findAll()
    }

    // READ ONE
    // GET /api/v1/trips/{id}
    @GetMapping("/{id}")
    fun getTripById(
        @PathVariable id: UUID
    ): ResponseEntity<Trip> {

        val trip = tripRepository.findById(id)

        return if (trip.isPresent) {
            ResponseEntity.ok(trip.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // UPDATE
    // PUT /api/v1/trips/{id}
    @PutMapping("/{id}")
    fun updateTrip(
        @PathVariable id: UUID,
        @RequestBody request: UpdateTripRequest
    ): ResponseEntity<Trip> {

        val existingTrip = tripRepository.findById(id)

        if (existingTrip.isEmpty) {
            return ResponseEntity.notFound().build()
        }

        val trip = existingTrip.get()

        val updatedTrip = trip.copy(
            name = request.name,
            startDate = request.startDate,
            endDate = request.endDate
        )

        val savedTrip = tripRepository.save(updatedTrip)

        return ResponseEntity.ok(savedTrip)
    }

    // DELETE
    // DELETE /api/v1/trips/{id}
    @DeleteMapping("/{id}")
    fun deleteTrip(
        @PathVariable id: UUID
    ): ResponseEntity<Void> {

        if (!tripRepository.existsById(id)) {
            return ResponseEntity.notFound().build()
        }

        tripRepository.deleteById(id)

        return ResponseEntity.noContent().build()
    }
}