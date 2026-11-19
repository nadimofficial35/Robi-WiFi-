package com.example

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.RobiViewModel
import com.example.ui.components.MainNavTab
import com.example.ui.components.ProfileModalDialog
import com.example.ui.components.RobiBottomNavBar
import com.example.ui.components.RobiNavRail
import com.example.ui.components.RobiTopBar
import com.example.ui.screens.BindIssueScreen
import com.example.ui.screens.ChangePasswordScreen
import com.example.ui.screens.ComplineReportScreen
import com.example.ui.screens.CreateInvoiceFormScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InvoicePreviewScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MyReportsScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UpdateIsoScreen
import com.example.ui.theme.RobiCrimson
import com.example.ui.theme.RobiWifiTheme

object AppRoutes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val SEARCH = "search"
    const val QUIZ = "quiz"
    const val SETTINGS = "settings"
    const val COMPLINE_REPORT = "compline_report"
    const val UPDATE_ISO = "update_iso"
    const val BIND_ISSUE = "bind_issue"
    const val CREATE_INVOICE_FORM = "create_invoice_form"
    const val INVOICE_PREVIEW = "invoice_preview"
    const val MY_REPORTS = "my_reports"
    const val CHANGE_PASSWORD = "change_password"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RobiWifiTheme {
                RobiWifiApp()
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun RobiWifiApp(
    viewModel: RobiViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var showProfileModal by rememberSaveable { mutableStateOf(false) }

    val photoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadProfilePhoto(uri)
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        val msg = uiState.toastMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.consumeToast()
        }
    }

    if (uiState.isRestoringSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = RobiCrimson)
        }
        return
    }

    val currentUser = uiState.currentUser
    if (currentUser == null) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                LoginScreen(
                    isLoading = uiState.isLoginLoading,
                    errorMessage = uiState.loginError,
                    onClearError = viewModel::clearLoginError,
                    onLoginSubmit = { number, password ->
                        viewModel.login(number, password) {
                            // Automatic switch once currentUser is non-null
                        }
                    }
                )
            }
        }
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoutes.HOME

    val selectedTab = when (currentRoute) {
        AppRoutes.SEARCH -> MainNavTab.SEARCH
        AppRoutes.QUIZ -> MainNavTab.QUIZ
        AppRoutes.SETTINGS, AppRoutes.MY_REPORTS, AppRoutes.CHANGE_PASSWORD -> MainNavTab.SETTING
        else -> MainNavTab.HOME
    }

    val openExternalUrl: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    val sharePdfUri: (Uri, String) -> Unit = { uri, title ->
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share / Open Invoice PDF"))
        } catch (_: Exception) {
        }
    }

    val navigateTab: (MainNavTab) -> Unit = { tab ->
        val route = when (tab) {
            MainNavTab.HOME -> AppRoutes.HOME
            MainNavTab.SEARCH -> AppRoutes.SEARCH
            MainNavTab.QUIZ -> {
                viewModel.openQuiz()
                AppRoutes.QUIZ
            }
            MainNavTab.SETTING -> AppRoutes.SETTINGS
        }
        navController.navigate(route) {
            popUpTo(AppRoutes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                RobiTopBar(
                    user = currentUser,
                    photoPath = uiState.userPhotoPath,
                    onBrandClick = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onProfileClick = { showProfileModal = true }
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    RobiBottomNavBar(
                        selectedTab = selectedTab,
                        onTabSelected = navigateTab
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    RobiNavRail(
                        selectedTab = selectedTab,
                        onTabSelected = navigateTab
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.HOME,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(AppRoutes.HOME) {
                        HomeScreen(
                            syncStatus = uiState.syncStatus,
                            onRefreshSync = { viewModel.syncAllData(showNotify = true) },
                            onOpenAccesscon = { openExternalUrl("http://accesscon.robi.com.bd/") },
                            onOpenComplineReport = { navController.navigate(AppRoutes.COMPLINE_REPORT) },
                            onOpenUpdateIso = { navController.navigate(AppRoutes.UPDATE_ISO) },
                            onOpenBindIssue = { navController.navigate(AppRoutes.BIND_ISSUE) },
                            onOpenCreateInvoice = { navController.navigate(AppRoutes.CREATE_INVOICE_FORM) }
                        )
                    }

                    composable(AppRoutes.COMPLINE_REPORT) {
                        ComplineReportScreen(
                            onBack = { navController.popBackStack() },
                            onSubmit = { srId, status, newDate, comment ->
                                viewModel.submitComplineReport(srId, status, newDate, comment) {
                                    navController.navigate(AppRoutes.HOME) {
                                        popUpTo(AppRoutes.HOME) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable(AppRoutes.UPDATE_ISO) {
                        UpdateIsoScreen(
                            onBack = { navController.popBackStack() },
                            onOpenUrl = openExternalUrl
                        )
                    }

                    composable(AppRoutes.BIND_ISSUE) {
                        BindIssueScreen(
                            onBack = { navController.popBackStack() },
                            onSubmit = { issueType, shwapDevice, oldLogin, orderNum, loginNum, fwaNum ->
                                viewModel.submitBindIssue(
                                    issueType = issueType,
                                    shwapDevice = shwapDevice,
                                    oldLogin = oldLogin,
                                    orderNumber = orderNum,
                                    loginNumber = loginNum,
                                    fwaNumber = fwaNum
                                ) {
                                    navController.navigate(AppRoutes.HOME) {
                                        popUpTo(AppRoutes.HOME) { inclusive = true }
                                    }
                                }
                            }
                        )
                    }

                    composable(AppRoutes.CREATE_INVOICE_FORM) {
                        CreateInvoiceFormScreen(
                            onBack = { navController.popBackStack() },
                            onNext = { custName, custEmail, invNum, sl1, desc1, sl2, desc3 ->
                                viewModel.buildInvoicePreview(
                                    custName = custName,
                                    custEmail = custEmail,
                                    invNumber = invNum,
                                    sl1Device = sl1,
                                    desc1Input = desc1,
                                    sl2Package = sl2,
                                    desc3Input = desc3
                                ) {
                                    navController.navigate(AppRoutes.INVOICE_PREVIEW)
                                }
                            }
                        )
                    }

                    composable(AppRoutes.INVOICE_PREVIEW) {
                        InvoicePreviewScreen(
                            invoice = uiState.latestInvoice,
                            qrBitmap = uiState.invoiceQrBitmap,
                            isGeneratingPdf = uiState.isGeneratingPdf,
                            hasGeneratedPdf = uiState.lastGeneratedPdf != null,
                            onEditForm = { navController.popBackStack() },
                            onHome = {
                                navController.navigate(AppRoutes.HOME) {
                                    popUpTo(AppRoutes.HOME) { inclusive = true }
                                }
                            },
                            onOpenUrl = openExternalUrl,
                            onDownloadPdf = {
                                viewModel.downloadInvoicePdf { result ->
                                    if (!result.savedToDownloads) {
                                        sharePdfUri(result.shareUri, result.fileName)
                                    }
                                }
                            },
                            onSharePdf = {
                                uiState.lastGeneratedPdf?.let { pdf ->
                                    sharePdfUri(pdf.shareUri, pdf.fileName)
                                }
                            }
                        )
                    }

                    composable(AppRoutes.SEARCH) {
                        SearchScreen(
                            reports = uiState.reports,
                            onSync = { viewModel.syncAllData(showNotify = true) }
                        )
                    }

                    composable(AppRoutes.QUIZ) {
                        QuizScreen(
                            currentUser = currentUser,
                            quizMap = uiState.quizMap,
                            currentIndex = uiState.quizCurrentIndex,
                            selectedOptionIndex = uiState.quizSelectedOptionIndex,
                            onSelectOption = viewModel::selectQuizOption,
                            onNextOrSubmit = viewModel::nextOrSubmitQuiz
                        )
                    }

                    composable(AppRoutes.SETTINGS) {
                        SettingsScreen(
                            onOpenMyReports = {
                                viewModel.syncAllData(showNotify = false)
                                navController.navigate(AppRoutes.MY_REPORTS)
                            },
                            onOpenChangePassword = {
                                viewModel.clearPasswordError()
                                navController.navigate(AppRoutes.CHANGE_PASSWORD)
                            },
                            onLogout = {
                                showProfileModal = false
                                viewModel.logout { }
                            }
                        )
                    }

                    composable(AppRoutes.MY_REPORTS) {
                        MyReportsScreen(
                            currentUser = currentUser,
                            reports = uiState.reports,
                            onBack = { navController.popBackStack() },
                            onRefresh = { viewModel.syncAllData(showNotify = true) }
                        )
                    }

                    composable(AppRoutes.CHANGE_PASSWORD) {
                        ChangePasswordScreen(
                            isUpdating = uiState.isPasswordUpdating,
                            errorMessage = uiState.passwordError,
                            onClearError = viewModel::clearPasswordError,
                            onBack = { navController.popBackStack() },
                            onSubmit = { oldPass, newPass, confirmPass ->
                                viewModel.changePassword(oldPass, newPass, confirmPass) {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showProfileModal) {
        ProfileModalDialog(
            user = currentUser,
            photoPath = uiState.userPhotoPath,
            onUploadPhotoClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onDismiss = { showProfileModal = false }
        )
    }
}
