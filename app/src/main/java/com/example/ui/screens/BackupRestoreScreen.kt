package com.example.ui.screens

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.TaskViewModel

@Composable
fun BackupRestoreScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val message by viewModel.backupMessage.collectAsState()

    // Automatic clear of notifications
    LaunchedEffect(message) {
        if (message != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                viewModel.clearBackupMessage()
            }, 5000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "ডাটা ব্যাকআপ এবং পুনরুদ্ধার",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "আপনার সকল তথ্য আপনার ডিভাইসে সুরক্ষিত থাকে। ফোন পরিবর্তনের পূর্বে ডাটা ব্যাকআপ ফাইলটি সংরক্ষণ করুন।",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Notification alert slot
        if (message != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "", tint = MaterialTheme.colorScheme.secondary)
                    Text(text = message!!, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }

        // 1. Export locally
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "লোকাল ব্যাকআপ সঞ্চয় (Export)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "আপনার তৈরি সকল কাজ এবং দৈনন্দিন অভ্যাসের চিরস্থায়ী ডেটা একটি JSON ফাইল এ রূপান্তর করে সংরক্ষিত হবে।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.exportBackup(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = "")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ব্যাকআপ ফাইল ডাউনলোড করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Import locally
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.SettingsBackupRestore, contentDescription = null, tint = CompletedColor)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "ব্যাকআপ পুনরুদ্ধার করুন (Import)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "পূর্বে সংরক্ষিত ব্যাকআপ ফাইলটি ডিভাইস স্টোরেজ থেকে পড়ে আপনার ড্যাশবোর্ডে পুনরুদ্ধার করা হবে।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { viewModel.importBackup(context) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.3.dp, CompletedColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CompletedColor)
                ) {
                    Icon(imageVector = Icons.Default.Upload, contentDescription = "")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ব্যাকআপ ফাইল পুনরুদ্ধার করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
