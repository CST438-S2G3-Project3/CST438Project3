package com.example.trip_advisor_api.controller

import com.example.trip_advisor_api.model.Activity
import com.example.trip_advisor_api.repository.ActivityRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.util.UUID

@WebMvcTest(ActivityController::class)
class ActivityControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var activityRepository: ActivityRepository

    @Test
    fun `GET all activities should return 200 OK and a list of activities`() {
        val fakeActivity = Activity(
            id = UUID.randomUUID(),
            destinationId = UUID.randomUUID(),
            title = "Mocked Test Activity",
            scheduledAt = OffsetDateTime.now(),
            createdAt = OffsetDateTime.now()
        )

        `when`(activityRepository.findAll()).thenReturn(listOf(fakeActivity))

        mockMvc.perform(get("/api/v1/activities"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].title").value("Mocked Test Activity"))
    }

    @Test
    fun `DELETE activity should return 204 No Content when successful`() {
        val activityId = UUID.randomUUID()
        `when`(activityRepository.existsById(activityId)).thenReturn(true)

        mockMvc.perform(delete("/api/v1/activities/$activityId"))
            .andExpect(status().isNoContent)

        verify(activityRepository).deleteById(activityId)
    }
}