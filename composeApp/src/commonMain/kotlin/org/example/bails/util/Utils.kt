package org.example.bails.util

import org.example.bails.presentation.scoreRecorder.Ball
import org.example.bails.presentation.scoreRecorder.Bowler
import org.example.bails.presentation.scoreRecorder.BowlerStats
import org.example.bails.presentation.scoreRecorder.Over
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

fun Over.getRuns(): Int {
    return this.balls.sumOf { it.score } + this.balls.count { it is Ball.WideBall || it is Ball.NoBall }
}

fun Over.getNumberOfWickets(): Int {
    return this.balls.count { it is Ball.Wicket }
}

fun List<Over>.getBowlerStats(bowler: Bowler): BowlerStats {
    val oversByBowler = this.filter { it.balls.any { it.bowler == bowler } }
    val oversBowled = getTheNumberOfOversBowledByTheBowler(oversByBowler, bowler)
    val maidenOvers = getMaidenOversBowledByTheBowler(oversByBowler, bowler)
    val runsGiven = getRunsGivenByABowler(oversByBowler, bowler)
    val economy = getEconomyOfABowler(oversByBowler, bowler)
    val wicketsTaken = oversByBowler.flatMap { it.balls }.filter { it.bowler == bowler }.count { it is Ball.Wicket }

    return BowlerStats(bowler, oversBowled, maidenOvers, runsGiven, wicketsTaken, economy)
}

private fun getTheNumberOfOversBowledByTheBowler(oversByBowler: List<Over>, currentBowler: Bowler): Float {
    return if (oversByBowler.isEmpty()) {
        0f
    } else {
        val totalValidBallsBowled = oversByBowler.flatMap { it.balls }.filter { it.isValidBall() && it.bowler == currentBowler }.size
        val overs = totalValidBallsBowled / 6
        val balls = totalValidBallsBowled % 6
        return overs + balls * 0.1f
    }
}

private fun getMaidenOversBowledByTheBowler(oversByBowler: List<Over>, bowler: Bowler): Int {
    return oversByBowler.count {
        it.balls.isNotEmpty() && it.balls.all { ball -> ball.score == 0 && ball.bowler == bowler }
    }
}

private fun getRunsGivenByABowler(oversByBowler: List<Over>, bowler: Bowler): Int {
    return oversByBowler.flatMap { it.balls }.filter { it.bowler == bowler }.sumOf { ball -> if (!ball.isValidBall()) ball.score + 1 else ball.score }
}

private fun getEconomyOfABowler(oversByBowler: List<Over>, bowler: Bowler): Float {
    val oversBowled = getTheNumberOfOversBowledByTheBowler(oversByBowler, bowler)
    val runsGiven = getRunsGivenByABowler(oversByBowler, bowler)

    return if (oversBowled > 0) {
        (runsGiven * 6.0 / (oversByBowler.flatMap { it.balls }.count { it.isValidBall() && it.bowler == bowler }.toFloat())).toFloat().roundToDecimals(2)
    } else {
        0f
    }
}