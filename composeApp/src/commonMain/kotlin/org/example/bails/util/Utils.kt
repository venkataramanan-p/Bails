package org.example.bails.util

import org.example.bails.presentation.scoreRecorder.Ball
import kotlin.math.roundToInt

fun Float.roundToDecimals(decimals: Int): Float {
    var dotAt = 1
    repeat(decimals) { dotAt *= 10 }
    val roundedValue = (this * dotAt).roundToInt()
    return (roundedValue / dotAt) + (roundedValue % dotAt).toFloat() / dotAt
}

fun Ball.isValidBall(): Boolean {
    return this is Ball.CorrectBall ||
            this is Ball.DotBall ||
            this is Ball.Wicket
}