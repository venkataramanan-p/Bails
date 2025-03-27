package org.example.bails.scoreRecorder

sealed interface ScoreRecorderScreenState {

    data class InningsRunning(
        val balls: Int,
        val score: Int,
        val wickets: Int,
        val allBalls: List<Ball>,
        val totalOvers: Int,
        val previousInningsSummary: InningsSummary? = null,
        val currentStriker: Player? = null,
        val currentNonStriker: Player? = null,
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

data class Ball(
    val ballType: BallType,
    val score: Int = 0,
    val strikerName: String = "",
    val nonStrikerName: String = "",
    val bowlerName: String = ""
)