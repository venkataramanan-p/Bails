package org.example.bails.data

import kotlinx.datetime.Clock
import org.example.bails.presentation.scoreRecorder.Over

data class Inning(
    val overs: List<Over>
)

object BailsDb {
    private val matchSummaries: MutableMap<Long, Inning> = mutableMapOf()

    fun updateMatchSummary(matchId: Long, inning: Inning) {
        matchSummaries[matchId] = inning
    }

    fun getMatchSummary(matchId: Long): Inning? {
        return matchSummaries[matchId]
    }

}