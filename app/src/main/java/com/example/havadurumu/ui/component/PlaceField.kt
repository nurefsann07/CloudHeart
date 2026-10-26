package com.example.havadurumu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.ui.theme.Ink
import com.example.havadurumu.ui.theme.Muted
import com.example.havadurumu.ui.theme.PinkLight

@Composable
fun PlaceField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<PlaceSuggestion>,
    onSuggestionPicked: (PlaceSuggestion) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (suggestions.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PinkLight)
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionRow(suggestion = suggestion, onClick = { onSuggestionPicked(suggestion) })
                }
            }
        }
    }
}

@Composable
fun SuggestionRow(suggestion: PlaceSuggestion, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaceIcon()
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(suggestion.displayName, fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 15.sp)
            Text(suggestion.kind, color = Muted, fontSize = 12.sp)
        }
    }
}
