package com.example.service

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.android.gms.ads.*

object AdManager {
    private const val TAG = "AdManager"

    // PLACEHOLDERS / AD UNIT IDS - CONFIG FOR PRODUCTION
    const val ADMOB_APP_ID = "ca-app-pub-2234027476476158~2228995516"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-2234027476476158/6220211417"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2234027476476158/6220211417"
    const val NATIVE_AD_UNIT_ID = "ca-app-pub-2234027476476158/6220211417"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-2234027476476158/6220211417"

    // State flows to control the overlay display of interstitial and rewarded ads in Jetpack Compose
    private val _showInterstitialAdFlow = MutableStateFlow(false)
    val showInterstitialAdFlow = _showInterstitialAdFlow.asStateFlow()

    private val _showRewardedAdFlow = MutableStateFlow(false)
    val showRewardedAdFlow = _showRewardedAdFlow.asStateFlow()
    
    private var rewardedOnSuccess: (() -> Unit)? = null

    fun initAdMob(context: Context) {
        Log.d(TAG, "AdMob fully initialized under APP ID: $ADMOB_APP_ID")
        try {
            MobileAds.initialize(context) {}
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize real MobileAds SDK", e)
        }
    }

    fun showInterstitialAd() {
        Log.d(TAG, "Loading Interstitial Ad: $INTERSTITIAL_AD_UNIT_ID")
        _showInterstitialAdFlow.value = true
    }

    fun hideInterstitialAd() {
        _showInterstitialAdFlow.value = false
    }

    fun showRewardedAd(onCompleted: () -> Unit) {
        Log.d(TAG, "Loading Rewarded Ad: $REWARDED_AD_UNIT_ID")
        rewardedOnSuccess = onCompleted
        _showRewardedAdFlow.value = true
    }

    fun hideRewardedAd(claimReward: Boolean) {
        _showRewardedAdFlow.value = false
        if (claimReward) {
            rewardedOnSuccess?.invoke()
        }
        rewardedOnSuccess = null
    }
}

// --- BEAUTIFUL COMPOSABLE AD PLACEMENT WIDGETS ---

@Composable
fun BannerAdWidget(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "স্পনসরড বিজ্ঞাপন / AD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "AdMob Live",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Real Google Mobile Ads (AdMob) Banner
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = AdManager.BANNER_AD_UNIT_ID
                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Log.e("AdMob", "Ad failed to load: ${error.message}")
                            }
                            override fun onAdLoaded() {
                                Log.d("AdMob", "Ad loaded successfully!")
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { adView ->
                    // Active reload is managed by AdMob internally
                }
            )
        }
    }
}

@Composable
fun NativeAdWidget(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("স্পনসরড / SPONSORED", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                }
                Text("BKash Payment Offers", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "টাস্ক মাস্টার প্রো এক্টিভ করুন মাত্র ১০ টাকা রিচার্জে! বিকাশ পেমেন্টে পাচ্ছেন মেগা ক্যাশব্যাক সুবিধা। বিশদ জানতে ক্লিক করুন।",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { /* Simulated Ad Click */ },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("অনলাইন পেমেন্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun InterstitialAdOverlay() {
    val showAd by AdManager.showInterstitialAdFlow.collectAsState()
    
    if (showAd) {
        AlertDialog(
            onDismissRequest = { AdManager.hideInterstitialAd() },
            confirmButton = {},
            dismissButton = {
                IconButton(
                    onClick = { AdManager.hideInterstitialAd() },
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Ad", tint = Color.White)
                }
            },
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = Color.LightGray) {
                            Text("ভিডিও বিজ্ঞাপন", fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.padding(2.dp))
                        }
                        Text("পূরক ইন্টারেস্টিং লিঙ্ক", fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Ad icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Task Master BD VIP!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "একটি অ্যাপের মাধ্যমে সকল কাজ পরিচালনা করুন সহজে এবং নিরাপদে। ক্লাউড সিঙ্ক ফিচার অতি শীঘ্রই যুক্ত হচ্ছে!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { AdManager.hideInterstitialAd() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("বিস্তারিত দেখুন")
                    }
                }
            }
        )
    }
}

@Composable
fun RewardedAdOverlay(onRewardClaimed: () -> Unit) {
    val showAd by AdManager.showRewardedAdFlow.collectAsState()
    var timerSeconds by remember { mutableStateOf(5) }

    LaunchedEffect(showAd) {
        if (showAd) {
            timerSeconds = 5
            while (timerSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                timerSeconds--
            }
        }
    }

    if (showAd) {
        AlertDialog(
            onDismissRequest = { /* Force watch to get reward */ },
            confirmButton = {},
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("পুরস্কারের বিজ্ঞাপন", fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                        }
                        Text(
                            text = if (timerSeconds > 0) "পুরস্কার পেতে অপেক্ষা করুন: $timerSeconds সেকেন্ড" else "পুরস্কার প্রস্তুত!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Reward Box",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(80.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ভিআইপি এআই কোড আনলক!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "সম্পূর্ণ বিজ্ঞাপনটি দেখলে আপনার আজকের দৈনিক বোনাস টোকেন যুক্ত হবে এবং সব এআই অ্যানালিটিক্স রিপোর্ট উন্মুক্ত হবে।",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { AdManager.hideRewardedAd(claimReward = false) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("বাতিল করুন", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                AdManager.hideRewardedAd(claimReward = true)
                                onRewardClaimed()
                            },
                            enabled = timerSeconds == 0,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("রিওয়ার্ড নিন", fontSize = 12.sp)
                        }
                    }
                }
            }
        )
    }
}
