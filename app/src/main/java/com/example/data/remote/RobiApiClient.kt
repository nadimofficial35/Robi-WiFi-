package com.example.data.remote

import com.example.data.model.SrReport
import com.example.data.model.UserProfile
import com.example.util.DatePhoneUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SyncAllResponse(
    val reports: List<SrReport>,
    val quizMap: Map<String, List<String>>,
    val users: List<UserProfile>
)

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

class RobiApiClient {
    companion object {
        const val WEB_APP_URL =
            "https://script.google.com/macros/s/AKfycbxEucCJhbOlfO5kKuIQVkdGruOQGkmKqKWtfkefxWoz8-j3H3MdaXMHaaKu3x8IPujD/exec"
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val textMediaType = "text/plain; charset=utf-8".toMediaType()

    suspend fun syncAll(): ApiResult<SyncAllResponse> = withContext(Dispatchers.IO) {
        try {
            val url = WEB_APP_URL.toHttpUrl().newBuilder()
                .addQueryParameter("action", "syncAll")
                .addQueryParameter("t", System.currentTimeMillis().toString())
                .build()
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                if (json.optString("status") == "success") {
                    val reports = parseReports(json.optJSONArray("reports"))
                    val quizMap = parseQuizMap(json.optJSONObject("quizMap"))
                    val users = parseUsers(json.optJSONArray("users"))
                    ApiResult.Success(SyncAllResponse(reports, quizMap, users))
                } else {
                    ApiResult.Error(json.optString("message", "Sync failed"))
                }
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    suspend fun login(rawNumber: String, password: String): ApiResult<UserProfile> =
        withContext(Dispatchers.IO) {
            val normNumber = DatePhoneUtils.formatPhoneNumber(rawNumber)
            val trimmedPass = password.trim()
            var serverMessage = "Number অথবা Password ভুল!"

            try {
                val url = WEB_APP_URL.toHttpUrl().newBuilder()
                    .addQueryParameter("action", "login")
                    .addQueryParameter("number", normNumber)
                    .addQueryParameter("password", trimmedPass)
                    .addQueryParameter("t", System.currentTimeMillis().toString())
                    .build()
                val request = Request.Builder().url(url).get().build()
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    if (json.optString("status") == "success") {
                        val userObj = json.optJSONObject("user")
                        if (userObj != null) {
                            return@withContext ApiResult.Success(parseUser(userObj))
                        }
                    } else if (json.has("message")) {
                        serverMessage = json.optString("message", serverMessage)
                    }
                }
            } catch (_: Exception) {
                // Fall through to syncAll user verification
            }

            // Fallback: verify against syncAll users list (matches original app behavior)
            when (val syncResult = syncAll()) {
                is ApiResult.Success -> {
                    val matched = syncResult.data.users.firstOrNull { u ->
                        DatePhoneUtils.formatPhoneNumber(u.number) == normNumber &&
                            u.password.trim() == trimmedPass
                    }
                    if (matched != null) {
                        ApiResult.Success(matched)
                    } else {
                        ApiResult.Error(serverMessage)
                    }
                }
                is ApiResult.Error -> {
                    ApiResult.Error("Connection Error! ইন্টারনেট বা সার্ভার চেক করুন।")
                }
            }
        }

    suspend fun changePassword(
        idNumber: String,
        oldPassword: String,
        newPassword: String
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            val url = WEB_APP_URL.toHttpUrl().newBuilder()
                .addQueryParameter("action", "changePassword")
                .addQueryParameter("idNumber", idNumber)
                .addQueryParameter("oldPassword", oldPassword)
                .addQueryParameter("newPassword", newPassword)
                .build()
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                if (json.optString("status") == "success") {
                    ApiResult.Success(json.optString("message", "Password updated successfully!"))
                } else {
                    ApiResult.Error(json.optString("message", "Password update failed!"))
                }
            }
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message ?: "Network error"}")
        }
    }

    suspend fun submitBindIssue(
        idNumber: String,
        issueType: String,
        extraInfo: String,
        orderNumber: String,
        loginNumber: String,
        fwaNumber: String
    ): Boolean {
        val payload = JSONObject().apply {
            put("type", "bindIssue")
            put("idNumber", idNumber)
            put("issueType", issueType)
            put("extraInfo", extraInfo)
            put("orderNumber", orderNumber)
            put("loginNumber", loginNumber)
            put("fwaNumber", fwaNumber)
        }
        return postPayload(payload)
    }

    suspend fun submitComplineReport(
        idNumber: String,
        srId: String,
        status: String,
        newDate: String,
        comment: String,
        supervisor: String
    ): Boolean {
        val payload = JSONObject().apply {
            put("type", "report")
            put("idNumber", idNumber)
            put("srId", srId)
            put("status", status)
            put("newDate", newDate)
            put("comment", comment)
            put("supervisor", supervisor)
        }
        return postPayload(payload)
    }

    suspend fun submitQuiz(
        idNumber: String,
        answers: List<String>
    ): Boolean {
        val arr = JSONArray()
        answers.forEach { arr.put(it) }
        val payload = JSONObject().apply {
            put("type", "quiz")
            put("idNumber", idNumber)
            put("answers", arr)
        }
        return postPayload(payload)
    }

    private suspend fun postPayload(payload: JSONObject): Boolean = withContext(Dispatchers.IO) {
        try {
            val body = payload.toString().toRequestBody(textMediaType)
            val request = Request.Builder()
                .url(WEB_APP_URL)
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful || response.code in 300..399
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun parseUser(obj: JSONObject): UserProfile {
        return UserProfile(
            idNumber = obj.optString("idNumber", ""),
            bdoName = obj.optString("bdoName", ""),
            number = obj.optString("number", ""),
            password = obj.optString("password", ""),
            wic = obj.optString("wic", ""),
            supervisor = obj.optString("supervisor", "")
        )
    }

    private fun parseUsers(arr: JSONArray?): List<UserProfile> {
        if (arr == null) return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(parseUser(obj))
            }
        }
    }

    private fun parseReports(arr: JSONArray?): List<SrReport> {
        if (arr == null) return emptyList()
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                add(
                    SrReport(
                        dateTime = obj.optString("dateTime", ""),
                        dateKey = obj.optString("dateKey", ""),
                        idNumber = obj.optString("idNumber", ""),
                        bdoName = obj.optString("bdoName", ""),
                        srId = obj.optString("srId", ""),
                        status = obj.optString("status", ""),
                        nextDate = obj.optString("nextDate", ""),
                        comment = obj.optString("comment", ""),
                        supervisor = obj.optString("supervisor", "")
                    )
                )
            }
        }
    }

    private fun parseQuizMap(obj: JSONObject?): Map<String, List<String>> {
        if (obj == null) return emptyMap()
        val result = mutableMapOf<String, List<String>>()
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val arr = obj.optJSONArray(key) ?: continue
            val answers = buildList {
                for (i in 0 until arr.length()) {
                    add(arr.optString(i, ""))
                }
            }
            result[key] = answers
        }
        return result
    }
}
