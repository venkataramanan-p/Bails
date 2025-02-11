package org.example.bails.scoreRecorder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class ScoreRecorderViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val numberOfOvers = savedStateHandle["numberOfOvers"] ?: 0

    var state: ScoreRecorderScreenState by mutableStateOf(
        ScoreRecorderScreenState.InningsRunning(
            balls = 0,
            score = 0,
            wickets = 0,
            allBalls = emptyList(),
            totalOvers = numberOfOvers
        )
    )

    fun recordBall(ball: Ball) {
        when(ball.ballType) {
            BallType.DOT_BALL -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball
                )
            }
            BallType.CORRECT_BALL -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score,
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball
                )
            }
            BallType.WICKET -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    wickets = (state as ScoreRecorderScreenState.InningsRunning).wickets + 1,
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score,
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball
                )
            }
            BallType.WIDE, BallType.NO_BALL -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball
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

    fun undoLastBall() {
        if((state as ScoreRecorderScreenState.InningsRunning).allBalls.isNotEmpty()) {
            val lastBall = (state as ScoreRecorderScreenState.InningsRunning).allBalls.last()
            when(lastBall.ballType) {
                BallType.DOT_BALL -> {
                    state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                        balls = (state as ScoreRecorderScreenState.InningsRunning).balls - 1,
                        allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls.dropLast(1)
                    )
                }
                BallType.CORRECT_BALL -> {
                    state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                        balls = (state as ScoreRecorderScreenState.InningsRunning).balls - 1,
                        score = (state as ScoreRecorderScreenState.InningsRunning).score - lastBall.score,
                        allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls.dropLast(1)
                    )
                }
                BallType.WICKET -> {
                    state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                        balls = (state as ScoreRecorderScreenState.InningsRunning).balls - 1,
                        wickets = (state as ScoreRecorderScreenState.InningsRunning).wickets - 1,
                        score = (state as ScoreRecorderScreenState.InningsRunning).score - lastBall.score,
                        allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls.dropLast(1)
                    )
                }
                BallType.WIDE, BallType.NO_BALL -> {
                    state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                        score = (state as ScoreRecorderScreenState.InningsRunning).score - 1 - lastBall.score,
                        allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls.dropLast(1)
                    )
                }
            }
            state = (state as ScoreRecorderScreenState.InningsRunning).copy()
        }
    }

    fun startNextInnings() {
        state = ScoreRecorderScreenState.InningsRunning(
            balls = 0,
            score = 0,
            wickets = 0,
            allBalls = emptyList(),
            totalOvers = numberOfOvers,
            previousInningsSummary = (state as ScoreRecorderScreenState.InningsBreak).previousInningsSummary
        )
    }
}

