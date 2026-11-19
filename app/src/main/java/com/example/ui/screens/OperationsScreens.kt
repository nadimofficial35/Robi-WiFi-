package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.RobiDropdownField
import com.example.ui.components.RobiTextField
import com.example.ui.components.SectionFormCard
import com.example.ui.theme.RobiCrimson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplineReportScreen(
    onBack: () -> Unit,
    onSubmit: (srId: String, status: String, newDate: String, comment: String) -> Unit
) {
    val statusOptions = listOf(
        "Customer Reschudul",
        "Customer Unrech",
        "Ask Later",
        "Cancel"
    )

    var srId by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("") }
    var nextDate by rememberSaveable { mutableStateOf("") }
    var comment by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val needsDate = status == "Customer Reschudul"
    val canSubmit = srId.isNotBlank() && status.isNotBlank() && comment.isNotBlank() &&
        (!needsDate || nextDate.isNotBlank())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionFormCard(
            title = "Compline Report Submission",
            onBack = onBack
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                RobiTextField(
                    value = srId,
                    onValueChange = { srId = it },
                    label = "SR ID",
                    placeholder = "Enter SR ID",
                    testTag = "compline_sr_id_input"
                )

                RobiDropdownField(
                    label = "Status",
                    selectedLabel = status,
                    placeholder = "Select Status",
                    options = statusOptions,
                    optionLabel = { it },
                    onOptionSelected = { selected ->
                        status = selected
                        if (selected != "Customer Reschudul") {
                            nextDate = ""
                        }
                    },
                    testTag = "compline_status_dropdown"
                )

                AnimatedVisibility(visible = needsDate) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        RobiTextField(
                            value = nextDate,
                            onValueChange = {},
                            readOnly = true,
                            label = "Next Date",
                            placeholder = "YYYY-MM-DD",
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Next Date"
                                )
                            },
                            testTag = "compline_next_date_input"
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { showDatePicker = true }
                        )
                    }
                }

                RobiTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = "Comment",
                    placeholder = "Write comment…",
                    singleLine = false,
                    minLines = 3,
                    testTag = "compline_comment_input"
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { onSubmit(srId, status, nextDate, comment) },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RobiCrimson,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("compline_submit_button")
                ) {
                    Text(
                        text = "Submit",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            nextDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = RobiCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun UpdateIsoScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionFormCard(
            title = "Update ISO File",
            onBack = onBack
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onOpenUrl("http://accesscon.robi.com.bd/") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1F2937),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("iso_accesscon_button")
                ) {
                    Text(
                        text = "Accesscon Robi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { onOpenUrl("https://www.appcreator24.com") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RobiCrimson,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("iso_appcreator_button")
                ) {
                    Text(
                        text = "AppCreator24",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun BindIssueScreen(
    onBack: () -> Unit,
    onSubmit: (
        issueType: String,
        shwapDevice: String,
        oldLogin: String,
        orderNumber: String,
        loginNumber: String,
        fwaNumber: String
    ) -> Unit
) {
    val issueOptions = listOf("Bind Issue", "Change Number", "Shwap")
    val deviceOptions = listOf("5G Device", "Cat4 to Cat6 Device")

    var issueType by rememberSaveable { mutableStateOf("") }
    var shwapDevice by rememberSaveable { mutableStateOf("") }
    var oldLogin by rememberSaveable { mutableStateOf("") }
    var orderNumber by rememberSaveable { mutableStateOf("") }
    var loginNumber by rememberSaveable { mutableStateOf("") }
    var fwaNumber by rememberSaveable { mutableStateOf("") }

    val extraValid = when (issueType) {
        "Shwap" -> shwapDevice.isNotBlank()
        "Change Number" -> oldLogin.isNotBlank()
        else -> true
    }
    val canSubmit = issueType.isNotBlank() && extraValid &&
        orderNumber.isNotBlank() && loginNumber.isNotBlank() && fwaNumber.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionFormCard(
            title = "Bind Issue Submission",
            onBack = onBack
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                RobiDropdownField(
                    label = "Bind Issue",
                    selectedLabel = issueType,
                    placeholder = "Select Issue",
                    options = issueOptions,
                    optionLabel = { it },
                    onOptionSelected = { selected ->
                        issueType = selected
                        if (selected != "Shwap") shwapDevice = ""
                        if (selected != "Change Number") oldLogin = ""
                    },
                    testTag = "bind_issue_dropdown"
                )

                AnimatedVisibility(visible = issueType == "Shwap") {
                    RobiDropdownField(
                        label = "Device Select",
                        selectedLabel = shwapDevice,
                        placeholder = "Select Device",
                        options = deviceOptions,
                        optionLabel = { it },
                        onOptionSelected = { shwapDevice = it },
                        testTag = "bind_shwap_device_dropdown"
                    )
                }

                AnimatedVisibility(visible = issueType == "Change Number") {
                    RobiTextField(
                        value = oldLogin,
                        onValueChange = { oldLogin = it },
                        label = "Old Login Number",
                        placeholder = "Enter Old Login Number",
                        testTag = "bind_old_login_input"
                    )
                }

                RobiTextField(
                    value = orderNumber,
                    onValueChange = { orderNumber = it },
                    label = "Order Number",
                    placeholder = "Enter Order Number",
                    testTag = "bind_order_number_input"
                )

                RobiTextField(
                    value = loginNumber,
                    onValueChange = { loginNumber = it },
                    label = "Login Number",
                    placeholder = "Enter Login Number",
                    testTag = "bind_login_number_input"
                )

                RobiTextField(
                    value = fwaNumber,
                    onValueChange = { fwaNumber = it },
                    label = "FWA Number",
                    placeholder = "Enter FWA Number",
                    testTag = "bind_fwa_number_input"
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        onSubmit(
                            issueType,
                            shwapDevice,
                            oldLogin,
                            orderNumber,
                            loginNumber,
                            fwaNumber
                        )
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RobiCrimson,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("bind_issue_submit_button")
                ) {
                    Text(
                        text = "Submit",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
