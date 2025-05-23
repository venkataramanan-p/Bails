package org.example.bails.presentation.scoreBoard

import org.example.bails.presentation.scoreRecorder.InningsSummary

sealed interface ScoreBoardScreenState {

    object Loading: ScoreBoardScreenState
    data class Success(
        val firstInnings: InningsSummary,
        val secondInnings: InningsSummary
    ): ScoreBoardScreenState
}