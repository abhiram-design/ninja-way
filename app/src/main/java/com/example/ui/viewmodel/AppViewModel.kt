package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiPart
import com.example.data.api.GeminiRequest
import com.example.data.api.RetrofitClient
import com.example.data.database.AppDatabase
import com.example.data.model.Lesson
import com.example.data.model.UserProgress
import com.example.data.model.SavedAiMessage
import com.example.data.model.LeaderboardPeer
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())
        
        // Seed database offline capability on initialization
        viewModelScope.launch {
            repository.prepopulateIfNeeded()
        }
    }

    val userProgress: StateFlow<UserProgress?> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val lessons: StateFlow<List<Lesson>> = repository.lessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiMessages: StateFlow<List<SavedAiMessage>> = repository.savedAiMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboardPeers: StateFlow<List<LeaderboardPeer>> = repository.leaderboardPeers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active states ---
    private val _currentLesson = MutableStateFlow<Lesson?>(null)
    val currentLesson: StateFlow<Lesson?> = _currentLesson.asStateFlow()

    private val _aiResponseLoading = MutableStateFlow(false)
    val aiResponseLoading: StateFlow<Boolean> = _aiResponseLoading.asStateFlow()

    // --- Interactive Code animations state ---
    private val _activeAnimationFrameIndex = MutableStateFlow(0)
    val activeAnimationFrameIndex: StateFlow<Int> = _activeAnimationFrameIndex.asStateFlow()

    // --- Music Player states ---
    val songs = listOf(
        Song("Lofi Code Cafe", "Acoustic chill beats", 120),
        Song("Synthwave Syntax", "Upbeat coding synth", 150),
        Song("GitHub Chill Hop", "Relaxing developer jazz", 180),
        Song("Anime Sensei Theme", "Inspirational piano", 140)
    )
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex.asStateFlow()

    private val _isMusicPlaying = MutableStateFlow(false)
    val isMusicPlaying: StateFlow<Boolean> = _isMusicPlaying.asStateFlow()

    private val _musicProgressSeconds = MutableStateFlow(0)
    val musicProgressSeconds: StateFlow<Int> = _musicProgressSeconds.asStateFlow()

    // --- Syntax Sorter game states ---
    private val _sorterItems = MutableStateFlow<List<String>>(emptyList())
    val sorterItems: StateFlow<List<String>> = _sorterItems.asStateFlow()
    
    private val _sorterCorrectSequence = MutableStateFlow<List<String>>(emptyList())
    
    private val _sorterStatus = MutableStateFlow("SORTING") // "SORTING", "CORRECT", "WRONG"
    val sorterStatus: StateFlow<String> = _sorterStatus.asStateFlow()

    private var currentSorterIndex = 0

    // --- Bug Hunter game states ---
    private val _bugHunterLines = MutableStateFlow<List<String>>(emptyList())
    val bugHunterLines: StateFlow<List<String>> = _bugHunterLines.asStateFlow()

    private val _bugCorrectLineIndex = MutableStateFlow(-1)
    val bugCorrectLineIndex: StateFlow<Int> = _bugCorrectLineIndex.asStateFlow()

    private val _bugHunterStatus = MutableStateFlow("HUNTING") // "HUNTING", "WON", "LOST"
    val bugHunterStatus: StateFlow<String> = _bugHunterStatus.asStateFlow()

    private val _bugSelectedLine = MutableStateFlow(-1)
    val bugSelectedLine: StateFlow<Int> = _bugSelectedLine.asStateFlow()

    private var currentBugIndex = 0

    // --- Duel states ---
    private val _activeDuelPeer = MutableStateFlow<LeaderboardPeer?>(null)
    val activeDuelPeer: StateFlow<LeaderboardPeer?> = _activeDuelPeer.asStateFlow()

    private val _duelQuestion = MutableStateFlow("")
    val duelQuestion: StateFlow<String> = _duelQuestion.asStateFlow()

    private val _duelOptions = MutableStateFlow<List<String>>(emptyList())
    val duelOptions: StateFlow<List<String>> = _duelOptions.asStateFlow()

    private val _duelCorrectIndex = MutableStateFlow(0)
    val duelCorrectIndex: StateFlow<Int> = _duelCorrectIndex.asStateFlow()

    private val _duelSelectedOption = MutableStateFlow(-1)
    val duelSelectedOption: StateFlow<Int> = _duelSelectedOption.asStateFlow()

    private val _duelOutcome = MutableStateFlow("CHALLENGE") // "CHALLENGE", "WINNER", "LOSER"
    val duelOutcome: StateFlow<String> = _duelOutcome.asStateFlow()

    // Music ticker simulation
    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isMusicPlaying.value) {
                    val currentLength = songs[_currentSongIndex.value].lengthSeconds
                    if (_musicProgressSeconds.value < currentLength) {
                        _musicProgressSeconds.value += 1
                    } else {
                        // Loop back to start or next song
                        _musicProgressSeconds.value = 0
                        _currentSongIndex.value = (_currentSongIndex.value + 1) % songs.size
                    }
                }
            }
        }
    }

    // --- Lesson management ---
    fun selectLesson(lesson: Lesson?) {
        _currentLesson.value = lesson
        _activeAnimationFrameIndex.value = 0
    }

    fun nextAnimationFrame(frameCount: Int) {
        if (_activeAnimationFrameIndex.value < frameCount - 1) {
            _activeAnimationFrameIndex.value += 1
        } else {
            _activeAnimationFrameIndex.value = 0 // Loop
        }
    }

    fun completeCurrentLesson() {
        val lesson = _currentLesson.value ?: return
        viewModelScope.launch {
            repository.updateLessonCompletion(lesson.id, true)
            _currentLesson.value = lesson.copy(completed = true)
        }
    }

    // --- AI Chat sensei ---
    fun askSensei(promptText: String) {
        if (promptText.isBlank()) return
        
        viewModelScope.launch {
            // Save User message
            repository.insertSavedAiMessage(
                SavedAiMessage(prompt = promptText, response = "", role = "user")
            )
            
            _aiResponseLoading.value = true
            
            val systemPrompt = "You are 'Sensei AI', an energetic, friendly, and expert coding instructor who loves anime, movies, and video games. Respond to coding or GitHub questions with creative analogies (e.g., comparing loops to Naruto jutsu, git branching to Doctor Strange multiverse, variables to chest boxes). Keep explanations short, readable, formatted nicely, and highly encouraging for student developers!"
            
            try {
                // Prepare request
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = promptText))
                        )
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    )
                )
                
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                }
                
                val senseiReply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "Ah! My network chakra is blocked. Make sure your API key is correctly configured in the Secrets panel, or try again offline!"
                
                repository.insertSavedAiMessage(
                    SavedAiMessage(prompt = promptText, response = senseiReply, role = "sensei")
                )
            } catch (e: Exception) {
                // Offline fallback response for Remote learning!
                val offlineReply = "Greetings Ninja coder! You are offline, but Sensei has a wisdom scroll for you: Keep practicing your variables and commits. When you connect back to the digital chakra, I will answer your prompt in full! Dattebayo!"
                repository.insertSavedAiMessage(
                    SavedAiMessage(prompt = promptText, response = offlineReply, role = "sensei")
                )
            } finally {
                _aiResponseLoading.value = false
            }
        }
    }

    // --- Music Controls ---
    fun togglePlayMusic() {
        _isMusicPlaying.value = !_isMusicPlaying.value
    }

    fun nextSong() {
        _musicProgressSeconds.value = 0
        _currentSongIndex.value = (_currentSongIndex.value + 1) % songs.size
    }

    fun previousSong() {
        _musicProgressSeconds.value = 0
        _currentSongIndex.value = if (_currentSongIndex.value > 0) _currentSongIndex.value - 1 else songs.size - 1
    }

    // --- Syntax Sorter mini game ---
    fun initSyntaxSorter() {
        val challenges = listOf(
            SorterChallenge(
                correct = listOf("x = 5", "y = \"Chakra\"", "print(x, y)"),
                shuffled = listOf("print(x, y)", "x = 5", "y = \"Chakra\"")
            ),
            SorterChallenge(
                correct = listOf("<html>", "  <h1>Hello Sensei</h1>", "</html>"),
                shuffled = listOf("</html>", "<html>", "  <h1>Hello Sensei</h1>")
            ),
            SorterChallenge(
                correct = listOf("if score > 10:", "    print(\"You win!\")", "else:", "    print(\"Keep training!\")"),
                shuffled = listOf("else:", "    print(\"You win!\")", "if score > 10:", "    print(\"Keep training!\")")
            )
        )
        currentSorterIndex = (currentSorterIndex + 1) % challenges.size
        val chall = challenges[currentSorterIndex]
        _sorterItems.value = chall.shuffled
        _sorterCorrectSequence.value = chall.correct
        _sorterStatus.value = "SORTING"
    }

    fun moveSorterItem(fromIndex: Int, toIndex: Int) {
        val currentList = _sorterItems.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            _sorterItems.value = currentList
        }
    }

    fun checkSorterSequence() {
        if (_sorterItems.value == _sorterCorrectSequence.value) {
            _sorterStatus.value = "CORRECT"
            
            // Earn 50 points
            viewModelScope.launch {
                val current = userProgress.first() ?: UserProgress()
                val newPoints = current.points + 50
                repository.insertUserProgress(current.copy(points = newPoints, level = (newPoints / 400) + 1))
            }
        } else {
            _sorterStatus.value = "WRONG"
        }
    }

    // --- Bug Hunter mini game ---
    fun initBugHunter() {
        val challenges = listOf(
            BugChallenge(
                lines = listOf(
                    "def learn_github()",  // Bug: missing colon
                    "    print(\"Committing code!\")",
                    "    return True"
                ),
                bugLineIndex = 0
            ),
            BugChallenge(
                lines = listOf(
                    "heroes = [\"Goku\", \"Naruto\"]",
                    "print(heroes[2])", // Bug: out of bounds
                    "print(\"Ready to fight!\")"
                ),
                bugLineIndex = 1
            ),
            BugChallenge(
                lines = listOf(
                    "git_active = True",
                    "if git_active = False:", // Bug: '=' instead of '=='
                    "    print(\"Start tracking\")"
                ),
                bugLineIndex = 1
            )
        )
        currentBugIndex = (currentBugIndex + 1) % challenges.size
        val chall = challenges[currentBugIndex]
        _bugHunterLines.value = chall.lines
        _bugCorrectLineIndex.value = chall.bugLineIndex
        _bugSelectedLine.value = -1
        _bugHunterStatus.value = "HUNTING"
    }

    fun selectBugLine(index: Int) {
        _bugSelectedLine.value = index
    }

    fun verifyBugChoice() {
        if (_bugSelectedLine.value == _bugCorrectLineIndex.value) {
            _bugHunterStatus.value = "WON"
            
            // Earn 50 points
            viewModelScope.launch {
                val current = userProgress.first() ?: UserProgress()
                val newPoints = current.points + 50
                repository.insertUserProgress(current.copy(points = newPoints, level = (newPoints / 400) + 1))
            }
        } else {
            _bugHunterStatus.value = "LOST"
        }
    }

    // --- Duel Arena mini game ---
    fun initDuel(peer: LeaderboardPeer) {
        _activeDuelPeer.value = peer
        _duelSelectedOption.value = -1
        _duelOutcome.value = "CHALLENGE"
        
        val duelPool = listOf(
            DuelQuestion(
                question = "How do you download a repository from GitHub to your local machine?",
                options = listOf("git clone <url>", "git download <url>", "git copy <url>"),
                correctIndex = 0
            ),
            DuelQuestion(
                question = "Which command staging area prepares changes for a commit?",
                options = "git stage -all,git add <filename>,git push origin".split(","),
                correctIndex = 1
            ),
            DuelQuestion(
                question = "If you have local changes clashing with incoming cloud changes, what occurs?",
                options = listOf("A branch ban", "A merge conflict", "A database timeout"),
                correctIndex = 1
            )
        )
        val question = duelPool.random()
        _duelQuestion.value = question.question
        _duelOptions.value = question.options
        _duelCorrectIndex.value = question.correctIndex
    }

    fun selectDuelOption(index: Int) {
        _duelSelectedOption.value = index
    }

    fun submitDuelAnswer() {
        val peer = _activeDuelPeer.value ?: return
        if (_duelSelectedOption.value == _duelCorrectIndex.value) {
            _duelOutcome.value = "WINNER"
            viewModelScope.launch {
                // We won the duel! Update peer to show won status and deduct 50 points from them
                repository.updatePeerDuel(peer.id, isChallenged = true, duelStatus = "WON", scoreDelta = -50)
            }
        } else {
            _duelOutcome.value = "LOSER"
            viewModelScope.launch {
                // We lost the duel! Update peer to show lost status and award them 50 points
                repository.updatePeerDuel(peer.id, isChallenged = true, duelStatus = "LOST", scoreDelta = 50)
            }
        }
    }
}

data class Song(val title: String, val subtitle: String, val lengthSeconds: Int)
data class SorterChallenge(val correct: List<String>, val shuffled: List<String>)
data class BugChallenge(val lines: List<String>, val bugLineIndex: Int)
data class DuelQuestion(val question: String, val options: List<String>, val correctIndex: Int)

class AppViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
