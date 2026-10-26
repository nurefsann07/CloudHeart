package com.example.havadurumu.ui.plans

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havadurumu.data.AppContainer
import com.example.havadurumu.model.ActivityType
import com.example.havadurumu.model.Advice
import com.example.havadurumu.model.DailyForecast
import com.example.havadurumu.model.Dates
import com.example.havadurumu.model.Plan
import com.example.havadurumu.model.PlanEvaluation
import com.example.havadurumu.model.PlanScore
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.WeatherCondition
import com.example.havadurumu.ui.LoadState
import com.example.havadurumu.ui.component.EmptyView
import com.example.havadurumu.ui.component.GradientHeader
import com.example.havadurumu.ui.component.PillChip
import com.example.havadurumu.ui.component.PlaceField
import com.example.havadurumu.ui.component.shareText
import com.example.havadurumu.ui.theme.Background
import com.example.havadurumu.ui.theme.BlueDark
import com.example.havadurumu.ui.theme.BlueLight
import com.example.havadurumu.ui.theme.CloudHeartTheme
import com.example.havadurumu.ui.theme.Ink
import com.example.havadurumu.ui.theme.Muted
import com.example.havadurumu.ui.theme.PinkDark
import com.example.havadurumu.ui.theme.ScoreBad
import com.example.havadurumu.ui.theme.ScoreBadBg
import com.example.havadurumu.ui.theme.ScoreGood
import com.example.havadurumu.ui.theme.ScoreGoodBg
import com.example.havadurumu.ui.theme.ScoreMid
import com.example.havadurumu.ui.theme.ScoreMidBg
import com.example.havadurumu.ui.theme.White

@Composable
fun PlansRoute(
    onOpenPlace: (String) -> Unit,
    viewModel: PlansViewModel = viewModel {
        PlansViewModel(AppContainer.planRepository, AppContainer.weatherRepository, AppContainer.placeRepository)
    }
) {
    val cards by viewModel.uiState.collectAsStateWithLifecycle()
    val citySuggestions by viewModel.citySuggestions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    PlansScreen(
        cards = cards,
        citySuggestions = citySuggestions,
        onCityQueryChange = viewModel::onCityQueryChange,
        onAddPlan = viewModel::addPlan,
        onRemovePlan = viewModel::removePlan,
        onRetry = viewModel::retry,
        onOpenPlace = onOpenPlace,
        onInvite = { message -> context.shareText(message) }
    )
}

@Composable
fun PlansScreen(
    cards: List<PlanCardState>,
    citySuggestions: List<PlaceSuggestion>,
    onCityQueryChange: (String) -> Unit,
    onAddPlan: (title: String, activity: ActivityType, city: String, dateIso: String) -> Unit,
    onRemovePlan: (Long) -> Unit,
    onRetry: (String) -> Unit,
    onOpenPlace: (String) -> Unit,
    onInvite: (String) -> Unit
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    val side = Modifier.padding(horizontal = 16.dp)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "header") {
            GradientHeader(
                title = "Planlarım",
                subtitle = "Hava planına uygun mu? Puanını gör, arkadaşlarını davet et"
            )
        }
        if (cards.isEmpty()) {
            item {
                EmptyView(
                    emoji = "🧺",
                    message = "Henüz planın yok.\nPiknik, koşu, sahil ya da kahve buluşması ekle; " +
                        "hava uygun mu sana söyleyeyim."
                )
            }
        }
        items(items = cards, key = { it.plan.id }) { card ->
            Box(side) {
                PlanCard(
                    card = card,
                    onRemove = { onRemovePlan(card.plan.id) },
                    onRetry = { onRetry(card.plan.city) },
                    onOpenPlace = { onOpenPlace(card.plan.city) },
                    onInvite = onInvite
                )
            }
        }
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = side.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PinkDark)
            ) { Text("＋ Plan ekle") }
        }
    }

    if (showAddDialog) {
        AddPlanDialog(
            citySuggestions = citySuggestions,
            onCityQueryChange = onCityQueryChange,
            onDismiss = {
                onCityQueryChange("")
                showAddDialog = false
            },
            onSave = { title, activity, city, dateIso ->
                onAddPlan(title, activity, city, dateIso)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PlanCard(
    card: PlanCardState,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
    onOpenPlace: () -> Unit,
    onInvite: (String) -> Unit
) {
    val plan = card.plan
    val evaluation = (card.evaluation as? LoadState.Success<PlanEvaluation>)?.data

    Card(
        onClick = onOpenPlace,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(BlueLight),
                    contentAlignment = Alignment.Center
                ) { Text(plan.activity.emoji, fontSize = 26.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(plan.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)

                    Text(
                        "${Dates.relativeName(plan.dateIso)} ${Dates.shortDate(plan.dateIso)} · " +
                            "${evaluation?.placeName ?: plan.city}",
                        fontSize = 13.sp,
                        color = Muted
                    )
                }
                when (card.evaluation) {
                    is LoadState.Success -> ScoreBadge(card.evaluation.data.score.score)
                    is LoadState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PinkDark,
                        strokeWidth = 2.dp
                    )
                    else -> Unit
                }
            }

            Spacer(Modifier.height(10.dp))

            when (val e = card.evaluation) {
                is LoadState.Success -> EvaluationDetails(e.data)
                is LoadState.Loading -> Text("Hava durumuna bakılıyor...", fontSize = 14.sp, color = Muted)
                is LoadState.Empty -> Text(
                    "Bu plan için tahmin yok (şehir bulunamadı ya da tarih 7 günlük tahminin dışında)",
                    fontSize = 14.sp,
                    color = Muted
                )
                is LoadState.Error -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(e.message, modifier = Modifier.weight(1f), fontSize = 14.sp, color = PinkDark)
                    TextButton(onClick = onRetry) { Text("Tekrar dene", color = PinkDark) }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onInvite(Advice.invitation(plan, evaluation)) },
                    colors = ButtonDefaults.buttonColors(containerColor = PinkDark)
                ) { Text("Davet et") }
                TextButton(onClick = onRemove) { Text("Sil", color = Muted) }
            }
        }
    }
}

