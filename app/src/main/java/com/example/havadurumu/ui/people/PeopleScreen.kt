package com.example.havadurumu.ui.people

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havadurumu.data.AppContainer
import com.example.havadurumu.model.Advice
import com.example.havadurumu.model.CurrentWeather
import com.example.havadurumu.model.Forecast
import com.example.havadurumu.model.Person
import com.example.havadurumu.model.Place
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.Relation
import com.example.havadurumu.model.WeatherCondition
import com.example.havadurumu.ui.LoadState
import com.example.havadurumu.ui.component.EmptyView
import com.example.havadurumu.ui.component.GradientHeader
import com.example.havadurumu.ui.component.PillChip
import com.example.havadurumu.ui.component.PlaceField
import com.example.havadurumu.ui.component.shareText
import com.example.havadurumu.ui.theme.Background
import com.example.havadurumu.ui.theme.CloudHeartTheme
import com.example.havadurumu.ui.theme.Ink
import com.example.havadurumu.ui.theme.Muted
import com.example.havadurumu.ui.theme.PinkDark
import com.example.havadurumu.ui.theme.PinkLight
import com.example.havadurumu.ui.theme.White

@Composable
fun PeopleRoute(
    onOpenPlace: (String) -> Unit,
    viewModel: PeopleViewModel = viewModel {
        PeopleViewModel(AppContainer.personRepository, AppContainer.weatherRepository, AppContainer.placeRepository)
    }
) {
    val cards by viewModel.uiState.collectAsStateWithLifecycle()
    val citySuggestions by viewModel.citySuggestions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    PeopleScreen(
        cards = cards,
        citySuggestions = citySuggestions,
        onCityQueryChange = viewModel::onCityQueryChange,
        onAddPerson = viewModel::addPerson,
        onRemovePerson = viewModel::removePerson,
        onRetry = viewModel::retry,
        onOpenPlace = onOpenPlace,

        onSendMessage = { message -> context.shareText(message) }
    )
}

@Composable
fun PeopleScreen(
    cards: List<PersonCardState>,
    citySuggestions: List<PlaceSuggestion>,
    onCityQueryChange: (String) -> Unit,
    onAddPerson: (name: String, relation: Relation, city: String) -> Unit,
    onRemovePerson: (Long) -> Unit,
    onRetry: (String) -> Unit,
    onOpenPlace: (String) -> Unit,
    onSendMessage: (String) -> Unit
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
                title = "Sevdiklerim",
                subtitle = "Sevdiklerinin şehrinde hava nasıl?"
            )
        }
        if (cards.isEmpty()) {
            item {
                EmptyView(
                    emoji = "💌",
                    message = "Henüz kimseyi eklemedin.\nAnneni, kardeşini ya da en yakın arkadaşını ekle; " +
                        "şehrindeki havayı gör ve ona tatlı bir mesaj gönder."
                )
            }
        }
        items(items = cards, key = { it.person.id }) { card ->
            Box(side) {
                PersonCard(
                    card = card,
                    onRemove = { onRemovePerson(card.person.id) },
                    onRetry = { onRetry(card.person.city) },
                    onOpenPlace = { onOpenPlace(card.person.city) },
                    onSendMessage = onSendMessage
                )
            }
        }
        item {
            Button(
                onClick = { showAddDialog = true },
                modifier = side.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PinkDark)
            ) { Text("＋ Sevdiğini ekle") }
        }
    }

    if (showAddDialog) {
        AddPersonDialog(
            citySuggestions = citySuggestions,
            onCityQueryChange = onCityQueryChange,
            onDismiss = {
                onCityQueryChange("")
                showAddDialog = false
            },
            onSave = { name, relation, city ->
                onAddPerson(name, relation, city)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PersonCard(
    card: PersonCardState,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
    onOpenPlace: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    val person = card.person
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
                        .background(PinkLight),
                    contentAlignment = Alignment.Center
                ) { Text(person.emoji, fontSize = 26.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(person.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)

                    val placeText = (card.weather as? LoadState.Success<Forecast>)
                        ?.data?.place?.displayName ?: person.city
                    Text(placeText, fontSize = 13.sp, color = Muted)
                }

                when (val w = card.weather) {
                    is LoadState.Success -> Text(
                        "${w.data.current.emoji} ${w.data.current.temperature}°",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PinkDark
                    )
                    is LoadState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PinkDark,
                        strokeWidth = 2.dp
                    )
                    else -> Unit
                }
            }

            Spacer(Modifier.height(10.dp))

            when (val w = card.weather) {
                is LoadState.Success -> {
                    Text(
                        "${w.data.current.condition.label} · ${Advice.careTip(w.data.current)}",
                        fontSize = 14.sp,
                        color = Ink
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onSendMessage(Advice.loveMessage(person, w.data)) },
                            colors = ButtonDefaults.buttonColors(containerColor = PinkDark)
                        ) { Text("Mesaj gönder") }
                        TextButton(onClick = onRemove) { Text("Sil", color = Muted) }
                    }
                }
                is LoadState.Loading -> Text("Hava durumuna bakılıyor...", fontSize = 14.sp, color = Muted)
                is LoadState.Empty -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "\"${person.city}\" bulunamadı",
                        modifier = Modifier.weight(1f),
                        fontSize = 14.sp,
                        color = PinkDark
                    )
                    TextButton(onClick = onRemove) { Text("Sil", color = Muted) }
                }
                is LoadState.Error -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(w.message, modifier = Modifier.weight(1f), fontSize = 14.sp, color = PinkDark)
                    TextButton(onClick = onRetry) { Text("Tekrar dene", color = PinkDark) }
                }
            }
        }
    }
}

@Composable
private fun AddPersonDialog(
    citySuggestions: List<PlaceSuggestion>,
    onCityQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (name: String, relation: Relation, city: String) -> Unit
) {
    var relation by rememberSaveable { mutableStateOf(Relation.MOTHER) }

    var name by rememberSaveable { mutableStateOf(Relation.MOTHER.label) }
    var city by rememberSaveable { mutableStateOf("") }
    val canSave = name.isNotBlank() && city.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sevdiğini ekle") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Senin neyin?", fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Relation.entries.forEach { r ->
                        PillChip(
                            text = "${r.emoji} ${r.label}",
                            selected = r == relation,
                            onClick = {
                                if (name.isBlank() || Relation.entries.any { it.label == name }) {
                                    name = if (r == Relation.OTHER || r == Relation.FRIEND) "" else r.label
                                }
                                relation = r
                            }
                        )
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(if (relation == Relation.FRIEND) "Adı (ör. Zeynep)" else "Adı (ör. Annem)")
                    },
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
                    label = "Nerede yaşıyor? (il, ilçe, semt)"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, relation, city) }, enabled = canSave) {
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
private fun PeopleScreenPreview() {
    val forecast = Forecast(
        place = Place("Ankara", null, 39.9, 32.8),
        current = CurrentWeather(2, 80, 10.0, WeatherCondition.SNOW, isDay = true),
        hourly = emptyList(),
        daily = emptyList()
    )
    CloudHeartTheme {
        PeopleScreen(
            cards = listOf(
                PersonCardState(Person(1, "Annem", "👩", "Ankara", Relation.MOTHER), LoadState.Success(forecast)),
                PersonCardState(Person(2, "Zeynep", "👯‍♀️", "İzmir", Relation.FRIEND), LoadState.Loading)
            ),
            citySuggestions = emptyList(),
            onCityQueryChange = {},
            onAddPerson = { _, _, _ -> },
            onRemovePerson = {},
            onRetry = {},
            onOpenPlace = {},
            onSendMessage = {}
        )
    }
}
