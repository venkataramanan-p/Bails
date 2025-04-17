package org.example.bails.data

import kotlinx.datetime.Clock
import org.example.bails.presentation.scoreRecorder.Over

data class Inning(
    val overs: List<Over>
)

object BailsDb {
    private val matchSummaries: MutableMap<Long, Inning> = mutableMapOf()

    fun updateMatchSummary(matchId: Long?, inning: Inning): Long {
        val matchId = matchId ?: Clock.System.now().toEpochMilliseconds()
        matchSummaries[matchId] = inning
        return matchId
    }

    fun getMatchSummary(matchId: Long): Inning? {
        return matchSummaries[matchId]
    }

}