@Composable
private fun EvaluationDetails(evaluation: PlanEvaluation) {
    val day = evaluation.day
    Text(
        "${day.condition.emoji} ${day.condition.label} · En yüksek ${day.max}° · En düşük ${day.min}°",
        fontSize = 14.sp,
        color = Ink
    )
    evaluation.score.reasons.forEach { reason ->
        Text("• $reason", fontSize = 14.sp, color = Ink)
    }
    val betterDay = evaluation.betterDay
    val betterScore = evaluation.betterScore
    if (betterDay != null && betterScore != null) {
        Spacer(Modifier.height(6.dp))
        Text(
            "${Dates.relativeName(betterDay.dateIso)} (${Dates.shortDate(betterDay.dateIso)}) " +
                "daha uygun: ${betterScore.score}/10 ${betterDay.condition.emoji}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = BlueDark
        )
    }
}

@Composable
private fun ScoreBadge(score: Int) {
    val (textColor, badgeColor) = scoreColors(score)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(badgeColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("$score/10", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
    }
}

private fun scoreColors(score: Int): Pair<Color, Color> = when {
    score >= 7 -> ScoreGood to ScoreGoodBg
    score >= 4 -> ScoreMid to ScoreMidBg
    else -> ScoreBad to ScoreBadBg
}

@Composable
private fun AddPlanDialog(
    citySuggestions: List<PlaceSuggestion>,
    onCityQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (title: String, activity: ActivityType, city: String, dateIso: String) -> Unit
) {
    val days = remember { Dates.upcomingDays(7) }
    var activity by rememberSaveable { mutableStateOf(ActivityType.PICNIC) }

    var title by rememberSaveable { mutableStateOf(ActivityType.PICNIC.label) }
    var city by rememberSaveable { mutableStateOf("") }
    var dateIso by rememberSaveable { mutableStateOf(days.first()) }
    val canSave = city.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni plan") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Ne yapacaksın?", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActivityType.entries.forEach { type ->
                        PillChip(
                            text = "${type.emoji} ${type.label}",
                            selected = type == activity,
                            onClick = {
                                if (title.isBlank() || ActivityType.entries.any { it.label == title }) {
                                    title = type.label
                                }
                                activity = type
                            }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık (ör. Moda'da piknik)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                PlaceField(
                    value = city,
                    onValueChange = {
                        city = it
                        onCityQueryChange(it)
                    },
                    suggestions = citySuggestions,
                    onSuggestionPicked = { suggestion ->
                        city = suggestion.displayName
                        onCityQueryChange("")
                    },
                    label = "Nerede? (il, ilçe ya da semt, ör. Moda)"
                )
                Text("Hangi gün?", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    days.forEach { iso ->
                        PillChip(
                            text = "${Dates.relativeName(iso)} ${Dates.shortDate(iso)}",
                            selected = iso == dateIso,
                            onClick = { dateIso = iso }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, activity, city, dateIso) }, enabled = canSave) {
                Text("Ekle", color = if (canSave) PinkDark else Muted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç", color = Muted) }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PlansScreenPreview() {
    val day = DailyForecast("2026-10-31", 14, 8, WeatherCondition.RAIN, 80, 20.0)
    val sunny = DailyForecast("2026-11-01", 22, 12, WeatherCondition.CLEAR, 0, 10.0)
    val evaluation = PlanEvaluation(
        placeName = "Kadıköy, İstanbul",
        day = day,
        score = PlanScore(2, listOf("Yağış ihtimali %80", "Piknik için serin (en yüksek 14°)")),
        betterDay = sunny,
        betterScore = PlanScore(9, listOf("Harika bir gün"))
    )
    CloudHeartTheme {
        PlansScreen(
            cards = listOf(
                PlanCardState(
                    Plan(1, "Moda'da piknik", ActivityType.PICNIC, "Kadıköy", "2026-10-31"),
                    LoadState.Success(evaluation)
                )
            ),
            citySuggestions = emptyList(),
            onCityQueryChange = {},
            onAddPlan = { _, _, _, _ -> },
            onRemovePlan = {},
            onRetry = {},
            onOpenPlace = {},
            onInvite = {}
        )
    }
}
