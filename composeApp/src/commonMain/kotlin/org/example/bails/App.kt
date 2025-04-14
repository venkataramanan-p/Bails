package org.example.bails

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.example.bails.matchConfig.MatchConfigScreen
import org.example.bails.scoreRecorder.ScoreRecorderScreen
import org.example.bails.scoreRecorder.ScoreRecorderViewModel


sealed interface BailsScreens {
    @Serializable
    data object MatchConfig : BailsScreens
    @Serializable
    data class ScoreRecorder(
        val numberOfOvers: Int,
        val strikerName: String,
        val nonStrikerName: String,
        val bowlerName: String
    ) : BailsScreens
}

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = BailsScreens.MatchConfig::class) {
        composable<BailsScreens.MatchConfig> {
            MatchConfigScreen(
                onStartMatch = { numberOfOvers, strikerName, nonStrikerName, bowlerName ->
                    navController.navigate(BailsScreens.ScoreRecorder(
                        numberOfOvers = numberOfOvers,
                        strikerName = strikerName,
                        nonStrikerName = nonStrikerName,
                        bowlerName = bowlerName
                    ))
                }
            )
        }

        composable<BailsScreens.ScoreRecorder> {
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
                onChangeBowler = viewmodel::onChangeBowler
            )
        }
    }
}