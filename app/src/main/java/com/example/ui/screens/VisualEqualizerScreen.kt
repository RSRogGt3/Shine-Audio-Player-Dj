package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodels.LocalMusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualEqualizerScreen(
    viewModel: LocalMusicViewModel
) {
    val context = LocalContext.current
    val currentSkin by viewModel.currentSkin.collectAsState()
    val eqLaneCount by viewModel.eqLaneCount.collectAsState()
    val isEqEnabled by viewModel.isEqEnabled.collectAsState()
    val eqPresetName by viewModel.eqPresetName.collectAsState()
    val eqTarget by viewModel.eqTarget.collectAsState()

    val primaryColor = Color(currentSkin.primaryColorHex)
    val accentColor = Color(currentSkin.accentColorHex)
    val backgroundColor = Color(currentSkin.backgroundColorHex)
    val surfaceColor = Color(currentSkin.surfaceColorHex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Equalizer,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pro Studio Equalizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "5 - 12 Lanes Multi-Band Klangregelung",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveCurrentSettings(context) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(primaryColor.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Einstellungen speichern", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0E14)
                )
            )
        },
        containerColor = Color(0xFF0A090F)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Lane Count Selector: 5 to 12 Lanes
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("eq_lane_selector_card"),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF161520),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lanes auswählen (5 - 12 Bänder)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = when (eqLaneCount) {
                                    5 -> "5 Bänder (Standard)"
                                    6 -> "6 Bänder (DJ Club)"
                                    7 -> "7 Bänder (Live PA)"
                                    8 -> "8 Bänder (Multi-Band)"
                                    9 -> "9 Bänder (Broadcast)"
                                    10 -> "10 Bänder (ISO Oktav)"
                                    11 -> "11 Bänder (Mastering)"
                                    12 -> "12 Bänder (Pro Studio)"
                                    else -> "$eqLaneCount Bänder"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5 - 12 Lane Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        (5..12).forEach { lanes ->
                            val isSelected = eqLaneCount == lanes
                            Surface(
                                onClick = { viewModel.setEqLaneCount(lanes) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) primaryColor else Color(0xFF222030),
                                border = BorderStroke(1.dp, if (isSelected) Color.White else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lane_chip_$lanes")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$lanes",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Target Selector: MASTER, DECK A, DECK B
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "MASTER" to "Gesamtmix",
                    "DECK_A" to "Deck A (Ch 1)",
                    "DECK_B" to "Deck B (Ch 2)"
                ).forEach { (targetId, label) ->
                    val isSelected = eqTarget == targetId
                    Surface(
                        onClick = { viewModel.setEqTarget(targetId) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) primaryColor.copy(alpha = 0.2f) else Color(0xFF181724),
                        border = BorderStroke(1.dp, if (isSelected) primaryColor else Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = targetId.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) primaryColor else Color.White
                            )
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // The main Visual Equalizer Component
            VisualEqualizerComponent(
                viewModel = viewModel,
                primaryColor = primaryColor,
                accentColor = accentColor,
                surfaceColor = surfaceColor
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
