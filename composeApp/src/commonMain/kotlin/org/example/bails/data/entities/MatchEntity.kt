package org.example.bails.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey
    val matchId: Long,
    val totalOvers: Int,
    val firstInningsJson: String,
    val secondInningsJson: String,
    val createdAt: Long,
    val isCompleted: Boolean = false,
    val team1Name: String = "Team 1",
    val team2Name: String = "Team 2"
)
