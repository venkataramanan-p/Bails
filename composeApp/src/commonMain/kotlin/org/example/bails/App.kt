package org.example.bails

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.example.bails.BailsScreens.ScoreBoard
import org.example.bails.BailsScreens.ScoreRecorder
import org.example.bails.presentation.matchConfig.MatchConfigScreen
import org.example.bails.presentation.scoreBoard.ScoreBoardScreen
import org.example.bails.presentation.scoreBoard.ScoreBoardScreenViewModel
import org.example.bails.presentation.scoreRecorder.ScoreRecorderScreen
import org.example.bails.presentation.scoreRecorder.ScoreRecorderViewModel


sealed interface BailsScreens {
    @Serializable
    data class MatchConfig(val matchId: Long? = null) : BailsScreens

    @Serializable
    data class ScoreRecorder(
        val matchId: Long? = null,
        val numberOfOvers: Int,
        val strikerName: String,
        val nonStrikerName: String,
        val bowlerName: String
    ) : BailsScreens

    @Serializable
    data class ScoreBoard(
        val matchId: Long
    )
}

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = BailsScreens.MatchConfig::class) {
        composable<BailsScreens.MatchConfig> { backStackEntry ->
            val matchConfig = backStackEntry.toRoute<BailsScreens.MatchConfig>()

            MatchConfigScreen(
                onStartMatch = { numberOfOvers, strikerName, nonStrikerName, bowlerName ->
                    navController.navigate(ScoreRecorder(
                        numberOfOvers = numberOfOvers,
                        strikerName = strikerName,
                        nonStrikerName = nonStrikerName,
                        bowlerName = bowlerName,
                        matchId = matchConfig.matchId
                    ))
                }
            )
        }

        composable<ScoreRecorder> { backStackEntry ->
            val viewmodel: ScoreRecorderViewModel = viewModel()

            ScoreRecorderScreen(
                state = viewmodel.state,
                undoLastBall = viewmodel::undoLastBall,
                recordBall = viewmodel::recordBall,
                onStartNextInnings = viewmodel::startNextInnings,
                onStartNextOver = viewmodel::startNextOver,
                goBack = navController::navigateUp,
                onToggleStrike = viewmodel::toggleStrike,
                onRetiredHurt = viewmodel::onRetiredHurt,
                onChangeBowler = viewmodel::onChangeBowler,
                navigateToScoreBoard = {
                    navController.navigate(ScoreBoard(viewmodel.matchId))
                }
            )
        }

        composable<ScoreBoard> {
            val viewmodel: ScoreBoardScreenViewModel = viewModel()

            ScoreBoardScreen(
                state = viewmodel.state,
                onStartNextInnings = {
                    val matchId = viewmodel.matchId
                    navController.navigate(
                        BailsScreens.MatchConfig(matchId = matchId)
                    )
                }
            )
        }
    }
}