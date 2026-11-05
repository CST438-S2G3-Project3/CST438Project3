package com.example.trip_advisor_api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/trips")
class TripController {

    @GetMapping
    fun getAllTrips(): List<String> {
        return listOf(
            "Trip 1: Paris, France - 5 days",
            "Trip 2: Tokyo, Japan - 10 days",
            "Trip 3: Banff, Canada - 4 days"
        )
    }
}