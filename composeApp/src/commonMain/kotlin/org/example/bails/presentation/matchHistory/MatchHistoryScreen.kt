package org.example.bails.presentation.matchHistory

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.bails.data.MatchSummaryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryScreen(
    state: MatchHistoryState,
    onNewMatch: () -> Unit,
    onMatchClick: (Long) -> Unit,
    onDeleteMatch: (Long) -> Unit,
) {
    var matchToDelete by remember { mutableStateOf<MatchSummaryItem?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Bails",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewMatch,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Match")
            }
        }
    ) { padding ->
        when (state) {
            is MatchHistoryState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is MatchHistoryState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matches yet",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tap + to start a new match",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            is MatchHistoryState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
                ) {
                    items(
                        items = state.matches,
                        key = { it.matchId }
                    ) { match ->
                        MatchCard(
                            match = match,
                            onClick = { onMatchClick(match.matchId) },
                            onDelete = { matchToDelete = match }
                        )
                    }
                }
            }
        }
    }

    matchToDelete?.let { match ->
        AlertDialog(
            onDismissRequest = { matchToDelete = null },
            title = { Text("Delete Match?") },
            text = {
                Text(
                    "Are you sure you want to delete this match from ${formatDate(match.createdAt)}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMatch(match.matchId)
                        matchToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { matchToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MatchCard(
    match: MatchSummaryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 16.dp)) {
            // Date, status and delete row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatDate(match.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (match.isCompleted) "Completed" else "In Progress",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (match.isCompleted)
                            MaterialTheme.colorScheme.tertiary
                        else
                            MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete match",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Overs info
            Text(
                text = "${match.totalOvers} Overs Match",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(8.dp))

            // Scores row
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = match.team1Name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${match.firstInningsScore}/${match.firstInningsWickets}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatOvers(match.firstInningsOvers) + " ov",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // VS divider
                Text(
                    text = "vs",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Team 2
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = match.team2Name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${match.secondInningsScore}/${match.secondInningsWickets}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatOvers(match.secondInningsOvers) + " ov",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatOvers(overs: Float): String {
    val completedOvers = overs.toInt()
    val balls = ((overs - completedOvers) * 10).toInt()
    return "$completedOvers.$balls"
}

private fun formatDate(epochMillis: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(epochMillis)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val month = localDateTime.month.name.take(3).lowercase()
            .replaceFirstChar { it.uppercase() }
        val day = localDateTime.dayOfMonth
        val year = localDateTime.year
        val hour = localDateTime.hour
        val minute = localDateTime.minute.toString().padStart(2, '0')
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        "$month $day, $year  $displayHour:$minute $amPm"
    } catch (e: Exception) {
        "Unknown date"
    }
}
