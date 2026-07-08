package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LeaderboardPeer
import com.example.data.model.Lesson
import com.example.data.model.SavedAiMessage
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppViewModel
import com.example.ui.viewmodel.Song
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class DevQuestTab(val title: String, val activeIcon: ImageVector, val inactiveIcon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    LESSONS("Learn", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
    GAMES("Arcade", Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports),
    SENSEI("AI Sensei", Icons.Filled.Psychology, Icons.Outlined.Psychology)
}

@Composable
fun MainScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    var currentTab by remember { mutableStateOf(DevQuestTab.DASHBOARD) }

    val userProgress by viewModel.userProgress.collectAsStateWithLifecycle()
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val leaderboardPeers by viewModel.leaderboardPeers.collectAsStateWithLifecycle()

    val currentLesson by viewModel.currentLesson.collectAsStateWithLifecycle()

    Row(modifier = modifier.fillMaxSize().background(CyberBg)) {
        // Navigation Rail for tablets/desktops
        if (isTablet) {
            NavigationRail(
                containerColor = CyberCard,
                contentColor = CyberTextMain,
                header = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(CyberPrimary, CyberTertiary))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Logo",
                                tint = CyberBg,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DevQuest",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyberPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            ) {
                Spacer(modifier = Modifier.weight(1f))
                DevQuestTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { 
                            Text(
                                text = tab.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            ) 
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF21005D),
                            selectedTextColor = CyberPrimary,
                            indicatorColor = CyberSecondary,
                            unselectedIconColor = CyberTextMuted,
                            unselectedTextColor = CyberTextMuted
                        ),
                        modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        // Main Content Area
        Scaffold(
            bottomBar = {
                if (!isTablet) {
                    NavigationBar(
                        containerColor = Color(0xFF211F26), // Bottom Bar bg from theme HTML
                        contentColor = CyberTextMain
                    ) {
                        DevQuestTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { 
                                    Text(
                                        text = tab.title.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    ) 
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF21005D),
                                    selectedTextColor = CyberPrimary,
                                    indicatorColor = CyberSecondary,
                                    unselectedIconColor = CyberTextMuted,
                                    unselectedTextColor = CyberTextMuted
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            },
            containerColor = CyberBg,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                    },
                    label = "TabTransition"
                ) { targetTab ->
                    when (targetTab) {
                        DevQuestTab.DASHBOARD -> DashboardTab(
                            viewModel = viewModel,
                            userProgress = userProgress,
                            peers = leaderboardPeers,
                            isTablet = isTablet
                        )
                        DevQuestTab.LESSONS -> LessonsTab(
                            viewModel = viewModel,
                            lessons = lessons,
                            currentLesson = currentLesson,
                            isTablet = isTablet
                        )
                        DevQuestTab.GAMES -> GamesTab(
                            viewModel = viewModel,
                            peers = leaderboardPeers,
                            isTablet = isTablet
                        )
                        DevQuestTab.SENSEI -> SenseiChatTab(
                            viewModel = viewModel,
                            aiMessages = aiMessages
                        )
                    }
                }
            }
        }
    }
}

