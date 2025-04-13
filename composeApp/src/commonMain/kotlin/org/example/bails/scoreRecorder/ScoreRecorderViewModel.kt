package org.example.bails.scoreRecorder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.datetime.Clock
import org.example.bails.util.roundToDecimals
import kotlin.math.min
import kotlin.math.roundToInt

class ScoreRecorderViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val numberOfOvers = savedStateHandle["numberOfOvers"] ?: 0
    private val strikerName = (savedStateHandle["strikerName"] as? String).takeIf { it?.isNotBlank() == true } ?: "Unnamed player"
    private val nonStrikerName = (savedStateHandle["nonStrikerName"] as? String).takeIf { it?.isNotBlank() == true } ?: "Unnamed player"
    private val bowlerName = (savedStateHandle["bowlerName"] as? String).takeIf { it?.isNotBlank() == true } ?: "Unnamed Player"

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
            )
        )
    )
    private val battersHistory = mutableListOf<Batter>()

    fun recordBall(ball: Ball) {
        previousBallState = state as ScoreRecorderScreenState.InningsRunning
        val isStrikeChanged = ball.score % 2 == 1
        val inningsRunningState = state as ScoreRecorderScreenState.InningsRunning

        when(ball) {
            is Ball.DotBall -> {
                state = inningsRunningState.copy(
                    balls = inningsRunningState.balls + 1,
                    allOvers = updateAllOversList(ball),
                )

            }
            is Ball.CorrectBall -> {
                state = inningsRunningState.copy(
                    score = inningsRunningState.score + ball.score,
                    balls = inningsRunningState.balls + 1,
                    allOvers = updateAllOversList(ball),
                )
            }
            is Ball.Wicket -> {
                var isStrikerOut = false
                if (ball.outPlayerId == (state as ScoreRecorderScreenState.InningsRunning).currentPlainStriker.id) {
                    isStrikerOut = true
//                    (state as ScoreRecorderScreenState.InningsRunning).currentPlainStriker
                } else {
                    isStrikerOut = false
//                    (state as ScoreRecorderScreenState.InningsRunning).currentPlainNonStriker
                }
//                battersHistory.add(outPlayer)

                state = inningsRunningState.copy(
                    wickets = inningsRunningState.wickets + 1,
                    score = inningsRunningState.score + ball.score,
                    balls = inningsRunningState.balls + 1,
                    allOvers = updateAllOversList(ball),
                )
            }
            is Ball.NoBall, is Ball.WideBall -> {
                state = inningsRunningState.copy(
                    score = inningsRunningState.score + ball.score + 1,
                    allOvers = updateAllOversList(ball),
                )
            }
        }
        if (state.asInningsRunning().balls % 6 == 0) {
            state = state.asInningsRunning().copy(isOverCompleted = true)
        }
        if (state.asInningsRunning().balls == state.asInningsRunning().totalOvers * 6) {
            state = ScoreRecorderScreenState.InningsBreak(
                previousInningsSummary = InningsSummary(
                    score = state.asInningsRunning().score,
                    wickets = state.asInningsRunning().wickets,
                    overs = state.asInningsRunning().balls / 6f,
                    allOvers = state.asInningsRunning().allOvers
                )
            )
        }
        state = state.asInningsRunning().copy(bowlersStats = getCurrentBowlerStats())
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
            val previousBall = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().balls.last()
            if (previousBall is Ball.Wicket) {
                battersHistory.removeLast()
            }
            state = previousState
            true
        } ?: run { false }

        /*if((state as ScoreRecorderScreenState.InningsRunning).allBalls.isNotEmpty()) {
            val lastBall = (state as ScoreRecorderScreenState.InningsRunning).allBalls.last()
            when(lastBall) {
                is Ball.DotBall -> {
                    state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                        balls = (state as ScoreRecorderScreenState.InningsRunning).balls - 1,
                        allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls.dropLast(1)
                    )
                }
                is Ball.CorrectBall -> {
                    state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                        balls = (state as ScoreRecorderScreenState.InningsRunning).balls - 1,
                        score = (state as ScoreRecorderScreenState.InningsRunning).score - lastBall.score,
                        allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls.dropLast(1)
                    )
                }
                is Ball.Wicket -> {
                    state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                        balls = (state as ScoreRecorderScreenState.InningsRunning).balls - 1,
                        wickets = (state as ScoreRecorderScreenState.InningsRunning).wickets - 1,
                        score = (state as ScoreRecorderScreenState.InningsRunning).score - lastBall.score,
                        allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls.dropLast(1)
                    )
                }
                is Ball.WideBall, is NoBall -> {
                    state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                        score = (state as ScoreRecorderScreenState.InningsRunning).score - 1 - lastBall.score,
                        allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls.dropLast(1)
                    )
                }
            }
            state = (state as ScoreRecorderScreenState.InningsRunning).copy()
        }*/
    }

    fun toggleStrike() {
        val currentStriker = state.asInningsRunning().currentPlainStriker
        val currentNonStriker = state.asInningsRunning().currentPlainNonStriker
        state = (state as ScoreRecorderScreenState.InningsRunning).copy(
            currentPlainStriker = currentNonStriker,
            currentPlainNonStriker = currentStriker
        )
    }

    fun onRetiredHurt(batterId: Long, newBatterName: String) {
        val currentStriker = state.asInningsRunning().currentPlainStriker
        val currentNonStriker = state.asInningsRunning().currentPlainNonStriker
        if (currentStriker.id == batterId) {
            state = state.asInningsRunning().copy(
                currentPlainStriker = PlainBatter(Clock.System.now().toEpochMilliseconds(), name = newBatterName),
                currentPlainNonStriker = currentNonStriker
            )
        } else if (currentNonStriker.id == batterId) {
            state = state.asInningsRunning().copy(
                currentPlainStriker = currentStriker,
                currentPlainNonStriker = PlainBatter(Clock.System.now().toEpochMilliseconds(), name = newBatterName)
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
        val oversBowled = (((oversByBowler.size - 1) + (oversByBowler.last().balls.filter { it.isValidBall() }.size * 0.1)).toFloat())
            .let { if (it % 1 == 0.6f) (it.toInt() + 1).toFloat() else it }
        val maidenOvers = oversByBowler.filter { it.balls.isNotEmpty() && it.balls.all { ball -> ball.score == 0 && ball.bowler == currentBowler } }.size
        val runsGiven = oversByBowler.sumOf { it.balls.sumOf { ball -> if (!ball.isValidBall()) ball.score + 1 else ball.score } }
        val economy = if (oversBowled > 0) {
            (runsGiven * 6.0 / (oversByBowler.map { it.balls }.flatten().filter { it.isValidBall() }.size.toFloat())).toFloat().roundToDecimals(2)
        } else {
            0f
        }
        val wicketsTaken = oversByBowler.sumOf { it.balls.count { ball -> ball is Ball.Wicket } }

        return BowlerStats(currentBowler, oversBowled, maidenOvers, runsGiven, wicketsTaken, economy)
    }

    fun getBattersStats(): BattersStats {
        val allBalls = state.asInningsRunning().allOvers.map { it.balls }.flatten()

        return BattersStats(
            strikerStats = BatterStats(
                batter = state.asInningsRunning().currentPlainStriker,
                runs = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainStriker }.sumOf { it.score },
                boundaries = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainStriker }.count { it.score == 4 },
                sixes = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainStriker }.count { it.score == 6 },
                ballsFaced = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainStriker }.count { it.score == 4 }
            ),
            nonStrikerStats = BatterStats(
                batter = state.asInningsRunning().currentPlainNonStriker,
                runs = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainNonStriker }.sumOf { it.score },
                boundaries = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainNonStriker }.count { it.score == 4 },
                sixes = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainNonStriker }.count { it.score == 6 },
                ballsFaced = allBalls.filter { it.iStriker == state.asInningsRunning().currentPlainNonStriker }.count { it.score == 4 }
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

    private fun Ball.isValidBall(): Boolean {
        return this is Ball.CorrectBall ||
                this is Ball.DotBall ||
                this is Ball.Wicket
    }
}