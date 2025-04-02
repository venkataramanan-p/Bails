package org.example.bails.matchConfig

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@Composable
fun MatchConfigScreen(
    onStartMatch: (Int, strikerName: String, nonStrikerName: String, bowlerName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { padding ->

        var numberOfOvers: String? by remember { mutableStateOf(null) }
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
                .padding(12.dp)
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current

            NumberFormField(
                title = "Number of overs",
                value = numberOfOvers ?: "",
                onValueChange = { newText ->
                    val number = newText.toIntOrNull()
                    if (number == null && newText.isNotEmpty()) return@NumberFormField  // Ignore non-numeric input
                    if (number == null || (number in 0..20)) numberOfOvers = newText  // Update only if within range
                },
                focusRequester = focusRequester,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            TextFormField(
                title = "Enter the striker batsman name: ",
                value = strikerName,
                onValueChange = { strikerName = it },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            TextFormField(
                title = "Enter the Non-striker batsman name: ",
                value = nonStrikerName,
                onValueChange = { nonStrikerName = it },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            TextFormField(
                title = "Enter the bowler name: ",
                value = bowlerName,
                onValueChange = { bowlerName = it },
                modifier = Modifier.padding(vertical = 8.dp),
                imeAction = ImeAction.Go,
                onGo = {
                    keyboardController?.hide()
                    onStartMatch(numberOfOvers?.toIntOrNull() ?: 0, strikerName, nonStrikerName, bowlerName)
                },
            )
            Button(
                onClick = { onStartMatch(numberOfOvers?.toIntOrNull() ?: 0, strikerName, nonStrikerName, bowlerName) },
                enabled = numberOfOvers?.toIntOrNull() != null && numberOfOvers!!.toIntOrNull()!! > 0,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text("Start")
            }
        }
    }
}

@Composable
fun TextFormField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
    onGo: KeyboardActionScope.() -> Unit = {},
) {
    Column(modifier = modifier) {
        Text(title, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions(imeAction = imeAction, capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            keyboardActions = KeyboardActions(onGo = onGo),
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}

@Composable
fun NumberFormField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    imeAction: ImeAction = ImeAction.Next,
) {
    Column(modifier = modifier) {
        Text(title, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = imeAction),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester = focusRequester)
        )
    }
}