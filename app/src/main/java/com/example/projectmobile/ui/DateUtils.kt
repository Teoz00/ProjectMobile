package com.example.projectmobile.ui

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun colorFromDate(date: String): Color {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    return try {
        val expDate = LocalDate.parse(date, formatter)
        val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expDate)
        when {
            daysLeft <= 3 -> Color.Red
            daysLeft in 4..7 -> Color(0xFFFFA500)
            else -> Color(0xFF4CAF50)
        }
    } catch (e: Exception) {
        Color.Gray
    }
}
