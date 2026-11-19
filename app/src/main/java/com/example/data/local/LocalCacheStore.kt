package com.example.data.local

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.SrReport
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

private val Context.dataStore by preferencesDataStore(name = "robi_wifi_store")

class LocalCacheStore(private val context: Context) {

    private object Keys {
        val CURRENT_USER = stringPreferencesKey("robi_user")
        val REPORTS = stringPreferencesKey("robi_cache_reports")
        val QUIZ_MAP = stringPreferencesKey("robi_cache_quiz")
        val USERS = stringPreferencesKey("robi_cache_users")
    }

    private fun photoKey(idNumber: String) =
        stringPreferencesKey("robi_user_photo_${idNumber.trim()}")

    suspend fun getCurrentUser(): UserProfile? {
        val prefs = context.dataStore.data.first()
        val raw = prefs[Keys.CURRENT_USER] ?: return null
        return parseUser(raw)
    }

    suspend fun saveCurrentUser(user: UserProfile?) {
        context.dataStore.edit { prefs ->
            if (user == null) {
                prefs.remove(Keys.CURRENT_USER)
            } else {
                prefs[Keys.CURRENT_USER] = userToJson(user).toString()
            }
        }
    }

    suspend fun getCachedReports(): List<SrReport> {
        val prefs = context.dataStore.data.first()
        val raw = prefs[Keys.REPORTS] ?: return emptyList()
        return parseReports(raw)
    }

    suspend fun saveCachedReports(reports: List<SrReport>) {
        val array = JSONArray()
        reports.forEach { r ->
            array.put(JSONObject().apply {
                put("dateTime", r.dateTime)
                put("dateKey", r.dateKey)
                put("idNumber", r.idNumber)
                put("bdoName", r.bdoName)
                put("srId", r.srId)
                put("status", r.status)
                put("nextDate", r.nextDate)
                put("comment", r.comment)
                put("supervisor", r.supervisor)
            })
        }
        context.dataStore.edit { it[Keys.REPORTS] = array.toString() }
    }

    suspend fun getCachedQuizMap(): Map<String, List<String>> {
        val prefs = context.dataStore.data.first()
        val raw = prefs[Keys.QUIZ_MAP] ?: return emptyMap()
        return parseQuizMap(raw)
    }

    suspend fun saveCachedQuizMap(quizMap: Map<String, List<String>>) {
        val obj = JSONObject()
        quizMap.forEach { (key, answers) ->
            val arr = JSONArray()
            answers.forEach { arr.put(it) }
            obj.put(key, arr)
        }
        context.dataStore.edit { it[Keys.QUIZ_MAP] = obj.toString() }
    }

    suspend fun getCachedUsers(): List<UserProfile> {
        val prefs = context.dataStore.data.first()
        val raw = prefs[Keys.USERS] ?: return emptyList()
        return parseUsers(raw)
    }

    suspend fun saveCachedUsers(users: List<UserProfile>) {
        val arr = JSONArray()
        users.forEach { arr.put(userToJson(it)) }
        context.dataStore.edit { it[Keys.USERS] = arr.toString() }
    }

    suspend fun getUserPhotoPath(idNumber: String): String? {
        if (idNumber.isBlank()) return null
        val prefs = context.dataStore.data.first()
        val path = prefs[photoKey(idNumber)] ?: return null
        return if (File(path).exists()) path else null
    }

    suspend fun saveUserPhotoFromUri(idNumber: String, uri: Uri): String? =
        withContext(Dispatchers.IO) {
            try {
                val avatarsDir = File(context.filesDir, "avatars").apply { mkdirs() }
                val safeId = idNumber.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
                val targetFile = File(avatarsDir, "avatar_${safeId}_${System.currentTimeMillis()}.jpg")
                // Remove old avatars for this user
                avatarsDir.listFiles()?.filter { it.name.startsWith("avatar_${safeId}_") }
                    ?.forEach { it.delete() }

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext null

                context.dataStore.edit { prefs ->
                    prefs[photoKey(idNumber)] = targetFile.absolutePath
                }
                targetFile.absolutePath
            } catch (_: Exception) {
                null
            }
        }

    private fun userToJson(user: UserProfile): JSONObject {
        return JSONObject().apply {
            put("idNumber", user.idNumber)
            put("bdoName", user.bdoName)
            put("number", user.number)
            put("password", user.password)
            put("wic", user.wic)
            put("supervisor", user.supervisor)
        }
    }

    private fun parseUser(raw: String): UserProfile? {
        return try {
            val obj = JSONObject(raw)
            val id = obj.optString("idNumber", "")
            if (id.isBlank()) return null
            UserProfile(
                idNumber = id,
                bdoName = obj.optString("bdoName", ""),
                number = obj.optString("number", ""),
                password = obj.optString("password", ""),
                wic = obj.optString("wic", ""),
                supervisor = obj.optString("supervisor", "")
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseUsers(raw: String): List<UserProfile> {
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    add(
                        UserProfile(
                            idNumber = obj.optString("idNumber", ""),
                            bdoName = obj.optString("bdoName", ""),
                            number = obj.optString("number", ""),
                            password = obj.optString("password", ""),
                            wic = obj.optString("wic", ""),
                            supervisor = obj.optString("supervisor", "")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseReports(raw: String): List<SrReport> {
        return try {
            val arr = JSONArray(raw)
            buildList {
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
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseQuizMap(raw: String): Map<String, List<String>> {
        return try {
            val obj = JSONObject(raw)
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
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
