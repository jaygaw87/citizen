package com.example.ui.navigation

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.data.local.DatabaseProvider
import com.example.data.local.PreferencesRepository
import com.example.data.model.HomeUiState
import com.example.data.model.PracticeType
import com.example.data.model.ProgressUiState
import com.example.data.model.SettingsUiState
import com.example.data.repository.QuestionRepository
import com.example.ui.about.AboutScreen
import com.example.ui.about.DisclaimerScreen
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.flashcards.FlashcardFilter
import com.example.ui.flashcards.FlashcardsScreen
import com.example.ui.flashcards.FlashcardsViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.placeholder.PlaceholderScreen
import com.example.ui.practice.PracticeHistoryScreen
import com.example.ui.practice.PracticeHistoryViewModel
import com.example.ui.practice.PracticeHomeViewModel
import com.example.ui.practice.PracticeQuestionScreen
import com.example.ui.practice.PracticeResultScreen
import com.example.ui.practice.PracticeResultViewModel
import com.example.ui.practice.PracticeScreen
import com.example.ui.practice.PracticeSessionViewModel
import com.example.ui.practice.ReviewAnswersScreen
import com.example.ui.progress.CategoryProgressScreen
import com.example.ui.progress.CategoryProgressViewModel
import com.example.ui.progress.ProgressScreen
import com.example.ui.progress.ProgressViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.speaking.SpeakingPracticeScreen
import com.example.ui.speaking.SpeakingPracticeViewModel
import com.example.ui.study.QuestionDetailScreen
import com.example.ui.study.QuestionDetailViewModel
import com.example.ui.study.StudyFilter
import com.example.ui.study.StudyScreen
import com.example.ui.study.StudyViewModel
import com.example.ui.weakareas.WeakAreasScreen
import com.example.ui.weakareas.WeakAreasViewModel
import com.example.ui.study.TopicsScreen
import com.example.ui.senior6520.Senior6520Screen
import com.example.ui.senior6520.Senior6520ViewModel
import com.example.ui.vocabulary.VocabularyScreen
import com.example.ui.vocabulary.VocabularyViewModel
import com.example.ui.vocabulary.VocabularyDetailScreen
import com.example.ui.vocabulary.VocabularyDetailViewModel
import com.example.ui.vocabulary.VocabularyFlashcardsScreen
import com.example.ui.vocabulary.VocabularyFlashcardsViewModel
import com.example.ui.vocabulary.VocabularyQuickReviewScreen
import com.example.ui.vocabulary.VocabularyQuickReviewViewModel
import com.example.ui.quickreview.QuickReviewScreen
import com.example.ui.quickreview.QuickReviewViewModel
import com.example.ui.quickreview.QuickReviewSection

