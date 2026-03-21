package org.example.bails.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.example.bails.data.entities.MatchEntity

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Query("SELECT * FROM matches WHERE matchId = :matchId")
    suspend fun getMatch(matchId: Long): MatchEntity?

    @Query("SELECT * FROM matches ORDER BY createdAt DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT totalOvers FROM matches WHERE matchId = :matchId")
    suspend fun getTotalOvers(matchId: Long): Int?

    @Query("UPDATE matches SET firstInningsJson = :inningsJson WHERE matchId = :matchId")
    suspend fun updateFirstInnings(matchId: Long, inningsJson: String)

    @Query("UPDATE matches SET secondInningsJson = :inningsJson WHERE matchId = :matchId")
    suspend fun updateSecondInnings(matchId: Long, inningsJson: String)

    @Query("UPDATE matches SET isCompleted = 1 WHERE matchId = :matchId")
    suspend fun markMatchCompleted(matchId: Long)

    @Query("UPDATE matches SET team2Name = :team2Name WHERE matchId = :matchId")
    suspend fun updateTeam2Name(matchId: Long, team2Name: String)

    @Query("DELETE FROM matches WHERE matchId = :matchId")
    suspend fun deleteMatch(matchId: Long)
}
