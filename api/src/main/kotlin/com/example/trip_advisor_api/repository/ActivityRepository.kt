package com.example.trip_advisor_api.repository

import com.example.trip_advisor_api.model.Activity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

/** Persistence operations for activities. */
@Repository
interface ActivityRepository : JpaRepository<Activity, UUID> {
    /** Checks whether a destination still contains activities before deletion. */
    fun existsByDestinationId(destinationId: UUID): Boolean
}
