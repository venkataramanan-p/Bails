package org.example.bails.scoreRecorder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.datetime.Clock
import org.example.bails.scoreRecorder.Ball.NoBall

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
            allBalls = emptyList(),
            totalOvers = numberOfOvers,
            currentStriker = Batter(id = Clock.System.now().toEpochMilliseconds(), strikerName),
            currentNonStriker = Batter(id = Clock.System.now().toEpochMilliseconds() + 1, nonStrikerName),
            bowlerName = Bowler(bowlerName)
        )
    )
    private val battersHistory = mutableListOf<Batter>()

    fun recordBall(ball: Ball) {
        previousBallState = state as ScoreRecorderScreenState.InningsRunning
        val striker = (state as ScoreRecorderScreenState.InningsRunning).currentStriker!!
        val nonStriker = (state as ScoreRecorderScreenState.InningsRunning).currentNonStriker!!
        val isStrikeChanged = ball.score % 2 == 1

        when(ball) {
            is Ball.DotBall -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball,
                    currentStriker = striker.copy(ballsFaced = striker.ballsFaced + 1),
                    currentNonStriker = nonStriker
                )

            }
            is Ball.CorrectBall -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score,
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball,
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
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball,
                    currentStriker = if(isStrikerOut) Batter(Clock.System.now().toEpochMilliseconds(), name = ball.newPlayerName)
                                    else (state as ScoreRecorderScreenState.InningsRunning).currentStriker,
                    currentNonStriker = if(!isStrikerOut) Batter(Clock.System.now().toEpochMilliseconds(), name = ball.newPlayerName)
                                    else (state as ScoreRecorderScreenState.InningsRunning).currentNonStriker
                )
            }
            is Ball.NoBall, is Ball.WideBall -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball,
                    currentStriker = if(isStrikeChanged) nonStriker else updateBatter(striker, ball),
                    currentNonStriker = if(isStrikeChanged) updateBatter(striker, ball) else nonStriker
                )
            }
        }
        if ((state as ScoreRecorderScreenState.InningsRunning).balls == (state as ScoreRecorderScreenState.InningsRunning).totalOvers * 6) {
            state = ScoreRecorderScreenState.InningsBreak(
                previousInningsSummary = InningsSummary(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score,
                    wickets = (state as ScoreRecorderScreenState.InningsRunning).wickets,
                    overs = (state as ScoreRecorderScreenState.InningsRunning).balls / 6f,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls
                )
            )
        }
    }

    fun undoLastBall(): Boolean {
        return previousBallState?.let { previousState ->
            val previousBall = (state as ScoreRecorderScreenState.InningsRunning).allBalls.last()
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

    private fun updateBatter(batter: Batter, ball: Ball): Batter {
        return batter.copy(
            ballsFaced = batter.ballsFaced + 1,
            boundaries = if (ball.score == 4) batter.boundaries + 1 else batter.boundaries,
            sixes = if (ball.score == 6) batter.sixes + 1 else batter.sixes,
            runs = batter.runs + ball.score,
        )
    }
}