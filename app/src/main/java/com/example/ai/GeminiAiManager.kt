package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@Serializable
data class GeminiPart(val text: String? = null)

@Serializable
data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
data class ThinkingConfig(val thinkingLevel: String? = null)

@Serializable
data class GenerationConfig(val thinkingConfig: ThinkingConfig? = null)

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class GeminiCandidate(val content: GeminiContent? = null)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class AiBeatResult(
    val bpm: Int = 120,
    val title: String = "AI Generated Beat",
    val patterns: Map<String, List<Boolean>> = emptyMap(),
    val advice: String = ""
)

object GeminiAiManager {
    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateBeatPattern(prompt: String): AiBeatResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalFallbackBeat(prompt)
        }

        val systemPrompt = """
            You are an expert electronic music producer and drum machine AI assistant.
            Generate a 16-step drum machine beat pattern based on the user request.
            Instruments available: KICK, SNARE, HIHAT, CLAP, BASS, SYNTH, PERC, FX.
            Respond strictly in valid JSON format matching this schema:
            {
              "bpm": 128,
              "title": "Short descriptive title",
              "patterns": {
                "KICK": [true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false],
                "SNARE": [false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false],
                "HIHAT": [true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false],
                "CLAP": [false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false],
                "BASS": [true, false, false, true, false, false, true, false, false, true, false, false, true, false, false, false],
                "SYNTH": [false, false, true, false, false, true, false, false, true, false, false, true, false, false, true, false],
                "PERC": [false, true, false, false, false, true, false, false, false, true, false, false, false, true, false, false],
                "FX": [true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false]
              },
              "advice": "1 short German sentence of production advice"
            }
            User Request: "${'$'}prompt"
        """.trimIndent()

        try {
            val reqBodyObj = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = systemPrompt)))),
                generationConfig = GenerationConfig(thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH"))
            )
            val jsonBody = json.encodeToString(GeminiRequest.serializer(), reqBodyObj)
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=${'$'}apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val parsed = json.decodeFromString(GeminiResponse.serializer(), responseString)
                val rawText = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                val cleanJson = extractJsonSubstring(rawText)
                if (cleanJson.isNotBlank()) {
                    return@withContext json.decodeFromString(AiBeatResult.serializer(), cleanJson)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext generateLocalFallbackBeat(prompt)
    }

    suspend fun getDjCopilotAdvice(trackTitle: String, currentBpm: Int, targetGenre: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "AI DJ Tipp: Nutze den [BASS KILL] Knopf vor dem Drop, um den Beat knackig in den Mix zu überführen!"
        }

        val prompt = """
            Du bist ein professioneller KI DJ Co-Pilot. Gib einen kurzen, prägnanten DJ-Tipp (max 2 Sätze auf Deutsch) für den Übergang beim Song "${'$'}trackTitle" (BPM: ${'$'}currentBpm, Genre: ${'$'}targetGenre). Erwähne gerne die Kill-Knöpfe (BASS KILL, MID KILL, HI KILL) oder den Crossfader!
        """.trimIndent()

        try {
            val reqBodyObj = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )
            val jsonBody = json.encodeToString(GeminiRequest.serializer(), reqBodyObj)
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=${'$'}apiKey"
            val request = Request.Builder().url(url).post(requestBody).build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful && responseString.isNotBlank()) {
                val parsed = json.decodeFromString(GeminiResponse.serializer(), responseString)
                val rawText = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawText.isNullOrBlank()) {
                    return@withContext rawText.trim()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext "AI DJ Co-Pilot: Nutze [BASS KILL] beim nächsten Breakout, um den Übergang makellos zu gestalten!"
    }

    private fun extractJsonSubstring(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        return if (start != -1 && end != -1 && end > start) {
            text.substring(start, end + 1)
        } else ""
    }

    private fun String?.isNullAndBlank(): Boolean = this == null || this.isBlank()

    private fun generateLocalFallbackBeat(prompt: String): AiBeatResult {
        val lower = prompt.lowercase()
        val isFast = lower.contains("techno") || lower.contains("fast") || lower.contains("drum")
        val isHipHop = lower.contains("hip") || lower.contains("lofi") || lower.contains("trap")

        val bpm = if (isFast) 130 else if (isHipHop) 92 else 124

        val kick = listOf(true, false, false, false, true, false, false, false, true, false, false, false, true, false, false, false)
        val snare = listOf(false, false, false, false, true, false, false, false, false, false, false, false, true, false, false, false)
        val hihat = listOf(true, false, true, false, true, false, true, false, true, false, true, false, true, false, true, false)
        val clap = listOf(false, false, false, false, true, false, false, false, false, false, false, false, true, false, true, false)
        val bass = listOf(true, false, true, false, false, true, false, false, true, false, true, false, false, true, false, false)
        val synth = listOf(false, false, true, false, false, false, true, false, false, false, true, false, false, false, true, false)

        return AiBeatResult(
            bpm = bpm,
            title = "AI Beat Pattern (${'$'}{prompt.take(16)})",
            patterns = mapOf(
                "KICK" to kick,
                "SNARE" to snare,
                "HIHAT" to hihat,
                "CLAP" to clap,
                "BASS" to bass,
                "SYNTH" to synth
            ),
            advice = "Lokale KI-Heuristik aktiv: Drücke [BASS KILL], um Platz für die Kickline zu schaffen."
        )
    }
}
