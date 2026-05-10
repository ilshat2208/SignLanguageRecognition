package com.example.signlanguage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnStart = findViewById<Button>(R.id.btnStart)
        val tvInstructions = findViewById<TextView>(R.id.tvInstructions)

        // Инструкция для пользователя
        tvInstructions.text = """
            📱 Приложение распознает базовые жесты языка:
            
            ✊ 0 - Кулак
            🖐️ 5 - Раскрытая ладонь
            ☝️ 1 - Указательный палец
            👍 Палец вверх
            👎 Палец вниз
            ✌️ 2 - Два пальца (V)
            ✌️ Победа
            🤟 3 - Три пальца (ILY)
            👌 OK
            
            📌 Как использовать:
            1. Нажмите "Начать распознавание"
            2. Разрешите доступ к камере
            3. Покажите жест перед камерой
            4. Результат отобразится на экране
            
            💡 Совет: Держите руку на расстоянии 20-40 см от камеры
        """.trimIndent()

        btnStart.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }
}