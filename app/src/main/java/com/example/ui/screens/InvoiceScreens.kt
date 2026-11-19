package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DeviceOption
import com.example.data.model.InvoiceCatalog
import com.example.data.model.InvoiceData
import com.example.ui.components.RobiDropdownField
import com.example.ui.components.RobiFacetEmblem
import com.example.ui.components.RobiTextField
import com.example.ui.components.SectionFormCard
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.RobiCrimson

@Composable
fun CreateInvoiceFormScreen(
    onBack: () -> Unit,
    onNext: (
        custName: String,
        custEmail: String,
        invNumber: String,
        sl1Device: DeviceOption,
        desc1: String,
        sl2Package: DeviceOption,
        desc3: String
    ) -> Unit
) {
    var custName by rememberSaveable { mutableStateOf("") }
    var custEmail by rememberSaveable { mutableStateOf("") }
    var invNumber by rememberSaveable { mutableStateOf("") }

    var sl1Index by rememberSaveable { mutableIntStateOf(-1) }
    var desc1 by rememberSaveable { mutableStateOf("") }

    var sl2Index by rememberSaveable { mutableIntStateOf(-1) }
    var desc3 by rememberSaveable { mutableStateOf("") }

    val selectedSl1 = InvoiceCatalog.sl1Devices.getOrNull(sl1Index)
    val selectedSl2 = InvoiceCatalog.sl2Packages.getOrNull(sl2Index)

    val unit1Display = selectedSl1?.unitPrice?.toInt()?.toString() ?: ""
    val unit2Display = selectedSl2?.unitPrice?.toInt()?.toString() ?: ""

    val canProceed = custName.isNotBlank() &&
        custEmail.isNotBlank() &&
        invNumber.isNotBlank() &&
        selectedSl1 != null &&
        desc1.isNotBlank() &&
        selectedSl2 != null &&
        desc3.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionFormCard(
            title = "Create Invoice",
            onBack = onBack
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                RobiTextField(
                    value = custName,
                    onValueChange = { custName = it },
                    label = "Customer Name",
                    placeholder = "Enter Customer Name",
                    testTag = "invoice_cust_name_input"
                )

                RobiTextField(
                    value = custEmail,
                    onValueChange = { custEmail = it },
                    label = "Customer E-mail",
                    placeholder = "Enter Customer E-mail / Contact",
                    testTag = "invoice_cust_email_input"
                )

                RobiTextField(
                    value = invNumber,
                    onValueChange = { invNumber = it },
                    label = "Invoice Number",
                    placeholder = "Enter Invoice Number",
                    testTag = "invoice_number_input"
                )

                // S.L. 01
                InvoiceSerialGroup(serialTitle = "S.L. 01") {
                    RobiDropdownField(
                        label = "Device Select",
                        selectedLabel = selectedSl1?.name.orEmpty(),
                        placeholder = "Select Device",
                        options = InvoiceCatalog.sl1Devices,
                        optionLabel = { "${it.name} (BDT ${it.unitPrice.toInt()})" },
                        onOptionSelected = { option ->
                            sl1Index = InvoiceCatalog.sl1Devices.indexOf(option)
                        },
                        testTag = "invoice_sl1_dropdown"
                    )
                    RobiTextField(
                        value = unit1Display,
                        onValueChange = {},
                        readOnly = true,
                        label = "Unit Price (Auto)",
                        placeholder = "0",
                        testTag = "invoice_sl1_unit_price"
                    )
                    RobiTextField(
                        value = desc1,
                        onValueChange = { desc1 = it },
                        label = "Description",
                        placeholder = "Enter Description (e.g. IMEI / Serial)",
                        testTag = "invoice_sl1_desc_input"
                    )
                }

                // S.L. 02
                InvoiceSerialGroup(serialTitle = "S.L. 02") {
                    RobiDropdownField(
                        label = "Package / Device Select",
                        selectedLabel = selectedSl2?.name.orEmpty(),
                        placeholder = "Select Option",
                        options = InvoiceCatalog.sl2Packages,
                        optionLabel = { "${it.name} (BDT ${it.unitPrice.toInt()})" },
                        onOptionSelected = { option ->
                            sl2Index = InvoiceCatalog.sl2Packages.indexOf(option)
                        },
                        testTag = "invoice_sl2_dropdown"
                    )
                    RobiTextField(
                        value = unit2Display,
                        onValueChange = {},
                        readOnly = true,
                        label = "Unit Price (Auto)",
                        placeholder = "0",
                        testTag = "invoice_sl2_unit_price"
                    )
                }

                // S.L. 03
                InvoiceSerialGroup(serialTitle = "S.L. 03") {
                    RobiTextField(
                        value = desc3,
                        onValueChange = { desc3 = it },
                        label = "Description",
                        placeholder = "Enter Description (e.g. 8801898519298)",
                        testTag = "invoice_sl3_desc_input"
                    )
                }

                Button(
                    onClick = {
                        if (selectedSl1 != null && selectedSl2 != null) {
                            onNext(
                                custName,
                                custEmail,
                                invNumber,
                                selectedSl1,
                                desc1,
                                selectedSl2,
                                desc3
                            )
                        }
                    },
                    enabled = canProceed,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RobiCrimson,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("invoice_next_button")
                ) {
                    Text(
                        text = "Next",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InvoiceSerialGroup(
    serialTitle: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = serialTitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RobiCrimson
            )
            content()
        }
    }
}

@Composable
fun InvoicePreviewScreen(
    invoice: InvoiceData,
    qrBitmap: Bitmap?,
    isGeneratingPdf: Boolean,
    hasGeneratedPdf: Boolean,
    onEditForm: () -> Unit,
    onHome: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onDownloadPdf: () -> Unit,
    onSharePdf: () -> Unit
) {
    val qrImageBitmap = remember(qrBitmap) { qrBitmap?.asImageBitmap() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top preview toolbar
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = onEditForm,
                    modifier = Modifier.testTag("invoice_edit_form_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Form", fontWeight = FontWeight.SemiBold)
                }

                Text(
                    text = "Invoice Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onHome,
                    modifier = Modifier.testTag("invoice_home_button")
                ) {
                    Text("Home", color = RobiCrimson, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Crisp A4-styled Invoice Sheet inside horizontal scroll container for mobile readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(14.dp)
                )
                .padding(8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Surface(
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                shadowElevation = 2.dp,
                modifier = Modifier.width(640.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Top Row: Geometric Emblem | Vertical COMPLETED | INVOICE / ROBI WIFI / Phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        RobiFacetEmblem(size = 56.dp)

                        Box(
                            modifier = Modifier
                                .padding(top = 18.dp)
                                .rotate(-90f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "COMPLETED",
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "INVOICE",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Text(
                                text = "ROBI WIFI",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF111827)
                            )
                            Text(
                                text = "+8801899999999",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827),
                                fontFamily = JetBrainsMono
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Billed To",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Billed To & Invoice Meta Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            MetaLineRow("Customer Name :", invoice.custName, underlined = true)
                            MetaLineRow("Customer E-mail :", invoice.custEmail, underlined = true)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            MetaBoxRow("Invoice Number :", invoice.invNumber)
                            MetaLineRow("Invoice Date :", invoice.invDate, underlined = false)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        Text(
                            text = "Payment Mode : ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "COD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            textDecoration = TextDecoration.Underline
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Items Table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Color(0xFF1F2937))
                    ) {
                        InvoiceTableRow(
                            sl = "S. L.",
                            desc = "Description",
                            unit = "Unit",
                            qty = "Qty",
                            amt = "Amunt",
                            isHeader = true
                        )
                        HorizontalDivider(thickness = 1.5.dp, color = Color(0xFF1F2937))
                        InvoiceTableRow(
                            sl = "01",
                            desc = invoice.desc1,
                            unit = invoice.unit1,
                            qty = "1",
                            amt = invoice.amt1,
                            isHeader = false
                        )
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFF374151))
                        InvoiceTableRow(
                            sl = "02",
                            desc = invoice.desc2,
                            unit = invoice.unit2,
                            qty = "1",
                            amt = invoice.amt2,
                            isHeader = false
                        )
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFF374151))
                        InvoiceTableRow(
                            sl = "03",
                            desc = invoice.desc3,
                            unit = invoice.unit3,
                            qty = "1",
                            amt = invoice.amt3,
                            isHeader = false
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bottom Grid: QR + Totals
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(98.dp)
                                .border(1.dp, Color(0xFFD1D5DB))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrImageBitmap != null) {
                                Image(
                                    bitmap = qrImageBitmap,
                                    contentDescription = stringResource(R.string.cd_invoice_qr),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("robiwifibd", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Column(modifier = Modifier.width(230.dp)) {
                            TotalRow("Subtotal :", invoice.subtotal)
                            TotalRow("Discount (%) :", "0.00")
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 5.dp),
                                color = Color(0xFF9CA3AF)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total :   BDT",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF111827)
                                )
                                Text(
                                    text = invoice.total,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF111827),
                                    fontFamily = JetBrainsMono
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Youtube.com/@Robiwifibd",
                        color = Color(0xFF2563EB),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable {
                            onOpenUrl("https://www.youtube.com/@robiwifibd")
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "https://robiwifi.robi.com.bd",
                            color = Color(0xFF2563EB),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                onOpenUrl("https://robiwifi.robi.com.bd")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = EmeraldSuccess,
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "Thank You",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 760.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onDownloadPdf,
                enabled = !isGeneratingPdf,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldSuccess,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("download_invoice_pdf_button")
            ) {
                if (isGeneratingPdf) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating PDF…")
                } else {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.download_invoice_pdf),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (hasGeneratedPdf) {
                OutlinedButton(
                    onClick = onSharePdf,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, RobiCrimson),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RobiCrimson),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("share_invoice_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.share_invoice_pdf),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MetaLineRow(label: String, value: String, underlined: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            modifier = Modifier.width(114.dp)
        )
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.ifBlank { "-" },
                fontSize = 12.sp,
                fontWeight = if (underlined) FontWeight.SemiBold else FontWeight.ExtraBold,
                color = Color(0xFF111827),
                textAlign = TextAlign.Center
            )
            if (underlined) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = 2.dp),
                    thickness = 1.dp,
                    color = Color(0xFF374151)
                )
            }
        }
    }
}

@Composable
private fun MetaBoxRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            modifier = Modifier.width(114.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .border(1.5.dp, Color(0xFF111827))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.ifBlank { "-" },
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111827)
            )
        }
    }
}

@Composable
private fun InvoiceTableRow(
    sl: String,
    desc: String,
    unit: String,
    qty: String,
    amt: String,
    isHeader: Boolean
) {
    val weight = if (isHeader) FontWeight.ExtraBold else FontWeight.SemiBold
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = sl,
            fontSize = 12.sp,
            fontWeight = weight,
            color = Color(0xFF111827),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.11f)
        )
        Text(
            text = desc,
            fontSize = 12.sp,
            fontWeight = weight,
            color = Color(0xFF111827),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.43f)
        )
        Text(
            text = unit,
            fontSize = 12.sp,
            fontWeight = weight,
            color = Color(0xFF111827),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.15f)
        )
        Text(
            text = qty,
            fontSize = 12.sp,
            fontWeight = weight,
            color = Color(0xFF111827),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.11f)
        )
        Text(
            text = amt,
            fontSize = 12.sp,
            fontWeight = weight,
            color = Color(0xFF111827),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.20f)
        )
    }
}

@Composable
private fun TotalRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            fontFamily = JetBrainsMono
        )
    }
}
