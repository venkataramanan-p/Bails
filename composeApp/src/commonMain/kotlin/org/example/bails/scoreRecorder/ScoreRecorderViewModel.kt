package org.example.bails.scoreRecorder

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlin.contracts.ExperimentalContracts

class ScoreRecorderViewModel(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val numberOfOvers = savedStateHandle["numberOfOvers"] ?: 0
    private val strikerName = savedStateHandle["strikerName"] ?: ""
    private val nonStrikerName = savedStateHandle["nonStrikerName"] ?: ""
    private val bowlerName = savedStateHandle["bowlerName"] ?: ""

    var state: ScoreRecorderScreenState by mutableStateOf(
        ScoreRecorderScreenState.InningsRunning(
            balls = 0,
            score = 0,
            wickets = 0,
            allBalls = emptyList(),
            totalOvers = numberOfOvers,
            currentStriker = Player(strikerName, score = 0, isBatting = true),
            currentNonStriker = Player(nonStrikerName, score = 0, isBatting = false),
            bowlerName = Bowler(bowlerName)
        )
    )

    fun recordBall(ball: Ball) {

        val batter1 = (state as ScoreRecorderScreenState.InningsRunning).currentStriker
        val batter2 = (state as ScoreRecorderScreenState.InningsRunning).currentNonStriker
        val isStrikeChanged = ball.score % 2 == 1

        when(ball.ballType) {
            BallType.DOT_BALL -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball,
                    currentStriker = if (batter1?.isBatting == true) batter1.copy(ballsFaced = batter1.ballsFaced + 1) else batter1,
                    currentNonStriker = if (batter2?.isBatting == true) batter2.copy(ballsFaced = batter2.ballsFaced + 1) else batter2
                )

            }
            BallType.CORRECT_BALL -> {

                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score,
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball,
                    currentStriker = if (batter1?.isBatting == true) batter1.copy(score = batter1.score + ball.score, ballsFaced = batter1.ballsFaced + 1, isBatting = if (isStrikeChanged) !batter1.isBatting else batter1.isBatting) else batter1?.copy(isBatting = if (isStrikeChanged) !batter1.isBatting else batter1.isBatting),
                    currentNonStriker = if (batter2?.isBatting == true) batter2.copy(ballsFaced = batter2.ballsFaced + 1, score = batter2.score + ball.score, isBatting = if (isStrikeChanged) !batter2.isBatting else batter2.isBatting) else batter2?.copy(isBatting = if (isStrikeChanged) !batter2.isBatting else batter2.isBatting)
                )
            }
            BallType.WICKET -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    wickets = (state as ScoreRecorderScreenState.InningsRunning).wickets + 1,
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score,
                    balls = (state as ScoreRecorderScreenState.InningsRunning).balls + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball,
                    currentStriker = if (batter1?.isBatting == true) Player(displayName = "Unknown player 1", score = 0, ballsFaced = 0, isBatting = true) else batter1,
                    currentNonStriker = if (batter2?.isBatting == true) Player(displayName = "Unknown player 1", score = 0, ballsFaced = 0, isBatting = true) else batter2
                )
            }
            BallType.WIDE, BallType.NO_BALL -> {
                state = (state as ScoreRecorderScreenState.InningsRunning).copy(
                    score = (state as ScoreRecorderScreenState.InningsRunning).score + ball.score + 1,
                    allBalls = (state as ScoreRecorderScreenState.InningsRunning).allBalls + ball,
                    currentStriker = if (batter1?.isBatting == true) batter1.copy(score = batter1.score + ball.score, ballsFaced = batter1.ballsFaced) else batter1,
                    currentNonStriker = if (batter2?.isBatting == true) batter2.copy(score = batter2.score + ball.score, ballsFaced = batter2.ballsFaced) else batter2
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

//@OptIn(ExperimentalContracts::class)
//inline fun <T> Collection<T>?.isNullOrEmpty(): Boolean {
//    contract {
//        returns(false) implies (this@isNullOrEmpty != null)
//    }
//
//    return this == null || this.isEmpty()
//}

fun printEachLine() {
    var list: MutableList<String>? = mutableListOf()
    if (!list.isNullOrEmpty()) {
        for (e in list) { //list smart-casted to List<String>
            println(e)
        }
    }
}

//fun printt(state: ScoreRecorderScreenState) {
//    println(state)
//}
//
@OptIn(ExperimentalContracts::class)
fun ScoreRecorderScreenState?.isInningsRunning(): Boolean {
//    contract {
//        returns(true) implies (this@isInningsRunning is ScoreRecorderScreenState.InningsRunning)
//    }

    return this is ScoreRecorderScreenState.InningsRunning
}
//
//@OptIn(ExperimentalContracts::class)
//fun ScoreRecorderScreenState?.isNotNull(): Boolean {
//    contract {
//        returns(true) implies (this@isNotNull != null)
//    }
//
//    return this != null
//}
//
//sealed class Alphabet {
//    class A(): Alphabet() {
//        fun print() {}
//    }
//    class b(): Alphabet() {
//        fun log() {}
//    }
//}

//fun main() {
////    val alphabet: Alphabet = Alphabet.A()
////
////    if (alphabet is Alphabet.A) {
////        alphabet.print()
////    }
//
//    val foo: Int? = 42
//    if (foo.isNotNull()) {
//        printAny(foo)
//    }
//}

//fun printAny(obj: Any) {
//    println(obj)
//}
//
//@OptIn(ExperimentalContracts::class)
//fun Any?.isNotNull(): Boolean {
//    contract {
//        returns(true) implies (this@isNotNull != null)
//    }
//    return this != null
//}
//
//@OptIn(ExperimentalContracts::class)
//fun Alphabet?.isA(): Boolean {
//    contract {
//        returns(true) implies (this@isA is Alphabet.A)
//    }
//    return this is Alphabet.A
//}
//
//
//sealed class Alphabet {
//    class A(): Alphabet() {
//        fun print() { println("I'm A") }
//    }
//    class B(): Alphabet() {
//        fun log() { }
//    }
//}
//
//fun main() {
//    var alphabet: Alphabet by Alphabet.A()
//
//    if (alphabet is Alphabet.A) {
//        alphabet.print()
//    }
//}