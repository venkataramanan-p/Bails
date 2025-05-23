package org.example.bails.presentation.scoreBoard

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.bails.presentation.scoreRecorder.BatterStats
import org.example.bails.presentation.scoreRecorder.BowlerStats
import org.example.bails.presentation.scoreRecorder.InningsSummary
import org.example.bails.presentation.scoreRecorder.getStrikeRate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreBoardScreen(state: ScoreBoardScreenState, onStartNextInnings: () -> Unit) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Score Board") }) }) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when(state) {
                is ScoreBoardScreenState.Success -> {
                    ScoreBoardSuccessScreen(state.firstInnings, state.secondInnings, onStartNextInnings)
                }
                is ScoreBoardScreenState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading")
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreBoardSuccessScreen(
    firstInning: InningsSummary,
    secondInning: InningsSummary,
    onStartNextInnings: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    var coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Team 1")
                Text("${firstInning.score} / ${firstInning.wickets}")
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Team 2")
                Text("${secondInning.score} / ${secondInning.wickets}")
            }
        }
        TabRow(selectedTabIndex = pagerState.currentPage, modifier = Modifier.fillMaxWidth()) {
            Tab(
                selected = pagerState.currentPage == 1,
                text = { Text("Team 1") },
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(page = 1) } },
            )
            Tab(
                selected = pagerState.currentPage == 2,
                text = { Text("Team 2") },
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(page = 2) } },
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
            Text("Innings Not Started")
            Button(onClick = onStartNextInnings) {
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
                modifier = Modifier.padding(8.dp),
            )
            BattersStats(
                allBattersStats = inningsSummary.allBattersStats
            )
            Text(
                "Bowlers",
                style = MaterialTheme.typography.titleSmall,
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
        .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
        .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text("Name", style = MaterialTheme.typography.bodySmall)
            allBattersStats.forEach {
                Text(it.batter.name, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("R", style = MaterialTheme.typography.bodySmall)
            allBattersStats.forEach {
                Text("${it.runs}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("B", style = MaterialTheme.typography.bodySmall)
            allBattersStats.forEach {
                Text("${it.ballsFaced}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("4s", style = MaterialTheme.typography.bodySmall)
            allBattersStats.forEach {
                Text("${it.boundaries}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("6s", style = MaterialTheme.typography.bodySmall)
            allBattersStats.forEach {
                Text("${it.sixes}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("S/R", style = MaterialTheme.typography.bodySmall)
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
        .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
        .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text("Name", style = MaterialTheme.typography.bodySmall)
            allBowlerStats.forEach {
                Text(it.bowler.name, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("O", style = MaterialTheme.typography.bodySmall)
            allBowlerStats.forEach {
                Text("${it.overs}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("M", style = MaterialTheme.typography.bodySmall)
            allBowlerStats.forEach {
                Text("${it.maidenOvers}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("R", style = MaterialTheme.typography.bodySmall)
            allBowlerStats.forEach {
                Text("${it.runs}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("W", style = MaterialTheme.typography.bodySmall)
            allBowlerStats.forEach {
                Text("${it.wickets}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Econ", style = MaterialTheme.typography.bodySmall)
            allBowlerStats.forEach {
                Text("${it.economy}", modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}