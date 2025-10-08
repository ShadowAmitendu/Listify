package com.amitendubikashdhusiya.listify.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CountdownSnackbar(
    message: String,
    actionLabel: String,
    onActionClick: () -> Unit,
    onDismiss: () -> Unit,
    durationMillis: Long = 2000L,
    modifier: Modifier = Modifier
) {
    var timeLeft by remember { mutableStateOf(durationMillis) }
    val progress by animateFloatAsState(
        targetValue = (timeLeft.toFloat() / durationMillis),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        val startTime = System.currentTimeMillis()
        while (timeLeft > 0) {
            delay(50)
            timeLeft = (durationMillis - (System.currentTimeMillis() - startTime)).coerceAtLeast(0)
        }
        onDismiss()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        tonalElevation = 6.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        onActionClick()
                        onDismiss()
                    }
                ) {
                    Text(
                        text = actionLabel,
                        color = MaterialTheme.colorScheme.inversePrimary
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.inversePrimary,
                trackColor = MaterialTheme.colorScheme.inverseSurface
            )
        }
    }
}