// =================== DASHBOARD TAB ===================
@Composable
fun DashboardTab(
    viewModel: AppViewModel,
    userProgress: com.example.data.model.UserProgress?,
    peers: List<LeaderboardPeer>,
    isTablet: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Profile Banner with Level, Streak, Points
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(CyberCard, CyberBg)
                        )
                    )
                    .border(1.dp, CyberPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Level ${userProgress?.level ?: 1} Coder",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track: GitHub & Coding Fundamentals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberTextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // XP Bar
                        val points = userProgress?.points ?: 0
                        val nextLevelXp = 400
                        val currentXp = points % nextLevelXp
                        val progressFraction = currentXp.toFloat() / nextLevelXp.toFloat()
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberBg)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progressFraction)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.horizontalGradient(listOf(CyberPrimary, CyberSecondary)))
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "$currentXp / $nextLevelXp XP",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberCard)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🔥 ${userProgress?.streak ?: 5}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyberTertiary
                        )
                        Text(
                            text = "Day Streak",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTextMuted
                        )
                    }
                }
            }
        }

        // Project Ideas & Offline Tips Callout
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .border(1.dp, CyberOutline, RoundedCornerShape(24.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Idea",
                            tint = CyberWarning,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Next Project Idea: Git-Anime-Tracker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextMain
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Build a local Python console application that lists your favorite anime characters and uses local variables to track their power levels! After building, initialize a Git timeline and make your first commit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTextMuted
                    )
                }
            }
        }

        // Leaderboard (Peers progress tracking)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
            ) {
                Text(
                    text = "Peer Leaderboard (Rank vs Friends)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberOutline, RoundedCornerShape(24.dp))
                ) {
                    Column {
                        // Current user pseudo-row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberPrimary.copy(alpha = 0.1f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "You",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberPrimary,
                                modifier = Modifier.width(48.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🥷", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Shinobi Student",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextMain
                                )
                                Text(
                                    text = "Status: Learning Variable Jutsu!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${userProgress?.points ?: 0} XP",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = CyberSecondary
                            )
                        }

                        HorizontalDivider(color = CyberBg)

                        peers.forEachIndexed { index, peer ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (index == 0) CyberWarning else CyberTextMuted,
                                    modifier = Modifier.width(48.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CyberBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(peer.avatarEmoji, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = peer.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTextMain
                                    )
                                    Text(
                                        text = peer.statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${peer.points} XP",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyberTextMain
                                )
                            }
                            if (index < peers.size - 1) {
                                HorizontalDivider(color = CyberBg)
                            }
                        }
                    }
                }
            }
        }

        // Music Player widget for focused study sessions
        item {
            MusicPlayerWidget(viewModel)
        }
    }
}

