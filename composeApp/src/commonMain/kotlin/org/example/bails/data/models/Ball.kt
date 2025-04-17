package org.example.bails.data.models

import androidx.room.Entity
import org.example.bails.presentation.scoreRecorder.BallType

@Entity
data class Ball(
    val ballId: Long,
    val ballType: BallType,
    val score: Int,
    val playerId: Long
)


