package org.example.bails.data

import kotlinx.datetime.Clock
import org.example.bails.presentation.scoreRecorder.Over

data class Inning(
    val overs: List<Over>
)

object BailsDb {
    private val matchSummaries: MutableMap<Long, Pair<Inning, Inning>> = mutableMapOf()

    fun updateMatchSummary(matchId: Long, isFirstInning: Boolean, inning: Inning) {
        if (isFirstInning) {
            matchSummaries[matchId] = Pair(inning, matchSummaries[matchId]?.second ?: Inning(emptyList()))
        } else {
            val existingInning = matchSummaries[matchId]?.first
            if (existingInning != null) {
                matchSummaries[matchId] = Pair(existingInning, inning)
            } else {
                matchSummaries[matchId] = Pair(Inning(emptyList()), inning)
            }
        }
    }

    fun getMatchSummary(matchId: Long): Pair<Inning, Inning>? {
        return matchSummaries[matchId]
    }

}