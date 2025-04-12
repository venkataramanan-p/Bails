package org.example.bails.scoreRecorder

sealed interface ScoreRecorderScreenState {

    data class InningsRunning(
        val balls: Int,
        val score: Int,
        val wickets: Int,
        val allOvers: List<Over>,
        val totalOvers: Int,
        val previousInningsSummary: InningsSummary? = null,
        val currentStriker: Batter,
        val currentNonStriker: Batter,
        val isOverCompleted: Boolean = false,
        val bowlersStats: BowlerStats
    ): ScoreRecorderScreenState

    data class InningsBreak(
        val previousInningsSummary: InningsSummary
    ): ScoreRecorderScreenState
}

data class BowlerStats(
    val bowler: Bowler,
    val overs: Float,
    val maidenOvers: Int,
    val runs: Int,
    val wickets: Int,
    val economy: Float
)

data class Over(
    val bowler: Bowler,
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
    val allOvers: List<Over>
)

enum class BallType(val displayStr: String) {
    CORRECT_BALL("Correct Ball"),
    WIDE("Wide"),
    NO_BALL("No Ball"),
    DOT_BALL("Dot ball"),
    WICKET("Wicket")
}

sealed class Ball(val score: Int, val bowler: Bowler) {
    data class CorrectBall(
        val runs: Int,
        val assignedBower: Bowler
    ) : Ball(runs, assignedBower)

    data class WideBall(
        val runs: Int = 1,
        val assignedBower: Bowler
    ) : Ball(runs, assignedBower)

    data class NoBall(
        val runs: Int = 1,
        val assignedBower: Bowler
    ) : Ball(runs, assignedBower)

    data class DotBall(val assignedBower: Bowler): Ball(0, assignedBower)

    data class Wicket(
        val runs: Int,
        val outPlayerId: Long,
        val newPlayerName: String,
        val assignedBower: Bowler
    ): Ball(runs, assignedBower)
}

data class Batter(
    val id: Long,
    val name: String,
    val runs: Int = 0,
    val boundaries: Int = 0,
    val sixes: Int = 0,
    val ballsFaced: Int = 0
)