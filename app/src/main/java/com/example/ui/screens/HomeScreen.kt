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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.SyncStatus
import com.example.ui.theme.AmberSoft
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BlueSoft
import com.example.ui.theme.EmeraldSoft
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeSoft
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.RobiCrimson
import com.example.ui.theme.RobiCrimsonSoft

@Composable
fun HomeScreen(
    syncStatus: SyncStatus,
    onRefreshSync: () -> Unit,
    onOpenAccesscon: () -> Unit,
    onOpenComplineReport: () -> Unit,
    onOpenUpdateIso: () -> Unit,
    onOpenBindIssue: () -> Unit,
    onOpenCreateInvoice: () -> Unit
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.main_menu),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    SyncBadgeChip(
                        syncStatus = syncStatus,
                        onClick = onRefreshSync
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeMenuActionCard(
                        title = stringResource(R.string.menu_accesscon),
                        icon = Icons.Default.Public,
                        containerColor = RobiCrimsonSoft,
                        accentColor = RobiCrimson,
                        testTag = "menu_accesscon_card",
                        onClick = onOpenAccesscon
                    )

                    HomeMenuActionCard(
                        title = stringResource(R.string.menu_compline_report),
                        icon = Icons.Default.EditNote,
                        containerColor = BlueSoft,
                        accentColor = BlueAccent,
                        testTag = "menu_compline_card",
                        onClick = onOpenComplineReport
                    )

                    HomeMenuActionCard(
                        title = stringResource(R.string.menu_update_iso),
                        icon = Icons.Default.FolderOpen,
                        containerColor = EmeraldSoft,
                        accentColor = EmeraldSuccess,
                        testTag = "menu_iso_card",
                        onClick = onOpenUpdateIso
                    )

                    HomeMenuActionCard(
                        title = stringResource(R.string.menu_bind_issue),
                        icon = Icons.Default.Link,
                        containerColor = PurpleSoft,
                        accentColor = PurpleAccent,
                        testTag = "menu_bind_issue_card",
                        onClick = onOpenBindIssue
                    )

                    HomeMenuActionCard(
                        title = stringResource(R.string.menu_create_invoice),
                        icon = Icons.Default.ReceiptLong,
                        containerColor = OrangeSoft,
                        accentColor = OrangeAccent,
                        testTag = "menu_create_invoice_card",
                        onClick = onOpenCreateInvoice
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncBadgeChip(
    syncStatus: SyncStatus,
    onClick: () -> Unit
) {
    val (label, icon, bg, fg) = when (syncStatus) {
        SyncStatus.LIVE -> Quad("Live", Icons.Default.CheckCircle, EmeraldSoft, EmeraldSuccess)
        SyncStatus.SYNCING -> Quad("Syncing…", Icons.Default.Sync, BlueSoft, BlueAccent)
        SyncStatus.OFFLINE_CACHE -> Quad("Offline Cache", Icons.Default.Bolt, AmberSoft, OrangeAccent)
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bg,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("home_sync_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.cd_sync),
                tint = fg,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fg
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun HomeMenuActionCard(
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
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
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
