package org.example.bails.presentation.scoreBoard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.example.bails.data.BailsDb
import org.example.bails.data.Inning
import org.example.bails.presentation.scoreRecorder.Ball
import org.example.bails.presentation.scoreRecorder.BatterStats
import org.example.bails.presentation.scoreRecorder.Bowler
import org.example.bails.presentation.scoreRecorder.BowlerStats
import org.example.bails.presentation.scoreRecorder.InningsSummary
import org.example.bails.presentation.scoreRecorder.Over
import org.example.bails.util.isValidBall
import org.example.bails.util.roundToDecimals

class ScoreBoardScreenViewModel(
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val matchId: Long = savedStateHandle["matchId"] ?: -1

    var state by mutableStateOf<ScoreBoardScreenState>(ScoreBoardScreenState.Loading)

    init {
        if (matchId == -1L) {
            // Handle the case where matchId is not provided
            // For example, you might want to throw an exception or log an error
        } else {
            // Load the match summary using the matchId
            loadMatchSummary(matchId)
        }
    }

    private fun loadMatchSummary(matchId: Long) {
        val matchSummary = BailsDb.getMatchSummary(matchId)
        println("match summary: $matchSummary")
        matchSummary?.let {
            val inningsSummary = matchSummary.toInningsSummary()
            println("innings summary: $inningsSummary")
            state = ScoreBoardScreenState.Success(inningsSummary)
        }
    }
}

fun Inning.toInningsSummary(): InningsSummary {
    val allBattersStats = mutableListOf<BatterStats>()
    val allBowlerStats = mutableListOf<BowlerStats>()
    var totalRuns = 0
    var totalWickets = 0
    var totalOvers = 0f

    for (over in overs) {
        for (ball in over.balls) {
            when (ball) {
                is Ball.CorrectBall -> {
                    totalRuns += ball.runs
                    totalOvers += 1f / overs.size
                }
                is Ball.Wicket -> {
                    totalWickets += 1
                    totalRuns += ball.runs
                }
                is Ball.WideBall -> {
                    totalRuns += ball.runs + 1
                }
                is Ball.NoBall -> {
                    totalRuns += ball.runs + 1
                }
                is Ball.DotBall -> {
                    // Do nothing for dot balls
                }
            }
        }
    }

    // Create the summary object
    return InningsSummary(
        score = totalRuns,
        wickets = totalWickets,
        overs = totalOvers,
        allOvers = overs,
        allBattersStats = getAllBattersStats(overs),
        allBowlerStats = getAllBowlerStats(overs)
    )
}

fun getAllBattersStats(allOvers: List<Over>): List<BatterStats> {
    val allBalls = allOvers.map { it.balls }.flatten()
    val allBatters = allBalls.map { it.iStriker }.distinct()
    return allBatters.map { batter ->
        val runs = allBalls.filter { it.iStriker == batter && it !is Ball.WideBall }.sumOf { it.score }
        val boundaries = allBalls.count { it.iStriker == batter && it.score == 4 && it !is Ball.WideBall }
        val sixes = allBalls.count { it.iStriker == batter && it.score == 6 && it !is Ball.WideBall }
        val ballsFaced = allBalls.count { it.iStriker == batter && it.isValidBall() }
        BatterStats(batter, runs, boundaries, sixes, ballsFaced)
    }
}

fun getAllBowlerStats(allOvers: List<Over>): List<BowlerStats> {
    val allBalls = allOvers.map { it.balls }.flatten()
    val allBowlers = allBalls.map { it.bowler }.distinct()
    return allBowlers.map { bowler ->
        val oversBowled = getTheNumberOfOversBowledByTheBowler(allOvers, bowler)
        val maidenOvers = getMaidenOversBowledByTheBowler(allOvers, bowler)
        val runsGiven = getRunsGivenByABowler(allOvers, bowler)
        val economy = getEconomyOfABowler(allOvers, bowler)
        val wicketsTaken = allBalls.filter { it.bowler == bowler }.count { it is Ball.Wicket }
        BowlerStats(bowler, oversBowled, maidenOvers, runsGiven, wicketsTaken, economy)
    }
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