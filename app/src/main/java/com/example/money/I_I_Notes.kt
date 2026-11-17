
package com.example.money

import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.widget.TextView
import android.widget.EditText
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat

class Notes : AppCompatActivity() {

    private lateinit var notesInput: EditText
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.i_i_notes)

        window.setBackgroundDrawableResource(android.R.color.black)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->

            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.setPadding(
                bars.left + 20,
                bars.top + 50,
                bars.right + 20,
                bars.bottom + 50
            )

            insets
        }

        // NOTES - Layout
        notesInput = findViewById(R.id.n_input)

        // STORAGE
        preferences = getSharedPreferences(
            "notes_storage",
            MODE_PRIVATE
        )

        // LOAD SAVED NOTES
        notesInput.setText(
            preferences.getString("content", "")
        )

        // AUTO SAVE AND NUMBER FORMATTING
        notesInput.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}

            override fun afterTextChanged(s: Editable?) {

                if (s == null) return

                val original = s.toString()
                val cursor = notesInput.selectionStart.coerceAtLeast(0)

                val formatted = formatNumbers(original)

                if (original != formatted) {

                    val digitsBeforeCursor = original
                        .take(cursor.coerceAtMost(original.length))
                        .count { it.isDigit() }

                    notesInput.removeTextChangedListener(this)

                    notesInput.setText(formatted)

                    var newCursor = 0
                    var digitCount = 0

                    while (
                        newCursor < formatted.length &&
                        digitCount < digitsBeforeCursor
                    ) {
                        if (formatted[newCursor].isDigit()) {
                            digitCount++
                        }
                        newCursor++
                    }

                    notesInput.setSelection(
                        newCursor.coerceAtMost(formatted.length)
                    )

                    notesInput.addTextChangedListener(this)
                }

                // SAVE NOTES
                preferences.edit()
                    .putString("content", formatted)
                    .apply()
            }
        })

        // MONEY
        findViewById<TextView>(R.id.ab_money).setOnClickListener {

            val intent = Intent(this, Money::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)
        }

        // STATUS
        findViewById<TextView>(R.id.ab_status).setOnClickListener {
            startActivity(Intent(this, Status::class.java))
        }

        // CALCULATOR
        findViewById<TextView>(R.id.ab_calculator).setOnClickListener {
            startActivity(Intent(this, Calculator::class.java))
        }

        findViewById<TextView>(R.id.ab_flight).setOnClickListener {
            startActivity(Intent(this, Status::class.java))
        }
    }

    // NUMBER FORMATTING
    private fun formatNumbers(input: String): String {
        val regex = Regex(
            """(?<![A-Za-z0-9])\d+(?: \d+)*(?:\.\d+)?(?![A-Za-z0-9])"""
        )

        return regex.replace(input) { match ->
            val number = match.value.replace(" ", "")
            val parts = number.split(".", limit = 2)

            val integer = parts[0]
            val formattedInteger = integer.reversed()
                .chunked(3)
                .joinToString(" ") { it.reversed() }
                .reversed()

            if (parts.size > 1) {
                val formattedDecimal = parts[1]
                    .chunked(3)
                    .joinToString(" ")

                "$formattedInteger.$formattedDecimal"
            } else {
                formattedInteger
            }
        }
    }

    // SAVE WHEN LEAVING
    override fun onPause() {
        super.onPause()

        if (::notesInput.isInitialized &&
            ::preferences.isInitialized
        ) {
            preferences.edit()
                .putString("content", notesInput.text.toString())
                .apply()
        }
    }
}
