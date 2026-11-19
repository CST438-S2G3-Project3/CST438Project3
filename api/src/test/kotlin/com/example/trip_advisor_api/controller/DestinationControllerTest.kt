package com.example.trip_advisor_api.controller

import com.example.trip_advisor_api.model.Destination
import com.example.trip_advisor_api.model.Trip
import com.example.trip_advisor_api.repository.ActivityRepository
import com.example.trip_advisor_api.repository.DestinationRepository
import com.example.trip_advisor_api.repository.TripRepository
import com.example.trip_advisor_api.service.DestinationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.security.Principal
import java.util.Optional
import java.util.UUID

/** MVC and real-service tests with mocked persistence; principals here are test fixtures, not JWT validation. */
@WebMvcTest(DestinationController::class)
@Import(DestinationService::class, DestinationErrorHandler::class)
class DestinationControllerTest {
    @Autowired private lateinit var mvc: MockMvc
    @MockitoBean private lateinit var trips: TripRepository
    @MockitoBean private lateinit var destinations: DestinationRepository
    @MockitoBean private lateinit var activities: ActivityRepository

    private val tripId = UUID.randomUUID()
    private val ownerId = UUID.randomUUID()
    private val destinationId = UUID.randomUUID()
    private val owner = Principal { ownerId.toString() }
    private val path = "/api/v1/trips/$tripId/destinations"
    private val destination = Destination(destinationId, tripId, "Park", "San Francisco")
    private val json = """{"name":"Park","location":"San Francisco"}"""

    @BeforeEach
    fun setup() {
        `when`(trips.findById(tripId)).thenReturn(Optional.of(Trip(id = tripId, userId = ownerId, name = "Trip")))
        `when`(destinations.findForUpdate(destinationId)).thenReturn(destination)
    }

    @Test
    fun `owner can create list edit and delete destinations`() {
        `when`(destinations.save(Destination(tripId = tripId, name = "Park", location = "San Francisco")))
            .thenReturn(destination)
        mvc.perform(post(path).principal(owner).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated).andExpect(jsonPath("$.id").value(destinationId.toString()))

        `when`(destinations.findAllByTripIdOrderByCreatedAtAscIdAsc(tripId)).thenReturn(listOf(destination))
        mvc.perform(get(path).principal(owner))
            .andExpect(status().isOk).andExpect(jsonPath("$[0].name").value("Park"))

        val edited = destination.copy(name = "Museum")
        `when`(destinations.save(edited)).thenReturn(edited)
        mvc.perform(put("$path/$destinationId").principal(owner).contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Museum","location":"San Francisco"}"""))
            .andExpect(status().isOk).andExpect(jsonPath("$.name").value("Museum"))

        mvc.perform(delete("$path/$destinationId").principal(owner))
            .andExpect(status().isNoContent).andExpect(content().string(""))
        verify(destinations).delete(destination)
    }

    @Test
    fun `all operations reject requests without an authenticated principal`() {
        val requests = listOf(get(path), post(path), put("$path/$destinationId"), delete("$path/$destinationId"))
        for (request in requests) {
            mvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(json)
                .header("Authorization", "Bearer unverified-token").header("X-User-Id", ownerId))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
        }
        verifyNoInteractions(destinations, activities)
        verify(trips, never()).findById(tripId)
    }

    @Test
    fun `all operations hide another user's trip`() {
        val other = Principal { UUID.randomUUID().toString() }
        for (request in listOf(get(path), post(path), put("$path/$destinationId"), delete("$path/$destinationId"))) {
            mvc.perform(request.principal(other).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound).andExpect(jsonPath("$.status").value(404))
        }
        verifyNoInteractions(destinations, activities)
    }

    @Test
    fun `missing trip returns problem details`() {
        `when`(trips.findById(tripId)).thenReturn(Optional.empty())
        mvc.perform(get(path).principal(owner)).andExpect(status().isNotFound)
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Not Found"))
            .andExpect(jsonPath("$.detail").value("Trip not found"))
            .andExpect(jsonPath("$.instance").value(path))
    }

    @Test
    fun `create and edit reject missing or blank details`() {
        for (body in listOf("{}", """{"name":" ","location":"City"}""", """{"name":"Park","location":null}""")) {
            for (request in listOf(post(path), put("$path/$destinationId"))) {
                mvc.perform(request.principal(owner).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest).andExpect(jsonPath("$.status").value(400))
            }
        }
        verifyNoInteractions(destinations, activities)
    }

    @Test
    fun `malformed JSON and UUID return safe problem responses`() {
        mvc.perform(post(path).principal(owner).contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest).andExpect(jsonPath("$.detail").value("Provide a JSON object with name and location"))
        mvc.perform(get("/api/v1/trips/not-a-uuid/destinations").principal(owner))
            .andExpect(status().isBadRequest).andExpect(jsonPath("$.status").value(400))
    }

    @Test
    fun `edit and delete reject a destination from another trip`() {
        `when`(destinations.findForUpdate(destinationId)).thenReturn(destination.copy(tripId = UUID.randomUUID()))
        for (request in listOf(put("$path/$destinationId"), delete("$path/$destinationId"))) {
            mvc.perform(request.principal(owner).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound)
        }
        verifyNoInteractions(activities)
    }

    @Test
    fun `deleting a destination with activities returns conflict`() {
        `when`(activities.existsByDestinationId(destinationId)).thenReturn(true)
        mvc.perform(delete("$path/$destinationId").principal(owner))
            .andExpect(status().isConflict).andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Remove this destination's activities before deleting it"))
        verify(destinations, never()).delete(destination)
    }

    @Test
    fun `principal without an application UUID is rejected`() {
        mvc.perform(get(path).principal(Principal { "not-an-application-user" }))
            .andExpect(status().isUnauthorized)
        verifyNoInteractions(destinations, activities)
    }
}
