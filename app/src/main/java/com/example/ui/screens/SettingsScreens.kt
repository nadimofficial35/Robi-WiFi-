package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SrReport
import com.example.data.model.UserProfile
import com.example.ui.components.RobiTextField
import com.example.ui.components.SectionFormCard
import com.example.ui.components.SrReportCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BlueSoft
import com.example.ui.theme.RobiCrimson
import com.example.ui.theme.RobiCrimsonSoft
import com.example.util.DatePhoneUtils

@Composable
fun SettingsScreen(
    onOpenMyReports: () -> Unit,
    onOpenChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = RobiCrimson
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsOptionCard(
                        title = stringResource(R.string.my_report_today),
                        icon = Icons.Default.Assignment,
                        containerColor = BlueSoft,
                        accentColor = BlueAccent,
                        testTag = "settings_my_report_button",
                        onClick = onOpenMyReports
                    )

                    SettingsOptionCard(
                        title = stringResource(R.string.change_password),
                        icon = Icons.Default.Key,
                        containerColor = AmberSoft,
                        accentColor = AmberAccent,
                        testTag = "settings_change_password_button",
                        onClick = onOpenChangePassword
                    )

                    SettingsOptionCard(
                        title = stringResource(R.string.logout),
                        icon = Icons.AutoMirrored.Filled.Logout,
                        containerColor = RobiCrimsonSoft,
                        accentColor = RobiCrimson,
                        testTag = "settings_logout_button",
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsOptionCard(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun MyReportsScreen(
    currentUser: UserProfile?,
    reports: List<SrReport>,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val todayKey = remember { DatePhoneUtils.getTodayDateKey() }
    val targetId = currentUser?.idNumber?.trim()?.lowercase().orEmpty()

    val todayReports = remember(reports, targetId, todayKey) {
        if (targetId.isBlank()) {
            emptyList()
        } else {
            reports.filter { r ->
                r.idNumber.trim().lowercase() == targetId &&
                    DatePhoneUtils.isReportForToday(r, todayKey)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionFormCard(
            title = "Today's Reports",
            subtitle = "Date: $todayKey",
            onBack = onBack,
            trailingAction = {
                TextButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("my_reports_refresh_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh")
                }
            }
        ) {
            if (todayReports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "আজকের কোনো রিপোর্ট পাওয়া যায়নি।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(todayReports) { report ->
                        SrReportCard(
                            report = report,
                            showReporter = false,
                            showEmptyNextDateAsNa = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChangePasswordScreen(
    isUpdating: Boolean,
    errorMessage: String?,
    onClearError: () -> Unit,
    onBack: () -> Unit,
    onSubmit: (oldPass: String, newPass: String, confirmPass: String) -> Unit
) {
    var oldPass by rememberSaveable { mutableStateOf("") }
    var newPass by rememberSaveable { mutableStateOf("") }
    var confirmPass by rememberSaveable { mutableStateOf("") }

    val canSubmit = oldPass.isNotBlank() && newPass.isNotBlank() && confirmPass.isNotBlank() && !isUpdating

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionFormCard(
            title = "Change Password",
            onBack = onBack
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                RobiTextField(
                    value = oldPass,
                    onValueChange = {
                        oldPass = it
                        if (errorMessage != null) onClearError()
                    },
                    label = "Old Password",
                    placeholder = "Enter old password",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    testTag = "change_pass_old_input"
                )

                RobiTextField(
                    value = newPass,
                    onValueChange = {
                        newPass = it
                        if (errorMessage != null) onClearError()
                    },
                    label = "New Password",
                    placeholder = "Enter new password",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    testTag = "change_pass_new_input"
                )

                RobiTextField(
                    value = confirmPass,
                    onValueChange = {
                        confirmPass = it
                        if (errorMessage != null) onClearError()
                    },
                    label = "Confirm New Password",
                    placeholder = "Confirm new password",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    testTag = "change_pass_confirm_input"
                )

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = RobiCrimsonSoft,
                        border = BorderStroke(1.dp, RobiCrimson.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = RobiCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = RobiCrimson,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Button(
                    onClick = { onSubmit(oldPass, newPass, confirmPass) },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RobiCrimson,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("change_pass_submit_button")
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Updating…")
                    } else {
                        Text(
                            text = "Update Password",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
