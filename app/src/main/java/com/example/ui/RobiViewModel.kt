package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LocalCacheStore
import com.example.data.model.DeviceOption
import com.example.data.model.InvoiceData
import com.example.data.model.RobiQuizBank
import com.example.data.model.SrReport
import com.example.data.model.SyncStatus
import com.example.data.model.UserProfile
import com.example.data.remote.ApiResult
import com.example.data.remote.RobiApiClient
import com.example.util.DatePhoneUtils
import com.example.util.InvoicePdfGenerator
import com.example.util.PdfGenerationResult
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RobiUiState(
    val isRestoringSession: Boolean = true,
    val currentUser: UserProfile? = null,
    val userPhotoPath: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LIVE,
    val reports: List<SrReport> = emptyList(),
    val quizMap: Map<String, List<String>> = emptyMap(),
    val users: List<UserProfile> = emptyList(),
    val isLoginLoading: Boolean = false,
    val loginError: String? = null,
    val isPasswordUpdating: Boolean = false,
    val passwordError: String? = null,
    val toastMessage: String? = null,
    val latestInvoice: InvoiceData = InvoiceData(),
    val invoiceQrBitmap: Bitmap? = null,
    val isGeneratingPdf: Boolean = false,
    val lastGeneratedPdf: PdfGenerationResult? = null,
    val quizCurrentIndex: Int = 0,
    val quizSelectedOptionIndex: Int? = null,
    val quizWorkingAnswers: List<String> = emptyList()
)

class RobiViewModel(application: Application) : AndroidViewModel(application) {
    private val cacheStore = LocalCacheStore(application.applicationContext)
    private val apiClient = RobiApiClient()

    private val _uiState = MutableStateFlow(RobiUiState())
    val uiState: StateFlow<RobiUiState> = _uiState.asStateFlow()

    init {
        restoreSessionAndSync()
    }

    private fun restoreSessionAndSync() {
        viewModelScope.launch {
            val savedUser = cacheStore.getCurrentUser()
            val cachedReports = cacheStore.getCachedReports()
            val cachedQuiz = cacheStore.getCachedQuizMap()
            val cachedUsers = cacheStore.getCachedUsers()
            val photoPath = savedUser?.let { cacheStore.getUserPhotoPath(it.idNumber) }
            val qr = withContext(Dispatchers.Default) {
                QrCodeGenerator.generateQrBitmap("https://www.youtube.com/@robiwifibd", 240)
            }

            _uiState.update {
                it.copy(
                    isRestoringSession = false,
                    currentUser = savedUser,
                    userPhotoPath = photoPath,
                    reports = cachedReports,
                    quizMap = cachedQuiz,
                    users = cachedUsers,
                    invoiceQrBitmap = qr
                )
            }
            syncAllData(showNotify = false)
        }
    }

