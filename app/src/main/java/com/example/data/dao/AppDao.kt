package com.example.data.dao

import androidx.room.*
import com.example.data.model.Lesson
import com.example.data.model.UserProgress
import com.example.data.model.SavedAiMessage
import com.example.data.model.LeaderboardPeer
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProgress(progress: UserProgress)

    @Query("SELECT * FROM lessons ORDER BY id ASC")
    fun getLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: Int): Lesson?

    @Query("UPDATE lessons SET completed = :completed WHERE id = :id")
    suspend fun updateLessonCompletion(id: Int, completed: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Query("SELECT * FROM saved_ai_messages ORDER BY timestamp ASC")
    fun getSavedAiMessages(): Flow<List<SavedAiMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedAiMessage(message: SavedAiMessage)

    @Query("SELECT * FROM leaderboard_peers ORDER BY points DESC")
    fun getLeaderboardPeers(): Flow<List<LeaderboardPeer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardPeers(peers: List<LeaderboardPeer>)

    @Query("UPDATE leaderboard_peers SET isChallenged = :isChallenged, duelStatus = :duelStatus, points = :points WHERE id = :id")
    suspend fun updatePeerDuel(id: String, isChallenged: Boolean, duelStatus: String, points: Int)
}
