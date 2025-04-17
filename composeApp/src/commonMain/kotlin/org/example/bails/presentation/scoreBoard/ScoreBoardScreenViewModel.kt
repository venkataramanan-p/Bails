package org.example.bails.presentation.scoreBoard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.example.bails.data.BailsDb
import org.example.bails.presentation.scoreRecorder.toInningsSummary

class ScoreBoardScreenViewModel(
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val matchId: Long = savedStateHandle["matchId"] ?: -1

    var state by mutableStateOf<ScoreBoardScreenState>(ScoreBoardScreenState.Loading)

    init {
        if (matchId == -1L) {
            // Handle the case where matchId is not provided
            // For example, you might want to throw an exception or log an error
        } else {
            // Load the match summary using the matchId
            loadMatchSummary(matchId)
        }
    }

    private fun loadMatchSummary(matchId: Long) {
        val matchSummary = BailsDb.getMatchSummary(matchId)
        matchSummary?.let {
            state = ScoreBoardScreenState.Success(matchSummary.toInningsSummary())
        }
    }
}