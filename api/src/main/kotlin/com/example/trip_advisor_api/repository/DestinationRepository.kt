package com.example.trip_advisor_api.repository

import com.example.trip_advisor_api.model.Destination
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

/** Persistence operations for destinations. */
interface DestinationRepository : JpaRepository<Destination, UUID> {
    /** Lists only the requested trip's destinations in a stable order. */
    fun findAllByTripIdOrderByCreatedAtAscIdAsc(tripId: UUID): List<Destination>

    /** Locks a destination until the calling transaction ends. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Destination d where d.id = :id")
    fun findForUpdate(@Param("id") id: UUID): Destination?
}
