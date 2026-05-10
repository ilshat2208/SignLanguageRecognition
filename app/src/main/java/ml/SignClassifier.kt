package com.example.signlanguage.ml

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.framework.image.BitmapImageBuilder

data class ClassificationResult(
    val label: String,
    val confidence: Float
)

class SignClassifier(private val context: Context) {

    private var gestureRecognizer: GestureRecognizer? = null

    init {
        setupRecognizer()
    }

    private fun setupRecognizer() {
        try {
            println("🔄 Загружаем модель gesture_recognizer.task...")

            // Проверяем наличие файла
            val fileSize = context.assets.open("gesture_recognizer.task").use {
                it.available()
            }
            println("📦 Размер модели: ${fileSize / 1024} KB")

            // ✅ ПРАВИЛЬНЫЙ СПОСОБ: создаем BaseOptions с моделью
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("gesture_recognizer.task")
                .build()

            println("✅ BaseOptions создан")

            // Создаем опции с BaseOptions
            val options = GestureRecognizer.GestureRecognizerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(1)
                .build()

            println("✅ Options созданы, создаем GestureRecognizer...")

            gestureRecognizer = GestureRecognizer.createFromOptions(context, options)

            if (gestureRecognizer != null) {
                println("✅✅✅ GestureRecognizer успешно создан!")
            } else {
                println("❌ GestureRecognizer = null")
            }

        } catch (e: Exception) {
            println("❌ Ошибка загрузки модели: ${e.message}")
            e.printStackTrace()
        }
    }

    fun classify(bitmap: Bitmap): ClassificationResult {
        if (gestureRecognizer == null) {
            return ClassificationResult("Модель не загружена", 0f)
        }

        try {
            // Конвертируем Bitmap в MPImage
            val mpImage = BitmapImageBuilder(bitmap).build()

            // Распознаем жест
            val result = gestureRecognizer?.recognize(mpImage)

            return processResult(result)

        } catch (e: Exception) {
            println("❌ Ошибка при распознавании: ${e.message}")
            return ClassificationResult("Ошибка: ${e.message}", 0f)
        }
    }

    private fun processResult(result: GestureRecognizerResult?): ClassificationResult {
        if (result == null) {
            return ClassificationResult("Результат null", 0f)
        }

        val gestures = result.gestures()

        if (gestures.isEmpty()) {
            return ClassificationResult("Рука не обнаружена", 0.3f)
        }

        val firstGesture = gestures[0]

        if (firstGesture.isEmpty()) {
            return ClassificationResult("Жест не распознан", 0.3f)
        }

        val topGesture = firstGesture[0]
        val gestureName = topGesture.categoryName()
        val confidence = topGesture.score()

        // Преобразуем названия жестов
        val displayName = when (gestureName.lowercase()) {
            "none" -> "Рука не обнаружена"
            "closed_fist" -> "✊ 0 - Кулак"
            "open_palm" -> "🖐️ 5 - Раскрытая ладонь"
            "pointing_up" -> "☝️ 1 - Указательный палец"
            "thumb_up" -> "👍 Палец вверх"
            "thumb_down" -> "👎 Палец вниз"
            "peace" -> "✌️ 2 - Два пальца (V)"
            "victory" -> "✌️ Победа"
            "iloveyou" -> "🤟 3 - Три пальца (ILY)"
            "ok" -> "👌 OK"
            else -> gestureName
        }

        println("🎯 Распознано: $displayName (уверенность: ${"%.1f".format(confidence * 100)}%)")

        return ClassificationResult(displayName, confidence)
    }

    fun close() {
        println("🔚 Закрываем GestureRecognizer")
        gestureRecognizer?.close()
        gestureRecognizer = null
    }
}