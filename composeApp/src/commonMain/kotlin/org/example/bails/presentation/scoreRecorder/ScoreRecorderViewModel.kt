package org.example.bails.presentation.scoreRecorder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.datetime.Clock
import org.example.bails.data.BailsDb
import org.example.bails.data.Inning
import org.example.bails.util.isValidBall
import org.example.bails.util.roundToDecimals

class ScoreRecorderViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val numberOfOvers = savedStateHandle["numberOfOvers"] ?: 0
    private val strikerName = (savedStateHandle["strikerName"] as? String).takeIf { it?.isNotBlank() == true } ?: "Unnamed player"
    private val nonStrikerName = (savedStateHandle["nonStrikerName"] as? String).takeIf { it?.isNotBlank() == true } ?: "Unnamed player"
    private val bowlerName = (savedStateHandle["bowlerName"] as? String).takeIf { it?.isNotBlank() == true } ?: "Unnamed Player"
    val matchId = savedStateHandle["matchId"] ?: Clock.System.now().toEpochMilliseconds()
    val isFirstInning = savedStateHandle.get<Long>("matchId") == null

    init  {
        println("matchId: $matchId ${savedStateHandle.get<Long>("matchId")}")
    }

    var previousBallState: ScoreRecorderScreenState.InningsRunning? = null
    val timeNowInMillis = Clock.System.now().toEpochMilliseconds()
    val initialStriker = Batter(id = timeNowInMillis, strikerName)
    val initialNonStriker = Batter(id = timeNowInMillis + 1, nonStrikerName)
    val initialBowler = Bowler(id = timeNowInMillis + 2, bowlerName)

    var state: ScoreRecorderScreenState by mutableStateOf(
        ScoreRecorderScreenState.InningsRunning(
            balls = 0,
            score = 0,
            wickets = 0,
            allOvers = listOf(Over(balls = emptyList())),
            totalOvers = numberOfOvers,
            currentPlainStriker = PlainBatter(id = initialStriker.id, strikerName),
            currentPlainNonStriker = PlainBatter(id = initialNonStriker.id, nonStrikerName),
            bowlersStats = BowlerStats(
                bowler = initialBowler,
                overs = 0f,
                maidenOvers = 0,
                runs = 0,
                wickets = 0,
                economy = 0f
            ),
            currentBowler = initialBowler,
            battersStats = BattersStats(
                strikerStats = BatterStats(batter = PlainBatter(id = initialStriker.id, strikerName)),
                nonStrikerStats = BatterStats(batter = PlainBatter(id = initialNonStriker.id, nonStrikerName))
            ),
            isFirstInning = isFirstInning,
            targetScore = BailsDb.getMatchSummary(matchId)?.first?.overs?.let { getTotalRuns(it) + 1 } ?: 0,
        )
    )
    private val battersHistory = mutableListOf<Batter>()

    fun recordBall(ball: Ball) {
        println(">>> beforeRecord: allBalls: ${state.asInningsRunning().allOvers.map { it.balls }.flatten().map { it.score }}")

        previousBallState = state as ScoreRecorderScreenState.InningsRunning
        val isStrikeChanged = ball.score % 2 == 1
        val inningsRunningState = state as ScoreRecorderScreenState.InningsRunning

        when(ball) {
            is Ball.DotBall -> {
                state = state.asInningsRunning().copy(
                    balls = state.asInningsRunning().balls + 1,
                    allOvers = updateAllOversList(ball),
                )

            }
            is Ball.CorrectBall -> {
                state = state.asInningsRunning().copy(
                    score = state.asInningsRunning().score + ball.score,
                    balls = state.asInningsRunning().balls + 1,
                    allOvers = updateAllOversList(ball),
                )
            }
            is Ball.Wicket -> {
                var isStrikerOut = false
                if (ball.outPlayerId == (state as ScoreRecorderScreenState.InningsRunning).currentPlainStriker.id) {
                    isStrikerOut = true
                } else {
                    isStrikerOut = false
                }
//                battersHistory.add(outPlayer)

                state = state.asInningsRunning().copy(
                    wickets = state.asInningsRunning().wickets + 1,
                    score = state.asInningsRunning().score + ball.score,
                    balls = state.asInningsRunning().balls + 1,
                    allOvers = updateAllOversList(ball),
                    currentPlainStriker = if(isStrikerOut) PlainBatter(Clock.System.now().toEpochMilliseconds(), ball.newPlayerName) else state.asInningsRunning().currentPlainStriker,
                    currentPlainNonStriker = if(isStrikerOut) state.asInningsRunning().currentPlainNonStriker else PlainBatter(Clock.System.now().toEpochMilliseconds(), ball.newPlayerName),
                )
            }
            is Ball.NoBall, is Ball.WideBall -> {
                state = state.asInningsRunning().copy(
                    score = state.asInningsRunning().score + ball.score + 1,
                    allOvers = updateAllOversList(ball),
                )
            }
        }

        BailsDb.updateMatchSummary(matchId, isFirstInning, Inning(state.asInningsRunning().allOvers))

        if (!state.asInningsRunning().isFirstInning && state.asInningsRunning().score >= state.asInningsRunning().targetScore) {
            state = state.asInningsRunning().copy(doesWonMatch = true)
        } else if (isStrikeChanged) {
            state = state.asInningsRunning().copy(
                currentPlainStriker = state.asInningsRunning().currentPlainNonStriker,
                currentPlainNonStriker = state.asInningsRunning().currentPlainStriker
            )
        }

        if (state.asInningsRunning().balls  != 0 && state.asInningsRunning().balls % 6 == 0 && ball.isValidBall()) {
            state = state.asInningsRunning().copy(isOverCompleted = true)
        }
        state = state.asInningsRunning().copy(
            bowlersStats = getCurrentBowlerStats(),
            battersStats = getBattersStats()
        )

        if (state.asInningsRunning().balls == state.asInningsRunning().totalOvers * 6) {
            state = ScoreRecorderScreenState.InningsBreak(
                previousInningsSummary = InningsSummary(
                    score = state.asInningsRunning().score,
                    wickets = state.asInningsRunning().wickets,
                    overs = state.asInningsRunning().balls / 6f,
                    allOvers = state.asInningsRunning().allOvers,
                    allBattersStats = getAllBattersStats(state.asInningsRunning().allOvers),
                    allBowlerStats = getAllBowlerStats(state.asInningsRunning().allOvers),
                )
            )
        }

        println(">>> afterRecord: allBalls: ${state.asInningsRunning().allOvers.map { it.balls }.flatten().map { it.score }}")
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

    fun startNextOver(bowlerId: Long?, bowlerName: String?) {
        val nextBowler = bowlerId?.let { bowlerId ->
            (state as ScoreRecorderScreenState.InningsRunning).allOvers
                .map { it.balls }.flatten()
                .find { it.bowler.id == bowlerId }?.bowler
        } ?: bowlerName?.let {
            Bowler(
                id = Clock.System.now().toEpochMilliseconds(),
                name = it
            )
        } ?: Bowler(
            id = Clock.System.now().toEpochMilliseconds(),
            name = "Unnamed Player"
        )
        state = (state as ScoreRecorderScreenState.InningsRunning).copy(
            currentPlainStriker = state.asInningsRunning().currentPlainNonStriker,
            currentPlainNonStriker = state.asInningsRunning().currentPlainStriker,
            allOvers = (state as ScoreRecorderScreenState.InningsRunning).allOvers + Over(
                balls = emptyList()
            ),
            isOverCompleted = false,
            currentBowler = nextBowler
        )
        state = (state as ScoreRecorderScreenState.InningsRunning).copy(
            bowlersStats = getCurrentBowlerStats(),
            battersStats = getBattersStats()
        )
    }

    fun undoLastBall(): Boolean {
        if ((state as ScoreRecorderScreenState.InningsRunning).allOvers.last().balls.isEmpty()) {
            return false
        }
        return previousBallState?.let { previousState ->
            state = previousState
            true
        } ?: run { false }
    }

    fun toggleStrike() {
        val currentStriker = state.asInningsRunning().currentPlainStriker
        val currentNonStriker = state.asInningsRunning().currentPlainNonStriker
        state = (state as ScoreRecorderScreenState.InningsRunning).copy(
            currentPlainStriker = currentNonStriker,
            currentPlainNonStriker = currentStriker,
            battersStats = state.asInningsRunning().battersStats.copy(
                strikerStats = state.asInningsRunning().battersStats.nonStrikerStats,
                nonStrikerStats = state.asInningsRunning().battersStats.strikerStats,
            )
        )
    }

    fun onRetiredHurt(batterId: Long, newBatterName: String) {
        val currentStriker = state.asInningsRunning().currentPlainStriker
        val currentNonStriker = state.asInningsRunning().currentPlainNonStriker
        val newBatter = PlainBatter(Clock.System.now().toEpochMilliseconds(), name = newBatterName)
        if (currentStriker.id == batterId) {
            state = state.asInningsRunning().copy(
                currentPlainStriker = newBatter,
                currentPlainNonStriker = currentNonStriker,
                battersStats = state.asInningsRunning().battersStats.copy(
                    strikerStats = BatterStats(batter = newBatter)
                ),
            )
        } else if (currentNonStriker.id == batterId) {
            state = state.asInningsRunning().copy(
                currentPlainStriker = currentStriker,
                currentPlainNonStriker = PlainBatter(Clock.System.now().toEpochMilliseconds(), name = newBatterName),
                battersStats = state.asInningsRunning().battersStats.copy(
                    nonStrikerStats = BatterStats(batter = newBatter)
                ),
            )
        }
    }

    fun startNextInnings() {
        // TODO:
    }

    fun getCurrentBowlerStats(): BowlerStats {
        val currentBowler = state.asInningsRunning().currentBowler
        val oversByBowler = state.asInningsRunning().allOvers
            .filter { it.balls.any { it.bowler == currentBowler } }
        val oversBowled = getTheNumberOfOversBowledByTheBowler(oversByBowler, currentBowler)
        val maidenOvers = getMaidenOversBowledByTheBowler(oversByBowler, currentBowler)
        val runsGiven = getRunsGivenByABowler(oversByBowler, currentBowler)
        val economy = getEconomyOfABowler(oversByBowler, currentBowler)
        val wicketsTaken = oversByBowler.flatMap { it.balls }.filter { it.bowler == currentBowler }.count { it is Ball.Wicket }

        return BowlerStats(currentBowler, oversBowled, maidenOvers, runsGiven, wicketsTaken, economy)
    }

    fun onChangeBowler(bowlerId: Long?, name: String?) {
        val nextBowler = bowlerId?.let { bowlerId ->
            (state as ScoreRecorderScreenState.InningsRunning).allOvers
                .map { it.balls }.flatten()
                .find { it.bowler.id == bowlerId }?.bowler
        } ?: name?.let {
            Bowler(
                id = Clock.System.now().toEpochMilliseconds(),
                name = it
            )
        } ?: Bowler(
            id = Clock.System.now().toEpochMilliseconds(),
            name = "Unnamed Player"
        )
        state = state.asInningsRunning().copy(
            currentBowler = nextBowler,
        )
        state = state.asInningsRunning().copy(
            bowlersStats = getCurrentBowlerStats()
        )
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

    fun getBattersStats(): BattersStats {
        val allBalls = state.asInningsRunning().allOvers.map { it.balls }.flatten()

        return BattersStats(
            strikerStats = BatterStats(
                batter = state.asInningsRunning().currentPlainStriker,
                runs = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainStriker && it !is Ball.WideBall }.sumOf { it.score },
                boundaries = allBalls.count { it.iStriker == state.asInningsRunning().currentPlainStriker && it.score == 4 && it !is Ball.WideBall },
                sixes = allBalls.count { it.iStriker == state.asInningsRunning().currentPlainStriker && it.score == 6 && it !is Ball.WideBall },
                ballsFaced = allBalls.count { it.iStriker == state.asInningsRunning().currentPlainStriker && it.isValidBall() }
            ),
            nonStrikerStats = BatterStats(
                batter = state.asInningsRunning().currentPlainNonStriker,
                runs = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainNonStriker && it !is Ball.WideBall }.sumOf { it.score },
                boundaries = allBalls.count { it.iStriker == state.asInningsRunning().currentPlainNonStriker && it.score == 4 && it !is Ball.WideBall },
                sixes = allBalls.count { it.iStriker == state.asInningsRunning().currentPlainNonStriker && it.score == 6 && it !is Ball.WideBall },
                ballsFaced = allBalls.count { it.iStriker == state.asInningsRunning().currentPlainNonStriker && it.isValidBall() }
            ),
        )
    }

    private fun updateAllOversList(ball: Ball): List<Over> {
        return state.asInningsRunning().allOvers.dropLast(1) +
                Over(balls = state.asInningsRunning().allOvers.last().balls + ball)
    }

    private fun updateBatter(batter: Batter, ball: Ball): Batter {
        return batter.copy(
            ballsFaced = if (ball.isValidBall()) batter.ballsFaced + 1 else batter.ballsFaced,
            boundaries = if (ball.score == 4) batter.boundaries + 1 else batter.boundaries,
            sixes = if (ball.score == 6) batter.sixes + 1 else batter.sixes,
            runs = batter.runs + ball.score,
        )
    }

    private fun getTotalRuns(overs: List<Over>): Int {
        val allBalls = overs.map { it.balls }.flatten()
        return allBalls.sumOf { it.score } +
                allBalls.count { it is Ball.WideBall || it is Ball.NoBall }
    }

}