    fun syncAllData(showNotify: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(syncStatus = SyncStatus.SYNCING) }
            when (val res = apiClient.syncAll()) {
                is ApiResult.Success -> {
                    val data = res.data
                    cacheStore.saveCachedReports(data.reports)
                    cacheStore.saveCachedQuizMap(data.quizMap)
                    if (data.users.isNotEmpty()) {
                        cacheStore.saveCachedUsers(data.users)
                    }
                    _uiState.update { state ->
                        state.copy(
                            syncStatus = SyncStatus.LIVE,
                            reports = data.reports,
                            quizMap = data.quizMap,
                            users = if (data.users.isNotEmpty()) data.users else state.users,
                            toastMessage = if (showNotify) "সব ডেটা আপডেট হয়েছে!" else state.toastMessage
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(syncStatus = SyncStatus.OFFLINE_CACHE)
                    }
                }
            }
        }
    }

    fun login(number: String, password: String, onSuccess: () -> Unit) {
        if (_uiState.value.isLoginLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoginLoading = true, loginError = null) }
            when (val res = apiClient.login(number, password)) {
                is ApiResult.Success -> {
                    val user = res.data
                    cacheStore.saveCurrentUser(user)
                    val photoPath = cacheStore.getUserPhotoPath(user.idNumber)
                    _uiState.update {
                        it.copy(
                            isLoginLoading = false,
                            currentUser = user,
                            userPhotoPath = photoPath,
                            loginError = null,
                            quizCurrentIndex = 0,
                            quizSelectedOptionIndex = null,
                            quizWorkingAnswers = emptyList()
                        )
                    }
                    onSuccess()
                    syncAllData(showNotify = false)
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoginLoading = false,
                            loginError = res.message
                        )
                    }
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            cacheStore.saveCurrentUser(null)
            _uiState.update {
                it.copy(
                    currentUser = null,
                    userPhotoPath = null,
                    loginError = null,
                    quizCurrentIndex = 0,
                    quizSelectedOptionIndex = null,
                    quizWorkingAnswers = emptyList()
                )
            }
            onLoggedOut()
        }
    }

    fun clearLoginError() {
        _uiState.update { it.copy(loginError = null) }
    }

    fun clearPasswordError() {
        _uiState.update { it.copy(passwordError = null) }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun uploadProfilePhoto(uri: Uri) {
        val user = _uiState.value.currentUser ?: return
        viewModelScope.launch {
            val savedPath = cacheStore.saveUserPhotoFromUri(user.idNumber, uri)
            if (savedPath != null) {
                _uiState.update {
                    it.copy(
                        userPhotoPath = savedPath,
                        toastMessage = "Profile photo updated!"
                    )
                }
            }
        }
    }

    fun submitComplineReport(
        srId: String,
        status: String,
        newDate: String,
        comment: String,
        onDone: () -> Unit
    ) {
        val user = _uiState.value.currentUser ?: run {
            _uiState.update { it.copy(toastMessage = "Please login again.") }
            return
        }
        val cleanSrId = srId.trim()
        val cleanComment = comment.trim()
        val effectiveNextDate = if (status == "Customer Reschudul") newDate.trim() else ""

        val nowStr = DatePhoneUtils.getFormattedNowDateTime()
        val todayKey = DatePhoneUtils.getTodayDateKey()
        val newEntry = SrReport(
            dateTime = nowStr,
            dateKey = todayKey,
            idNumber = user.idNumber,
            bdoName = user.bdoName,
            srId = cleanSrId,
            status = status,
            nextDate = effectiveNextDate,
            comment = cleanComment,
            supervisor = user.supervisor
        )

        val updatedReports = listOf(newEntry) + _uiState.value.reports
        _uiState.update {
            it.copy(
                reports = updatedReports,
                toastMessage = "Report Submitted Successfully!"
            )
        }
        onDone()

        viewModelScope.launch {
            cacheStore.saveCachedReports(updatedReports)
            apiClient.submitComplineReport(
                idNumber = user.idNumber,
                srId = cleanSrId,
                status = status,
                newDate = effectiveNextDate,
                comment = cleanComment,
                supervisor = user.supervisor
            )
        }
    }

    fun submitBindIssue(
        issueType: String,
        shwapDevice: String,
        oldLogin: String,
        orderNumber: String,
        loginNumber: String,
        fwaNumber: String,
        onDone: () -> Unit
    ) {
        val user = _uiState.value.currentUser ?: run {
            _uiState.update { it.copy(toastMessage = "Please login again.") }
            return
        }
        val extraInfo = when (issueType) {
            "Shwap" -> shwapDevice
            "Change Number" -> oldLogin.trim()
            else -> ""
        }

        _uiState.update { it.copy(toastMessage = "Bind Issue Submitted Successfully!") }
        onDone()

        viewModelScope.launch {
            apiClient.submitBindIssue(
                idNumber = user.idNumber,
                issueType = issueType,
                extraInfo = extraInfo,
                orderNumber = orderNumber.trim(),
                loginNumber = loginNumber.trim(),
                fwaNumber = fwaNumber.trim()
            )
        }
    }

    fun changePassword(
        oldPassword: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        val user = _uiState.value.currentUser ?: return
        val oldP = oldPassword.trim()
        val newP = newPassword.trim()
        val confirmP = confirmPassword.trim()

        if (newP != confirmP) {
            _uiState.update {
                it.copy(passwordError = "New Password এবং Confirm New Password মিলছে না!")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPasswordUpdating = true, passwordError = null) }
            when (val res = apiClient.changePassword(user.idNumber, oldP, newP)) {
                is ApiResult.Success -> {
                    val updatedUser = user.copy(password = newP)
                    cacheStore.saveCurrentUser(updatedUser)
                    _uiState.update {
                        it.copy(
                            isPasswordUpdating = false,
                            currentUser = updatedUser,
                            passwordError = null,
                            toastMessage = res.data
                        )
                    }
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isPasswordUpdating = false,
                            passwordError = res.message
                        )
                    }
                }
            }
        }
    }

    fun openQuiz() {
        _uiState.update {
            it.copy(
                quizCurrentIndex = 0,
                quizSelectedOptionIndex = null,
                quizWorkingAnswers = emptyList()
            )
        }
        syncAllData(showNotify = false)
    }

    fun selectQuizOption(optionIndex: Int) {
        _uiState.update { it.copy(quizSelectedOptionIndex = optionIndex) }
    }

    fun nextOrSubmitQuiz() {
        val state = _uiState.value
        val selectedIdx = state.quizSelectedOptionIndex ?: return
        val questions = RobiQuizBank.questions
        val currentQ = questions.getOrNull(state.quizCurrentIndex) ?: return
        val chosenText = currentQ.options.getOrNull(selectedIdx) ?: return
        val newAnswers = state.quizWorkingAnswers + chosenText

        if (state.quizCurrentIndex < questions.lastIndex) {
            _uiState.update {
                it.copy(
                    quizCurrentIndex = state.quizCurrentIndex + 1,
                    quizSelectedOptionIndex = null,
                    quizWorkingAnswers = newAnswers
                )
            }
        } else {
            val user = state.currentUser ?: return
            val targetId = user.idNumber.trim().lowercase()
            val updatedQuizMap = state.quizMap.toMutableMap().apply {
                put(targetId, newAnswers)
            }
            _uiState.update {
                it.copy(
                    quizMap = updatedQuizMap,
                    quizWorkingAnswers = emptyList(),
                    quizSelectedOptionIndex = null,
                    toastMessage = "Quiz Submitted to Sheet3!"
                )
            }
            viewModelScope.launch {
                cacheStore.saveCachedQuizMap(updatedQuizMap)
                apiClient.submitQuiz(user.idNumber, newAnswers)
            }
        }
    }

    fun buildInvoicePreview(
        custName: String,
        custEmail: String,
        invNumber: String,
        sl1Device: DeviceOption,
        desc1Input: String,
        sl2Package: DeviceOption,
        desc3Input: String,
        onReady: () -> Unit
    ) {
        val cleanCustName = custName.trim()
        val cleanCustEmail = custEmail.trim()
        val cleanInvNumber = invNumber.trim()
        val cleanDesc1 = desc1Input.trim()
        val fullDesc1 = if (cleanDesc1.isNotEmpty()) {
            "(${sl1Device.name}) - $cleanDesc1"
        } else {
            sl1Device.name
        }
        val cleanDesc3 = desc3Input.trim()

        val amt1 = sl1Device.unitPrice
        val amt2 = sl2Package.unitPrice
        val subtotal = amt1 + amt2
        val total = subtotal

        val invoice = InvoiceData(
            custName = cleanCustName,
            custEmail = cleanCustEmail,
            invNumber = cleanInvNumber,
            invDate = DatePhoneUtils.getFormattedInvoiceDate(),
            desc1 = fullDesc1,
            unit1 = DatePhoneUtils.formatUnitDisplay(amt1),
            amt1 = DatePhoneUtils.formatAmountPlain(amt1),
            desc2 = sl2Package.name,
            unit2 = DatePhoneUtils.formatUnitDisplay(amt2),
            amt2 = DatePhoneUtils.formatAmountPlain(amt2),
            desc3 = cleanDesc3,
            unit3 = "0",
            amt3 = "0.00",
            subtotal = DatePhoneUtils.formatAmountPlain(subtotal),
            total = DatePhoneUtils.formatAmountComma(total)
        )

        _uiState.update {
            it.copy(
                latestInvoice = invoice,
                lastGeneratedPdf = null
            )
        }
        onReady()
    }

    fun downloadInvoicePdf(onPdfReady: (PdfGenerationResult) -> Unit) {
        if (_uiState.value.isGeneratingPdf) return
        val invoice = _uiState.value.latestInvoice
        val qrBitmap = _uiState.value.invoiceQrBitmap

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPdf = true) }
            try {
                val result = withContext(Dispatchers.IO) {
                    InvoicePdfGenerator.generateAndSaveInvoicePdf(
                        context = getApplication(),
                        invoice = invoice,
                        qrBitmap = qrBitmap
                    )
                }
                _uiState.update {
                    it.copy(
                        isGeneratingPdf = false,
                        lastGeneratedPdf = result,
                        toastMessage = if (result.savedToDownloads) {
                            "Invoice PDF saved to Downloads!"
                        } else {
                            "Invoice PDF ready!"
                        }
                    )
                }
                onPdfReady(result)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingPdf = false,
                        toastMessage = "PDF তৈরি করতে সমস্যা হয়েছে: ${e.message}"
                    )
                }
            }
        }
    }
}
