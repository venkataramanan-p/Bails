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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreRecorderScreen(
    state: ScoreRecorderScreenState,
    undoLastBall: () -> Unit,
    recordBall: (Ball) -> Unit,
    onStartNextInnings: () -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // todo: interrupt back press and show confirmation dialog

    var showUndoConfirmAlert by rememberSaveable { mutableStateOf(false) }
    var showConfirmBackPressAlert by rememberSaveable { mutableStateOf(false) }

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
                OversAndWickets(balls = state.balls, wickets = state.wickets)
                ScoreRecorder(recordBall = recordBall)
                BallsHistory(state.allBalls)
            }
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
fun BallsHistory(balls: List<Ball>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    val overs = groupBallsIntoOvers(balls)

    LaunchedEffect(balls.size) {
        if (balls.isNotEmpty()) {
            listState.animateScrollToItem(balls.lastIndex)
        }
    }

    Column(
        modifier = Modifier
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
                        overs[it].forEach { ball ->
                            if (ball.ballType == BallType.CORRECT_BALL) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(width = 2.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = ball.score.toString())
                                }
                            } else if (ball.ballType == BallType.WICKET) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(width = 2.dp, color = Color.Red, shape = RoundedCornerShape(12.dp))
                                        .background(Color.Red.copy(alpha = 0.4f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = "W - ${ball.score}")
                                }
                            } else if (ball.ballType == BallType.NO_BALL) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(width = 2.dp, color = Color.Green, shape = RoundedCornerShape(12.dp))
                                        .background(Color.Green.copy(alpha = 0.4f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = "NB - ${ball.score}")
                                }
                            } else if (ball.ballType == BallType.WIDE) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(width = 2.dp, color = Color.Green, shape = RoundedCornerShape(12.dp))
                                        .background(Color.Green.copy(alpha = 0.4f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(text = "W - ${ball.score}")
                                }
                            } else if (ball.ballType == BallType.DOT_BALL) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScoreRecorder(recordBall: (Ball) -> Unit, modifier: Modifier = Modifier) {
    var currentBall: BallType? by rememberSaveable { mutableStateOf(null) }
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
                                recordBall(Ball(BallType.DOT_BALL))
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
                            recordBall(Ball(currentBall!!, it))
                            currentBall = null
                        }
                    )
                }
            }
        }
    }
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

        if (ball.ballType != BallType.WIDE && ball.ballType != BallType.NO_BALL) {
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