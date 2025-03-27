package org.example.bails.data.models

import androidx.room.Entity
import org.example.bails.scoreRecorder.BallType

@Entity
data class Ball(
    val ballId: Long,
    val ballType: BallType,
    val score: Int,
    val playerId: Long
)


