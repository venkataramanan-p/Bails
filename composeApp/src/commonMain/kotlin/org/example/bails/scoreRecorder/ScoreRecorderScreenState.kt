package org.example.bails.scoreRecorder

sealed interface ScoreRecorderScreenState {

    data class InningsRunning(
        val balls: Int,
        val score: Int,
        val wickets: Int,
        val allBalls: List<Ball>,
        val totalOvers: Int,
        val previousInningsSummary: InningsSummary? = null,
        val currentStriker: Batter,
        val currentNonStriker: Batter,
        val bowlerName: Bowler? = null
    ): ScoreRecorderScreenState

    data class InningsBreak(
        val previousInningsSummary: InningsSummary
    ): ScoreRecorderScreenState
}

data class Bowler(
    val name: String
)

data class InningsSummary(
    val score: Int,
    val wickets: Int,
    val overs: Float,
    val allBalls: List<Ball>
)

enum class BallType(val displayStr: String) {
    CORRECT_BALL("Correct Ball"),
    WIDE("Wide"),
    NO_BALL("No Ball"),
    DOT_BALL("Dot ball"),
    WICKET("Wicket")
}

sealed class Ball(val score: Int) {
    data class CorrectBall(
        val runs: Int,
    ) : Ball(runs)

    data class WideBall(
        val runs: Int = 1,
    ) : Ball(runs)

    data class NoBall(
        val runs: Int = 1,
    ) : Ball(runs)

    object DotBall: Ball(0)

    data class Wicket(
        val runs: Int,
        val outPlayerId: Long,
        val newPlayerName: String,
    ): Ball(runs)
}

data class Batter(
    val id: Long,
    val name: String,
    val runs: Int = 0,
    val boundaries: Int = 0,
    val sixes: Int = 0,
    val ballsFaced: Int = 0
)