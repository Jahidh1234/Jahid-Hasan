package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.BannerAdWidget
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskViewModel

@Composable
fun AiInsightsScreen(viewModel: TaskViewModel) {
    val aiResponse by viewModel.aiInsights.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingTask.collectAsState()

    // Custom Query States
    val customAiResponse by viewModel.customAiQueryResponse.collectAsState()
    val isQueryingCustom by viewModel.isQueryingCustomAi.collectAsState()
    var userQueryText by remember { mutableStateOf("") }
    
    var selectedTab by remember { mutableStateOf(0) } // 0 = Coach Report, 1 = Gemini Chat

    // Load insights on creation once
    LaunchedEffect(Unit) {
        viewModel.loadAiInsightsLog()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Header card with premium gradient and psychology logo
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Assistant",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "গুগল জেমিনি এআই অ্যাসিস্ট্যান্ট",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "আপনার ধর্মীয়, ব্যক্তিগত ও অফিসিয়াল রুটিন সাজাতে সাহায্যকারী এআই",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Material 3 Tabs for selecting Coach Report vs Interactive Custom QA
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("রিপোর্ট বিশ্লেষণ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("জেমিনি চ্যাট", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Output Report Card Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (selectedTab == 0) {
                    // TAB 0: AUTOMATED PRODUCTIVITY INSIGHT REPORT
                    if (isAnalyzing) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "জেমিনি এআই আপনার কাজের ইতিহাস এবং স্ট্যাটিস্টিকস বিশ্লেষণ করছে...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 24.dp),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "অনুগ্রহ করে কিছুক্ষণ সময় দিন।",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        if (aiResponse.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "উৎপাদনশীলতা রিপোর্ট:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(
                                        onClick = { viewModel.triggerAiInsights() },
                                        modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = aiResponse,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 19.sp
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "কোনো বিশ্লেষণ নেই!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "আপনার কাজগুলো বিশ্লেষণ করে মোটিভেশনাল পরামর্শ পেতে নিচে ক্লিক করুন।",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(onClick = { viewModel.triggerAiInsights() }) {
                                    Text("এআই কোচ রিপোর্ট জেনারেট করুন")
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: INTERACTIVE CUSTOM Q&A WITH GEMINI AI
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Response panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            if (isQueryingCustom) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "জেমিনি এআই উত্তর প্রস্তুত করছে...",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else if (customAiResponse.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "জেমিনি এআই উত্তর:",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        TextButton(onClick = { viewModel.clearCustomAiQuery() }) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("মুছে ফেলুন", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = customAiResponse,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 18.sp
                                    )
                                }
                            } else {
                                Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QuestionAnswer,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "গুগল জেমিনি চ্যাট সহকারী",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "ব্যক্তিগত, পারিবারিক, মহিলা শাখা বা পুরুষ শাখার কাজগুলো কীভাবে সাজাবেন সে বিষয়ে যেকোনো মোটিভেশন বা উপদেশ এআই থেকে সরাসরি জিজ্ঞাসা করুন।",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp))

                        // Custom Query input row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = userQueryText,
                                onValueChange = { userQueryText = it },
                                placeholder = { Text("জেমিনিকে জিজ্ঞাসা করুন...") },
                                modifier = Modifier.weight(1f),
                                maxLines = 2,
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                            )

                            IconButton(
                                onClick = {
                                    if (userQueryText.isNotBlank()) {
                                        viewModel.askGeminiCustom(userQueryText)
                                        userQueryText = ""
                                    }
                                },
                                enabled = !isQueryingCustom && userQueryText.isNotBlank(),
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        if (userQueryText.isNotBlank() && !isQueryingCustom) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            Color.LightGray.copy(alpha = 0.5f)
                                        },
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Banner ad placement in AI Panel
        BannerAdWidget()
    }
}
