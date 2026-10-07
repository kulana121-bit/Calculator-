package com.example

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppNavTab
import com.example.ui.components.LiquidGlassNavBar
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.CalculatorScreen
import com.example.ui.screens.CurrencyScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThemeScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.UnitConverterScreen
import com.example.ui.theme.LiquidGlassBackground
import com.example.ui.theme.SmartCalculatorTheme
import com.example.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: CalculatorViewModel by viewModels()

    companion object {
        fun setAppLocale(context: Context, lang: String) {
            val langCode = when (lang) {
                "Spanish" -> "es"
                "French" -> "fr"
                "German" -> "de"
                "Japanese" -> "ja"
                "Hindi" -> "hi"
                else -> "en"
            }
            val locale = java.util.Locale(langCode)
            java.util.Locale.setDefault(locale)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                localeManager?.applicationLocales = LocaleList(locale)
            }

            val resources = context.resources
            val config = Configuration(resources.configuration)
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.example.util.SoundHelper.init(this)

        // Restore language setting on app startup
        val prefs = getSharedPreferences("smart_calculator_prefs", MODE_PRIVATE)
        val savedLang = prefs.getString("pref_language", "English") ?: "English"
        setAppLocale(this, savedLang)

        setContent {
            val state by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(Unit) {
                viewModel.toastEvent.collectLatest { message ->
                    snackbarHostState.showSnackbar(message)
                }
            }

            // Back button handling
            BackHandler(enabled = state.isSettingsOpen || state.isThemesOpen || state.isAboutOpen || state.currentTab != AppNavTab.CALCULATOR) {
                when {
                    state.isAboutOpen -> viewModel.closeAbout()
                    state.isThemesOpen -> viewModel.closeThemes()
                    state.isSettingsOpen -> viewModel.closeSettings()
                    state.currentTab != AppNavTab.CALCULATOR -> viewModel.selectTab(AppNavTab.CALCULATOR)
                }
            }

            SmartCalculatorTheme(themeMode = state.theme) {
                androidx.compose.runtime.CompositionLocalProvider(
                    com.example.ui.components.LocalVibrationEnabled provides state.hapticFeedbackEnabled,
                    com.example.ui.components.LocalSoundEnabled provides state.soundEnabled
                ) {
                    LiquidGlassBackground(theme = state.theme) {
                    val overlayScreen = when {
                        state.isAboutOpen -> "ABOUT"
                        state.isThemesOpen -> "THEMES"
                        state.isSettingsOpen -> "SETTINGS"
                        else -> "MAIN"
                    }

                    AnimatedContent(
                        targetState = overlayScreen,
                        transitionSpec = {
                            if (targetState == "MAIN") {
                                (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { -it / 4 } +
                                        fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { it } +
                                        fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
                            } else {
                                (slideInHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { it } +
                                        fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(280, easing = FastOutSlowInEasing)) { -it / 4 } +
                                        fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)))
                            }
                        },
                        label = "MainOverlayTransition"
                    ) { screen ->
                        when (screen) {
                            "ABOUT" -> {
                                AboutScreen(
                                    theme = state.theme,
                                    onBack = { viewModel.closeAbout() }
                                )
                            }
                            "THEMES" -> {
                                ThemeScreen(
                                    currentTheme = state.theme,
                                    viewModel = viewModel,
                                    onBack = { viewModel.closeThemes() }
                                )
                            }
                            "SETTINGS" -> {
                                SettingsScreen(
                                    state = state,
                                    viewModel = viewModel,
                                    onBack = { viewModel.closeSettings() }
                                )
                            }
                            else -> {
                                Scaffold(
                                    modifier = Modifier.fillMaxSize(),
                                    contentWindowInsets = WindowInsets.statusBars,
                                    containerColor = Color.Transparent,
                                    contentColor = state.theme.textPrimary,
                                    snackbarHost = {
                                        SnackbarHost(
                                            hostState = snackbarHostState,
                                            modifier = Modifier.padding(bottom = 80.dp)
                                        )
                                    },
                                    bottomBar = {
                                        LiquidGlassNavBar(
                                            selectedTab = state.currentTab,
                                            onTabSelected = { viewModel.selectTab(it) },
                                            theme = state.theme
                                        )
                                    }
                                ) { innerPadding ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(innerPadding)
                                    ) {
                                        AnimatedContent(
                                            targetState = state.currentTab,
                                            transitionSpec = {
                                                val forward = targetState.ordinal > initialState.ordinal
                                                if (forward) {
                                                    (slideInHorizontally(
                                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                                    ) { width -> (width * 0.25f).toInt() } + fadeIn(
                                                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                                                    )) togetherWith (slideOutHorizontally(
                                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                                    ) { width -> (-width * 0.25f).toInt() } + fadeOut(
                                                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                                                    ))
                                                } else {
                                                    (slideInHorizontally(
                                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                                    ) { width -> (-width * 0.25f).toInt() } + fadeIn(
                                                        animationSpec = tween(220, easing = FastOutSlowInEasing)
                                                    )) togetherWith (slideOutHorizontally(
                                                        animationSpec = tween(280, easing = FastOutSlowInEasing)
                                                    ) { width -> (width * 0.25f).toInt() } + fadeOut(
                                                        animationSpec = tween(180, easing = FastOutSlowInEasing)
                                                    ))
                                                }
                                            },
                                            label = "TabTransition"
                                        ) { tab ->
                                            when (tab) {
                                                AppNavTab.CALCULATOR -> CalculatorScreen(
                                                    state = state,
                                                    viewModel = viewModel
                                                )
                                                AppNavTab.CURRENCY -> CurrencyScreen(
                                                    state = state,
                                                    viewModel = viewModel
                                                )
                                                AppNavTab.UNITS -> UnitConverterScreen(
                                                    state = state,
                                                    viewModel = viewModel
                                                )
                                                AppNavTab.TOOLS -> ToolsScreen(
                                                    state = state,
                                                    viewModel = viewModel
                                                )
                                                AppNavTab.HISTORY -> HistoryScreen(
                                                    viewModel = viewModel,
                                                    theme = state.theme
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }

    // Hardware Keyboard Support
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyDown(keyCode, event)

        if (viewModel.uiState.value.currentTab == AppNavTab.CALCULATOR) {
            when (keyCode) {
                KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> { viewModel.onInput("0"); return true }
                KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> { viewModel.onInput("1"); return true }
                KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> { viewModel.onInput("2"); return true }
                KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> { viewModel.onInput("3"); return true }
                KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> { viewModel.onInput("4"); return true }
                KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> {
                    if (event.isShiftPressed) viewModel.onInput("%") else viewModel.onInput("5")
                    return true
                }
                KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> {
                    if (event.isShiftPressed) viewModel.onInput("^") else viewModel.onInput("6")
                    return true
                }
                KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> { viewModel.onInput("7"); return true }
                KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> {
                    if (event.isShiftPressed) viewModel.onInput("×") else viewModel.onInput("8")
                    return true
                }
                KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> {
                    if (event.isShiftPressed) viewModel.onInput("(") else viewModel.onInput("9")
                    return true
                }
                KeyEvent.KEYCODE_NUMPAD_DOT, KeyEvent.KEYCODE_PERIOD -> { viewModel.onInput("."); return true }
                KeyEvent.KEYCODE_PLUS, KeyEvent.KEYCODE_NUMPAD_ADD -> { viewModel.onInput("+"); return true }
                KeyEvent.KEYCODE_MINUS, KeyEvent.KEYCODE_NUMPAD_SUBTRACT -> { viewModel.onInput("-"); return true }
                KeyEvent.KEYCODE_STAR, KeyEvent.KEYCODE_NUMPAD_MULTIPLY -> { viewModel.onInput("×"); return true }
                KeyEvent.KEYCODE_SLASH, KeyEvent.KEYCODE_NUMPAD_DIVIDE -> { viewModel.onInput("÷"); return true }
                KeyEvent.KEYCODE_EQUALS, KeyEvent.KEYCODE_NUMPAD_EQUALS, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                    viewModel.onEquals()
                    return true
                }
                KeyEvent.KEYCODE_DEL -> { viewModel.onBackspace(); return true }
                KeyEvent.KEYCODE_ESCAPE, KeyEvent.KEYCODE_C -> {
                    if (keyCode == KeyEvent.KEYCODE_ESCAPE || (keyCode == KeyEvent.KEYCODE_C && !event.isCtrlPressed)) {
                        viewModel.onClear()
                        return true
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}
