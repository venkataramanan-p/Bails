package org.example.bails

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ScoreboardViewModel: ViewModel() {

    var balls = mutableStateOf(0)
    var score = mutableStateOf(0)
    var wickets = mutableStateOf(0)
    var allBalls = mutableStateOf<List<Ball>>(emptyList())

    fun increaseBall() {
        balls.value++
    }

    fun addScore(score: Int) {
        this.score.value += score
    }

    fun addWicket(wicket: Int) {
        wickets.value += wicket
    }

    fun recordBall(ball: Ball) {
        when(ball.ballType) {
            BallType.DOT_BALL -> {
                increaseBall()
            }
            BallType.CORRECT_BALL -> {
                increaseBall()
                addScore(ball.score)
            }
            BallType.WICKET -> {
                addWicket(1)
                addScore(ball.score)
                increaseBall()
            }
            BallType.WIDE, BallType.NO_BALL -> {
                addScore(1 + ball.score)
            }
        }
        allBalls.value += ball
    }

    fun undoLastBall() {
        if(allBalls.value.isNotEmpty()) {
            val lastBall = allBalls.value.last()
            when(lastBall.ballType) {
                BallType.DOT_BALL -> {
                    balls.value--
                }
                BallType.CORRECT_BALL -> {
                    balls.value--
                    score.value -= lastBall.score
                }
                BallType.WICKET -> {
                    wickets.value--
                    score.value -= lastBall.score
                    balls.value--
                }
                BallType.WIDE, BallType.NO_BALL -> {
                    score.value -= 1 + lastBall.score
                }
            }
            allBalls.value = allBalls.value.dropLast(1)
        }
    }
}