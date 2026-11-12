package com.example.trip_advisor_api.controller

import com.example.trip_advisor_api.dto.CreateActivityRequest
import com.example.trip_advisor_api.model.Activity
import com.example.trip_advisor_api.repository.ActivityRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/activities")
class ActivityController(
    private val activityRepository: ActivityRepository
) {

    // Retrieve ALL activities
    @GetMapping
    fun getAllActivities(): ResponseEntity<List<Activity>> {
        val activities = activityRepository.findAll()
        return ResponseEntity.ok(activities)
    }

    // Retrieve a SINGLE activity by its ID
    @GetMapping("/{id}")
    fun getActivityById(@PathVariable id: UUID): ResponseEntity<Activity> {
        val activity = activityRepository.findById(id)
        return if (activity.isPresent) {
            ResponseEntity.ok(activity.get())
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun addActivity(@RequestBody request: CreateActivityRequest): ResponseEntity<Activity> {
        val newActivity = Activity(
            destinationId = request.destinationId,
            title = request.title,
            scheduledAt = request.scheduledAt
        )

        val savedActivity = activityRepository.save(newActivity)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedActivity)
    }

    @DeleteMapping("/{id}")
    fun deleteActivity(@PathVariable id: UUID): ResponseEntity<Void> {
        return if (activityRepository.existsById(id)) {
            activityRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}