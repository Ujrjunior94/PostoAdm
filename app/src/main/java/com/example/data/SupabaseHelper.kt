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

object SupabaseHelper {
    private const val TAG = "SupabaseHelper"
    private const val PREFS_NAME = "SupabasePrefs"
    private const val KEY_URL = "supabase_url"
    private const val KEY_ANON = "supabase_anon_key"

    fun saveCredentials(context: Context, url: String, anonKey: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_URL, url.trim().trimEnd('/'))
            .putString(KEY_ANON, anonKey.trim())
            .apply()
    }

    fun getCredentials(context: Context): Pair<String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val url = prefs.getString(KEY_URL, "") ?: ""
        val key = prefs.getString(KEY_ANON, "") ?: ""
        if (url.isEmpty() || key.isEmpty()) {
            return Pair("https://fgogbzscfqkwlqazugkx.supabase.co", "sb_publishable_Zp5YN0CWKy0nV8Ku5C7ALw_uqMe6HUG")
        }
        return Pair(url, key)
    }

    fun clearCredentials(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun isConfigured(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val url = prefs.getString(KEY_URL, "") ?: ""
        val key = prefs.getString(KEY_ANON, "") ?: ""
        // Only considered configured if the user has entered their own credentials
        return url.isNotEmpty() && key.isNotEmpty()
    }

    suspend fun uploadBackup(context: Context, cnpj: String, backupJson: String): Result<Unit> = withContext(Dispatchers.IO) {
        val (supabaseUrl, anonKey) = getCredentials(context)
        if (supabaseUrl.isEmpty() || anonKey.isEmpty()) {
            return@withContext Result.failure(Exception("Supabase não configurado. Por favor, insira as credenciais."))
        }

        try {
            val endpoint = "$supabaseUrl/rest/v1/posto_backups"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("apikey", anonKey)
            conn.setRequestProperty("Authorization", "Bearer $anonKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Prefer", "resolution=merge-duplicates")
            conn.doOutput = true

            // Supabase REST endpoint expects an array of objects or a single object.
            // We'll wrap the upsert payload in an object containing primary key 'id' and the 'data' payload.
            val payload = JSONObject().apply {
                put("id", cnpj)
                put("data", JSONObject(backupJson)) // Parse the backup as actual JSON object for rich JSONB queries in Supabase
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
                Log.e(TAG, "Erro de backup no Supabase: $errorMsg")
                Result.failure(Exception("Supabase retornou erro ($responseCode): $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao fazer backup no Supabase", e)
            Result.failure(e)
        }
    }

    suspend fun downloadBackup(context: Context, cnpj: String): Result<String?> = withContext(Dispatchers.IO) {
        val (supabaseUrl, anonKey) = getCredentials(context)
        if (supabaseUrl.isEmpty() || anonKey.isEmpty()) {
            return@withContext Result.failure(Exception("Supabase não configurado. Por favor, insira as credenciais."))
        }

        try {
            // Retrieve only the 'data' field for the specific CNPJ
            val endpoint = "$supabaseUrl/rest/v1/posto_backups?id=eq.${cnpj}&select=data"
            val url = URL(endpoint)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("apikey", anonKey)
            conn.setRequestProperty("Authorization", "Bearer $anonKey")
            conn.setRequestProperty("Content-Type", "application/json")

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = conn.inputStream.use { stream ->
                    BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }
                }

                val jsonArray = JSONArray(responseText)
                if (jsonArray.length() > 0) {
                    val firstRow = jsonArray.getJSONObject(0)
                    val dataObj = firstRow.optJSONObject("data")
                    if (dataObj != null) {
                        Result.success(dataObj.toString())
                    } else {
                        Result.success(null)
                    }
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
                Log.e(TAG, "Erro de download no Supabase: $errorMsg")
                Result.failure(Exception("Supabase retornou erro ($responseCode): $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exceção ao recuperar backup do Supabase", e)
            Result.failure(e)
        }
    }
}
