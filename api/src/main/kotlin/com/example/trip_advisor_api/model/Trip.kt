package com.example.trip_advisor_api.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "trips")
data class Trip(

    @Id
    @GeneratedValue
    @Column(name = "id")
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    val userId: UUID,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(name = "created_at", insertable = false, updatable = false)
    val createdAt: OffsetDateTime? = null
)