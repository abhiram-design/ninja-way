package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgress(
    @PrimaryKey val id: Int = 1,
    val points: Int = 0,
    val level: Int = 1,
    val streak: Int = 1,
    val lastCompletedDate: String = "",
    val githubSkillLevel: Int = 1 // Tracking GitHub specific career level
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val category: String, // e.g. "BASICS", "LOOPS", "GITHUB"
    val codeSnippet: String,
    val gitExplanation: String,
    val completed: Boolean = false,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val correctOption: Int, // 0, 1, or 2
    val animationFramesJson: String, // Describes animated visualization frames
    val animeReference: String,
    val memeText: String
)

@Entity(tableName = "saved_ai_messages")
data class SavedAiMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val response: String,
    val role: String, // "user" or "sensei"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaderboard_peers")
data class LeaderboardPeer(
    @PrimaryKey val id: String,
    val name: String,
    val points: Int,
    val level: Int,
    val avatarEmoji: String,
    val statusText: String,
    val isChallenged: Boolean = false,
    val duelStatus: String = "CHALLENGE" // "CHALLENGE", "PLAYING", "WON", "LOST"
)
