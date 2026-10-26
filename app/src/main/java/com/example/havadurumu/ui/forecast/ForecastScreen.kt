package com.example.havadurumu.ui.forecast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.havadurumu.data.AppContainer
import com.example.havadurumu.model.Advice
import com.example.havadurumu.model.CurrentWeather
import com.example.havadurumu.model.DailyForecast
import com.example.havadurumu.model.District
import com.example.havadurumu.model.DistrictWeather
import com.example.havadurumu.model.TurkishText
import com.example.havadurumu.model.Dates
import com.example.havadurumu.model.Forecast
import com.example.havadurumu.model.HourlyForecast
import com.example.havadurumu.model.Place
import com.example.havadurumu.model.WeatherCondition
import com.example.havadurumu.ui.LoadState
import com.example.havadurumu.ui.component.EmptyView
import com.example.havadurumu.ui.component.ErrorView
import com.example.havadurumu.ui.component.FavoriteHeart
import com.example.havadurumu.ui.component.GradientHeader
import com.example.havadurumu.ui.component.LoadingView
import com.example.havadurumu.ui.component.SectionTitle
import com.example.havadurumu.ui.theme.Background
import com.example.havadurumu.ui.theme.BlueDark
import com.example.havadurumu.ui.theme.BlueLight
import com.example.havadurumu.ui.theme.BrandColors
import com.example.havadurumu.ui.theme.CloudHeartTheme
import com.example.havadurumu.ui.theme.Ink
import com.example.havadurumu.ui.theme.Muted
import com.example.havadurumu.ui.theme.PinkDark
import com.example.havadurumu.ui.theme.PinkLight
import com.example.havadurumu.ui.theme.White
import com.example.havadurumu.ui.theme.weatherColors

@Composable
fun ForecastRoute(
    placeName: String,
    onBack: () -> Unit,
    onOpenPlace: (String) -> Unit,
    viewModel: ForecastViewModel = viewModel(key = placeName) {
        ForecastViewModel(
            placeName,
            AppContainer.weatherRepository,
            AppContainer.districtRepository,
            AppContainer.favoriteRepository
        )
    }
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val districts by viewModel.districts.collectAsStateWithLifecycle()
    val favoriteKeys by viewModel.favoriteKeys.collectAsStateWithLifecycle()
    ForecastScreen(
        placeName = placeName,
        state = state,
        districts = districts,
        favoriteKeys = favoriteKeys,
        onBack = onBack,
        onRetry = viewModel::load,
        onRetryDistricts = viewModel::loadDistricts,
        onToggleFavorite = viewModel::toggleFavorite,
        onOpenPlace = onOpenPlace
    )
}

@Composable
fun ForecastScreen(
    placeName: String,
    state: LoadState<Forecast>,
    districts: LoadState<List<DistrictWeather>>?,
    favoriteKeys: Set<String>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRetryDistricts: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenPlace: (String) -> Unit
) {
    val isFavorite: (String) -> Boolean = { name -> TurkishText.normalize(name) in favoriteKeys }
    val success = state as? LoadState.Success<Forecast>

    val colors = success?.data?.current
        ?.let { weatherColors(it.condition, it.isDay) }
        ?: BrandColors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.first().copy(alpha = 0.18f), Background)))
    ) {
        GradientHeader(
            title = placeName,
            subtitle = success?.data?.place?.displayName?.let { it } ?: "Hava durumu",
            colors = colors,
            onBack = onBack,
            trailing = {
                FavoriteHeart(isFavorite = isFavorite(placeName), onClick = { onToggleFavorite(placeName) }, color = Color.White)
            }
        )

        when (state) {
            is LoadState.Loading -> LoadingView()
            is LoadState.Error -> ErrorView(message = state.message, onRetry = onRetry)
            is LoadState.Empty -> EmptyView(
                emoji = "☁️",
                message = "\"$placeName\" diye bir yer bulamadım.\nYazımı kontrol edip tekrar dener misin?"
            )
            is LoadState.Success -> ForecastContent(
                forecast = state.data,
                districts = districts,
                isFavorite = isFavorite,
                onRetryDistricts = onRetryDistricts,
                onToggleFavorite = onToggleFavorite,
                onOpenPlace = onOpenPlace
            )
        }
    }
}

@Composable
private fun ForecastContent(
    forecast: Forecast,
    districts: LoadState<List<DistrictWeather>>?,
    isFavorite: (String) -> Boolean,
    onRetryDistricts: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenPlace: (String) -> Unit
) {
    val current = forecast.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NowCard(current = current, today = forecast.today)

        AdviceCard(
            title = "Bugün ne giymeliyim?",
            lines = Advice.clothing(current, forecast.today, forecast.place),
            background = PinkLight,
            titleColor = PinkDark
        )
        AdviceCard(
            title = "Bugün ne yapsam?",
            lines = Advice.activities(current, forecast.place, forecast.today?.dateIso),
            background = BlueLight,
            titleColor = BlueDark
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoBox("Nem", "%${current.humidity}", BlueLight, BlueDark, Modifier.weight(1f))
            InfoBox("Rüzgâr", Advice.windText(current.windSpeed), PinkLight, PinkDark, Modifier.weight(1f))
        }

        if (forecast.hourly.isNotEmpty()) {
            SectionTitle("Saatlik tahmin")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(forecast.hourly) { hour -> HourBox(hour) }
            }
        }

        if (forecast.daily.isNotEmpty()) {
            SectionTitle("${forecast.daily.size} günlük tahmin")
            forecast.daily.forEach { day -> DayRow(day) }
        }

        if (districts != null) {
            DistrictsSection(
                state = districts,
                isFavorite = isFavorite,
                onRetry = onRetryDistricts,
                onToggleFavorite = onToggleFavorite,
                onOpenPlace = onOpenPlace
            )
        }

        Text(
            text = "Hava: Open-Meteo.com · Konum: © OpenStreetMap",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 12.sp,
            color = Muted
        )
    }
}

