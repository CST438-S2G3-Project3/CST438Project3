package com.example.trip_advisor_api.repository

import com.example.trip_advisor_api.model.Trip
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TripRepository : JpaRepository<Trip, UUID>