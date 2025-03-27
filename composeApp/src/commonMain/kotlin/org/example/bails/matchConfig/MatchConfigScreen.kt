package org.example.bails.matchConfig

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = CenterHorizontally) {
                Text("Enter the number of overs ", modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = numberOfOvers ?: "",
                    onValueChange = { newText ->
                        val number = newText.toIntOrNull()
                        if (number == null && newText.isNotEmpty()) return@OutlinedTextField  // Ignore non-numeric input
                        if (number == null || (number in 0..20)) numberOfOvers = newText  // Update only if within range
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp),
                    modifier = Modifier
                        .width(100.dp)
                        .focusRequester(focusRequester)
                )
                Text("Enter the striker batsman name: ")
                OutlinedTextField(
                    value = strikerName,
                    onValueChange = { strikerName = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text("Enter the Non-striker batsman name: ")
                OutlinedTextField(
                    value = "",
                    onValueChange = { nonStrikerName = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Text("Enter the bowler name: ")
                OutlinedTextField(
                    value = "",
                    onValueChange = { bowlerName = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Button(
                    onClick = { onStartMatch(numberOfOvers?.toIntOrNull() ?: 0) },
                    enabled = numberOfOvers?.toIntOrNull() != null && numberOfOvers!!.toIntOrNull()!! > 0,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Text("Start")
                }
            }
        }
    }
}