package org.example.bails.presentation.scoreBoard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import bails.composeapp.generated.resources.Res
import bails.composeapp.generated.resources.ic_ball
import kotlinx.coroutines.launch
import org.example.bails.presentation.scoreRecorder.BatterStats
import org.example.bails.presentation.scoreRecorder.BowlerStats
import org.example.bails.presentation.scoreRecorder.InningsSummary
import org.example.bails.presentation.scoreRecorder.getStrikeRate
import org.example.bails.util.rememberImageSharer
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreBoardScreen(
    state: ScoreBoardScreenState,
    onStartNextInnings: () -> Unit,
    onGoHome: () -> Unit = {},
) {
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedTeamIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Score Board", color = MaterialTheme.colorScheme.primary) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(onClick = onGoHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Home",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (state is ScoreBoardScreenState.Success) {
                        IconButton(onClick = { showShareDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Scoreboard",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when(state) {
                is ScoreBoardScreenState.Success -> {
                    ScoreBoardSuccessScreen(
                        firstInning = state.firstInnings,
                        secondInning = state.secondInnings,
                        team1Name = state.team1Name,
                        team2Name = state.team2Name,
                        onStartNextInnings = onStartNextInnings,
                        onPageChanged = { selectedTeamIndex = it }
                    )
                }
                is ScoreBoardScreenState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showShareDialog && state is ScoreBoardScreenState.Success) {
        SharePreviewDialog(
            teamLabel = if (selectedTeamIndex == 0) state.team1Name else state.team2Name,
            innings = if (selectedTeamIndex == 0) state.firstInnings else state.secondInnings,
            oppositionLabel = if (selectedTeamIndex == 0) state.team2Name else state.team1Name,
            oppositionInnings = if (selectedTeamIndex == 0) state.secondInnings else state.firstInnings,
            onDismiss = { showShareDialog = false }
        )
    }
}

@Composable
fun ScoreBoardSuccessScreen(
    firstInning: InningsSummary,
    secondInning: InningsSummary,
    team1Name: String = "Team 1",
    team2Name: String = "Team 2",
    onStartNextInnings: () -> Unit,
    onPageChanged: (Int) -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    team1Name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${firstInning.score} / ${firstInning.wickets}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    team2Name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${secondInning.score} / ${secondInning.wickets}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        TabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth()) {
            Tab(
                selected = pagerState.currentPage == 0,
                text = { Text(team1Name) },
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(page = 0) } },
            )
            Tab(
                selected = pagerState.currentPage == 1,
                text = { Text(team2Name) },
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(page = 1) } },
            )
        }
        HorizontalPager(state = pagerState) { page ->
            ScoreBoard(
                inningsSummary = if (page == 0) firstInning else secondInning,
                onStartNextInnings = onStartNextInnings,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ScoreBoard(inningsSummary: InningsSummary, onStartNextInnings: () -> Unit, modifier: Modifier = Modifier) {
    if (inningsSummary.score == 0 && inningsSummary.overs == 0.0f) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_ball),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Text(
                "Innings Not Started",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onStartNextInnings,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Start Innings")
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                "Batters",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp),
            )
            BattersStats(
                allBattersStats = inningsSummary.allBattersStats
            )
            Text(
                "Bowlers",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp),
            )
            BowlersStats(
                allBowlerStats = inningsSummary.allBowlerStats
            )
        }
    }
}

@Composable
fun BattersStats(allBattersStats: List<BatterStats>, modifier: Modifier = Modifier) {
    Row(modifier = modifier
        .padding(horizontal = 8.dp)
        .border(1.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(4.dp))
        .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text("Name", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBattersStats.forEach {
                Text(it.batter.name, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("R", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBattersStats.forEach {
                Text("${it.runs}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("B", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBattersStats.forEach {
                Text("${it.ballsFaced}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("4s", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBattersStats.forEach {
                Text("${it.boundaries}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("6s", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBattersStats.forEach {
                Text("${it.sixes}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("S/R", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBattersStats.forEach {
                Text("${it.getStrikeRate()}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun BowlersStats(allBowlerStats: List<BowlerStats>) {
    Row(modifier = Modifier
        .padding(horizontal = 8.dp)
        .border(1.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(4.dp))
        .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text("Name", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBowlerStats.forEach {
                Text(it.bowler.name, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("O", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBowlerStats.forEach {
                Text("${it.overs}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("M", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBowlerStats.forEach {
                Text("${it.maidenOvers}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("R", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBowlerStats.forEach {
                Text("${it.runs}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("W", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBowlerStats.forEach {
                Text("${it.wickets}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Econ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            allBowlerStats.forEach {
                Text("${it.economy}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePreviewDialog(
    teamLabel: String,
    innings: InningsSummary,
    oppositionLabel: String,
    oppositionInnings: InningsSummary,
    onDismiss: () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    val imageSharer = rememberImageSharer()
    var isSharing by remember { mutableStateOf(false) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Share Preview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(12.dp))

            // Scrollable preview of the shareable card
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                ShareableScoreCard(
                    teamLabel = teamLabel,
                    innings = innings,
                    oppositionLabel = oppositionLabel,
                    oppositionInnings = oppositionInnings,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }
                            drawLayer(graphicsLayer)
                        }
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (!isSharing) {
                            isSharing = true
                            coroutineScope.launch {
                                val bitmap = graphicsLayer.toImageBitmap()
                                imageSharer.share(bitmap, "Bails Scoreboard")
                                isSharing = false
                                onDismiss()
                            }
                        }
                    },
                    enabled = !isSharing
                ) {
                    Text(if (isSharing) "Sharing..." else "Share")
                }
            }
        }
    }
}
