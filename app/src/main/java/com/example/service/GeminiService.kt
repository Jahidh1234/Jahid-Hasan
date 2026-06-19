package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    // OkHttpClient with 60s timeouts as recommended by gemini-api skill
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateAnalyticsInsights(
        tasks: List<TaskEntity>,
        completedCount: Int,
        pendingCount: Int,
        overdueCount: Int,
        completionRate: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or uses default template key.")
            return@withContext """
                ⚠️ এআই উইজেট নিষ্ক্রিয়! 
                
                জেমিনি এআই অ্যানালিটিক্স সুবিধা পেতে গুগল এআই স্টুডিওর (AI Studio Secrets Panel) সিক্রেটস প্যানেলে আপনার 'GEMINI_API_KEY' যুক্ত করুন। 
                
                আপনার কাজের সংক্ষিপ্ত সারসংক্ষেপ:
                • মোট কাজ: ${tasks.size}
                • সম্পন্ন কাজ: $completedCount
                • অসমাপ্ত কাজ: $pendingCount
                • ওভারডিউ (সময় উত্তীর্ণ): $overdueCount
                • লক্ষ্য অর্জনের হার: $completionRate%
            """.trimIndent()
        }

        // Build history data for the prompt
        val historyDetails = StringBuilder()
        tasks.take(20).forEach { task ->
            val status = if (task.isCompleted) "সম্পন্ন" else "অসমাপ্ত"
            val delay = if (task.delayMinutes > 0) "বিলম্ব: ${task.delayMinutes} মিনিট" else "কোনো বিলম্ব নেই"
            historyDetails.append("- ${task.title} (ক্যাটাগরি: ${task.category}, অগ্রাধিকার: ${task.priority}, অবস্থা: $status, $delay)\n")
        }

        val prompt = """
            You are "Task Master BD AI Assistant", a friendly, premium productivity coach specializing in task analytics.
            Generate a personalized, highly motivational and actionable weekly/monthly productivity review in Bengali based on the user's data provided below.
            
            USER DATA:
            - Total Registered Tasks: ${tasks.size}
            - Completed Tasks: $completedCount
            - Pending Tasks: $pendingCount
            - Overdue (Overdue Tasks): $overdueCount
            - Task Completion Percentage: $completionRate%
            
            REPRESENTATIVE TASKS LOG:
            $historyDetails
            
            PROVIDE AN OUTSTANDING, RICH FORMATTED REPORT IN BENGALI WITH THE FOLLOWING SECTIONS:
            1. **সামগ্রিক অগ্রগতি বিশ্লেষণ** - Focus on the completionRate and what it means (e.g., exemplary, average, or needs focus). Speak in warm coach-like Bengali.
            2. **বিলম্বিত কাজের প্যাটার্ন** - Identify which tasks got delayed and suggest practical time management tricks (e.g. Pomodoro, Eisenhower matrix) in Bengali.
            3. **অগ্রাধিকারের ভিত্তিতে পরামর্শ** - Recommendations based on priority levels (High vs low priority).
            4. **উৎপাদনশীলতার স্পেশাল টিপ** - Offer a custom motivational Bengali slogan or productivity secret to energize the user's day.
            
            Keep the tone deeply encouraging, professional, and visually formatted with markdown bullets and bold sections. Do not mention system developer logs.
        """.trimIndent()

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBodyJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "Response failed: code=${response.code}, body=$errorBody")
                    return@withContext "দুঃখিত, জেমিনি এআই (Gemini AI) থেকে রেসপন্স পেতে ব্যর্থ হয়েছে। অনুগ্রহ করে আপনার ইন্টারনেট সংযোগ অথবা সিক্রেট এপিআই চাবি (API Key) সঠিকতা যাচাই করে পুনরায় চেষ্টা করুন।"
                }

                val responseBody = response.body?.string() ?: return@withContext "কোনো ডাটা রিটার্ন হয়নি!"
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    if (contentObj != null) {
                        val parts = contentObj.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "বিশ্লেষণ করা সম্ভব হয়নি।")
                        }
                    }
                }
                "সঠিক রেসপন্স ক্যাশ করা যায়নি।"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
            "অ্যানালিটিক্স রিপোর্ট জেনারেট করার সময় একটি ত্রুটি ঘটেছে: ${e.localizedMessage}"
        }
    }

    suspend fun askGeminiQuestion(question: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "⚠️ এআই উইজেট নিষ্ক্রিয়! জেমিনি এআই ব্যবহার করতে গুগল এআই স্টুডিওর (AI Studio Secrets Panel) সিক্রেটস প্যানেলে আপনার 'GEMINI_API_KEY' যুক্ত করুন।"
        }

        val prompt = """
            You are "Task Master BD AI Assistant", a friendly Islamic and branch productivity coach in Bangladesh.
            Respond gracefully, helpfully and concisely to the following user question in Bengali:
            
            USER QUESTION:
            $question
            
            Keep the tone warm, respectful, motivational, and answer in clear Bengali using markdown bullets if needed.
        """.trimIndent()

        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBodyJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "Response failed: code=${response.code}, body=$errorBody")
                    return@withContext "দুঃখিত, জেমিনি এআই (Gemini AI) থেকে রেসপন্স পেতে ব্যর্থ হয়েছে।"
                }

                val responseBody = response.body?.string() ?: return@withContext "কোনো ডাটা রিটার্ন হয়নি!"
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    if (contentObj != null) {
                        val parts = contentObj.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "উত্তর প্রস্তুত করা সম্ভব হয়নি।")
                        }
                    }
                }
                "সঠিক রেসপন্স ক্যাশ করা যায়নি।"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error asking Gemini: ${e.message}", e)
            "উত্তর প্রস্তুত করার সময় একটি ত্রুটি ঘটেছে: ${e.localizedMessage}"
        }
    }
}
