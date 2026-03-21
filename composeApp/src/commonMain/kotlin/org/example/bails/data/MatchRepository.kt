package org.example.bails.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.bails.data.entities.MatchEntity
import org.example.bails.presentation.scoreRecorder.Ball
import org.example.bails.presentation.scoreRecorder.Over

class MatchRepository(
    private val matchDao: MatchDao = DatabaseProvider.getDatabase().matchDao()
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun createMatch(matchId: Long, totalOvers: Int, team1Name: String) {
        matchDao.insertMatch(
            MatchEntity(
                matchId = matchId,
                totalOvers = totalOvers,
                firstInningsJson = json.encodeToString(emptyList<Over>()),
                secondInningsJson = json.encodeToString(emptyList<Over>()),
                createdAt = matchId,
                team1Name = team1Name
            )
        )
    }

    suspend fun getTotalOvers(matchId: Long): Int? {
        return matchDao.getTotalOvers(matchId)
    }

    data class MatchDetail(
        val firstInning: Inning,
        val secondInning: Inning,
        val team1Name: String,
        val team2Name: String
    )

    suspend fun getMatchSummary(matchId: Long): MatchDetail? {
        val entity = matchDao.getMatch(matchId) ?: return null
        val firstOvers: List<Over> = json.decodeFromString(entity.firstInningsJson)
        val secondOvers: List<Over> = json.decodeFromString(entity.secondInningsJson)
        return MatchDetail(
            firstInning = Inning(firstOvers),
            secondInning = Inning(secondOvers),
            team1Name = entity.team1Name,
            team2Name = entity.team2Name
        )
    }

    suspend fun updateInnings(matchId: Long, isFirstInning: Boolean, inning: Inning) {
        val inningsJson = json.encodeToString(inning.overs)
        if (isFirstInning) {
            matchDao.updateFirstInnings(matchId, inningsJson)
        } else {
            matchDao.updateSecondInnings(matchId, inningsJson)
        }
    }

    suspend fun markMatchCompleted(matchId: Long) {
        matchDao.markMatchCompleted(matchId)
    }

    suspend fun updateTeam2Name(matchId: Long, team2Name: String) {
        matchDao.updateTeam2Name(matchId, team2Name)
    }

    fun getAllMatches(): Flow<List<MatchSummaryItem>> {
        return matchDao.getAllMatches().map { entities ->
            entities.map { entity ->
                val firstOvers: List<Over> = json.decodeFromString(entity.firstInningsJson)
                val secondOvers: List<Over> = json.decodeFromString(entity.secondInningsJson)
                MatchSummaryItem(
                    matchId = entity.matchId,
                    totalOvers = entity.totalOvers,
                    team1Name = entity.team1Name,
                    team2Name = entity.team2Name,
                    firstInningsScore = calculateScore(firstOvers),
                    firstInningsWickets = calculateWickets(firstOvers),
                    firstInningsOvers = calculateOversCount(firstOvers),
                    secondInningsScore = calculateScore(secondOvers),
                    secondInningsWickets = calculateWickets(secondOvers),
                    secondInningsOvers = calculateOversCount(secondOvers),
                    createdAt = entity.createdAt,
                    isCompleted = entity.isCompleted
                )
            }
        }
    }

    suspend fun deleteMatch(matchId: Long) {
        matchDao.deleteMatch(matchId)
    }

    private fun calculateScore(overs: List<Over>): Int {
        return overs.flatMap { it.balls }.sumOf { ball ->
            when (ball) {
                is Ball.WideBall, is Ball.NoBall -> ball.score + 1
                else -> ball.score
            }
        }
    }

    private fun calculateWickets(overs: List<Over>): Int {
        return overs.flatMap { it.balls }.count { it is Ball.Wicket }
    }

    private fun calculateOversCount(overs: List<Over>): Float {
        val allBalls = overs.flatMap { it.balls }
        val validBalls = allBalls.count { it !is Ball.WideBall && it !is Ball.NoBall }
        val completedOvers = validBalls / 6
        val remainingBalls = validBalls % 6
        return completedOvers + remainingBalls * 0.1f
    }
}

data class MatchSummaryItem(
    val matchId: Long,
    val totalOvers: Int,
    val team1Name: String,
    val team2Name: String,
    val firstInningsScore: Int,
    val firstInningsWickets: Int,
    val firstInningsOvers: Float,
    val secondInningsScore: Int,
    val secondInningsWickets: Int,
    val secondInningsOvers: Float,
    val createdAt: Long,
    val isCompleted: Boolean
)