@Composable
fun MusicPlayerWidget(viewModel: AppViewModel) {
    val currentSongIndex by viewModel.currentSongIndex.collectAsStateWithLifecycle()
    val isMusicPlaying by viewModel.isMusicPlaying.collectAsStateWithLifecycle()
    val musicProgressSeconds by viewModel.musicProgressSeconds.collectAsStateWithLifecycle()

    val song = viewModel.songs[currentSongIndex]
    
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 800.dp)
            .border(1.dp, CyberOutline, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🎧 Lofi Study Radio (Focused Learning)",
                style = MaterialTheme.typography.labelMedium,
                color = CyberPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio Wave Visualizer Animation using Canvas
                val infiniteTransition = rememberInfiniteTransition(label = "music")
                val height1 by infiniteTransition.animateFloat(
                    initialValue = 5f,
                    targetValue = 25f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "wave1"
                )
                val height2 by infiniteTransition.animateFloat(
                    initialValue = 10f,
                    targetValue = 35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "wave2"
                )
                val height3 by infiniteTransition.animateFloat(
                    initialValue = 5f,
                    targetValue = 30f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(450, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "wave3"
                )

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberBg),
                    contentAlignment = Alignment.Center
                ) {
                    if (isMusicPlaying) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Box(modifier = Modifier.width(4.dp).height(height1.dp).background(CyberPrimary))
                            Box(modifier = Modifier.width(4.dp).height(height2.dp).background(CyberSecondary))
                            Box(modifier = Modifier.width(4.dp).height(height3.dp).background(CyberTertiary))
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music icon",
                            tint = CyberTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextMain
                    )
                    Text(
                        text = song.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time and Progress Bar
            val formattedProgress = String.format("%02d:%02d", musicProgressSeconds / 60, musicProgressSeconds % 60)
            val formattedLength = String.format("%02d:%02d", song.lengthSeconds / 60, song.lengthSeconds % 60)
            
            Slider(
                value = musicProgressSeconds.toFloat(),
                onValueChange = {},
                valueRange = 0f..song.lengthSeconds.toFloat(),
                colors = SliderDefaults.colors(
                    activeTrackColor = CyberPrimary,
                    inactiveTrackColor = CyberBg,
                    thumbColor = CyberPrimary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(formattedProgress, style = MaterialTheme.typography.labelSmall, color = CyberTextMuted)
                Text(formattedLength, style = MaterialTheme.typography.labelSmall, color = CyberTextMuted)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.previousSong() }) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = CyberTextMain,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                FloatingActionButton(
                    onClick = { viewModel.togglePlayMusic() },
                    containerColor = CyberPrimary,
                    contentColor = CyberBg,
                    shape = CircleShape,
                    modifier = Modifier.size(52.dp).testTag("music_play_button")
                ) {
                    Icon(
                        imageVector = if (isMusicPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play Pause",
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = { viewModel.nextSong() }) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Track",
                        tint = CyberTextMain,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}


// =================== LESSONS TAB ===================
@Composable
fun LessonsTab(
    viewModel: AppViewModel,
    lessons: List<Lesson>,
    currentLesson: Lesson?,
    isTablet: Boolean
) {
    var selectedCategory by remember { mutableStateOf("ALL") }

    val filteredLessons = remember(lessons, selectedCategory) {
        if (selectedCategory == "ALL") lessons else lessons.filter { it.category == selectedCategory }
    }

    if (currentLesson != null) {
        // Active Lesson Screen with Animations, Memes and Quiz
        ActiveLessonView(viewModel, currentLesson)
    } else {
        // Lessons list
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Learn Coding & GitHub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CyberPrimary
            )
            Text(
                text = "Offline accessible lessons powered by pop-culture references, safe point checkpoints, and custom visual animations.",
                style = MaterialTheme.typography.bodyMedium,
                color = CyberTextMuted
            )

            // Category Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                listOf("ALL", "BASICS", "LOOPS", "GITHUB").forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberPrimary,
                            selectedLabelColor = CyberBg,
                            containerColor = CyberCard,
                            labelColor = CyberTextMain
                        )
                    )
                }
            }

            // Grid or Column of Lessons
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filteredLessons) { lesson ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectLesson(lesson) }
                            .border(
                                1.dp,
                                if (lesson.completed) CyberSecondary.copy(alpha = 0.4f) else CyberPrimary.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .testTag("lesson_card_${lesson.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (lesson.completed) CyberSecondary.copy(alpha = 0.2f) else CyberPrimary.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (lesson.completed) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                                    contentDescription = "Status",
                                    tint = if (lesson.completed) CyberSecondary else CyberPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = lesson.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTertiary
                                    )
                                    if (lesson.category == "GITHUB") {
                                        Badge(containerColor = CyberPrimary.copy(alpha = 0.2f), contentColor = CyberPrimary) {
                                            Text("Career Prep")
                                        }
                                    }
                                }
                                Text(
                                    text = lesson.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextMain
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open",
                                tint = CyberTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveLessonView(viewModel: AppViewModel, lesson: Lesson) {
    val activeFrameIndex by viewModel.activeAnimationFrameIndex.collectAsStateWithLifecycle()
    val frames = remember(lesson) {
        try {
            val arr = JSONArray(lesson.animationFramesJson)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                Frame(obj.getString("title"), obj.getString("val"))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    var selectedAnswer by remember { mutableStateOf(-1) }
    var answerChecked by remember { mutableStateOf(false) }
    var answerStatusText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectLesson(null) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMain
                )
            }
        }

        // Animated Logic Visualizer (Simulated video player)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📽️ Concept Visualizer (Animated Playback)",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Animation Screen Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberBg)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (frames.isNotEmpty()) {
                            val frame = frames[activeFrameIndex]
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = frame.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = frame.value,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyberTextMain,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Text("Loading animation...", color = CyberTextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Player Control Panel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Frame ${activeFrameIndex + 1} of ${frames.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTextMuted
                        )
                        Row {
                            Button(
                                onClick = { viewModel.nextAnimationFrame(frames.size) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberBg),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play Frame", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Next Frame")
                            }
                        }
                    }
                }
            }
        }

        // Lesson description and anime reference
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lesson Scroll",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lesson.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTextMain
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Git Command / Code Syntax:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberSecondary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberBg)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = lesson.codeSnippet,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = CyberSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberTertiary.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text("💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Anime Sensei Analogy:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyberTertiary
                            )
                            Text(
                                text = lesson.animeReference,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberTextMain
                            )
                        }
                    }
                }
            }
        }

        // Meme & movie quotes reference block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .border(1.dp, CyberWarning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎭", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Meme & Pop Reference",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberWarning
                        )
                        Text(
                            text = lesson.memeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberTextMain,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Interactive quiz card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 800.dp)
                    .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡ Mini-Quiz: Verify Your Chakra",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Question: What is the primary purpose of this lesson's concept?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberTextMain
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val options = listOf(lesson.optionA, lesson.optionB, lesson.optionC)
                    options.forEachIndexed { idx, option ->
                        val isSelected = selectedAnswer == idx
                        val outlineColor = if (answerChecked) {
                            if (idx == lesson.correctOption) CyberSecondary else if (isSelected) CyberError else CyberBg
                        } else {
                            if (isSelected) CyberPrimary else CyberBg
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                                .clickable(enabled = !answerChecked) { selectedAnswer = idx },
                            color = CyberBg
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { if (!answerChecked) selectedAnswer = idx },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyberPrimary, unselectedColor = CyberTextMuted)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = option, style = MaterialTheme.typography.bodyMedium, color = CyberTextMain)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!answerChecked) {
                        Button(
                            onClick = {
                                if (selectedAnswer != -1) {
                                    answerChecked = true
                                    if (selectedAnswer == lesson.correctOption) {
                                        answerStatusText = "Correct! +150 XP earned offline!"
                                        viewModel.completeCurrentLesson()
                                    } else {
                                        answerStatusText = "Oops! Try again to unlock points."
                                    }
                                }
                            },
                            enabled = selectedAnswer != -1,
                            modifier = Modifier.fillMaxWidth().testTag("submit_quiz_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberBg)
                        ) {
                            Text("Submit Answer")
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = answerStatusText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedAnswer == lesson.correctOption) CyberSecondary else CyberError
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    selectedAnswer = -1
                                    answerChecked = false
                                    answerStatusText = ""
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCard, contentColor = CyberPrimary)
                            ) {
                                Text("Retry Quiz")
                            }
                        }
                    }
                }
            }
        }
    }
}

