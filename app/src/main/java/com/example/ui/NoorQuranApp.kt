package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.AppLanguage
import com.example.data.repository.ThemeMode
import com.example.ui.components.NoorBottomNav
import com.example.ui.components.NoorTopBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrayerScreen
import com.example.ui.screens.QuranListScreen
import com.example.ui.screens.QuranReaderScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.strings.AppStrings
import com.example.ui.theme.NoorQuranTheme
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.example.ui.viewmodel.QuranViewModel

@Composable
fun NoorQuranApp(
    viewModel: QuranViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val settings by viewModel.userSettings.collectAsState()
    val lang = settings.appLanguage
    val hijriDate by viewModel.hijriDate.collectAsState()

    val isDark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    NoorQuranTheme(darkTheme = isDark) {
        val showBottomBar = currentRoute != Screen.Reader.route
        val showTopBar = currentRoute != Screen.Reader.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showTopBar) {
                    val title = when (currentRoute) {
                        Screen.Home.route -> AppStrings.get("app_name", lang)
                        Screen.Quran.route -> AppStrings.get("quran", lang)
                        Screen.Search.route -> AppStrings.get("search", lang)
                        Screen.Prayer.route -> AppStrings.get("prayer_times", lang)
                        Screen.Settings.route -> AppStrings.get("settings", lang)
                        Screen.Tasbih.route -> AppStrings.get("tasbih", lang)
                        Screen.Qibla.route -> AppStrings.get("qibla", lang)
                        Screen.NamesOfAllah.route -> AppStrings.get("names_of_allah", lang)
                        Screen.Dua.route -> AppStrings.get("dua", lang)
                        else -> AppStrings.get("app_name", lang)
                    }

                    val subtitle = if (currentRoute == Screen.Home.route) {
                        when (lang) {
                            AppLanguage.BENGALI -> hijriDate.formatBn()
                            AppLanguage.ARABIC -> hijriDate.formatAr()
                            AppLanguage.ENGLISH -> hijriDate.formatEn()
                        }
                    } else null

                    NoorTopBar(
                        title = title,
                        subtitle = subtitle,
                        lang = lang,
                        showBackButton = currentRoute != Screen.Home.route &&
                                currentRoute != Screen.Quran.route &&
                                currentRoute != Screen.Search.route &&
                                currentRoute != Screen.Prayer.route &&
                                currentRoute != Screen.Settings.route,
                        onBackClick = {
                            if (currentRoute == Screen.Tasbih.route) {
                                com.example.ads.AdManager.showInterstitial(activity) {
                                    navController.popBackStack()
                                }
                            } else {
                                navController.popBackStack()
                            }
                        },
                        onSearchClick = if (currentRoute != Screen.Search.route) {
                            { navController.navigate(Screen.Search.route) }
                        } else null,
                        onLanguageClick = {
                            val nextLang = when (lang) {
                                AppLanguage.ENGLISH -> AppLanguage.BENGALI
                                AppLanguage.BENGALI -> AppLanguage.ARABIC
                                AppLanguage.ARABIC -> AppLanguage.ENGLISH
                            }
                            viewModel.setLanguage(nextLang)
                        }
                    )
                }
            },
            bottomBar = {
                if (showBottomBar) {
                    NoorBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        lang = lang
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Home.route) {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigate = { screen ->
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenSurah = { surahNumber, targetAyah ->
                                viewModel.openSurah(surahNumber, targetAyah)
                                navController.navigate(Screen.Reader.route)
                            }
                        )
                    }

                    composable(Screen.Quran.route) {
                        QuranListScreen(
                            viewModel = viewModel,
                            onOpenSurah = { surahNumber, targetAyah ->
                                viewModel.openSurah(surahNumber, targetAyah)
                                navController.navigate(Screen.Reader.route)
                            }
                        )
                    }

                    composable(Screen.Search.route) {
                        SearchScreen(
                            viewModel = viewModel,
                            onOpenAyah = { surahNumber, ayahNumber ->
                                viewModel.openSurah(surahNumber, ayahNumber)
                                navController.navigate(Screen.Reader.route)
                            }
                        )
                    }

                    composable(Screen.Prayer.route) {
                        PrayerScreen(viewModel = viewModel)
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(viewModel = viewModel)
                    }

                    composable(Screen.Tasbih.route) {
                        BackHandler {
                            com.example.ads.AdManager.showInterstitial(activity) {
                                navController.popBackStack()
                            }
                        }
                        com.example.ui.screens.TasbihScreen(viewModel = viewModel)
                    }

                    composable(Screen.Qibla.route) {
                        BackHandler { navController.popBackStack() }
                        com.example.ui.screens.QiblaScreen(viewModel = viewModel)
                    }

                    composable(Screen.NamesOfAllah.route) {
                        BackHandler { navController.popBackStack() }
                        com.example.ui.screens.NamesOfAllahScreen(viewModel = viewModel)
                    }

                    composable(Screen.Dua.route) {
                        BackHandler { navController.popBackStack() }
                        com.example.ui.screens.DuaScreen(viewModel = viewModel)
                    }

                    composable(Screen.Reader.route) {
                        BackHandler {
                            navController.popBackStack()
                        }
                        QuranReaderScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
