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

enum class BallType(val displayStr: String) {
    CORRECT_BALL("Correct Ball"),
    WIDE("Wide"),
    NO_BALL("No Ball"),
    DOT_BALL("Dot ball"),
    WICKET("Wicket")
}

data class Ball(
    val ballType: BallType,
    val score: Int = 0
)

sealed interface BailsScreens {
    @Serializable
    data object MatchConfig : BailsScreens
    @Serializable
    data class ScoreRecorder(val numberOfOvers: Int) : BailsScreens
}

@Composable
fun App() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = BailsScreens.MatchConfig::class) {
        composable<BailsScreens.MatchConfig> {
            MatchConfigScreen(
                onStartMatch = { numberOfOvers ->
                    navController.navigate(BailsScreens.ScoreRecorder(numberOfOvers = numberOfOvers))
                }
            )
        }

        composable<BailsScreens.ScoreRecorder> {
            val viewmodel: ScoreRecorderViewModel = viewModel()

            ScoreRecorderScreen(viewmodel)
        }
    }
}