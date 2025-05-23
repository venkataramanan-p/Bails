package org.example.bails.presentation.scoreRecorder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bails.composeapp.generated.resources.Res
import bails.composeapp.generated.resources.ic_ball
import kotlinx.coroutines.launch
import org.example.bails.util.getBowlerStats
import org.example.bails.util.getNumberOfWickets
import org.example.bails.util.getRuns
import org.example.bails.util.roundToDecimals
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreRecorderScreen(
    state: ScoreRecorderScreenState,
    undoLastBall: () -> Boolean,
    recordBall: (Ball) -> Unit,
    onStartNextInnings: () -> Unit,
    onStartNextOver: (bowlerId: Long?, bowlerName: String?) -> Unit,
    goBack: () -> Unit,
    onToggleStrike: () -> Unit,
    onChangeBowler: (bowlerId: Long?, bowlerName: String?) -> Unit,
    onRetiredHurt: (Long, newBatterName: String) -> Unit,
    navigateToScoreBoard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // todo: interrupt back press and show confirmation dialog

    val coroutineScope = rememberCoroutineScope()

    var showUndoConfirmAlert by rememberSaveable { mutableStateOf(false) }
    var showConfirmBackPressAlert by rememberSaveable { mutableStateOf(false) }
    var showNextBowlerSelecttionBottomSheet by rememberSaveable { mutableStateOf(false) }
    var nextBowlerSelectionBottomSheetState = rememberModalBottomSheetState()
    var showEnterPlayerNameBottomSheet by rememberSaveable { mutableStateOf(false) }
    var enterPlayerNameBottomSheetState = rememberModalBottomSheetState()
    var isChangingBowler by rememberSaveable { mutableStateOf(false) }

    if (showNextBowlerSelecttionBottomSheet) {
        SelectNextBowlerBottomSheet(
            allOver = state.asInningsRunning().allOvers,
            sheetState = nextBowlerSelectionBottomSheetState,
            bowlers = (state as ScoreRecorderScreenState.InningsRunning).allOvers.map { it.balls }.flatten().map { it.bowler }.toSet().toList(),
            onBowlerSelected = {
                if(isChangingBowler) {
                    onChangeBowler(it, null)
                    isChangingBowler = false
                } else {
                    onStartNextOver(it, null)
                }

                showNextBowlerSelecttionBottomSheet = false
            },
            onAddNewBowler = {
                coroutineScope.launch {
                    nextBowlerSelectionBottomSheetState.hide()
                }.invokeOnCompletion {
                    showNextBowlerSelecttionBottomSheet = false
                    showEnterPlayerNameBottomSheet = true
                }
            },
            onDismiss = {
                showNextBowlerSelecttionBottomSheet = false
            }
        )
    }

    if (showEnterPlayerNameBottomSheet) {
        EnterPlayerNameBottomSheet(
            sheetState = enterPlayerNameBottomSheetState,
            playerName = "",
            onSetPlayerName = { newBowlerName ->
                coroutineScope.launch {
                    enterPlayerNameBottomSheetState.hide()
                }.invokeOnCompletion {
                    showEnterPlayerNameBottomSheet = false
                    if (isChangingBowler) {
                        onChangeBowler(null, newBowlerName)
                        isChangingBowler = false
                    } else {
                        onStartNextOver(null, newBowlerName)
                    }
                }
            },
            onDismiss = {
                showEnterPlayerNameBottomSheet = false
            }
        )
    }

    LaunchedEffect(state) {
        if (state.asInningsRunning().isInningsCompleted) {
            navigateToScoreBoard()
        }
    }

    if (state is ScoreRecorderScreenState.InningsRunning && state.isOverCompleted) {
        OverCompleted(
            onUndoLastBall = {
                undoLastBall()
                showUndoConfirmAlert = false
            },
            onStartNextOver = {
                showNextBowlerSelecttionBottomSheet = true
            },
            runs = state.asInningsRunning().allOvers.last().getRuns(),
            wickets = state.asInningsRunning().allOvers.last().getNumberOfWickets()
        )
    }

    if (state is ScoreRecorderScreenState.InningsRunning && state.doesWonMatch) {
        MatchWonAlert(
            navigateToScoreBoard = {
                navigateToScoreBoard()
            }
        )
    }

    if (showUndoConfirmAlert) {
        UndoConfirmationAlert(
            onUndo = {
                undoLastBall()
                showUndoConfirmAlert = false
            },
            onCancel = {showUndoConfirmAlert = false}
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bails") },
                actions = {
                    if (state is ScoreRecorderScreenState.InningsRunning) {
                        IconButton(onClick = { showUndoConfirmAlert = true }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Reset",
                                modifier = Modifier.scale(-1f, 1f)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->

        if (showConfirmBackPressAlert) {
            ConfirmBackPressAlert(
                onCancel = {},
                onConfirm = { goBack() }
            )
        }

        if (state is ScoreRecorderScreenState.InningsBreak) {
            InningsBreak(
                state.previousInningsSummary,
                onStartNextInnings
            )
        } else if (state is ScoreRecorderScreenState.InningsRunning) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                if (!state.asInningsRunning().isFirstInning) {
                    TargetScoreUI(state.asInningsRunning().targetScore ?: 0)
                }
                ScoreDisplay(score = state.score, wickets = state.wickets)
                OversAndWickets(
                    balls = state.balls,
                    wickets = state.wickets,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                BallsHistory(state.allOvers)
                ScoreRecorder(
                    state = state,
                    currentStriker = state.battersStats.strikerStats,
                    currentNonStriker = state.battersStats.nonStrikerStats,
                    recordBall = recordBall,
                    currentBowler = state.currentBowler,
                    onToggleStrike = onToggleStrike,
                    onRetiredHurt = onRetiredHurt,
                    onChangeBowler = {
                        isChangingBowler = true
                        showNextBowlerSelecttionBottomSheet = true
                    },
                    navigateToScoreBoardScreen = navigateToScoreBoard,
                    modifier = Modifier.padding(top = 8.dp),
                    onUndoLastBall = { showUndoConfirmAlert = true },
                )
            }
        }
    }
}

@Composable
fun TargetScoreUI(target: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Text("Target $target", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun BowlerStats(stats: BowlerStats, onChangeBowler: () -> Unit) {
    var showBowlerOptions by rememberSaveable { mutableStateOf(false) }
    Row(modifier = Modifier
        .padding(horizontal = 8.dp)
        .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
        .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text("Name", style = MaterialTheme.typography.bodySmall)
            Text(stats.bowler.name, modifier = Modifier.clickable { showBowlerOptions = !showBowlerOptions }.padding(vertical = 8.dp))
            AnimatedVisibility(showBowlerOptions) {
                ChangeBowlerButton(onClick = {
                    showBowlerOptions = false
                    onChangeBowler()
                })
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("O", style = MaterialTheme.typography.bodySmall)
            Text("${stats.overs}", modifier = Modifier.padding(vertical = 8.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("M", style = MaterialTheme.typography.bodySmall)
            Text("${stats.maidenOvers}", modifier = Modifier.padding(vertical = 8.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("R", style = MaterialTheme.typography.bodySmall)
            Text("${stats.runs}", modifier = Modifier.padding(vertical = 8.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("W", style = MaterialTheme.typography.bodySmall)
            Text("${stats.wickets}", modifier = Modifier.padding(vertical = 8.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Econ", style = MaterialTheme.typography.bodySmall)
            Text("${stats.economy}", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterPlayerNameBottomSheet(
    sheetState: SheetState,
    playerName: String,
    onSetPlayerName: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = FocusRequester()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {},
        shape = RectangleShape
    ) {
        var playerName by rememberSaveable { mutableStateOf(playerName) }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = playerName,
                onValueChange = { newValue -> playerName = newValue },
                placeholder = { Text("Enter player name here...") },
                singleLine = true,
                keyboardActions = KeyboardActions(onGo = { onSetPlayerName(playerName) }),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Go,
                    capitalization = KeyboardCapitalization.Words
                ),
                modifier = Modifier
                    .padding(4.dp)
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
            Button(
                onClick = { onSetPlayerName(playerName) },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Set")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchWonAlert(navigateToScoreBoard: () -> Unit) {
    BasicAlertDialog(onDismissRequest = {}) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 16.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_ball),
                contentDescription = "Match won icon",
                modifier = Modifier
                    .size(100.dp)
                    .padding(top = 16.dp)
            )
            Text(
                text = "Match won!!!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Button(onClick = navigateToScoreBoard) {
                Text("Go to ScoreBoard")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverCompleted(onStartNextOver: () -> Unit, onUndoLastBall: () -> Unit, runs: Int, wickets: Int) {
    BasicAlertDialog(onDismissRequest = {}) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 16.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_ball),
                contentDescription = "Over completed icon",
                modifier = Modifier
                    .size(100.dp)
                    .padding(top = 16.dp)
            )
            Text(
                text = "Over completed!!!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text(
                text = "This over: $runs Runs - $wickets Wickets",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Button(onClick = onStartNextOver) {
                Text("Start Next Over")
            }
            TextButton(onClick = onUndoLastBall) {
                Text("Undo Last ball")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectNextBowlerBottomSheet(
    allOver: List<Over>,
    sheetState: SheetState,
    bowlers: List<Bowler>,
    onBowlerSelected: (bowlerId: Long) -> Unit,
    onAddNewBowler: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        SelectNextBowlerUI(allOver, bowlers, onBowlerSelected, onAddNewBowler)
    }
}

@Composable
fun SelectNextBowlerUI(
    allOvers: List<Over>,
    bowlers: List<Bowler>,
    onBowlerSelected: (bowlerId: Long) -> Unit,
    onAddNewBowler: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Select the next bowler",
                fontWeight = FontWeight.Medium,
            )
            TextButton(onClick = onAddNewBowler) {
                Text("Add new bowler")
            }
        }
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = DividerDefaults.color,
                    shape = RoundedCornerShape(12.dp)
                ),
        ) {
            bowlers.forEachIndexed { index, it ->
                Bowler(allOvers, it, onSelected = { onBowlerSelected(it.id) })
                if (index != bowlers.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun Bowler(allOvers: List<Over>, bowler: Bowler, onSelected: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelected() }
            .padding(16.dp)
    ) {
        Text(bowler.name, modifier = Modifier.weight(1f))
        Text(
            "${allOvers.getBowlerStats(bowler).economy}",
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            "${allOvers.getBowlerStats(bowler).wickets}",
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun ChangeStrikerButton(onClick: () -> Unit) {
    Text(
        text = "Assign as striker",
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .padding(horizontal = 8.dp)
            .clip(shape = RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable{ onClick() }
            .padding(4.dp),
    )
}

@Composable
fun RetiredHurtButton(onClick: () -> Unit) {
    Text(
        text = "Retired Hurt",
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .padding(horizontal = 8.dp)
            .clip(shape = RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.error)
            .clickable{ onClick() }
            .padding(4.dp),
    )
}

@Composable
fun ChangeBowlerButton(onClick: () -> Unit) {
    Text(
        text = "Change",
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .padding(horizontal = 8.dp)
            .clip(shape = RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.error)
            .clickable{ onClick() }
            .padding(4.dp),
    )
}

@Composable
fun Batters(
    player1: BatterStats,
    player2: BatterStats,
    modifier: Modifier = Modifier,
    onToggleStrike: () -> Unit,
    onRetiredHurt: (PlainBatter) -> Unit,
) {

    var showStrikerBatterOptions by rememberSaveable { mutableStateOf(false) }
    var showNonStrikerBatterOptions by rememberSaveable { mutableStateOf(false) }

    Row(modifier = modifier
        .padding(horizontal = 8.dp)
        .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
        .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text("Name", style = MaterialTheme.typography.bodySmall)
            Text(player1.batter.name, modifier = Modifier.padding(vertical = 8.dp).clickable { showStrikerBatterOptions = !showStrikerBatterOptions })
            AnimatedVisibility(showStrikerBatterOptions) {
                RetiredHurtButton(onClick = {
                    showStrikerBatterOptions = false
                    onRetiredHurt(player1.batter)
                })
            }
            Text(player2.batter.name, modifier = Modifier.padding(vertical = 8.dp).clickable { showNonStrikerBatterOptions = !showNonStrikerBatterOptions })
            AnimatedVisibility(showNonStrikerBatterOptions) {
                Row {
                    ChangeStrikerButton {
                        showNonStrikerBatterOptions = false
                        onToggleStrike()
                    }
                    RetiredHurtButton {
                        showNonStrikerBatterOptions = false
                        onRetiredHurt(player2.batter)
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("R", style = MaterialTheme.typography.bodySmall)
            Text("${player1.runs}", modifier = Modifier.padding(vertical = 8.dp))
            Text("${player2.runs}", modifier = Modifier.padding(vertical = 8.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("B", style = MaterialTheme.typography.bodySmall)
            Text("${player1.ballsFaced}", modifier = Modifier.padding(vertical = 8.dp))
            Text("${player2.ballsFaced}", modifier = Modifier.padding(vertical = 8.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("4s", style = MaterialTheme.typography.bodySmall)
            Text("${player1.boundaries}", modifier = Modifier.padding(vertical = 8.dp))
            Text("${player2.boundaries}", modifier = Modifier.padding(vertical = 8.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("6s", style = MaterialTheme.typography.bodySmall)
            Text("${player1.sixes}", modifier = Modifier.padding(vertical = 8.dp))
            Text("${player2.sixes}", modifier = Modifier.padding(vertical = 8.dp))
        }
        Column(modifier = Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("S/R", style = MaterialTheme.typography.bodySmall)
            Text("${player1.getStrikeRate()}", modifier = Modifier.padding(vertical = 8.dp))
            Text("${player2.getStrikeRate()}", modifier = Modifier.padding(vertical = 8.dp))
        }
    }
}

@Composable
fun InningsBreak(previousInningsSummary: InningsSummary, onStartNextInnings: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Innings Break", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Score: ${previousInningsSummary.score} / ${previousInningsSummary.wickets}",
                modifier = Modifier.padding(8.dp)
            )
            Text(
                text = "Overs: ${previousInningsSummary.overs}",
                modifier = Modifier.padding(8.dp)
            )
            Text(
                "Batters",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(8.dp),
            )
            BattersStats(
                allBattersStats = previousInningsSummary.allBattersStats
            )
        }
        Text(
            "Bowlers",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(8.dp),
        )
        BowlersStats(
            allBowlerStats = previousInningsSummary.allBowlerStats
        )
        Button(onClick = onStartNextInnings) {
            Text("Start Next innings")
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
fun BallsHistory(overs: List<Over>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    LaunchedEffect(overs.map { it.balls }.flatten().size) {
        if (overs.isNotEmpty() && overs.first().balls.isNotEmpty()) {
            listState.animateScrollToItem(overs.map { it.balls }.flatten().lastIndex)
        }
    }

    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .animateContentSize()
    ) {
        Text(
            text = "History",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(top = 12.dp)
                .padding(horizontal = 12.dp)
        )

        if (overs.size == 0) {
            Text("Inninga not started", textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth())
        }
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(start = 12.dp, end = 96.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(overs.size) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Over ${it + 1}", modifier = Modifier.padding(bottom = 12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        overs[it].balls.forEach { ball ->
                            if (ball is Ball.CorrectBall) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface)
                                ) {
                                    Text(text = ball.score.toString())
                                }
                            } else if (ball is Ball.Wicket) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .background(Color.Red.copy(alpha = 0.4f))
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(text = "W - ${ball.score}")
                                }
                            } else if (ball is Ball.NoBall) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .background(Color.Green.copy(alpha = 0.4f))
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(text = "NB - ${ball.score}")
                                }
                            } else if (ball is Ball.WideBall) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .background(Color.Green.copy(alpha = 0.4f))
                                        .padding(horizontal = 4.dp)
                                ) {
                                    Text(text = "W - ${ball.score}")
                                }
                            } else if (ball is Ball.DotBall) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(width = 1.dp, color = Color.Gray)
                                ) {
                                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(Color.Green))
                                }
                            }
                        }
                    }
                }
                if (it != overs.lastIndex) {
                    Row(modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp)) {
                        Row(
                            modifier = Modifier
                                .padding(top = 24.dp)
                                .height(20.dp)
                                .width(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.LightGray)
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
fun ColumnScope.ScoreDisplay(score: Int, modifier: Modifier = Modifier, wickets: Int) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score - $wickets",
            fontSize = 50.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OversAndWickets(balls: Int, wickets: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Text("Overs: ", fontWeight = FontWeight.Bold)
            Text("${balls / 6}.${balls % 6}")
        }
        Row {
            Text("Wickets: ", fontWeight = FontWeight.Bold)
            Text("${wickets}")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ScoreRecorder(
    state: ScoreRecorderScreenState.InningsRunning,
    currentStriker: BatterStats?,
    currentNonStriker: BatterStats?,
    currentBowler: Bowler,
    recordBall: (Ball) -> Unit,
    onToggleStrike: () -> Unit,
    onRetiredHurt: (Long, newBatterName: String) -> Unit,
    onChangeBowler: () -> Unit,
    navigateToScoreBoardScreen: () -> Unit,
    onUndoLastBall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentBall: BallType? by rememberSaveable { mutableStateOf(null) }
    var showEnterPlayerNameBottomSheet by rememberSaveable { mutableStateOf(false) }
    var enterPlayerNameBottomSheetState = rememberModalBottomSheetState()
    var showSelectOutPlayerSheet by rememberSaveable { mutableStateOf(false) }
    var selectOutPlayerBottomSheetState = rememberModalBottomSheetState()
    var retiredHurtPlayerId by rememberSaveable { mutableStateOf<Long?>(null) }
    var currentOutPlayerId: Long? by rememberSaveable { mutableStateOf(null) }
    var currentScore: Int by rememberSaveable { mutableStateOf(0)}
    val coroutineScope = rememberCoroutineScope()

    if (showEnterPlayerNameBottomSheet) {
        EnterPlayerNameBottomSheet(
            sheetState = enterPlayerNameBottomSheetState,
            playerName = "",
            onSetPlayerName = { newPlayerName ->
                retiredHurtPlayerId?.let {
                    onRetiredHurt(it, newPlayerName)
                    retiredHurtPlayerId = null
                }
                currentOutPlayerId?.let {
                    recordBall(Ball.Wicket(currentScore, it, newPlayerName, currentBowler, state.currentPlainStriker, state.currentPlainNonStriker))
                }
                showEnterPlayerNameBottomSheet = false
                currentBall = null
            },
            onDismiss = { showEnterPlayerNameBottomSheet = false }
        )
    }

    if (showSelectOutPlayerSheet) {
        SelectOutPlayerName(
            bottomSheetState = selectOutPlayerBottomSheetState,
            player1Name = currentStriker?.batter,
            player2Name = currentNonStriker?.batter,
            onDismissRequest = { showSelectOutPlayerSheet = false },
            onSelected = { outPlayerId ->
                coroutineScope.launch {
                    selectOutPlayerBottomSheetState.hide()
                }.invokeOnCompletion {
                    currentOutPlayerId = outPlayerId
                    showSelectOutPlayerSheet = false
                    showEnterPlayerNameBottomSheet = true
                }
            }
        )
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Batters",
                style = MaterialTheme.typography.titleSmall,
            )
            TextButton(onClick = navigateToScoreBoardScreen) {
                Text("Score Board")
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Arrow",
                )
            }
        }
        Batters(
            player1 = state.battersStats.strikerStats,
            player2 = state.battersStats.nonStrikerStats,
            onToggleStrike = onToggleStrike,
            onRetiredHurt = {
                retiredHurtPlayerId = it.id
                showEnterPlayerNameBottomSheet = true
            }
        )
        Text(
            text = "Bowler",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(8.dp)
        )
        BowlerStats(stats = state.bowlersStats, onChangeBowler = onChangeBowler)
        HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
        ScoreRecorderBoard(
            recordBall = { ballType, score ->
                if (ballType == BallType.WICKET) {
                    currentScore = score
                    currentBall = BallType.WICKET
                    showSelectOutPlayerSheet = true
                } else {
                    when(ballType) {
                        BallType.WIDE -> recordBall(Ball.WideBall(score, currentBowler, state.currentPlainStriker, state.currentPlainNonStriker))
                        BallType.NO_BALL -> recordBall(Ball.NoBall(score, currentBowler, state.currentPlainStriker, state.currentPlainNonStriker))
                        BallType.DOT_BALL -> recordBall(Ball.DotBall(currentBowler, state.currentPlainStriker, state.currentPlainNonStriker))
                        BallType.CORRECT_BALL -> recordBall(Ball.CorrectBall(score, currentBowler, state.currentPlainStriker, state.currentPlainNonStriker))
                        else -> Unit
                    }
                }
            },
            onUndoLastBall = onUndoLastBall
        )
        /*Row(modifier = modifier.fillMaxWidth().height(100.dp).padding(horizontal = 12.dp)) {
            AnimatedVisibility(visible = currentBall == null) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                ) {
                    BallType.entries.forEach {
                        BallLabel(
                            text = it.displayStr,
                            onClick = {
                                if (it == BallType.DOT_BALL) {
                                    recordBall(Ball.DotBall(currentBowler, state.currentPlainStriker, state.currentPlainNonStriker))
                                } else {
                                    currentBall = it
                                }
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(visible = currentBall != null) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(100.dp)
                ) {
                    (0..6).forEach {
                        ScoreLabel(
                            score = it,
                            onClick = {
                                if (currentBall == BallType.WICKET) {
                                    showSelectOutPlayerSheet = true
                                    currentScore = it
                                } else {
                                    when(currentBall) {
                                        BallType.WIDE -> recordBall(Ball.WideBall(it, currentBowler, state.currentPlainStriker, state.currentPlainNonStriker))
                                        BallType.CORRECT_BALL -> recordBall(Ball.CorrectBall(it, currentBowler, state.currentPlainStriker, state.currentPlainNonStriker))
                                        BallType.NO_BALL ->  recordBall(Ball.NoBall(it, currentBowler, state.currentPlainStriker, state.currentPlainNonStriker))
                                        else -> Unit
                                    }
                                    currentBall = null
                                }
                            }
                        )
                    }
                }
            }
        }*/
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectOutPlayerName(
    bottomSheetState: SheetState,
    player1Name: PlainBatter?,
    player2Name: PlainBatter?,
    onDismissRequest: () -> Unit,
    onSelected: (playerId: Long) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = bottomSheetState) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Text(
                text = "Who got out?",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = DividerDefaults.color,
                        shape = RoundedCornerShape(12.dp)
                    ),
            ) {
                PlayerNameText(name = player1Name?.name ?: "", onSelected = { onSelected(player1Name?.id ?: 0L) })
                HorizontalDivider()
                PlayerNameText(name = player2Name?.name ?: "", onSelected = { onSelected(player2Name?.id ?: 0L) })
            }
        }
    }
}

@Composable
fun PlayerNameText(name: String, onSelected: () -> Unit) {
    Text(
        text = name,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSelected() }
            .padding(16.dp)
    )
}

@Composable
fun BallLabel(text: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(width = 2.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)

    ) {
        Text(text = text)
    }
}

@Composable
fun ScoreLabel(score: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(width = 2.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = score.toString(), fontSize = 20.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UndoConfirmationAlert(modifier: Modifier = Modifier, onCancel: () -> Unit, onUndo: () -> Unit) {
    AlertDialog(
        title = {
            Text("Confirm Undo")
        },
        text = {
            Text("Are you sure to undo the previous ball?")
        },
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = onUndo) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("No")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmBackPressAlert(modifier: Modifier = Modifier, onCancel: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        title = {
            Text("Confirm Back?")
        },
        text = {
            Text("Are you sure to go back? Your progress will be lost permanently.")
        },
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Go Back")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Stay here")
            }
        }
    )
}

@Composable
fun ScoreRecorderBoard(
    recordBall: (ballType: BallType, score: Int) -> Unit,
    onUndoLastBall: () -> Unit
) {

    var ball: BallType by rememberSaveable { mutableStateOf(BallType.CORRECT_BALL) }

    fun recordScore(score: Int) {
        if (score == 0 && ball == BallType.CORRECT_BALL) {
            recordBall(BallType.DOT_BALL, score)
        } else {
            recordBall(ball, score)
        }
        ball = BallType.CORRECT_BALL
    }

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Column(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Row(modifier = Modifier.weight(1f)) {
                    RunBox("0", onClick = { recordScore(0) })
                    RunBox("1", onClick = { recordScore(1) })
                    RunBox("2", onClick = { recordScore(2) })
                }
                Row(modifier = Modifier.weight(1f)) {
                    RunBox("3", onClick = { recordScore(3) })
                    RunBox("4\nFour", onClick = { recordScore(4) })
                    RunBox("6\nSIX", onClick = { recordScore(6) })
                }
            }
            Row(modifier = Modifier.fillMaxWidth().background(Color.Gray.copy(alpha = 0.1f))) {
                BallTypeBox(
                    text = "WD",
                    onClick = { ball = BallType.WIDE},
                    isSelected = ball == BallType.WIDE
                )
                BallTypeBox(
                    text = "NB",
                    onClick = { ball = BallType.NO_BALL },
                    isSelected = ball == BallType.NO_BALL
                )
                BallTypeBox(
                    text = "BYE",
                    onClick = { },
                    isSelected = false
                )
            }
        }
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            BallTypeBox("UNDO", textColor = Color.Blue, onClick = { onUndoLastBall() }, isSelected = false)
            HorizontalDivider()
            BallTypeBox("5, 7", onClick = {}, isSelected = false)
            HorizontalDivider()
            BallTypeBox(
                "OUT",
                textColor = Color.Red,
                isSelected = ball == BallType.WICKET,
                onClick = {
                    ball = BallType.WICKET
                }
            )
            HorizontalDivider()
            BallTypeBox("LB", onClick = {}, isSelected = false)
        }
    }
}

@Composable
fun RowScope.BallTypeBox(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable{ onClick() }
            .then(
                if (isSelected)
                    Modifier.border(color = Color.Green, width = 1.dp)
                else
                    Modifier.border(color = Color.Gray.copy(alpha = 0.1f), width = 0.5.dp)
            )
            .weight(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text)
    }
}

@Composable
fun ColumnScope.BallTypeBox(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected)
                    Modifier.border(color = Color.Green, width = 1.dp)
                else
                    Modifier
            )
            .clickable{ onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor)
    }
}

@Composable
fun RowScope.RunBox(runs: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxHeight()
            .clickable { onClick() }
            .weight(1f)
            .border(color = Color.Gray.copy(alpha = 0.2f), width = 1.dp)
    ) {
        Text(runs, textAlign = TextAlign.Center)
    }
}


fun BatterStats.getStrikeRate(): Float {
    return if(this.ballsFaced == 0) 0f
    else ((this.runs / this.ballsFaced.toFloat()) * 100f).roundToDecimals(2)
}

@Preview
@Composable
fun ScoreRecorderBoardPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(modifier = Modifier.background(color = Color.White)) {
            ScoreRecorderBoard(recordBall = {_, _ -> }, onUndoLastBall = {})
        }
    }
}

@Preview
@Composable
fun OverCompletedAlertPreview() {
    MaterialTheme {
        OverCompleted(
            onStartNextOver = {},
            onUndoLastBall = {},
            runs = 13,
            wickets = 1,
        )
    }
}

@Preview
@Composable
fun SelectNextBowlerBotttomSheetPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface {
            SelectNextBowlerUI(
                allOvers = listOf(
                    Over(balls = listOf())
                ),
                bowlers = listOf(
                    Bowler(1L, "Venkat"),
                    Bowler(2L, "Ramanan")
                ),
                onBowlerSelected = {},
                onAddNewBowler = {},
            )
        }
    }
}