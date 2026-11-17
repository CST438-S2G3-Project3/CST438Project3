package com.example.trip_advisor_api.repository

import com.example.trip_advisor_api.model.Activity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ActivityRepository : JpaRepository<Activity, UUID>