package com.example.trip_advisor_api.model

import jakarta.persistence.*
import java.util.UUID
import java.time.OffsetDateTime
import org.hibernate.annotations.CreationTimestamp

@Entity
@Table(name = "activities")
data class Activity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "destination_id", nullable = false)
    val destinationId: UUID,

    @Column(nullable = false)
    val title: String,

    @Column(name = "scheduled_at")
    val scheduledAt: OffsetDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: OffsetDateTime? = null
)