@Composable
private fun NowCard(current: CurrentWeather, today: DailyForecast?) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(current.emoji, fontSize = 64.sp)
            Text("${current.temperature}°", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = PinkDark)
            Text(current.condition.label, fontSize = 18.sp, color = Ink)
            if (today != null) {
                Spacer(Modifier.height(8.dp))
                Row {
                    Text("En yüksek ${today.max}°", color = PinkDark, fontWeight = FontWeight.SemiBold)
                    Text("  ·  ", color = Muted)
                    Text("En düşük ${today.min}°", color = BlueDark, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun AdviceCard(title: String, lines: List<String>, background: Color, titleColor: Color) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(Modifier.height(6.dp))
            lines.forEach { line ->
                Text("• $line", fontSize = 15.sp, color = Ink, lineHeight = 22.sp)
            }
        }
    }
}

@Composable
private fun InfoBox(title: String, value: String, background: Color, textColor: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .padding(16.dp)
    ) {
        Text(title, fontSize = 14.sp, color = textColor)
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Composable
private fun HourBox(hour: HourlyForecast) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(White)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(hour.label, fontSize = 13.sp, color = Muted)
        Text(hour.condition.emoji, fontSize = 22.sp)
        Text("${hour.temperature}°", fontWeight = FontWeight.Bold, color = PinkDark)
    }
}

@Composable
private fun DayRow(day: DailyForecast) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(Dates.relativeName(day.dateIso), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${Dates.shortDate(day.dateIso)} · Yağış %${day.precipitationProbability}",
                    fontSize = 13.sp,
                    color = Muted
                )
            }
            Text(day.condition.emoji, fontSize = 26.sp)
            Spacer(Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text("En yüksek ${day.max}°", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PinkDark)
                Text("En düşük ${day.min}°", fontSize = 14.sp, color = BlueDark)
            }
        }
    }
}

@Composable
private fun DistrictsSection(
    state: LoadState<List<DistrictWeather>>,
    isFavorite: (String) -> Boolean,
    onRetry: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onOpenPlace: (String) -> Unit
) {
    when (state) {
        is LoadState.Loading -> {
            SectionTitle("İlçeler")
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PinkDark, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("İlçelerin havasına bakılıyor...", color = Muted)
            }
        }
        is LoadState.Error -> {
            SectionTitle("İlçeler")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(state.message, modifier = Modifier.weight(1f), color = PinkDark)
                TextButton(onClick = onRetry) { Text("Tekrar dene", color = PinkDark) }
            }
        }
        is LoadState.Empty -> Unit
        is LoadState.Success -> {
            SectionTitle("İlçeler (${state.data.size})")
            state.data.forEach { item ->
                DistrictRow(
                    item = item,
                    isFavorite = isFavorite(item.district.displayName),
                    onClick = { onOpenPlace(item.district.displayName) },
                    onToggleFavorite = { onToggleFavorite(item.district.displayName) }
                )
            }
        }
    }
}

@Composable
private fun DistrictRow(
    item: DistrictWeather,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.district.name,
                modifier = Modifier.weight(1f),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink
            )
            Text(item.emoji, fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Text("${item.temperature}°", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PinkDark)
            FavoriteHeart(isFavorite = isFavorite, onClick = onToggleFavorite)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForecastScreenPreview() {
    val sample = Forecast(
        place = Place("Bodrum", "Muğla", 37.03, 27.43),
        current = CurrentWeather(24, 55, 12.0, WeatherCondition.CLEAR, isDay = true),
        hourly = listOf(
            HourlyForecast("Şimdi", 24, WeatherCondition.CLEAR),
            HourlyForecast("15:00", 25, WeatherCondition.PARTLY_CLOUDY)
        ),
        daily = listOf(DailyForecast("2026-10-26", 26, 17, WeatherCondition.CLEAR, 5, 14.0))
    )
    CloudHeartTheme {
        ForecastScreen(
            placeName = "Muğla",
            state = LoadState.Success(sample),
            districts = LoadState.Success(
                listOf(DistrictWeather(District("Bodrum", "Muğla", 37.03, 27.43), 24, WeatherCondition.CLEAR, true))
            ),
            favoriteKeys = setOf("bodrum, mugla"),
            onBack = {},
            onRetry = {},
            onRetryDistricts = {},
            onToggleFavorite = {},
            onOpenPlace = {}
        )
    }
}
