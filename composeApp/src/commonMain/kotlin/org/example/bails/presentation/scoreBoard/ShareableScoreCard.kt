package org.example.bails.presentation.scoreBoard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bails.composeapp.generated.resources.Res
import bails.composeapp.generated.resources.ic_ball
import org.example.bails.presentation.scoreRecorder.BatterStats
import org.example.bails.presentation.scoreRecorder.BowlerStats
import org.example.bails.presentation.scoreRecorder.InningsSummary
import org.example.bails.presentation.scoreRecorder.getStrikeRate
import org.jetbrains.compose.resources.painterResource

// Self-contained colors — not dependent on MaterialTheme
private val CardBackground = Color(0xFF121218)
private val CardSurface = Color(0xFF1E1E2E)
private val CardCyan = Color(0xFF00E5FF)
private val CardTextWhite = Color(0xFFEAEAEA)
private val CardTextGray = Color(0xFF9E9EAE)
private val CardOutline = Color(0xFF3A3A4A)

@Composable
fun ShareableScoreCard(
    teamLabel: String,
    innings: InningsSummary,
    oppositionLabel: String,
    oppositionInnings: InningsSummary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(390.dp)
            .background(CardBackground)
            .padding(16.dp)
    ) {
        // ── Header ──
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_ball),
                contentDescription = null,
                tint = CardCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Bails",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = CardCyan,
            )
        }
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = CardCyan.copy(alpha = 0.3f), thickness = 1.dp)
        Spacer(Modifier.height(12.dp))

        // ── Match Score Summary ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface, RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(teamLabel, fontSize = 11.sp, color = CardTextGray)
                Text(
                    "${innings.score}/${innings.wickets}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CardCyan,
                )
                Text("(${innings.overs} ov)", fontSize = 11.sp, color = CardTextGray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(oppositionLabel, fontSize = 11.sp, color = CardTextGray)
                Text(
                    "${oppositionInnings.score}/${oppositionInnings.wickets}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CardTextWhite,
                )
                Text("(${oppositionInnings.overs} ov)", fontSize = 11.sp, color = CardTextGray)
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Batting ──
        Text(
            "$teamLabel — Batting",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CardCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        ShareBattersTable(innings.allBattersStats)

        Spacer(Modifier.height(16.dp))

        // ── Bowling ──
        Text(
            "Bowling",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = CardCyan,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        ShareBowlersTable(innings.allBowlerStats)

        Spacer(Modifier.height(20.dp))

        // ── Footer ──
        Text(
            "Shared from Bails",
            fontSize = 10.sp,
            color = CardTextGray.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ShareBattersTable(batters: List<BatterStats>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardOutline, RoundedCornerShape(6.dp))
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text("Batter", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CardCyan, modifier = Modifier.weight(1f))
            HeaderCell("R")
            HeaderCell("B")
            HeaderCell("4s")
            HeaderCell("6s")
            HeaderCell("S/R")
        }
        // Data rows
        batters.forEach { batter ->
            HorizontalDivider(color = CardOutline, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(batter.batter.name, fontSize = 12.sp, color = CardTextWhite, modifier = Modifier.weight(1f))
                DataCell("${batter.runs}")
                DataCell("${batter.ballsFaced}")
                DataCell("${batter.boundaries}")
                DataCell("${batter.sixes}")
                DataCell("${batter.getStrikeRate()}")
            }
        }
    }
}

@Composable
private fun ShareBowlersTable(bowlers: List<BowlerStats>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardOutline, RoundedCornerShape(6.dp))
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text("Bowler", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CardCyan, modifier = Modifier.weight(1f))
            HeaderCell("O")
            HeaderCell("M")
            HeaderCell("R")
            HeaderCell("W")
            HeaderCell("Econ")
        }
        // Data rows
        bowlers.forEach { bowler ->
            HorizontalDivider(color = CardOutline, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(bowler.bowler.name, fontSize = 12.sp, color = CardTextWhite, modifier = Modifier.weight(1f))
                DataCell("${bowler.overs}")
                DataCell("${bowler.maidenOvers}")
                DataCell("${bowler.runs}")
                DataCell("${bowler.wickets}")
                DataCell("${bowler.economy}")
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = CardCyan,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(36.dp)
    )
}

@Composable
private fun DataCell(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        color = CardTextWhite,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(36.dp)
    )
}
