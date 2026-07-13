package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object VercelHelper {
    private const val TAG = "VercelHelper"
    private const val PREFS_NAME = "VercelPrefs"
    private const val KEY_URL = "vercel_url"
    private const val KEY_TOKEN = "vercel_token"

    fun saveCredentials(context: Context, url: String, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_URL, url.trim().trimEnd('/'))
            .putString(KEY_TOKEN, token.trim())
            .apply()
    }

    fun getCredentials(context: Context): Pair<String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val url = prefs.getString(KEY_URL, "") ?: ""
        val token = prefs.getString(KEY_TOKEN, "") ?: ""
        return Pair(url, token)
    }

    fun clearCredentials(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun isConfigured(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val url = prefs.getString(KEY_URL, "") ?: ""
        return url.isNotEmpty()
    }

    suspend fun uploadBackup(context: Context, cnpj: String, backupJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        val (vercelUrl, token) = getCredentials(context)
        if (vercelUrl.isEmpty()) {
            return@withContext Result.failure(Exception("Plataforma Vercel não configurada. Por favor, insira a URL."))
        }

        try {
            // If the URL already looks like a full endpoint, use it. Otherwise, assume /api/backup.
            val endpoint = if (vercelUrl.contains("/api/")) vercelUrl else "${vercelUrl.trimEnd('/')}/api/backup"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            if (token.isNotEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true

            val payload = JSONObject().apply {
                put("id", cnpj)
                put("cnpj", cnpj)
                put("data", JSONObject(backupJson))
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                Result.success(Unit)
            } else {
                val errorStream = conn.errorStream
                val errorMsg = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream, "UTF-8")).use { it.readText() }
                } else {
                    "Código de resposta: $responseCode"
                }
                Log.e(TAG, "Erro de backup no Vercel: $errorMsg")
                Result.failure(Exception("Vercel retornou erro ($responseCode): $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao fazer backup no Vercel", e)
            Result.failure(e)
        }
    }

    suspend fun downloadBackup(context: Context, cnpj: String): Result<String?> = withContext(Dispatchers.IO) {
        val (vercelUrl, token) = getCredentials(context)
        if (vercelUrl.isEmpty()) {
            return@withContext Result.failure(Exception("Plataforma Vercel não configurada. Por favor, insira a URL."))
        }

        try {
            val endpoint = if (vercelUrl.contains("/api/")) {
                if (vercelUrl.contains("?")) "$vercelUrl&cnpj=$cnpj" else "$vercelUrl?cnpj=$cnpj"
            } else {
                "${vercelUrl.trimEnd('/')}/api/backup?cnpj=$cnpj"
            }
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            if (token.isNotEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer $token")
            }
            conn.setRequestProperty("Content-Type", "application/json")

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = conn.inputStream.use { stream ->
                    BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
                }

                if (responseText.trim().startsWith("[")) {
                    val arr = JSONArray(responseText)
                    if (arr.length() > 0) {
                        val obj = arr.getJSONObject(0)
                        val dataObj = obj.optJSONObject("data") ?: obj
                        Result.success(dataObj.toString())
                    } else {
                        Result.success(null)
                    }
                } else if (responseText.trim().startsWith("{")) {
                    val obj = JSONObject(responseText)
                    val dataObj = obj.optJSONObject("data") ?: obj
                    Result.success(dataObj.toString())
                } else {
                    Result.success(null)
                }
            } else {
                val errorStream = conn.errorStream
                val errorMsg = if (errorStream != null) {
                    BufferedReader(InputStreamReader(errorStream, "UTF-8")).use { it.readText() }
                } else {
                    "Código de resposta: $responseCode"
                }
                Log.e(TAG, "Erro de download no Vercel: $errorMsg")
                Result.failure(Exception("Vercel retornou erro ($responseCode): $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao recuperar backup do Vercel", e)
            Result.failure(e)
        }
    }
}
