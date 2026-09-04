package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Quran : Screen("quran")
    data object Search : Screen("search")
    data object Prayer : Screen("prayer")
    data object Settings : Screen("settings")
    data object Reader : Screen("reader")
    data object Tasbih : Screen("tasbih")
    data object Qibla : Screen("qibla")
    data object NamesOfAllah : Screen("names_of_allah")
    data object Dua : Screen("dua")
}
