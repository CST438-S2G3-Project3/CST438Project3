package com.example.trip_advisor_api.controller

import com.example.trip_advisor_api.model.Trip
import com.example.trip_advisor_api.repository.TripRepository
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import tools.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.util.Optional
import java.util.UUID

@WebMvcTest(TripController::class)
class TripControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var tripRepository: TripRepository


    // =========================================================
    // CREATE
    // POST /api/v1/trips
    // =========================================================

    @Test
    fun `POST trips creates a trip and returns 201`() {

        val tripId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val savedTrip = Trip(
            id = tripId,
            userId = userId,
            name = "Japan Vacation",
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 15)
        )

        `when`(tripRepository.save(any(Trip::class.java)))
            .thenReturn(savedTrip)

        val request = mapOf(
            "userId" to userId.toString(),
            "name" to "Japan Vacation",
            "startDate" to "2026-06-01",
            "endDate" to "2026-06-15"
        )

        mockMvc.post("/api/v1/trips") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isCreated() }

                jsonPath("$.id") {
                    value(tripId.toString())
                }

                jsonPath("$.userId") {
                    value(userId.toString())
                }

                jsonPath("$.name") {
                    value("Japan Vacation")
                }

                jsonPath("$.startDate") {
                    value("2026-06-01")
                }

                jsonPath("$.endDate") {
                    value("2026-06-15")
                }
            }

        verify(tripRepository).save(any(Trip::class.java))
    }


    // =========================================================
    // READ ALL
    // GET /api/v1/trips
    // =========================================================

    @Test
    fun `GET trips returns all trips`() {

        val userId = UUID.randomUUID()

        val trips = listOf(
            Trip(
                id = UUID.randomUUID(),
                userId = userId,
                name = "Japan Vacation",
                startDate = LocalDate.of(2026, 6, 1),
                endDate = LocalDate.of(2026, 6, 15)
            ),
            Trip(
                id = UUID.randomUUID(),
                userId = userId,
                name = "Italy Vacation",
                startDate = LocalDate.of(2026, 8, 1),
                endDate = LocalDate.of(2026, 8, 10)
            )
        )

        `when`(tripRepository.findAll())
            .thenReturn(trips)

        mockMvc.get("/api/v1/trips")
            .andExpect {
                status { isOk() }

                jsonPath("$.length()") {
                    value(2)
                }

                jsonPath("$[0].name") {
                    value("Japan Vacation")
                }

                jsonPath("$[1].name") {
                    value("Italy Vacation")
                }
            }

        verify(tripRepository).findAll()
    }


    @Test
    fun `GET trips returns empty list when there are no trips`() {

        `when`(tripRepository.findAll())
            .thenReturn(emptyList())

        mockMvc.get("/api/v1/trips")
            .andExpect {
                status { isOk() }

                jsonPath("$.length()") {
                    value(0)
                }
            }

        verify(tripRepository).findAll()
    }


    // =========================================================
    // READ ONE
    // GET /api/v1/trips/{id}
    // =========================================================

    @Test
    fun `GET trip by id returns trip when found`() {

        val tripId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val trip = Trip(
            id = tripId,
            userId = userId,
            name = "Japan Vacation",
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 15)
        )

        `when`(tripRepository.findById(tripId))
            .thenReturn(Optional.of(trip))

        mockMvc.get("/api/v1/trips/$tripId")
            .andExpect {
                status { isOk() }

                jsonPath("$.id") {
                    value(tripId.toString())
                }

                jsonPath("$.userId") {
                    value(userId.toString())
                }

                jsonPath("$.name") {
                    value("Japan Vacation")
                }

                jsonPath("$.startDate") {
                    value("2026-06-01")
                }

                jsonPath("$.endDate") {
                    value("2026-06-15")
                }
            }

        verify(tripRepository).findById(tripId)
    }


    @Test
    fun `GET trip by id returns 404 when not found`() {

        val tripId = UUID.randomUUID()

        `when`(tripRepository.findById(tripId))
            .thenReturn(Optional.empty())

        mockMvc.get("/api/v1/trips/$tripId")
            .andExpect {
                status { isNotFound() }
            }

        verify(tripRepository).findById(tripId)
    }


    // =========================================================
    // UPDATE
    // PUT /api/v1/trips/{id}
    // =========================================================

    @Test
    fun `PUT trip updates existing trip and returns 200`() {

        val tripId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val existingTrip = Trip(
            id = tripId,
            userId = userId,
            name = "Original Trip",
            startDate = LocalDate.of(2026, 6, 1),
            endDate = LocalDate.of(2026, 6, 10)
        )

        val updatedTrip = existingTrip.copy(
            name = "Updated Trip",
            startDate = LocalDate.of(2026, 7, 1),
            endDate = LocalDate.of(2026, 7, 15)
        )

        `when`(tripRepository.findById(tripId))
            .thenReturn(Optional.of(existingTrip))

        `when`(tripRepository.save(any(Trip::class.java)))
            .thenReturn(updatedTrip)

        val request = mapOf(
            "name" to "Updated Trip",
            "startDate" to "2026-07-01",
            "endDate" to "2026-07-15"
        )

        mockMvc.put("/api/v1/trips/$tripId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isOk() }

                jsonPath("$.id") {
                    value(tripId.toString())
                }

                jsonPath("$.userId") {
                    value(userId.toString())
                }

                jsonPath("$.name") {
                    value("Updated Trip")
                }

                jsonPath("$.startDate") {
                    value("2026-07-01")
                }

                jsonPath("$.endDate") {
                    value("2026-07-15")
                }
            }

        verify(tripRepository).findById(tripId)
        verify(tripRepository).save(any(Trip::class.java))
    }


    @Test
    fun `PUT trip returns 404 when trip does not exist`() {

        val tripId = UUID.randomUUID()

        `when`(tripRepository.findById(tripId))
            .thenReturn(Optional.empty())

        val request = mapOf(
            "name" to "Updated Trip",
            "startDate" to "2026-07-01",
            "endDate" to "2026-07-15"
        )

        mockMvc.put("/api/v1/trips/$tripId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isNotFound() }
            }

        verify(tripRepository).findById(tripId)

        verify(tripRepository, never())
            .save(any(Trip::class.java))
    }


    // =========================================================
    // DELETE
    // DELETE /api/v1/trips/{id}
    // =========================================================

    @Test
    fun `DELETE trip deletes existing trip and returns 204`() {

        val tripId = UUID.randomUUID()

        `when`(tripRepository.existsById(tripId))
            .thenReturn(true)

        mockMvc.delete("/api/v1/trips/$tripId")
            .andExpect {
                status { isNoContent() }
            }

        verify(tripRepository).existsById(tripId)
        verify(tripRepository).deleteById(tripId)
    }


    @Test
    fun `DELETE trip returns 404 when trip does not exist`() {

        val tripId = UUID.randomUUID()

        `when`(tripRepository.existsById(tripId))
            .thenReturn(false)

        mockMvc.delete("/api/v1/trips/$tripId")
            .andExpect {
                status { isNotFound() }
            }

        verify(tripRepository).existsById(tripId)

        verify(tripRepository, never())
            .deleteById(tripId)
    }
}