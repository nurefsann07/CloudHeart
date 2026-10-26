package com.example.havadurumu.ui.city

import com.example.havadurumu.ui.component.PlaceIcon
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havadurumu.data.AppContainer
import com.example.havadurumu.model.City
import com.example.havadurumu.model.PlaceSuggestion
import com.example.havadurumu.model.TURKISH_CITIES
import com.example.havadurumu.ui.component.EmptyView
import com.example.havadurumu.ui.component.FavoriteHeart
import com.example.havadurumu.ui.component.GradientHeader
import com.example.havadurumu.ui.component.PillChip
import com.example.havadurumu.ui.component.SectionTitle
import com.example.havadurumu.ui.theme.Background
import com.example.havadurumu.ui.theme.BlueDark
import com.example.havadurumu.ui.theme.BlueLight
import com.example.havadurumu.ui.theme.CloudHeartTheme
import com.example.havadurumu.ui.theme.Muted
import com.example.havadurumu.ui.theme.PinkDark
import com.example.havadurumu.ui.theme.PinkLight
import com.example.havadurumu.ui.theme.White

@Composable
fun CityRoute(
    onOpenPlace: (String) -> Unit,
    viewModel: CityViewModel = viewModel {
        CityViewModel(AppContainer.favoriteRepository, AppContainer.placeRepository)
    }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CityScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onFavoritesOnlyChange = viewModel::onFavoritesOnlyChange,
        onToggleFavorite = viewModel::onToggleFavorite,
        onOpenPlace = { name ->
            viewModel.onPlaceSelected(name)
            onOpenPlace(name)
        }
    )
}

@Composable
fun CityScreen(
    state: CityUiState,
    onQueryChange: (String) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenPlace: (String) -> Unit
) {
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
                title = "CloudHeart",
                subtitle = "Türkiye'nin şehirlerini keşfet"
            ) {
                Spacer(Modifier.height(16.dp))
                SearchField(query = state.query, onQueryChange = onQueryChange)
            }
        }
        item(key = "tabs") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PillChip(
                    text = "Tümü",
                    selected = !state.favoritesOnly,
                    onClick = { onFavoritesOnlyChange(false) },
                    modifier = Modifier.weight(1f)
                )
                PillChip(
                    text = "Favoriler (${state.favoriteCount})",
                    selected = state.favoritesOnly,
                    onClick = { onFavoritesOnlyChange(true) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item(key = "count") {
            Text(
                text = if (state.favoritesOnly) {
                    "${state.cities.size + state.favoritePlaces.size} favori"
                } else {
                    "${state.cities.size + state.districts.size} sonuç"
                },
                modifier = Modifier.padding(start = 20.dp),
                fontSize = 13.sp,
                color = Muted
            )
        }
        items(items = state.cities, key = { it.plate }) { city ->
            Box(side) {
                CityCard(
                    city = city,
                    isFavorite = state.isFavorite(city.name),
                    isSelected = city.name == state.lastSelected,
                    onClick = { onOpenPlace(city.name) },
                    onToggleFavorite = { onToggleFavorite(city.name) }
                )
            }
        }

        items(items = state.favoritePlaces, key = { "f-$it" }) { place ->
            Box(side) {
                PlaceCard(
                    title = place,
                    subtitle = "favori yer",
                    isFavorite = true,
                    isSelected = place == state.lastSelected,
                    onClick = { onOpenPlace(place) },
                    onToggleFavorite = { onToggleFavorite(place) }
                )
            }
        }

        if (state.districts.isNotEmpty()) {
            item(key = "districts-title") {
                SectionTitle("İlçe ve semtler", modifier = side.padding(top = 4.dp, start = 4.dp))
            }
            items(items = state.districts, key = { "d-" + it.key }) { suggestion ->
                Box(side) {
                    PlaceCard(
                        title = suggestion.displayName,
                        subtitle = suggestion.kind,
                        isFavorite = state.isFavorite(suggestion.displayName),
                        isSelected = suggestion.displayName == state.lastSelected,
                        onClick = { onOpenPlace(suggestion.displayName) },
                        onToggleFavorite = { onToggleFavorite(suggestion.displayName) }
                    )
                }
            }
        }

        if (state.cities.isEmpty() && state.districts.isEmpty() && state.favoritePlaces.isEmpty()) {
            val query = state.query.trim()
            when {
                query.isNotEmpty() && !state.favoritesOnly -> item(key = "district") {
                    Box(side) { DistrictCard(query = query, onClick = { onOpenPlace(query) }) }
                }
                state.favoritesOnly && state.favoriteCount > 0 -> item(key = "no-match") {
                    EmptyView(
                        emoji = "🔍",
                        message = "Favorilerinde \"$query\" yok.",
                        actionText = "Aramayı temizle",
                        onAction = { onQueryChange("") }
                    )
                }
                state.favoritesOnly -> item(key = "no-favorites") {
                    EmptyView(
                        emoji = "☁️",
                        message = "Henüz favorin yok.\nKalplere dokunarak ekleyebilirsin."
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("İl, ilçe ya da semt ara") },
        singleLine = true,
        trailingIcon = {
            if (query.isNotEmpty()) {
                TextButton(onClick = { onQueryChange("") }) {
                    Text("✕", color = PinkDark, fontSize = 18.sp)
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedBorderColor = White,
            unfocusedBorderColor = White
        )
    )
}

@Composable
private fun CityCard(
    city: City,
    isFavorite: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val odd = city.plate % 2 == 1
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PinkLight else White),
        border = if (isSelected) BorderStroke(2.dp, PinkDark) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (odd) PinkLight else BlueLight),
                contentAlignment = Alignment.Center
            ) {
                Text(city.plateText, fontWeight = FontWeight.Bold, color = if (odd) PinkDark else BlueDark)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Türkiye", fontSize = 13.sp, color = Muted)
            }
            FavoriteHeart(isFavorite = isFavorite, onClick = onToggleFavorite)
        }
    }
}

@Composable
private fun PlaceCard(
    title: String,
    subtitle: String,
    isFavorite: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PinkLight else BlueLight),
        border = if (isSelected) BorderStroke(2.dp, PinkDark) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaceIcon()
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = BlueDark)
                Text(subtitle, fontSize = 13.sp, color = Muted)
            }
            FavoriteHeart(isFavorite = isFavorite, onClick = onToggleFavorite)
        }
    }
}

@Composable
private fun DistrictCard(query: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BlueLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Öneri bulamadım", fontWeight = FontWeight.SemiBold, color = BlueDark)
            Text("Yine de \"$query\" için hava durumuna bakmayı deneyeyim mi? →", color = BlueDark)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CityScreenPreview() {
    CloudHeartTheme {
        CityScreen(
            state = CityUiState(
                favoriteKeys = setOf("ankara", "kadikoy, istanbul"),
                favoriteCount = 2,
                cities = TURKISH_CITIES.take(3),
                districts = listOf(PlaceSuggestion("Kadıköy", "İstanbul", "ilçe")),
                lastSelected = "Ankara"
            ),
            onQueryChange = {},
            onFavoritesOnlyChange = {},
            onToggleFavorite = {},
            onOpenPlace = {}
        )
    }
}
