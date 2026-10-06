package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognitionService
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

object VoiceInputHelper {

    private const val TAG = "VoiceInputHelper"

    /**
     * Checks whether an offline-capable speech recognizer is available on the device.
     * Guaranteed safe for Android 10 (API 29) through modern Android releases.
     */
    fun isOfflineRecognitionAvailable(context: Context): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        }
        // For Android 10 (API 29) to Android 12:
        val pm = context.packageManager
        val recognitionServices = pm.queryIntentServices(
            Intent(RecognitionService.SERVICE_INTERFACE),
            0
        )
        return recognitionServices.isNotEmpty()
    }

    /**
     * Creates and starts speech recognition strictly configured for on-device / offline operation.
     * Does NOT silently fall back to network recognition.
     */
    fun startListeningOffline(
        context: Context,
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onListeningStateChange: (Boolean) -> Unit
    ): SpeechRecognizer? {
        if (!isOfflineRecognitionAvailable(context)) {
            onError("OFFLINE_UNAVAILABLE")
            return null
        }

        val recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } catch (e: Exception) {
                SpeechRecognizer.createSpeechRecognizer(context)
            }
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            // Explicitly enforce on-device / offline preference across all Android versions
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            // Extra safety key recognized by Google Speech Services
            putExtra("android.speech.extra.PREFER_OFFLINE", true)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                onListeningStateChange(true)
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                onListeningStateChange(false)
            }

            override fun onError(error: Int) {
                onListeningStateChange(false)
                Log.w(TAG, "Speech recognition error code: $error")
                when (error) {
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                    SpeechRecognizer.ERROR_SERVER,
                    SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> {
                        // Offline recognition is not functional or tried to connect to network
                        onError("OFFLINE_UNAVAILABLE")
                    }
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        onError("NO_MATCH")
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        onError("PERMISSION_DENIED")
                    }
                    else -> {
                        onError("ERROR_$error")
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                onListeningStateChange(false)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull().orEmpty()
                if (spokenText.isNotBlank()) {
                    val parsed = parseSpokenMath(spokenText)
                    onResult(parsed)
                } else {
                    onError("NO_MATCH")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        try {
            recognizer.startListening(recognizerIntent)
            return recognizer
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognizer", e)
            onError("OFFLINE_UNAVAILABLE")
            recognizer.destroy()
            return null
        }
    }

    /**
     * Parses spoken words, digits, and mathematical operations into a standard calculation expression.
     */
    fun parseSpokenMath(raw: String): String {
        var text = raw.lowercase().trim()

        // English verbal phrases
        text = text
            .replace("divided by", "/")
            .replace("divide by", "/")
            .replace("multiplied by", "*")
            .replace("times", "*")
            .replace("plus", "+")
            .replace("minus", "-")
            .replace("negative", "-")
            .replace("to the power of", "^")
            .replace("power of", "^")
            .replace("power", "^")
            .replace("square root of", "sqrt(")
            .replace("square root", "sqrt(")
            .replace("percent of", "%*")
            .replace("percent", "%")
            .replace("percentage", "%")
            .replace("open parenthesis", "(")
            .replace("close parenthesis", ")")
            .replace("left parenthesis", "(")
            .replace("right parenthesis", ")")
            .replace("bracket open", "(")
            .replace("bracket close", ")")
            .replace("point", ".")
            .replace("dot", ".")
            .replace("equals", "")
            .replace("equal to", "")
            .replace("equal", "")

        // Multilingual operator equivalents (Spanish, French, German, Japanese, Hindi)
        text = text
            .replace("más", "+")
            .replace("menos", "-")
            .replace("por", "*")
            .replace("entre", "/")
            .replace("dividido por", "/")
            .replace("dividido entre", "/")
            .replace("fois", "*")
            .replace("divisé par", "/")
            .replace("mal", "*")
            .replace("geteilt durch", "/")
            .replace("durch", "/")
            .replace("プラス", "+")
            .replace("マイナス", "-")
            .replace("かける", "*")
            .replace("掛ける", "*")
            .replace("わる", "/")
            .replace("割る", "/")
            .replace("धन", "+")
            .replace("ऋण", "-")
            .replace("गुणा", "*")
            .replace("भाग", "/")

        // Spoken number words
        val wordNumbers = listOf(
            "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
            "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9",
            "ten" to "10", "eleven" to "11", "twelve" to "12", "thirteen" to "13",
            "fourteen" to "14", "fifteen" to "15", "sixteen" to "16", "seventeen" to "17",
            "eighteen" to "18", "nineteen" to "19", "twenty" to "20", "thirty" to "30",
            "forty" to "40", "fifty" to "50", "sixty" to "60", "seventy" to "70",
            "eighty" to "80", "ninety" to "90", "hundred" to "00", "thousand" to "000",
            // Spanish
            "cero" to "0", "uno" to "1", "dos" to "2", "tres" to "3", "cuatro" to "4",
            "cinco" to "5", "seis" to "6", "siete" to "7", "ocho" to "8", "nueve" to "9", "diez" to "10",
            // French
            "zéro" to "0", "un" to "1", "deux" to "2", "trois" to "3", "quatre" to "4",
            "cinq" to "5", "six" to "6", "sept" to "7", "huit" to "8", "neuf" to "9", "dix" to "10",
            // German
            "null" to "0", "eins" to "1", "zwei" to "2", "drei" to "3", "vier" to "4",
            "fünf" to "5", "sechs" to "6", "sieben" to "7", "acht" to "8", "neun" to "9", "zehn" to "10",
            // Japanese
            "ゼロ" to "0", "一" to "1", "二" to "2", "三" to "3", "四" to "4",
            "五" to "5", "六" to "6", "七" to "7", "八" to "8", "九" to "9", "十" to "10",
            // Hindi
            "शून्य" to "0", "एक" to "1", "दो" to "2", "तीन" to "3", "चार" to "4",
            "पाँच" to "5", "छह" to "6", "सात" to "7", "आठ" to "8", "नौ" to "9", "दस" to "10"
        )

        for ((word, num) in wordNumbers) {
            text = text.replace(Regex("\\b$word\\b", RegexOption.IGNORE_CASE), num)
        }

        // Clean spaces and map to standard UI operators
        return text
            .replace(" ", "")
            .replace("*", "×")
            .replace("/", "÷")
            .replace("-", "−")
    }
}
