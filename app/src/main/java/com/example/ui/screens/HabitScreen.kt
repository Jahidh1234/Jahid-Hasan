package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HabitCompletion
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitScreen(viewModel: TaskViewModel) {
    val completions by viewModel.habitCompletionsFlow.collectAsState()

    // Determine today string
    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // Modern Prayer Habits Definitions
    val prayersList = remember {
        listOf(
            PrayerDetail("NAMAZ_FAJR", "ফজর (Fajr)", "ভোর", Icons.Default.FilterDrama, Color(0xFF64B5F6)),
            PrayerDetail("NAMAZ_DHUHR", "যোহর (Dhuhr)", "দুপুর", Icons.Default.WbSunny, Color(0xFFFFB74D)),
            PrayerDetail("NAMAZ_ASR", "আসর (Asr)", "বিকাল", Icons.Default.Cloud, Color(0xFFE0E0E0)),
            PrayerDetail("NAMAZ_MAGHRIB", "মাগরিব (Maghrib)", "সন্ধ্যা", Icons.Default.WbSunny, Color(0xFFFF8A65)),
            PrayerDetail("NAMAZ_ISHA", "এশা (Isha)", "রাত", Icons.Default.NightsStay, Color(0xFF9575CD))
        )
    }

    // Calculate prayer achievements
    val todayPrayers = completions.filter { it.dateString == todayStr && it.isCompleted && it.habitType.startsWith("NAMAZ_") }
    val todayPrayersCount = todayPrayers.size
    
    // Joint Namaz/prayer streak (any prayer prayed)
    val namazCompletions = completions.filter { it.habitType.startsWith("NAMAZ_") }
    val prayerStreak = viewModel.getHabitStreak("NAMAZ_FAJR") // We can use Fajr or show any active prayers streak

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Spiritual Habit banner (Editorial Deep Blue Theme)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                EditorialDeepBlue,
                                EditorialBlue
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "আত্মিক ও ধর্মীয় কল্যাণ",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "দৈনিক ইবাদত ও তিলাওয়াত",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "নিয়মিত নামাজ আদায় এবং কুরআন তিলাওয়াত আপনার হৃদয়ে শান্তি ও জীবনে বরকত বয়ে আনবে।",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 1. Five Waqt Prayer Tracker Widget
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "আজকের নামাজ ট্র্যাকার (Namaz Tracker)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EditorialDeepBlue
                            )
                            Text(
                                text = "আপনার প্রতিদিনের পাঁচ ওয়াক্ত নামাজ রেকর্ড করুন।",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Streaks / Count
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(EditorialLightBlue)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "সম্পন্ন: $todayPrayersCount/৫",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EditorialDeepBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress Track for prayers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(EditorialGray)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(todayPrayersCount / 5f)
                                .clip(CircleShape)
                                .background(EditorialProgressBlue)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Waqt Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        prayersList.forEach { prayer ->
                            val isPrayed = completions.any { it.habitType == prayer.type && it.dateString == todayStr && it.isCompleted }
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.toggleHabit(prayer.type, todayStr) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isPrayed) prayer.color else EditorialGray)
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isPrayed) prayer.color else Color.LightGray.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isPrayed) Icons.Default.Check else prayer.icon,
                                        contentDescription = prayer.title,
                                        tint = if (isPrayed) Color.White else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = prayer.shortName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isPrayed) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isPrayed) EditorialDeepBlue else Color.DarkGray
                                )
                                Text(
                                    text = prayer.timeOfDay,
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Quran Recitation Widget
        item {
            val quranCompletion = completions.find { it.habitType == "QURAN_RECITE" && it.dateString == todayStr }
            val isRecited = quranCompletion?.isCompleted ?: false
            val currentPages = quranCompletion?.intakeValue ?: 0
            val quranStreak = viewModel.getHabitStreak("QURAN_RECITE")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "কুরআন তিলাওয়াত (Quran Recitation)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialDeepBlue
                                )
                                Text(
                                    text = "প্রতিদিন অর্থসহ কুরআন তিলাওয়াত করুন।",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // QURAN STREAK
                        if (quranStreak > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "$quranStreak দিন",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // RECITED CHECKBOX / TOGGLE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isRecited) Color(0xFFE8F5E9).copy(alpha = 0.5f) else EditorialGray)
                            .clickable { viewModel.toggleHabit("QURAN_RECITE", todayStr) }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(2.dp, Color(0xFF2E7D32), RoundedCornerShape(6.dp))
                                        .background(
                                            if (isRecited) Color(0xFF2E7D32) else Color.Transparent,
                                            RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isRecited) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Recited",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "আজকে কুরআন তিলাওয়াত করেছি",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isRecited) Color(0xFF2E7D32) else EditorialDeepBlue
                                )
                            }
                            Text(
                                text = if (isRecited) "সম্পন্ন" else "বাকি আছে",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRecited) Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }

                    if (isRecited) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // QUANTITY SELECTOR (PAGES COUTNER)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "তিলাওয়াতকৃত পৃষ্ঠা সংখ্যা",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EditorialDeepBlue
                                )
                                Text(
                                    text = "তিলাওয়াতকৃত পরিমাণ নির্ধারণ করুন",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Decrement button
                                IconButton(
                                    onClick = { viewModel.changeQuranPages(todayStr, isIncrement = false) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(EditorialGray, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Decrement pages",
                                        modifier = Modifier.size(16.dp),
                                        tint = EditorialDeepBlue
                                    )
                                }

                                Text(
                                    text = "$currentPages পৃষ্ঠা",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF2E7D32)
                                )

                                // Increment button
                                IconButton(
                                    onClick = { viewModel.changeQuranPages(todayStr, isIncrement = true) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(Color(0xFFE8F5E9), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increment pages",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFF2E7D32)
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

// Data holder classes
data class PrayerDetail(
    val type: String,
    val title: String,
    val shortName: String,
    val icon: ImageVector,
    val color: Color,
    val timeOfDay: String = ""
)
