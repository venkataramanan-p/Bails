package org.example.bails.presentation.scoreBoard

import org.example.bails.presentation.scoreRecorder.InningsSummary

sealed interface ScoreBoardScreenState {

    object Loading: ScoreBoardScreenState
    data class Success(
        val firstInnings: InningsSummary,
        val secondInnings: InningsSummary,
        val team1Name: String = "Team 1",
        val team2Name: String = "Team 2"
    ): ScoreBoardScreenState
}