package org.example.bails.scoreRecorder

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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreRecorderScreen(
    state: ScoreRecorderScreenState,
    undoLastBall: () -> Boolean,
    recordBall: (Ball) -> Unit,
    onStartNextInnings: () -> Unit,
    onStartNextOver: (bowlerId: Long?, bowlerName: String?) -> Unit,
    goBack: () -> Unit,
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

    if (showNextBowlerSelecttionBottomSheet) {
        SelectNextBowlerBottomSheet(
            sheetState = nextBowlerSelectionBottomSheetState,
            bowlers = (state as ScoreRecorderScreenState.InningsRunning).allOvers.map { it.bowler }.toSet().toList(),
            onBowlerSelected = {
                onStartNextOver(it, null)
                showNextBowlerSelecttionBottomSheet = false
            },
            onAddNewBowler = {
                coroutineScope.launch {
                    nextBowlerSelectionBottomSheetState.hide()
                }.invokeOnCompletion {
                    showNextBowlerSelecttionBottomSheet = false
                    showEnterPlayerNameBottomSheet = true
                }
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
                    onStartNextOver(null, newBowlerName)
                }
            },
            onDismiss = {}
        )
    }

    if (state is ScoreRecorderScreenState.InningsRunning && state.isOverCompleted) {
        OverCompleted(
            onUndoLastBall = {
                undoLastBall()
                showUndoConfirmAlert = false
            },
            onStartNextOver = {

                showNextBowlerSelecttionBottomSheet = true
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Bails")
                },
                actions = {
                    if (state is ScoreRecorderScreenState.InningsRunning) {
                        IconButton(
                            onClick = { showUndoConfirmAlert = true }
                        ) {
                            Icon(
                                Icons.Filled.Refresh,
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
                if (showUndoConfirmAlert) {
                    UndoConfirmationAlert(
                        onUndo = {
                            undoLastBall()
                            showUndoConfirmAlert = false
                        },
                        onCancel = {showUndoConfirmAlert = false}
                    )
                }

                ScoreDisplay(score = state.score)
                OversAndWickets(
                    balls = state.balls,
                    wickets = state.wickets,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    "Batters",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(8.dp),
                )
                Batters(
                    player1 = state.currentStriker,
                    player2 = state.currentNonStriker,
                )
                Text(
                    text = "Bowler",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(8.dp)
                )
                BowlerStats(stats = state.bowlersStats)
                ScoreRecorder(
                    currentStriker = state.currentStriker,
                    currentNonStriker = state.currentNonStriker,
                    recordBall = recordBall,
                    currentBowler = state.allOvers.last().bowler,
                    modifier = Modifier.padding(top = 8.dp)
                )
                BallsHistory(state.allOvers)
            }
        }
    }
}

@Composable
fun BowlerStats(stats: BowlerStats) {
    Row(modifier = Modifier
        .padding(horizontal = 8.dp)
        .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
        .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text("Name", style = MaterialTheme.typography.bodySmall)
            Text(stats.bowler.name, modifier = Modifier.padding(vertical = 8.dp))
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
fun OverCompleted(onStartNextOver: () -> Unit, onUndoLastBall: () -> Unit) {
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
    sheetState: SheetState,
    bowlers: List<Bowler>,
    onBowlerSelected: (bowlerId: Long) -> Unit,
    onAddNewBowler: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = {}, sheetState = sheetState) {
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
                bowlers.forEach {
                    PlayerNameText(name = it.name, onSelected = { onBowlerSelected(it.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun Batters(player1: Batter, player2: Batter, modifier: Modifier = Modifier) {
    Row(modifier = modifier
        .padding(horizontal = 8.dp)
        .border(1.dp, Color.Black, shape = RoundedCornerShape(4.dp))
        .padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text("Name", style = MaterialTheme.typography.bodySmall)
            Row {
                Text(player1.name, modifier = Modifier.padding(vertical = 8.dp))
            }
            Row {
                Text(player2.name, modifier = Modifier.padding(vertical = 8.dp))
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
            Text("${ if(player1.ballsFaced == 0) 0 else (player1.runs / player1.ballsFaced) * 100}", modifier = Modifier.padding(vertical = 8.dp))
            Text("${if(player2.ballsFaced == 0) 0 else (player2.runs / player2.ballsFaced) * 100}", modifier = Modifier.padding(vertical = 8.dp))
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
        }
        Button(onClick = onStartNextInnings) {
            Text("Start Next innings")
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
            .padding(vertical = 12.dp)
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
                    Text("Over ${it + 1}")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        overs[it].balls.forEach { ball ->
                            if (ball is Ball.CorrectBall) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(width = 2.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = ball.score.toString())
                                }
                            } else if (ball is Ball.Wicket) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(width = 2.dp, color = Color.Red, shape = RoundedCornerShape(12.dp))
                                        .background(Color.Red.copy(alpha = 0.4f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = "W - ${ball.score}")
                                }
                            } else if (ball is Ball.NoBall) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(width = 2.dp, color = Color.Green, shape = RoundedCornerShape(12.dp))
                                        .background(Color.Green.copy(alpha = 0.4f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = "NB - ${ball.score}")
                                }
                            } else if (ball is Ball.WideBall) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(width = 2.dp, color = Color.Green, shape = RoundedCornerShape(12.dp))
                                        .background(Color.Green.copy(alpha = 0.4f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = "W - ${ball.score}")
                                }
                            } else if (ball is Ball.DotBall) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(12.dp))
                                        .background(Color.LightGray)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = "0")
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
                                .height(24.dp)
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
fun ColumnScope.ScoreDisplay(score: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = score.toString(),
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
fun ScoreRecorder(currentStriker: Batter?, currentNonStriker: Batter?, currentBowler: Bowler, recordBall: (Ball) -> Unit, modifier: Modifier = Modifier) {
    var currentBall: BallType? by rememberSaveable { mutableStateOf(null) }
    var showEnterPlayerNameBottomSheet by rememberSaveable { mutableStateOf(false) }
    var enterPlayerNameBottomSheetState = rememberModalBottomSheetState()
    var showSelectOutPlayerSheet by rememberSaveable { mutableStateOf(false) }
    var selectOutPlayerBottomSheetState = rememberModalBottomSheetState()
    var currentOutPlayerId: Long? by rememberSaveable { mutableStateOf(null) }
    var currentScore: Int by rememberSaveable { mutableStateOf(0)}
    val coroutineScope = rememberCoroutineScope()

    if (showEnterPlayerNameBottomSheet) {
        EnterPlayerNameBottomSheet(
            sheetState = enterPlayerNameBottomSheetState,
            playerName = "",
            onSetPlayerName = { newPlayerName ->
                currentOutPlayerId?.let {
                    recordBall(Ball.Wicket(currentScore, it, newPlayerName, currentBowler))
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
            player1Name = currentStriker,
            player2Name = currentNonStriker,
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


    Row(modifier = modifier.fillMaxWidth().height(100.dp).padding(horizontal = 12.dp)) {
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
                                recordBall(Ball.DotBall(currentBowler))
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
                                    BallType.WIDE -> recordBall(Ball.WideBall(it, currentBowler))
                                    BallType.CORRECT_BALL -> recordBall(Ball.CorrectBall(it, currentBowler))
                                    BallType.NO_BALL ->  recordBall(Ball.NoBall(it, currentBowler))
                                    else -> Unit
                                }
                                currentBall = null
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectOutPlayerName(
    bottomSheetState: SheetState,
    player1Name: Batter?,
    player2Name: Batter?,
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


fun groupBallsIntoOvers(balls: List<Ball>): List<List<Ball>> {
    val overs = mutableListOf<List<Ball>>()
    val currentOver = mutableListOf<Ball>()
    var validBallCount = 0

    for (ball in balls) {
        currentOver.add(ball)

        if (ball !is Ball.WideBall && ball !is Ball.NoBall) {
            validBallCount++
        }

        if (validBallCount == 6) {
            overs.add(currentOver.toList()) // Add completed over
            currentOver.clear()
            validBallCount = 0
        }
    }

    if (currentOver.isNotEmpty()) {
        overs.add(currentOver) // Add remaining balls (incomplete over)
    }

    return overs
}