data class Frame(val title: String, val value: String)


// =================== GAMES TAB (ARCADE) ===================
@Composable
fun GamesTab(
    viewModel: AppViewModel,
    peers: List<LeaderboardPeer>,
    isTablet: Boolean
) {
    var selectedGameTab by remember { mutableStateOf("SORTER") }

    val sorterItems by viewModel.sorterItems.collectAsStateWithLifecycle()
    val sorterStatus by viewModel.sorterStatus.collectAsStateWithLifecycle()

    val bugLines by viewModel.bugHunterLines.collectAsStateWithLifecycle()
    val bugSelectedLine by viewModel.bugSelectedLine.collectAsStateWithLifecycle()
    val bugStatus by viewModel.bugHunterStatus.collectAsStateWithLifecycle()

    val activeDuelPeer by viewModel.activeDuelPeer.collectAsStateWithLifecycle()
    val duelQuestion by viewModel.duelQuestion.collectAsStateWithLifecycle()
    val duelOptions by viewModel.duelOptions.collectAsStateWithLifecycle()
    val duelSelectedOption by viewModel.duelSelectedOption.collectAsStateWithLifecycle()
    val duelOutcome by viewModel.duelOutcome.collectAsStateWithLifecycle()

    // Initialize once
    LaunchedEffect(selectedGameTab) {
        if (selectedGameTab == "SORTER" && sorterItems.isEmpty()) {
            viewModel.initSyntaxSorter()
        } else if (selectedGameTab == "HUNTER" && bugLines.isEmpty()) {
            viewModel.initBugHunter()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Coding Arcade",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = CyberPrimary
            )
            Badge(containerColor = CyberSecondary.copy(alpha = 0.2f), contentColor = CyberSecondary) {
                Text("Gamified")
            }
        }

        // Game selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CyberCard)
                .padding(4.dp)
        ) {
            listOf("SORTER" to "Syntax Sorter", "HUNTER" to "Bug Hunter", "DUELS" to "Duels Arena").forEach { (tabId, tabName) ->
                val isSelected = selectedGameTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CyberPrimary else Color.Transparent)
                        .clickable { selectedGameTab = tabId }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tabName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CyberBg else CyberTextMain
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedGameTab) {
            "SORTER" -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🧩 Syntax Sorter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Arrange the code blocks in the correct logic sequence, then compile to defeat the bug monster!",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Render block items with arrow swap controls to ensure absolute responsiveness
                        sorterItems.forEachIndexed { index, item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = CyberBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberSecondary
                                    )
                                    Row {
                                        IconButton(
                                            onClick = { viewModel.moveSorterItem(index, index - 1) },
                                            enabled = index > 0,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Move Up",
                                                tint = if (index > 0) CyberPrimary else CyberTextMuted
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.moveSorterItem(index, index + 1) },
                                            enabled = index < sorterItems.size - 1,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Move Down",
                                                tint = if (index < sorterItems.size - 1) CyberPrimary else CyberTextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (sorterStatus) {
                            "CORRECT" -> {
                                Text(
                                    text = "🎉 Perfect Compilation! Bug defeated. +50 XP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.initSyntaxSorter() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberBg)
                                ) {
                                    Text("Next Challenge")
                                }
                            }
                            "WRONG" -> {
                                Text(
                                    text = "❌ Build Error: Invalid order. Re-arrange and try compiling again!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyberError,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.checkSorterSequence() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberTertiary, contentColor = CyberTextMain)
                                ) {
                                    Text("Retry Compile")
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { viewModel.checkSorterSequence() },
                                    modifier = Modifier.fillMaxWidth().testTag("compile_sorter_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberBg)
                                ) {
                                    Text("Compile & Run")
                                }
                            }
                        }
                    }
                }
            }

            "HUNTER" -> {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🕷️ Bug Hunter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find and tap the line containing a syntax bug or crash condition to fix it!",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        bugLines.forEachIndexed { index, line ->
                            val isSelected = bugSelectedLine == index
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = bugStatus == "HUNTING") { viewModel.selectBugLine(index) },
                                color = if (isSelected) CyberTertiary.copy(alpha = 0.2f) else CyberBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) CyberTertiary else CyberPrimary.copy(alpha = 0.1f)
                                )
                            ) {
                                Row(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "${index + 1}: ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberTertiary
                                    )
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberTextMain
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        when (bugStatus) {
                            "WON" -> {
                                Text(
                                    text = "🏆 Spot On! Bug fixed successfully! +50 XP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.initBugHunter() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberBg)
                                ) {
                                    Text("Next Bug")
                                }
                            }
                            "LOST" -> {
                                Text(
                                    text = "❌ That line runs cleanly. The bug is still hiding!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyberError,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.initBugHunter() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCard, contentColor = CyberPrimary)
                                ) {
                                    Text("Try Different Bug")
                                }
                            }
                            else -> {
                                Button(
                                    onClick = { viewModel.verifyBugChoice() },
                                    enabled = bugSelectedLine != -1,
                                    modifier = Modifier.fillMaxWidth().testTag("verify_bug_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberBg)
                                ) {
                                    Text("Deploy Patch")
                                }
                            }
                        }
                    }
                }
            }

            "DUELS" -> {
                if (activeDuelPeer != null) {
                    // Ongoing Duel Arena Screen
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .border(1.dp, CyberPrimary.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚔️ VS ${activeDuelPeer?.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTertiary
                                )
                                Text(
                                    text = "XP: ${activeDuelPeer?.points}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberTextMuted
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = duelQuestion,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextMain
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            duelOptions.forEachIndexed { index, option ->
                                val isSelected = duelSelectedOption == index
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable(enabled = duelOutcome == "CHALLENGE") { viewModel.selectDuelOption(index) },
                                    color = if (isSelected) CyberPrimary.copy(alpha = 0.2f) else CyberBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isSelected) CyberPrimary else CyberPrimary.copy(alpha = 0.1f))
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = CyberTextMain,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            when (duelOutcome) {
                                "WINNER" -> {
                                    Text(
                                        text = "🏆 VICTORY! You won the duel! Peer score adjusted and +100 XP awarded!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.initDuel(activeDuelPeer!!) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberBg)
                                    ) {
                                        Text("Rematch")
                                    }
                                }
                                "LOSER" -> {
                                    Text(
                                        text = "💀 DEFEAT! The opponent had faster keyboard chakra! Peer points increased.",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberError,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.initDuel(activeDuelPeer!!) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberPrimary, contentColor = CyberBg)
                                    ) {
                                        Text("Try Rematch")
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { viewModel.submitDuelAnswer() },
                                        enabled = duelSelectedOption != -1,
                                        modifier = Modifier.fillMaxWidth().testTag("submit_duel_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberTertiary, contentColor = CyberTextMain)
                                    ) {
                                        Text("Submit Answer")
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Choose peer to challenge
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                    ) {
                        Text(
                            text = "Select Peer to Challenge:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        peers.forEach { peer ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberCard),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(CyberBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(peer.avatarEmoji, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = peer.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = CyberTextMain
                                            )
                                            Text(
                                                text = "Level ${peer.level}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CyberTextMuted
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { viewModel.initDuel(peer) },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberTertiary, contentColor = CyberTextMain)
                                    ) {
                                        Text("Duel ⚔️")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// =================== SENSEI AI CHAT TAB ===================
@Composable
fun SenseiChatTab(
    viewModel: AppViewModel,
    aiMessages: List<SavedAiMessage>
) {
    var userPromptText by remember { mutableStateOf("") }
    val responseLoading by viewModel.aiResponseLoading.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to end when messages update
    LaunchedEffect(aiMessages.size) {
        if (aiMessages.isNotEmpty()) {
            listState.animateScrollToItem(aiMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Chat Header
        Card(
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧙‍♂️", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sensei AI Coding Mentor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberPrimary
                    )
                    Text(
                        text = "Active Sensei: Custom Anime & Tech Analogies",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Quick Suggestion Chips
        val suggestions = listOf("Explain GitHub Merges", "How do loops work?", "Help me fix a bug")

        // Chat conversation history
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (aiMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔮", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Welcome student! Ask Sensei about variables, loop traps, GitHub merges, or coding career tips.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyberTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(aiMessages) { msg ->
                    val isUser = msg.role == "user"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 16.dp
                                    )
                                )
                                .background(if (isUser) CyberPrimary.copy(alpha = 0.2f) else CyberCard)
                                .border(
                                    1.dp,
                                    if (isUser) CyberPrimary.copy(alpha = 0.3f) else CyberSecondary.copy(alpha = 0.1f),
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isUser) 16.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 16.dp
                                    )
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isUser) "You" else "Sensei AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) CyberPrimary else CyberSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isUser) msg.prompt else msg.response,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyberTextMain
                                )
                            }
                        }
                    }
                }
            }

            if (responseLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberCard)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Sensei is translating scroll...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyberSecondary,
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }
            }
        }

        // Suggestions bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                Surface(
                    modifier = Modifier.clickable {
                        userPromptText = suggestion
                    },
                    color = CyberCard,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CyberPrimary.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = userPromptText,
                onValueChange = { userPromptText = it },
                placeholder = { Text("Ask Sensei...", color = CyberTextMuted) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input"),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = CyberTextMain,
                    unfocusedTextColor = CyberTextMain,
                    focusedContainerColor = CyberCard,
                    unfocusedContainerColor = CyberCard,
                    focusedIndicatorColor = CyberPrimary,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (userPromptText.isNotBlank()) {
                        viewModel.askSensei(userPromptText)
                        userPromptText = ""
                    }
                },
                containerColor = CyberPrimary,
                contentColor = CyberBg,
                shape = CircleShape,
                modifier = Modifier.size(48.dp).testTag("ai_send_button")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
            }
        }
    }
}