@Composable
fun AppNavGraph(
    navController: NavHostController,
    preferencesRepository: PreferencesRepository,
    settingsUiState: SettingsUiState,
    onboardingCompleted: Boolean,
    modifier: Modifier = Modifier,
    questionRepository: QuestionRepository = DatabaseProvider.getQuestionRepository(LocalContext.current)
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Top-level tab routes that show bottom navigation
    val topLevelRoutes = listOf(
        NavRoutes.Home.route,
        NavRoutes.Study.route,
        NavRoutes.Practice.route,
        NavRoutes.Progress.route,
        NavRoutes.Settings.route
    )

    val showBottomBar = currentRoute in topLevelRoutes || (currentRoute != null && currentRoute.startsWith("study"))

    fun navigateToTopLevelTab(targetRoute: String) {
        val resolvedRoute = if (targetRoute == NavRoutes.Study.route || targetRoute.startsWith("study")) {
            NavRoutes.Study.createRoute("ALL")
        } else {
            targetRoute
        }

        val currentIsStudy = currentRoute == NavRoutes.Study.route || currentRoute?.startsWith("study") == true
        val targetIsStudy = resolvedRoute.startsWith("study")

        if (currentRoute == resolvedRoute || (currentIsStudy && targetIsStudy)) {
            return
        }

        if (resolvedRoute == NavRoutes.Home.route) {
            val popped = navController.popBackStack(NavRoutes.Home.route, inclusive = false)
            if (!popped) {
                navController.navigate(NavRoutes.Home.route) {
                    launchSingleTop = true
                }
            }
        } else {
            navController.navigate(resolvedRoute) {
                popUpTo(NavRoutes.Home.route) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        navigateToTopLevelTab(route)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (onboardingCompleted) NavRoutes.Home.route else NavRoutes.Onboarding.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Onboarding
            composable(NavRoutes.Onboarding.route) {
                OnboardingScreen(
                    onFinishOnboarding = {
                        preferencesRepository.setOnboardingCompleted(true)
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // 2. Home Tab
            composable(NavRoutes.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.provideFactory(questionRepository, preferencesRepository)
                )
                val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

                HomeScreen(
                    uiState = homeUiState,
                    onNavigateToSettings = { navigateToTopLevelTab(NavRoutes.Settings.route) },
                    onNavigateToQuestions = {
                        if (homeUiState.continueQuestionId != null) {
                            navController.navigate(NavRoutes.QuestionDetail.createRoute(homeUiState.continueQuestionId!!))
                        } else {
                            navController.navigate(NavRoutes.Study.createRoute("ALL"))
                        }
                    },
                    onNavigateToPracticeTest = { type ->
                        navController.navigate(NavRoutes.PracticeTest.createRoute(type.uppercase()))
                    },
                    onNavigateToFlashcards = { navController.navigate(NavRoutes.Flashcards.createRoute("ALL")) },
                    onNavigateToMockInterview = { navController.navigate(NavRoutes.SpeakingPractice.createRoute()) },
                    onNavigateToSpeakingPractice = { navController.navigate(NavRoutes.SpeakingPractice.createRoute()) },
                    onNavigateToAudioPractice = { navController.navigate(NavRoutes.SpeakingPractice.createRoute()) },
                    onNavigateToFavorites = { navController.navigate(NavRoutes.Study.createRoute("FAVORITES")) },
                    onNavigateToProgress = { navigateToTopLevelTab(NavRoutes.Progress.route) },
                    onNavigateToMistakes = { navController.navigate(NavRoutes.QuickReview.route) },
                    onNavigateToQuickReview = { navController.navigate(NavRoutes.QuickReview.route) },
                    onNavigateToSpecialConsideration = { navController.navigate(NavRoutes.SpecialConsideration65_20.route) },
                    onNavigateToVocabulary = { navController.navigate(NavRoutes.Vocabulary.route) },
                    onNavigateToRecommended = { actionType, categoryName ->
                        when (actionType) {
                            "UNSTUDIED" -> navController.navigate(NavRoutes.Study.createRoute("UNSTUDIED"))
                            "MISTAKES" -> navController.navigate(NavRoutes.Study.createRoute("NEEDS_PRACTICE"))
                            "CATEGORY" -> {
                                if (categoryName != null) {
                                    navController.navigate(NavRoutes.PracticeTest.createRoute("TOPIC", categoryName))
                                } else {
                                    navController.navigate(NavRoutes.Practice.route)
                                }
                            }
                            else -> navController.navigate(NavRoutes.PracticeTest.createRoute("FULL"))
                        }
                    },
                    onNavigateToContinue = {
                        if (homeUiState.continueQuestionId != null) {
                            navController.navigate(NavRoutes.QuestionDetail.createRoute(homeUiState.continueQuestionId!!))
                        } else {
                            navController.navigate(NavRoutes.PracticeTest.createRoute("FULL"))
                        }
                    }
                )
            }

            // 3. Study Tab
            composable(
                route = NavRoutes.Study.route,
                arguments = listOf(
                    navArgument("filter") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                    }
                )
            ) { backStackEntry ->
                val filterParam = backStackEntry.arguments?.getString("filter") ?: "ALL"
                val initialFilter = StudyFilter.fromName(filterParam)
                val studyViewModel: StudyViewModel = viewModel(
                    factory = StudyViewModel.provideFactory(questionRepository, initialFilter)
                )
                LaunchedEffect(filterParam) {
                    studyViewModel.onFilterSelected(StudyFilter.fromName(filterParam))
                }
                val studyUiState by studyViewModel.uiState.collectAsStateWithLifecycle()
                StudyScreen(
                    uiState = studyUiState,
                    onFilterSelected = studyViewModel::onFilterSelected,
                    onSearchQueryChanged = studyViewModel::onSearchQueryChanged,
                    onToggleFavorite = studyViewModel::toggleFavorite,
                    onQuestionClick = { qId, currentFilter, query ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId, currentFilter.name, query))
                    },
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) }
                )
            }

            // 4. Practice Tab
            composable(NavRoutes.Practice.route) {
                val practiceHomeViewModel: PracticeHomeViewModel = viewModel(
                    factory = PracticeHomeViewModel.provideFactory(questionRepository)
                )
                val practiceHomeUiState by practiceHomeViewModel.uiState.collectAsStateWithLifecycle()

                PracticeScreen(
                    uiState = practiceHomeUiState,
                    onStartPractice = { type, topic, count ->
                        navController.navigate(NavRoutes.PracticeTest.createRoute(type, topic, count))
                    },
                    onNavigateToResult = { resultId ->
                        navController.navigate(NavRoutes.TestResult.createRoute(resultId))
                    },
                    onNavigateToHistory = {
                        navController.navigate(NavRoutes.PracticeHistory.route)
                    },
                    onNavigateToFlashcards = {
                        navController.navigate(NavRoutes.Flashcards.createRoute("ALL"))
                    },
                    onNavigateToMockInterview = {
                        navController.navigate(NavRoutes.SpeakingPractice.createRoute())
                    },
                    onNavigateToSpeaking = {
                        navController.navigate(NavRoutes.SpeakingPractice.createRoute())
                    },
                    onNavigateToSettings = {
                        navController.navigate(NavRoutes.Settings.route)
                    }
                )
            }

            // 5. Progress Tab
            composable(NavRoutes.Progress.route) {
                val progressViewModel: ProgressViewModel = viewModel(
                    factory = ProgressViewModel.provideFactory(questionRepository, preferencesRepository)
                )
                val progressUiState by progressViewModel.uiState.collectAsStateWithLifecycle()

                ProgressScreen(
                    uiState = progressUiState,
                    onNavigateToCategoryProgress = { cat ->
                        navController.navigate(NavRoutes.CategoryProgress.createRoute(cat))
                    },
                    onNavigateToWeakAreas = {
                        navController.navigate(NavRoutes.WeakAreas.route)
                    },
                    onNavigateToPracticeWeakAreas = {
                        navController.navigate(NavRoutes.PracticeTest.createRoute("WEAK_AREAS"))
                    },
                    onNavigateTo6520Practice = {
                        navController.navigate(NavRoutes.PracticeTest.createRoute("SENIOR_65_20"))
                    },
                    onNavigateToFavoritesPractice = {
                        navController.navigate(NavRoutes.PracticeTest.createRoute("FAVORITES"))
                    },
                    onNavigateToFlashcards = {
                        navController.navigate(NavRoutes.Flashcards.createRoute("ALL"))
                    },
                    onNavigateToSpeakingPractice = {
                        navController.navigate(NavRoutes.SpeakingPractice.createRoute())
                    },
                    onNavigateToPracticeHistory = {
                        navController.navigate(NavRoutes.PracticeHistory.route)
                    },
                    onNavigateToSettings = {
                        navigateToTopLevelTab(NavRoutes.Settings.route)
                    }
                )
            }

            // 6. Settings Tab
            composable(NavRoutes.Settings.route) {
                val coroutineScope = rememberCoroutineScope()
                SettingsScreen(
                    uiState = settingsUiState,
                    onThemeChange = { mode -> preferencesRepository.setThemeMode(mode) },
                    onQuestionAudioChange = { enabled -> preferencesRepository.setQuestionAudio(enabled) },
                    onAnswerAudioChange = { enabled -> preferencesRepository.setAnswerAudio(enabled) },
                    onAudioAutoplayChange = { enabled -> preferencesRepository.setAudioAutoplay(enabled) },
                    onPlaybackSpeedChange = { speed -> preferencesRepository.setPlaybackSpeed(speed) },
                    onDailyReminderChange = { enabled -> preferencesRepository.setDailyReminder(enabled) },
                    onDailyStudyGoalChange = { goal -> preferencesRepository.setDailyStudyGoal(goal) },
                    onResetProgress = {
                        coroutineScope.launch {
                            questionRepository.resetLearningProgress()
                        }
                    },
                    onResetOnboarding = {
                        preferencesRepository.resetOnboarding()
                    },
                    onNavigateToAbout = { navController.navigate(NavRoutes.About.route) },
                    onNavigateToDisclaimer = { navController.navigate(NavRoutes.Disclaimer.route) },
                    onBackClick = if (currentRoute != NavRoutes.Settings.route) {
                        { navController.navigateUp() }
                    } else null
                )
            }

            // Study Sub-routes
            composable(NavRoutes.Questions.route) {
                val studyViewModel: StudyViewModel = viewModel(
                    factory = StudyViewModel.provideFactory(questionRepository, StudyFilter.ALL)
                )
                val studyUiState by studyViewModel.uiState.collectAsStateWithLifecycle()
                StudyScreen(
                    uiState = studyUiState,
                    onFilterSelected = studyViewModel::onFilterSelected,
                    onSearchQueryChanged = studyViewModel::onSearchQueryChanged,
                    onToggleFavorite = studyViewModel::toggleFavorite,
                    onQuestionClick = { qId, currentFilter, query ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId, currentFilter.name, query))
                    },
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) },
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(
                route = NavRoutes.QuestionDetail.route,
                arguments = listOf(
                    navArgument("questionId") { type = NavType.StringType },
                    navArgument("filter") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                    },
                    navArgument("search") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val questionId = backStackEntry.arguments?.getString("questionId") ?: "Q001"
                val filterParam = backStackEntry.arguments?.getString("filter") ?: "ALL"
                val searchParam = backStackEntry.arguments?.getString("search") ?: ""
                val decodedSearch = try {
                    java.net.URLDecoder.decode(searchParam, "UTF-8")
                } catch (_: Exception) {
                    searchParam
                }
                val currentFilter = StudyFilter.fromName(filterParam)

                val detailViewModel: QuestionDetailViewModel = viewModel(
                    key = "detail_${questionId}_${filterParam}_${decodedSearch}",
                    factory = QuestionDetailViewModel.provideFactory(
                        questionRepository,
                        questionId,
                        currentFilter,
                        decodedSearch
                    )
                )
                val uiState by detailViewModel.uiState.collectAsStateWithLifecycle()
                QuestionDetailScreen(
                    uiState = uiState,
                    onBackClick = { navController.navigateUp() },
                    onPreviousClick = detailViewModel::goToPrevious,
                    onNextClick = detailViewModel::goToNext,
                    onToggleFavorite = detailViewModel::toggleFavorite,
                    onMarkStudied = detailViewModel::markStudied
                )
            }

            composable(NavRoutes.Topics.route) {
                val studyViewModel: StudyViewModel = viewModel(
                    factory = StudyViewModel.provideFactory(questionRepository)
                )
                val studyUiState by studyViewModel.uiState.collectAsStateWithLifecycle()
                TopicsScreen(
                    categoryProgressList = studyUiState.categoryProgressList,
                    onTopicSelected = { filter ->
                        navController.navigate(NavRoutes.Study.createRoute(filter.name))
                    },
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(NavRoutes.Favorites.route) {
                val studyViewModel: StudyViewModel = viewModel(
                    factory = StudyViewModel.provideFactory(questionRepository, StudyFilter.FAVORITES)
                )
                LaunchedEffect(Unit) {
                    studyViewModel.onFilterSelected(StudyFilter.FAVORITES)
                }
                val studyUiState by studyViewModel.uiState.collectAsStateWithLifecycle()
                StudyScreen(
                    uiState = studyUiState,
                    onFilterSelected = studyViewModel::onFilterSelected,
                    onSearchQueryChanged = studyViewModel::onSearchQueryChanged,
                    onToggleFavorite = studyViewModel::toggleFavorite,
                    onQuestionClick = { qId, currentFilter, query ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId, currentFilter.name, query))
                    },
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) },
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(NavRoutes.Mistakes.route) {
                val studyViewModel: StudyViewModel = viewModel(
                    factory = StudyViewModel.provideFactory(questionRepository, StudyFilter.NEEDS_PRACTICE)
                )
                LaunchedEffect(Unit) {
                    studyViewModel.onFilterSelected(StudyFilter.NEEDS_PRACTICE)
                }
                val studyUiState by studyViewModel.uiState.collectAsStateWithLifecycle()
                StudyScreen(
                    uiState = studyUiState,
                    onFilterSelected = studyViewModel::onFilterSelected,
                    onSearchQueryChanged = studyViewModel::onSearchQueryChanged,
                    onToggleFavorite = studyViewModel::toggleFavorite,
                    onQuestionClick = { qId, currentFilter, query ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId, currentFilter.name, query))
                    },
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) },
                    onBackClick = { navController.navigateUp() }
                )
            }

            // Phase 4: Flashcards
            composable(
                route = NavRoutes.Flashcards.route,
                arguments = listOf(
                    navArgument("filter") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                    }
                )
            ) { backStackEntry ->
                val context = LocalContext.current
                val filterParam = backStackEntry.arguments?.getString("filter") ?: "ALL"
                val initialFilter = FlashcardFilter.fromString(filterParam)
                val flashcardsViewModel: FlashcardsViewModel = viewModel(
                    key = "flashcards_${filterParam}",
                    factory = FlashcardsViewModel.provideFactory(
                        context.applicationContext as Application,
                        questionRepository,
                        initialFilter
                    )
                )
                val flashcardsUiState by flashcardsViewModel.uiState.collectAsStateWithLifecycle()
                FlashcardsScreen(
                    uiState = flashcardsUiState,
                    onFilterSelected = flashcardsViewModel::setFilter,
                    onFlipCard = flashcardsViewModel::flipCard,
                    onNextCard = flashcardsViewModel::nextCard,
                    onPreviousCard = flashcardsViewModel::previousCard,
                    onToggleShuffle = flashcardsViewModel::toggleShuffle,
                    onToggleFavorite = flashcardsViewModel::toggleFavorite,
                    onToggleStudied = flashcardsViewModel::toggleStudied,
                    onHearQuestion = flashcardsViewModel::hearQuestion,
                    onHearAnswer = flashcardsViewModel::hearAnswer,
                    onStopAudio = flashcardsViewModel::stopAudio,
                    onBackClick = { navController.navigateUp() }
                )
            }

            // Phase 4: Practice Test Session
            composable(
                route = NavRoutes.PracticeTest.route,
                arguments = listOf(
                    navArgument("testType") {
                        type = NavType.StringType
                        defaultValue = "QUICK"
                    },
                    navArgument("topic") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("count") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val testTypeParam = backStackEntry.arguments?.getString("testType") ?: "QUICK"
                val topicParam = backStackEntry.arguments?.getString("topic") ?: ""
                val countParam = backStackEntry.arguments?.getInt("count") ?: 0
                val decodedTopic = try {
                    java.net.URLDecoder.decode(topicParam, "UTF-8")
                } catch (_: Exception) {
                    topicParam
                }
                val practiceType = PracticeType.fromString(testTypeParam)
                val sessionCount = if (countParam > 0) countParam else practiceType.defaultCount

                val sessionViewModel: PracticeSessionViewModel = viewModel(
                    key = "session_${testTypeParam}_${decodedTopic}_${sessionCount}",
                    factory = PracticeSessionViewModel.provideFactory(
                        repository = questionRepository,
                        practiceType = practiceType,
                        topic = decodedTopic.ifBlank { null },
                        requestedCount = sessionCount
                    )
                )
                val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

                PracticeQuestionScreen(
                    uiState = sessionUiState,
                    onRevealAnswer = sessionViewModel::revealAnswer,
                    onEvaluateAnswer = sessionViewModel::evaluateAnswer,
                    onNextQuestion = sessionViewModel::nextQuestion,
                    onPreviousQuestion = sessionViewModel::previousQuestion,
                    onToggleFavorite = sessionViewModel::toggleFavoriteCurrent,
                    onRequestLeave = sessionViewModel::showLeaveConfirmation,
                    onConfirmLeave = { navController.navigateUp() },
                    onSessionCompleted = { resultId ->
                        navController.navigate(NavRoutes.TestResult.createRoute(resultId)) {
                            popUpTo(NavRoutes.Practice.route)
                        }
                    }
                )
            }

            // Phase 4: Test Result Screen
            composable(
                route = NavRoutes.TestResult.route,
                arguments = listOf(
                    navArgument("resultId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val resultId = backStackEntry.arguments?.getString("resultId") ?: ""
                val resultViewModel: PracticeResultViewModel = viewModel(
                    key = "result_$resultId",
                    factory = PracticeResultViewModel.provideFactory(questionRepository, resultId)
                )
                val resultUiState by resultViewModel.uiState.collectAsStateWithLifecycle()
                PracticeResultScreen(
                    uiState = resultUiState,
                    onReviewAnswers = { resId ->
                        navController.navigate(NavRoutes.ReviewAnswers.createRoute(resId))
                    },
                    onPracticeAgain = { typeStr ->
                        navController.navigate(NavRoutes.PracticeTest.createRoute(typeStr)) {
                            popUpTo(NavRoutes.Practice.route)
                        }
                    },
                    onBackToPractice = {
                        navController.navigate(NavRoutes.Practice.route) {
                            popUpTo(NavRoutes.Practice.route) { inclusive = true }
                        }
                    },
                    onReviewMistakes = { resId ->
                        navController.navigate(NavRoutes.ReviewAnswers.createRoute(resId))
                    }
                )
            }

            // Phase 4: Review Answers Screen
            composable(
                route = NavRoutes.ReviewAnswers.route,
                arguments = listOf(
                    navArgument("resultId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val resultId = backStackEntry.arguments?.getString("resultId") ?: ""
                val resultViewModel: PracticeResultViewModel = viewModel(
                    key = "review_$resultId",
                    factory = PracticeResultViewModel.provideFactory(questionRepository, resultId)
                )
                val reviewUiState by resultViewModel.uiState.collectAsStateWithLifecycle()
                ReviewAnswersScreen(
                    uiState = reviewUiState,
                    onBackClick = { navController.navigateUp() }
                )
            }

            // Phase 4: Practice History Screen
            composable(NavRoutes.PracticeHistory.route) {
                val historyViewModel: PracticeHistoryViewModel = viewModel(
                    factory = PracticeHistoryViewModel.provideFactory(questionRepository)
                )
                val historyUiState by historyViewModel.uiState.collectAsStateWithLifecycle()
                PracticeHistoryScreen(
                    uiState = historyUiState,
                    onResultClick = { resId ->
                        navController.navigate(NavRoutes.TestResult.createRoute(resId))
                    },
                    onClearHistory = historyViewModel::clearHistory,
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(NavRoutes.RecentTests.route) {
                val historyViewModel: PracticeHistoryViewModel = viewModel(
                    factory = PracticeHistoryViewModel.provideFactory(questionRepository)
                )
                val historyUiState by historyViewModel.uiState.collectAsStateWithLifecycle()
                PracticeHistoryScreen(
                    uiState = historyUiState,
                    onResultClick = { resId ->
                        navController.navigate(NavRoutes.TestResult.createRoute(resId))
                    },
                    onClearHistory = historyViewModel::clearHistory,
                    onBackClick = { navController.navigateUp() }
                )
            }

            // Phase 5: Speaking Practice (Oral Civics Interview Simulation)
            composable(
                route = NavRoutes.SpeakingPractice.route,
                arguments = listOf(
                    navArgument("topic") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("count") {
                        type = NavType.IntType
                        defaultValue = 20
                    }
                )
            ) { backStackEntry ->
                val topicParam = backStackEntry.arguments?.getString("topic") ?: ""
                val countParam = backStackEntry.arguments?.getInt("count") ?: 20
                val decodedTopic = try {
                    java.net.URLDecoder.decode(topicParam, "UTF-8")
                } catch (_: Exception) {
                    topicParam
                }

                val context = LocalContext.current
                val application = context.applicationContext as android.app.Application
                val speakingViewModel: SpeakingPracticeViewModel = viewModel(
                    key = "speaking_${decodedTopic}_$countParam",
                    factory = SpeakingPracticeViewModel.provideFactory(
                        application = application,
                        questionRepository = questionRepository,
                        preferencesRepository = preferencesRepository,
                        requestedCount = countParam,
                        topicFilter = decodedTopic.ifBlank { null }
                    )
                )
                val speakingUiState by speakingViewModel.uiState.collectAsStateWithLifecycle()

                SpeakingPracticeScreen(
                    uiState = speakingUiState,
                    onStartListening = speakingViewModel::startListening,
                    onStopListening = speakingViewModel::stopListening,
                    onHearQuestion = speakingViewModel::hearQuestion,
                    onHearAnswer = speakingViewModel::hearAnswer,
                    onRevealAnswer = speakingViewModel::revealAnswer,
                    onManualOverride = speakingViewModel::manualOverride,
                    onTryAgain = speakingViewModel::tryAgain,
                    onNextQuestion = speakingViewModel::nextQuestion,
                    onToggleFavorite = speakingViewModel::toggleFavoriteCurrent,
                    onShowLeaveConfirmation = speakingViewModel::showLeaveConfirmation,
                    onNavigateBack = { navController.navigateUp() },
                    onViewResults = { resId ->
                        navController.navigate(NavRoutes.TestResult.createRoute(resId)) {
                            popUpTo(NavRoutes.Practice.route)
                        }
                    }
                )
            }

            composable(NavRoutes.MockInterview.route) {
                val context = LocalContext.current
                val application = context.applicationContext as android.app.Application
                val speakingViewModel: SpeakingPracticeViewModel = viewModel(
                    key = "mock_interview_speaking",
                    factory = SpeakingPracticeViewModel.provideFactory(
                        application = application,
                        questionRepository = questionRepository,
                        preferencesRepository = preferencesRepository,
                        requestedCount = 20,
                        topicFilter = null
                    )
                )
                val speakingUiState by speakingViewModel.uiState.collectAsStateWithLifecycle()

                SpeakingPracticeScreen(
                    uiState = speakingUiState,
                    onStartListening = speakingViewModel::startListening,
                    onStopListening = speakingViewModel::stopListening,
                    onHearQuestion = speakingViewModel::hearQuestion,
                    onHearAnswer = speakingViewModel::hearAnswer,
                    onRevealAnswer = speakingViewModel::revealAnswer,
                    onManualOverride = speakingViewModel::manualOverride,
                    onTryAgain = speakingViewModel::tryAgain,
                    onNextQuestion = speakingViewModel::nextQuestion,
                    onToggleFavorite = speakingViewModel::toggleFavoriteCurrent,
                    onShowLeaveConfirmation = speakingViewModel::showLeaveConfirmation,
                    onNavigateBack = { navController.navigateUp() },
                    onViewResults = { resId ->
                        navController.navigate(NavRoutes.TestResult.createRoute(resId)) {
                            popUpTo(NavRoutes.Home.route)
                        }
                    }
                )
            }

            composable(NavRoutes.AudioPractice.route) {
                val context = LocalContext.current
                val application = context.applicationContext as android.app.Application
                val speakingViewModel: SpeakingPracticeViewModel = viewModel(
                    key = "audio_practice_speaking",
                    factory = SpeakingPracticeViewModel.provideFactory(
                        application = application,
                        questionRepository = questionRepository,
                        preferencesRepository = preferencesRepository,
                        requestedCount = 20,
                        topicFilter = null
                    )
                )
                val speakingUiState by speakingViewModel.uiState.collectAsStateWithLifecycle()

                SpeakingPracticeScreen(
                    uiState = speakingUiState,
                    onStartListening = speakingViewModel::startListening,
                    onStopListening = speakingViewModel::stopListening,
                    onHearQuestion = speakingViewModel::hearQuestion,
                    onHearAnswer = speakingViewModel::hearAnswer,
                    onRevealAnswer = speakingViewModel::revealAnswer,
                    onManualOverride = speakingViewModel::manualOverride,
                    onTryAgain = speakingViewModel::tryAgain,
                    onNextQuestion = speakingViewModel::nextQuestion,
                    onToggleFavorite = speakingViewModel::toggleFavoriteCurrent,
                    onShowLeaveConfirmation = speakingViewModel::showLeaveConfirmation,
                    onNavigateBack = { navController.navigateUp() },
                    onViewResults = { resId ->
                        navController.navigate(NavRoutes.TestResult.createRoute(resId)) {
                            popUpTo(NavRoutes.Home.route)
                        }
                    }
                )
            }

            composable(NavRoutes.Vocabulary.route) {
                val vocabViewModel: VocabularyViewModel = viewModel(
                    factory = VocabularyViewModel.provideFactory(questionRepository)
                )
                val vocabUiState by vocabViewModel.uiState.collectAsStateWithLifecycle()
                VocabularyScreen(
                    uiState = vocabUiState,
                    onBackClick = { navController.navigateUp() },
                    onFilterSelected = vocabViewModel::setFilter,
                    onCategorySelected = vocabViewModel::setCategory,
                    onSearchQueryChanged = vocabViewModel::setSearchQuery,
                    onToggleFavorite = vocabViewModel::toggleFavorite,
                    onVocabularyClick = { vocabId ->
                        navController.navigate(NavRoutes.VocabularyDetail.createRoute(vocabId))
                    },
                    onQuickReviewClick = { filter ->
                        navController.navigate(NavRoutes.VocabularyQuickReview.createRoute(filter))
                    },
                    onFlashcardsClick = { category ->
                        navController.navigate(NavRoutes.VocabularyFlashcards.createRoute(category))
                    }
                )
            }

            composable(
                route = NavRoutes.VocabularyQuickReview.route,
                arguments = listOf(
                    navArgument("filter") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                    }
                )
            ) { backStackEntry ->
                val context = LocalContext.current
                val filter = backStackEntry.arguments?.getString("filter") ?: "ALL"
                val quickReviewViewModel: VocabularyQuickReviewViewModel = viewModel(
                    factory = VocabularyQuickReviewViewModel.provideFactory(
                        application = context.applicationContext as Application,
                        questionRepository = questionRepository,
                        filterMode = filter
                    )
                )
                val quickReviewUiState by quickReviewViewModel.uiState.collectAsStateWithLifecycle()
                VocabularyQuickReviewScreen(
                    uiState = quickReviewUiState,
                    onBackClick = { navController.navigateUp() },
                    onNextCard = quickReviewViewModel::nextCard,
                    onPreviousCard = quickReviewViewModel::previousCard,
                    onHearWord = quickReviewViewModel::hearWord,
                    onHearDefinition = quickReviewViewModel::hearDefinition,
                    onStopAudio = quickReviewViewModel::stopAudio,
                    onToggleFavorite = quickReviewViewModel::toggleFavorite,
                    onMarkLearned = quickReviewViewModel::markLearned,
                    onMarkNeedsReview = quickReviewViewModel::markNeedsReview,
                    onRestartSession = quickReviewViewModel::restartSession
                )
            }

            composable(
                route = NavRoutes.VocabularyFlashcards.route,
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                    },
                    navArgument("filter") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                    }
                )
            ) { backStackEntry ->
                val context = LocalContext.current
                val category = backStackEntry.arguments?.getString("category") ?: "ALL"
                val filter = backStackEntry.arguments?.getString("filter") ?: "ALL"
                val flashcardsViewModel: VocabularyFlashcardsViewModel = viewModel(
                    factory = VocabularyFlashcardsViewModel.provideFactory(
                        application = context.applicationContext as Application,
                        questionRepository = questionRepository,
                        category = category,
                        filter = filter
                    )
                )
                val flashcardsUiState by flashcardsViewModel.uiState.collectAsStateWithLifecycle()
                VocabularyFlashcardsScreen(
                    uiState = flashcardsUiState,
                    onBackClick = { navController.navigateUp() },
                    onFlipCard = flashcardsViewModel::flipCard,
                    onNextCard = flashcardsViewModel::nextCard,
                    onPreviousCard = flashcardsViewModel::previousCard,
                    onToggleShuffle = flashcardsViewModel::toggleShuffle,
                    onToggleFavorite = flashcardsViewModel::toggleFavorite,
                    onMarkLearned = flashcardsViewModel::markLearned,
                    onMarkNeedsReview = flashcardsViewModel::markNeedsReview,
                    onCategorySelected = flashcardsViewModel::setCategory,
                    onFilterSelected = flashcardsViewModel::setFilter,
                    onHearWord = flashcardsViewModel::hearWord,
                    onHearDefinition = flashcardsViewModel::hearDefinition,
                    onStopAudio = flashcardsViewModel::stopAudio
                )
            }

            composable(
                route = NavRoutes.VocabularyDetail.route,
                arguments = listOf(
                    navArgument("vocabId") {
                        type = NavType.StringType
                        defaultValue = "v001"
                    }
                )
            ) { backStackEntry ->
                val context = LocalContext.current
                val vocabId = backStackEntry.arguments?.getString("vocabId") ?: "v001"
                val detailViewModel: VocabularyDetailViewModel = viewModel(
                    key = "vocab_detail_$vocabId",
                    factory = VocabularyDetailViewModel.provideFactory(
                        application = context.applicationContext as Application,
                        questionRepository = questionRepository,
                        vocabId = vocabId
                    )
                )
                val detailUiState by detailViewModel.uiState.collectAsStateWithLifecycle()
                VocabularyDetailScreen(
                    uiState = detailUiState,
                    onBackClick = { navController.navigateUp() },
                    onPreviousClick = {
                        detailUiState.previousVocabId?.let { detailViewModel.navigateTo(it) }
                    },
                    onNextClick = {
                        detailUiState.nextVocabId?.let { detailViewModel.navigateTo(it) }
                    },
                    onHearWord = detailViewModel::hearWord,
                    onHearDefinition = detailViewModel::hearDefinition,
                    onStopAudio = detailViewModel::stopAudio,
                    onToggleFavorite = detailViewModel::toggleFavorite,
                    onSetStatus = detailViewModel::setStatus,
                    onQuestionClick = { qId ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId))
                    }
                )
            }

            composable(
                route = NavRoutes.SpecialConsideration65_20.route,
                arguments = listOf(
                    navArgument("filter") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                    }
                )
            ) { backStackEntry ->
                val filterArg = backStackEntry.arguments?.getString("filter") ?: "ALL"
                val initialFilter = com.example.ui.senior6520.SeniorFilter.fromString(filterArg)
                val seniorViewModel: Senior6520ViewModel = viewModel(
                    key = "senior_6520_$filterArg",
                    factory = Senior6520ViewModel.provideFactory(questionRepository, initialFilter = initialFilter)
                )
                val seniorUiState by seniorViewModel.uiState.collectAsStateWithLifecycle()
                Senior6520Screen(
                    uiState = seniorUiState,
                    onBackClick = { navController.navigateUp() },
                    onFilterSelected = seniorViewModel::setFilter,
                    onSearchQueryChanged = seniorViewModel::setSearchQuery,
                    onToggleFavorite = seniorViewModel::toggleFavorite,
                    onQuestionClick = { qId ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId, "SENIOR_65_20"))
                    },
                    onStudyClick = {
                        navController.navigate(NavRoutes.Study.createRoute("SENIOR_65_20"))
                    },
                    onPracticeClick = { mode, count ->
                        navController.navigate(NavRoutes.PracticeTest.createRoute("SENIOR_65_20", mode, count))
                    },
                    onFlashcardsClick = {
                        navController.navigate(NavRoutes.Flashcards.createRoute("SENIOR_65_20"))
                    },
                    onSpeakingClick = {
                        navController.navigate(NavRoutes.SpeakingPractice.createRoute("SENIOR_65_20", 20))
                    }
                )
            }

            composable(
                route = NavRoutes.QuickReview.route,
                arguments = listOf(
                    navArgument("section") {
                        type = NavType.StringType
                        defaultValue = "ALL"
                    }
                )
            ) { backStackEntry ->
                val sectionParam = backStackEntry.arguments?.getString("section") ?: "ALL"
                val initialSection = try {
                    QuickReviewSection.valueOf(sectionParam.uppercase())
                } catch (_: Exception) {
                    QuickReviewSection.ALL
                }
                val quickReviewViewModel: QuickReviewViewModel = viewModel(
                    key = "quick_review_$sectionParam",
                    factory = QuickReviewViewModel.provideFactory(questionRepository, initialSection)
                )
                val quickReviewUiState by quickReviewViewModel.uiState.collectAsStateWithLifecycle()
                QuickReviewScreen(
                    uiState = quickReviewUiState,
                    onBackClick = { navController.navigateUp() },
                    onSectionSelected = quickReviewViewModel::setSection,
                    onSearchQueryChanged = quickReviewViewModel::setSearchQuery,
                    onToggleFavorite = quickReviewViewModel::toggleFavorite,
                    onQuestionClick = { qId ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId))
                    },
                    onPracticeNeedsWork = {
                        navController.navigate(NavRoutes.PracticeTest.createRoute("WEAK_AREAS"))
                    }
                )
            }

            composable(NavRoutes.QuestionsToReview.route) {
                val studyViewModel: StudyViewModel = viewModel(
                    factory = StudyViewModel.provideFactory(questionRepository, StudyFilter.NEEDS_PRACTICE)
                )
                LaunchedEffect(Unit) {
                    studyViewModel.onFilterSelected(StudyFilter.NEEDS_PRACTICE)
                }
                val studyUiState by studyViewModel.uiState.collectAsStateWithLifecycle()
                StudyScreen(
                    uiState = studyUiState,
                    onFilterSelected = studyViewModel::onFilterSelected,
                    onSearchQueryChanged = studyViewModel::onSearchQueryChanged,
                    onToggleFavorite = studyViewModel::toggleFavorite,
                    onQuestionClick = { qId, currentFilter, query ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId, currentFilter.name, query))
                    },
                    onNavigateToSettings = { navController.navigate(NavRoutes.Settings.route) },
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(NavRoutes.StudyActivity.route) {
                PlaceholderScreen(
                    title = "Study Activity",
                    subtitle = "Session History & Consistency",
                    icon = Icons.Outlined.Timeline,
                    description = "Track your study consistency and progress toward interview readiness.",
                    plannedFeatures = listOf(
                        "Daily streak counter and weekly study time charts",
                        "Questions completed per day visual breakdown"
                    ),
                    onBackClick = { navController.navigateUp() },
                    testTag = "placeholder_study_activity"
                )
            }

            // Category Progress Detail Screen
            composable(
                route = NavRoutes.CategoryProgress.route,
                arguments = listOf(
                    navArgument("categoryName") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                val decodedName = try {
                    java.net.URLDecoder.decode(categoryName, "UTF-8")
                } catch (_: Exception) {
                    categoryName
                }
                val catViewModel: CategoryProgressViewModel = viewModel(
                    factory = CategoryProgressViewModel.provideFactory(decodedName, questionRepository)
                )
                val catState by catViewModel.uiState.collectAsStateWithLifecycle()

                CategoryProgressScreen(
                    uiState = catState,
                    onBackClick = { navController.navigateUp() },
                    onPracticeTopic = { cat ->
                        navController.navigate(NavRoutes.PracticeTest.createRoute("TOPIC", cat))
                    },
                    onStudyTopic = { cat ->
                        val filter = when {
                            cat.contains("Government", ignoreCase = true) -> "AMERICAN_GOVERNMENT"
                            cat.contains("History", ignoreCase = true) -> "AMERICAN_HISTORY"
                            else -> "INTEGRATED_CIVICS"
                        }
                        navController.navigate(NavRoutes.Study.createRoute(filter))
                    },
                    onFlashcardsTopic = { cat ->
                        val filter = when {
                            cat.contains("Government", ignoreCase = true) -> "AMERICAN_GOVERNMENT"
                            cat.contains("History", ignoreCase = true) -> "AMERICAN_HISTORY"
                            else -> "INTEGRATED_CIVICS"
                        }
                        navController.navigate(NavRoutes.Flashcards.createRoute(filter))
                    },
                    onQuestionClick = { qId ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId))
                    }
                )
            }

            // Weak Areas Screen
            composable(NavRoutes.WeakAreas.route) {
                val weakAreasViewModel: WeakAreasViewModel = viewModel(
                    factory = WeakAreasViewModel.provideFactory(questionRepository)
                )
                val weakState by weakAreasViewModel.uiState.collectAsStateWithLifecycle()

                WeakAreasScreen(
                    uiState = weakState,
                    onBackClick = { navController.navigateUp() },
                    onPracticeWeakAreas = {
                        navController.navigate(NavRoutes.PracticeTest.createRoute("WEAK_AREAS"))
                    },
                    onPracticeCategory = { cat ->
                        navController.navigate(NavRoutes.PracticeTest.createRoute("TOPIC", cat))
                    },
                    onQuestionClick = { qId ->
                        navController.navigate(NavRoutes.QuestionDetail.createRoute(qId))
                    }
                )
            }

            // Info & Legal
            composable(NavRoutes.About.route) {
                AboutScreen(
                    onNavigateToDisclaimer = { navController.navigate(NavRoutes.Disclaimer.route) },
                    onBackClick = { navController.navigateUp() }
                )
            }

            composable(NavRoutes.Disclaimer.route) {
                DisclaimerScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }
        }
    }
}
