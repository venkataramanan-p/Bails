package org.example.bails.presentation.matchConfig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.example.bails.data.BailsDb

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchConfigScreen(
    matchId: Long? = null,
    onStartMatch: (Int, strikerName: String, nonStrikerName: String, bowlerName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSecondInnings = matchId != null
    val savedOvers = matchId?.let { BailsDb.getTotalOvers(it) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isSecondInnings) "2nd Innings" else "New Match",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->

        var numberOfOvers: String? by remember { mutableStateOf(savedOvers?.toString()) }
        var strikerName: String by remember { mutableStateOf("") }
        var nonStrikerName: String by remember { mutableStateOf("") }
        var bowlerName: String by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current

            NumberFormField(
                title = "Overs",
                value = numberOfOvers ?: "",
                onValueChange = { newText ->
                    if (isSecondInnings) return@NumberFormField
                    val number = newText.toIntOrNull()
                    if (number == null && newText.isNotEmpty()) return@NumberFormField
                    if (number == null || (number in 0..20)) numberOfOvers = newText
                },
                focusRequester = focusRequester,
                placeholder = "1 - 20",
                enabled = !isSecondInnings,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            TextFormField(
                title = "Striker",
                value = strikerName,
                onValueChange = { strikerName = it },
                placeholder = "e.g., Rohit",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            TextFormField(
                title = "Non-Striker",
                value = nonStrikerName,
                onValueChange = { nonStrikerName = it },
                placeholder = "e.g., Virat",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            TextFormField(
                title = "Bowler",
                value = bowlerName,
                onValueChange = { bowlerName = it },
                placeholder = "e.g., Bumrah",
                modifier = Modifier.padding(vertical = 8.dp),
                imeAction = ImeAction.Go,
                onGo = {
                    keyboardController?.hide()
                    onStartMatch(numberOfOvers?.toIntOrNull() ?: 0, strikerName, nonStrikerName, bowlerName)
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    keyboardController?.hide()
                    onStartMatch(numberOfOvers?.toIntOrNull() ?: 0, strikerName, nonStrikerName, bowlerName)
                },
                enabled = numberOfOvers?.toIntOrNull() != null && numberOfOvers!!.toIntOrNull()!! > 0,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Start Match", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFormField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    imeAction: ImeAction = ImeAction.Next,
    onGo: KeyboardActionScope.() -> Unit = {},
) {
    Column(modifier = modifier) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (placeholder.isNotEmpty()) {{
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }} else null,
            keyboardOptions = KeyboardOptions(imeAction = imeAction, capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            keyboardActions = KeyboardActions(onGo = onGo),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberFormField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Next,
) {
    Column(modifier = modifier) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            placeholder = if (placeholder.isNotEmpty()) {{
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }} else null,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = imeAction),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester = focusRequester)
        )
    }
}
