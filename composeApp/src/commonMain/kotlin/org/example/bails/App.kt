package org.example.bails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.example.bails.BailsScreens.ScoreBoard
import org.example.bails.BailsScreens.ScoreRecorder
import org.example.bails.presentation.matchConfig.MatchConfigScreen
import org.example.bails.presentation.matchHistory.MatchHistoryScreen
import org.example.bails.presentation.matchHistory.MatchHistoryViewModel
import org.example.bails.presentation.scoreBoard.ScoreBoardScreen
import org.example.bails.presentation.scoreBoard.ScoreBoardScreenViewModel
import org.example.bails.presentation.scoreRecorder.ScoreRecorderScreen
import org.example.bails.presentation.scoreRecorder.ScoreRecorderViewModel
import org.example.bails.ui.theme.BailsTheme


sealed interface BailsScreens {
    @Serializable
    data object MatchHistory : BailsScreens

    @Serializable
    data class MatchConfig(val matchId: Long? = null) : BailsScreens

    @Serializable
    data class ScoreRecorder(
        val matchId: Long? = null,
        val numberOfOvers: Int,
        val teamName: String,
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
    BailsTheme {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = BailsScreens.MatchHistory) {

            composable<BailsScreens.MatchHistory> {
                val viewModel: MatchHistoryViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                MatchHistoryScreen(
                    state = state,
                    onNewMatch = {
                        navController.navigate(BailsScreens.MatchConfig())
                    },
                    onMatchClick = { matchId ->
                        navController.navigate(ScoreBoard(matchId))
                    },
                    onDeleteMatch = viewModel::deleteMatch
                )
            }

            composable<BailsScreens.MatchConfig> { backStackEntry ->
                val matchConfig = backStackEntry.toRoute<BailsScreens.MatchConfig>()

                MatchConfigScreen(
                    matchId = matchConfig.matchId,
                    onStartMatch = { numberOfOvers, teamName, strikerName, nonStrikerName, bowlerName ->
                        navController.navigate(
                            ScoreRecorder(
                                numberOfOvers = numberOfOvers,
                                teamName = teamName,
                                strikerName = strikerName,
                                nonStrikerName = nonStrikerName,
                                bowlerName = bowlerName,
                                matchId = matchConfig.matchId
                            )
                        )
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
                    goHome = {
                        navController.navigate(BailsScreens.MatchHistory) {
                            popUpTo(BailsScreens.MatchHistory) { inclusive = true }
                        }
                    },
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
                    },
                    onGoHome = {
                        navController.navigate(BailsScreens.MatchHistory) {
                            popUpTo(BailsScreens.MatchHistory) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
