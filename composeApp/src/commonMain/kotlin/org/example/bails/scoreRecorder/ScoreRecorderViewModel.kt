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

    var state: ScoreRecorderScreenState by mutableStateOf(
        ScoreRecorderScreenState.InningsRunning(
            balls = 0,
            score = 0,
            wickets = 0,
            allOvers = listOf(Over(bowler = Bowler(id = Clock.System.now().toEpochMilliseconds(), bowlerName), balls = emptyList())),
            totalOvers = numberOfOvers,
            currentStriker = Batter(id = Clock.System.now().toEpochMilliseconds(), strikerName),
            currentNonStriker = Batter(id = Clock.System.now().toEpochMilliseconds() + 1, nonStrikerName),
            bowlersStats = BowlerStats(
                bowler = Bowler(id = Clock.System.now().toEpochMilliseconds(), bowlerName),
                overs = 0f,
                maidenOvers = 0,
                runs = 0,
                wickets = 0,
                economy = 0f
            )
        )
    )
    private val battersHistory = mutableListOf<Batter>()
    private val bowlersHistory = mutableListOf<Bowler>()

    fun recordBall(ball: Ball) {
        previousBallState = state as ScoreRecorderScreenState.InningsRunning
        val striker = (state as ScoreRecorderScreenState.InningsRunning).currentStriker!!
        val nonStriker = (state as ScoreRecorderScreenState.InningsRunning).currentNonStriker!!
        val isStrikeChanged = ball.score % 2 == 1

        when(ball) {
            is Ball.DotBall -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allOvers = (state as ScoreRecorderScreenState.InningsRunning).allOvers.dropLast(1) +
                            Over(
                                bowler = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().bowler,
                                balls = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().balls + ball
                            ),
                    currentStriker = striker.copy(ballsFaced = striker.ballsFaced + 1),
                    currentNonStriker = nonStriker,
                )

            }
            is Ball.CorrectBall -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score,
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allOvers = (state as ScoreRecorderScreenState.InningsRunning).allOvers.dropLast(1) +
                            Over(
                                bowler = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().bowler,
                                balls = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().balls + ball
                            ),
                    currentStriker = if(isStrikeChanged) nonStriker else updateBatter(striker, ball),
                    currentNonStriker =  if(isStrikeChanged) updateBatter(striker, ball) else nonStriker
                )
            }
            is Ball.Wicket -> {
                var isStrikerOut = false
                val outPlayer = if (ball.outPlayerId == (state as ScoreRecorderScreenState.InningsRunning).currentStriker?.id) {
                    isStrikerOut = true
                    (state as ScoreRecorderScreenState.InningsRunning).currentStriker
                } else {
                    isStrikerOut = false
                    (state as ScoreRecorderScreenState.InningsRunning).currentNonStriker
                }
                outPlayer?.let {
                    battersHistory.add(outPlayer)
                }

                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    wickets = (state as ScoreRecorderScreenState.InningsRunning).wickets + 1,
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score,
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allOvers = (state as ScoreRecorderScreenState.InningsRunning).allOvers.dropLast(1) +
                            Over(
                                bowler = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().bowler,
                                balls = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().balls + ball
                            ),
                    currentStriker = if(isStrikerOut) Batter(Clock.System.now().toEpochMilliseconds(), name = ball.newPlayerName)
                                    else (state as ScoreRecorderScreenState.InningsRunning).currentStriker,
                    currentNonStriker = if(!isStrikerOut) Batter(Clock.System.now().toEpochMilliseconds(), name = ball.newPlayerName)
                                    else (state as ScoreRecorderScreenState.InningsRunning).currentNonStriker
                )
            }
            is Ball.NoBall, is Ball.WideBall -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score + 1,
                    allOvers = (state as ScoreRecorderScreenState.InningsRunning).allOvers.dropLast(1) +
                            Over(
                                bowler = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().bowler,
                                balls = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().balls + ball
                            ),
                    currentStriker = if(isStrikeChanged) nonStriker else updateBatter(striker, ball),
                    currentNonStriker = if(isStrikeChanged) updateBatter(striker, ball) else nonStriker
                )
            }
        }
        if ((state as ScoreRecorderScreenState.InningsRunning).balls % 6 == 0) {
            state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                isOverCompleted = true
            )
        }
        if ((state as ScoreRecorderScreenState.InningsRunning).balls == (state as ScoreRecorderScreenState.InningsRunning).totalOvers * 6) {
            state = ScoreRecorderScreenState.InningsBreak(
                previousInningsSummary = InningsSummary(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score,
                    wickets = (state as ScoreRecorderScreenState.InningsRunning).wickets,
                    overs = (state as ScoreRecorderScreenState.InningsRunning).balls / 6f,
                    allOvers = (state as ScoreRecorderScreenState.InningsRunning).allOvers
                )
            )
        }
        state = (state as ScoreRecorderScreenState.InningsRunning).copy(bowlersStats = getCurrentBowlerStats())
    }

    fun startNextOver(bowlerId: Long?, bowlerName: String?) {
        val nextBowler = bowlerId?.let { bowlerId ->
            (state as ScoreRecorderScreenState.InningsRunning).allOvers
                .map { it.bowler }
                .find { it.id == bowlerId }
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
            currentStriker = (state as ScoreRecorderScreenState.InningsRunning).currentNonStriker,
            currentNonStriker = (state as ScoreRecorderScreenState.InningsRunning).currentStriker,
            allOvers = (state as ScoreRecorderScreenState.InningsRunning).allOvers + Over(
                bowler = nextBowler,
                balls = emptyList()
            ),
            isOverCompleted = false,
        )
        state = (state as ScoreRecorderScreenState.InningsRunning).copy(
            bowlersStats = getCurrentBowlerStats()
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
        val currentStriker = (state as ScoreRecorderScreenState.InningsRunning).currentStriker
        val currentNonStriker = (state as ScoreRecorderScreenState.InningsRunning).currentNonStriker
        state = (state as ScoreRecorderScreenState.InningsRunning).copy(
            currentStriker = currentNonStriker,
            currentNonStriker = currentStriker
        )
    }

    fun onRetiredHurt(batterId: Long, newBatterName: String) {
        val currentStriker = (state as ScoreRecorderScreenState.InningsRunning).currentStriker
        val currentNonStriker = (state as ScoreRecorderScreenState.InningsRunning).currentNonStriker
        if (currentStriker.id == batterId) {
            state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                currentStriker = Batter(Clock.System.now().toEpochMilliseconds(), name = newBatterName),
                currentNonStriker = currentNonStriker
            )
        } else if (currentNonStriker.id == batterId) {
            state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                currentStriker = currentStriker,
                currentNonStriker = Batter(Clock.System.now().toEpochMilliseconds(), name = newBatterName)
            )
        }
    }

    fun startNextInnings() {
//        state = ScoreRecorderScreenState.InningsRunning(
//            balls = 0,
//            score = 0,
//            wickets = 0,
//            allBalls = emptyList(),
//            totalOvers = numberOfOvers,
//            previousInningsSummary = (state as ScoreRecorderScreenState.InningsBreak).previousInningsSummary
//        )

    }

    fun getCurrentBowlerStats(): BowlerStats {
        val currentBowler = (state as ScoreRecorderScreenState.InningsRunning).allOvers.last().bowler
        val oversByBowler = (state as ScoreRecorderScreenState.InningsRunning).allOvers
            .filter { it.bowler.id == currentBowler.id }
        val oversBowled = (((oversByBowler.size - 1) + (oversByBowler.last().balls.filter { it.isValidBall() }.size * 0.1)).toFloat())
            .let { if (it % 1 == 0.6f) (it.toInt() + 1).toFloat() else it }
        val maidenOvers = oversByBowler.filter { it.balls.isNotEmpty() && it.balls.all { ball -> ball.score == 0 } }.size
        val runsGiven = oversByBowler.sumOf { it.balls.sumOf { ball -> if (!ball.isValidBall()) ball.score + 1 else ball.score } }
        val economy = if (oversBowled > 0) {
            (runsGiven * 6.0 / (oversByBowler.map { it.balls }.flatten().filter { it.isValidBall() }.size.toFloat())).toFloat().roundToDecimals(2)
        } else {
            0f
        }
        val wicketsTaken = oversByBowler.sumOf { it.balls.count { ball -> ball is Ball.Wicket } }

        return BowlerStats(currentBowler, oversBowled, maidenOvers, runsGiven, wicketsTaken, economy)
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