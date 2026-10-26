package com.example.havadurumu.ui

import com.example.havadurumu.R
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.annotation.DrawableRes
import android.net.Uri
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.havadurumu.ui.city.CityRoute
import com.example.havadurumu.ui.forecast.ForecastRoute
import com.example.havadurumu.ui.people.PeopleRoute
import com.example.havadurumu.ui.plans.PlansRoute
import com.example.havadurumu.ui.theme.PinkDark
import com.example.havadurumu.ui.theme.PinkLight
import com.example.havadurumu.ui.theme.White

private object Routes {
    const val CITIES = "cities"
    const val PEOPLE = "people"
    const val PLANS = "plans"
    const val ARG_PLACE = "place"
    const val FORECAST = "forecast/{$ARG_PLACE}"

    fun forecast(placeName: String) = "forecast/${Uri.encode(placeName)}"
}

private enum class Tab(val route: String, @DrawableRes val icon: Int, val label: String) {
    CITIES(Routes.CITIES, R.drawable.ic_place, "Şehirler"),
    PEOPLE(Routes.PEOPLE, R.drawable.ic_heart, "Sevdiklerim"),
    PLANS(Routes.PLANS, R.drawable.ic_calendar, "Planlarım")
}

@Composable
fun CloudHeartApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = Tab.entries.any { it.route == currentRoute }

    val openPlace: (String) -> Unit = { name -> navController.navigate(Routes.forecast(name)) }

    Scaffold(

        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = White) {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = PinkDark,
                                selectedIconColor = PinkDark,
                                indicatorColor = PinkLight
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.CITIES,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.CITIES) { CityRoute(onOpenPlace = openPlace) }
            composable(Routes.PEOPLE) { PeopleRoute(onOpenPlace = openPlace) }
            composable(Routes.PLANS) { PlansRoute(onOpenPlace = openPlace) }
            composable(
                route = Routes.FORECAST,
                arguments = listOf(navArgument(Routes.ARG_PLACE) { type = NavType.StringType })
            ) { entry ->
                ForecastRoute(
                    placeName = entry.arguments?.getString(Routes.ARG_PLACE).orEmpty(),
                    onBack = { navController.popBackStack() },
                    onOpenPlace = openPlace
                )
            }
        }
    }
}
