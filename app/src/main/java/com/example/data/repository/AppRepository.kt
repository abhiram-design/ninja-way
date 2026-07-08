package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.Lesson
import com.example.data.model.UserProgress
import com.example.data.model.SavedAiMessage
import com.example.data.model.LeaderboardPeer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class AppRepository(private val appDao: AppDao) {

    val userProgress: Flow<UserProgress?> = appDao.getUserProgress()
    val lessons: Flow<List<Lesson>> = appDao.getLessons()
    val savedAiMessages: Flow<List<SavedAiMessage>> = appDao.getSavedAiMessages()
    val leaderboardPeers: Flow<List<LeaderboardPeer>> = appDao.getLeaderboardPeers()

    suspend fun insertUserProgress(progress: UserProgress) = appDao.insertUserProgress(progress)

    suspend fun updateLessonCompletion(id: Int, completed: Boolean) {
        appDao.updateLessonCompletion(id, completed)
        
        // Recalculate points! Give 150 points per lesson completed
        val currentProgress = userProgress.first() ?: UserProgress()
        val completedLessonsCount = lessons.first().count { it.completed || (it.id == id && completed) }
        
        val newPoints = completedLessonsCount * 150
        val newLevel = (newPoints / 400) + 1 // 400 points per level
        
        appDao.insertUserProgress(
            currentProgress.copy(
                points = newPoints,
                level = if (newLevel > 1) newLevel else 1,
                githubSkillLevel = if (completedLessonsCount >= 3) 2 else 1
            )
        )
    }

    suspend fun insertSavedAiMessage(message: SavedAiMessage) = appDao.insertSavedAiMessage(message)

    suspend fun updatePeerDuel(id: String, isChallenged: Boolean, duelStatus: String, scoreDelta: Int) {
        val peers = leaderboardPeers.first()
        val peer = peers.find { it.id == id } ?: return
        val newPoints = peer.points + scoreDelta
        appDao.updatePeerDuel(id, isChallenged, duelStatus, newPoints)
        
        // If player won the duel, add 100 points to user progress!
        if (duelStatus == "WON") {
            val currentProgress = userProgress.first() ?: UserProgress()
            val userNewPoints = currentProgress.points + 100
            val userNewLevel = (userNewPoints / 400) + 1
            appDao.insertUserProgress(
                currentProgress.copy(
                    points = userNewPoints,
                    level = userNewLevel
                )
            )
        }
    }

    suspend fun prepopulateIfNeeded() {
        val currentLessons = appDao.getLessons().first()
        if (currentLessons.isEmpty()) {
            // Seed lessons
            val defaultLessons = listOf(
                Lesson(
                    id = 1,
                    title = "Variable Shadow Clone Jutsu",
                    category = "BASICS",
                    description = "Learn how variables act as named containers for data! In programming, a variable is like a scroll or container where you store information under a custom name, so you can summon it anytime.",
                    codeSnippet = "shadow_clones = 3\nchakra_type = \"Wind\"\nis_ninja = True\n\nprint(shadow_clones)",
                    gitExplanation = "Variables hold values that can change. Creating one is like carving a label on a wooden chest in Minecraft.",
                    completed = false,
                    optionA = "A function that runs repeatedly",
                    optionB = "A named container to store data values",
                    optionC = "A terminal command to upload code",
                    correctOption = 1,
                    animationFramesJson = """[{"title":"Empty Chest","val":"None"},{"title":"Assigned '3'","val":"3 shadow_clones"},{"title":"Output Summoned!","val":"Prints 3 clones"}]""",
                    animeReference = "Naruto: Just like Naruto assigns his chakra into multiple Shadow Clones, you assign value into custom named variable containers!",
                    memeText = "Code compiler looking at you re-declaring the same variable: 'How many times do I have to teach you this lesson, old man?'"
                ),
                Lesson(
                    id = 2,
                    title = "The Infinite Tsukuyomi Loop",
                    category = "LOOPS",
                    description = "Learn about loops (For and While) which repeat instructions. An infinite loop occurs when there's no break condition, crashing your program under Madara's illusion!",
                    codeSnippet = "while training_hard:\n    punches += 1\n    if punches >= 100:\n        break # dispelling the loop!",
                    gitExplanation = "Loops run code blocks until a specific criteria is met. Use them to repeat tasks without writing redundant lines.",
                    completed = false,
                    optionA = "An infinite execution trap that crashes the app",
                    optionB = "A simple conditional check",
                    optionC = "A file that saves code locally",
                    correctOption = 0,
                    animationFramesJson = """[{"title":"Punches = 0","val":"Loop starts"},{"title":"Punches = 50","val":"Continuing loop"},{"title":"Punches = 100","val":"Break triggered! Loop dispelled!"}]""",
                    animeReference = "Naruto: To escape the Infinite Tsukuyomi Loop, you must implement a strict break statement to end the cycle of pain!",
                    memeText = "Endless loop is like Saitama running around the Earth repeatedly because he missed a Saturday grocery sale."
                ),
                Lesson(
                    id = 3,
                    title = "Git Init: Creating the Timeline",
                    category = "GITHUB",
                    description = "Learn what Git is and how 'git init' creates a local repository. Git is a version control system that acts like Dr. Strange tracking multiple multiverse timelines for your code.",
                    codeSnippet = "# Initialize a new git repository local archive\ngit init\ngit status",
                    gitExplanation = "git init creates a hidden .git folder, marking the directory as a tracked workspace. This starts your tracking timeline.",
                    completed = false,
                    optionA = "Deletes all files in the current folder",
                    optionB = "Initializes a new local Git repository",
                    optionC = "Downloads a zip file from github.com",
                    correctOption = 1,
                    animationFramesJson = """[{"title":"Empty Folder","val":"Just standard files"},{"title":"Run 'git init'","val":"Creates hidden .git timeline!"},{"title":"Timeline active","val":"Now tracking file changes!"}]""",
                    animeReference = "Steins;Gate: Initiating a Git repo is like turning your microwave into a time machine. Your timelines are now officially tracked!",
                    memeText = "When you do your first 'git init' and realize you are now the supreme lord of your project's multiverse timelines."
                ),
                Lesson(
                    id = 4,
                    title = "Git Commit: Safe Point Checkpoint",
                    category = "GITHUB",
                    description = "Learn how to save your progress with 'git add' and 'git commit'. A commit is like a permanent save file in Dark Souls: if you fail or die, you can respawn at that exact save point.",
                    codeSnippet = "git add main.py\ngit commit -m \"Summoned a legendary variable bug fix\"",
                    gitExplanation = "git add places changes in the staging area (preparing items in inventory). git commit permanently saves them to your timeline with a custom message.",
                    completed = false,
                    optionA = "Uploads files directly to GitHub servers",
                    optionB = "Saves staged changes as a permanent checkpoint in your history",
                    optionC = "Creates a secret back-up on local floppy disks",
                    correctOption = 1,
                    animationFramesJson = """[{"title":"Modified Files","val":"Changes untracked"},{"title":"'git add'","val":"Staged in launching area"},{"title":"'git commit'","val":"Timeline checkpoint created! Safe!"}]""",
                    animeReference = "Dark Souls: A commit is your bonfire. Resting there saves your state so you don't lose your souls (or your written scripts) when the next boss level crashes.",
                    memeText = "Me committing code: 'git commit -m \"fixed a minor bug\"' (actual changes: 4578 lines of code rewritten in pure panic)."
                ),
                Lesson(
                    id = 5,
                    title = "Fusion Dance: Merges and Conflicts",
                    category = "GITHUB",
                    description = "Learn how to merge different branches (timelines). Just like Goku and Vegeta fusing into Gogeta, a merge combines two branches. If they edited the same line, a Merge Conflict occurs!",
                    codeSnippet = "git checkout main\ngit merge feature-jutsu\n# CONFLICT (content): Merge conflict in main.py",
                    gitExplanation = "Merging integrates updates from feature branches into the main trunk. When edits clash on the same lines, you must manually edit and resolve them.",
                    completed = false,
                    optionA = "A crash that forces you to delete the repository",
                    optionB = "When different lines of code are automatically fused",
                    optionC = "When two branches have clashing changes on the same line that must be resolved manually",
                    correctOption = 2,
                    animationFramesJson = """[{"title":"Branch A & B","val":"Separate branches"},{"title":"git merge","val":"Branches clash!"},{"title":"Conflict resolved","val":"Timeline unified successfully!"}]""",
                    animeReference = "Dragon Ball Z: Failed fusion dances create a clunky, deformed fighter. That's a merge conflict! Open the file, keep the best code, and complete the fusion!",
                    memeText = "My team trying to merge 5 branches together on a Friday afternoon: 'I have a bad feeling about this!'"
                ),
                Lesson(
                    id = 6,
                    title = "Pull Request: The Council Council",
                    category = "GITHUB",
                    description = "Learn how Pull Requests (PR) work on GitHub for career success. Pull requests allow you to submit your timeline to the main branch of an organization, so developers can review your contribution.",
                    codeSnippet = "# Push local timeline to GitHub cloud\ngit push origin main\n# Open PR on Github web UI for review",
                    gitExplanation = "PR is the holy grail of open source and teams. It opens a forum for peer-review before merging your changes to ensure high-quality software.",
                    completed = false,
                    optionA = "A request to delete your GitHub profile",
                    optionB = "An action to copy code from other internet pages",
                    optionC = "A proposed set of changes submitted for peer-review before merging",
                    correctOption = 2,
                    animationFramesJson = """[{"title":"Push to Cloud","val":"Cloud repository updated"},{"title":"Open Pull Request","val":"Peer Review in Progress"},{"title":"Approved & Merged!","val":"Merged to master. Huge win!"}]""",
                    animeReference = "Lord of the Rings: One does not simply merge into 'main' branch! You must present your Pull Request to the Council of Elrond first.",
                    memeText = "PR Reviewer: 'Your code is elegant, but let's change these 45 variable names to match our corporate guidelines.'"
                )
            )
            appDao.insertLessons(defaultLessons)
        }

        val currentProgress = appDao.getUserProgress().first()
        if (currentProgress == null) {
            appDao.insertUserProgress(UserProgress(id = 1, points = 0, level = 1, streak = 5))
        }

        val currentPeers = appDao.getLeaderboardPeers().first()
        if (currentPeers.isEmpty()) {
            val defaultPeers = listOf(
                LeaderboardPeer("1", "Linus Torvalds", 1500, 4, "🐧", "I merged your timeline without a conflict."),
                LeaderboardPeer("2", "Saitama", 1200, 3, "👨‍🦲", "One-Punch code compiler."),
                LeaderboardPeer("3", "Ada Lovelace", 1000, 3, "🎩", "First programmer in history."),
                LeaderboardPeer("4", "Sasuke Uchiha", 750, 2, "⚡", "Too busy training to commit."),
                LeaderboardPeer("5", "Goku", 600, 2, "🥋", "Can your code beat Goku though?"),
                LeaderboardPeer("6", "Nezuko", 300, 1, "🪵", "Muffled keyboard noises.")
            )
            appDao.insertLeaderboardPeers(defaultPeers)
        }
    }
}
