package org.example.bails.presentation.scoreRecorder

import org.example.bails.data.Inning

sealed interface ScoreRecorderScreenState {

    data class InningsRunning(
        val balls: Int,
        val score: Int,
        val wickets: Int,
        val allOvers: List<Over>,
        val totalOvers: Int,
        val previousInningsSummary: InningsSummary? = null,
        val currentPlainStriker: PlainBatter,
        val currentPlainNonStriker: PlainBatter,
        val currentBowler: Bowler,
        val battersStats: BattersStats,
        val isOverCompleted: Boolean = false,
        val bowlersStats: BowlerStats,
        val isFirstInning: Boolean,
        val targetScore: Int,
        val doesWonMatch: Boolean = false
    ): ScoreRecorderScreenState

    data class InningsBreak(
        val previousInningsSummary: InningsSummary
    ): ScoreRecorderScreenState

    fun asInningsRunning(): InningsRunning {
        return this as InningsRunning
    }
}

data class BattersStats(
    val strikerStats: BatterStats,
    val nonStrikerStats: BatterStats
)

data class BatterStats(
    val batter: PlainBatter,
    val runs: Int = 0,
    val boundaries: Int = 0,
    val sixes: Int = 0,
    val ballsFaced: Int = 0
)

data class BowlerStats(
    val bowler: Bowler,
    val overs: Float,
    val maidenOvers: Int,
    val runs: Int,
    val wickets: Int,
    val economy: Float
)

data class Over(
    val balls: List<Ball>,
)

data class Bowler(
    val id: Long,
    val name: String
)

data class InningsSummary(
    val score: Int,
    val wickets: Int,
    val overs: Float,
    val allOvers: List<Over>,
    val allBattersStats: List<BatterStats>,
    val allBowlerStats: List<BowlerStats>
)

enum class BallType(val displayStr: String) {
    CORRECT_BALL("Correct Ball"),
    WIDE("Wide"),
    NO_BALL("No Ball"),
    DOT_BALL("Dot ball"),
    WICKET("Wicket")
}

sealed class Ball(val score: Int, val bowler: Bowler, val iStriker: PlainBatter, val iNonStriker: PlainBatter) {
    data class CorrectBall(
        val runs: Int,
        val assignedBower: Bowler,
        val striker: PlainBatter,
        val nonStriker: PlainBatter,
    ) : Ball(runs, assignedBower, striker, nonStriker)

    data class WideBall(
        val runs: Int = 1,
        val assignedBower: Bowler,
        val striker: PlainBatter,
        val nonStriker: PlainBatter,
    ) : Ball(runs, assignedBower, striker, nonStriker)

    data class NoBall(
        val runs: Int = 1,
        val assignedBower: Bowler,
        val striker: PlainBatter,
        val nonStriker: PlainBatter,
    ) : Ball(runs, assignedBower, striker, nonStriker)

    data class DotBall(
        val assignedBower: Bowler,
        val striker: PlainBatter,
        val nonStriker: PlainBatter
    ): Ball(0, assignedBower, striker, nonStriker)

    data class Wicket(
        val runs: Int,
        val outPlayerId: Long,
        val newPlayerName: String,
        val assignedBower: Bowler,
        val striker: PlainBatter,
        val nonStriker: PlainBatter,
    ): Ball(runs, assignedBower, striker, nonStriker)
}

data class PlainBatter(
    val id: Long,
    val name: String
)

data class Batter(
    val id: Long,
    val name: String,
    val runs: Int = 0,
    val boundaries: Int = 0,
    val sixes: Int = 0,
    val ballsFaced: Int = 0
)