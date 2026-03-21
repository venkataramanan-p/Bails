package org.example.bails.presentation.matchHistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.example.bails.data.MatchRepository
import org.example.bails.data.MatchSummaryItem

class MatchHistoryViewModel : ViewModel() {

    private val repository = MatchRepository()

    val state: StateFlow<MatchHistoryState> = repository.getAllMatches()
        .map { matches ->
            if (matches.isEmpty()) {
                MatchHistoryState.Empty
            } else {
                MatchHistoryState.Success(matches)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MatchHistoryState.Loading)

    fun deleteMatch(matchId: Long) {
        viewModelScope.launch {
            repository.deleteMatch(matchId)
        }
    }
}

sealed interface MatchHistoryState {
    data object Loading : MatchHistoryState
    data object Empty : MatchHistoryState
    data class Success(val matches: List<MatchSummaryItem>) : MatchHistoryState
}
