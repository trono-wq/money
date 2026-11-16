package com.example.money

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import android.app.DatePickerDialog
import java.util.Calendar
import java.util.Locale
import android.text.Editable
import android.text.TextWatcher

class Money : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.i_money)

        val date = findViewById<EditText>(R.id.m_i_date)

        formatNumberInput(findViewById(R.id.m_i_allowance))
        formatNumberInput(findViewById(R.id.m_i_debt))
        formatNumberInput(findViewById(R.id.m_i_credit))
        formatNumberInput(findViewById(R.id.m_i_spent))

        date.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    date.setText(
                        String.format(
                            Locale.US,
                            "%02d / %02d / %04d (%s)",
                            day,
                            month + 1,
                            year,
                            Calendar.getInstance().apply {
                                set(year, month, day)
                            }.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)
                        ))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, true)

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

        findViewById<TextView>(R.id.ab_status).setOnClickListener {
            startActivity(Intent(this, Status::class.java))
        }

        findViewById<TextView>(R.id.ab_calculator).setOnClickListener {
            startActivity(Intent(this, Calculator::class.java))
        }

        findViewById<TextView>(R.id.ab_notes).setOnClickListener {
            startActivity(Intent(this, Notes::class.java))
        }

        findViewById<TextView>(R.id.ab_history).setOnClickListener {
            startActivity(Intent(this, History::class.java))
        }
    }

    private fun formatNumberInput(editText: EditText) {

        editText.addTextChangedListener(object : TextWatcher {

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
                val cursor = editText.selectionStart.coerceAtLeast(0)

                val digitsBeforeCursor = original
                    .take(cursor.coerceAtMost(original.length))
                    .count { it.isDigit() }

                val clean = original.replace(" ", "")

                if (clean.startsWith("0")) {
                    editText.removeTextChangedListener(this)
                    editText.setText(clean.dropWhile { it == '0' })
                    editText.setSelection(editText.text.length)
                    editText.addTextChangedListener(this)
                    return
                }
                // REMOVE LEADING ZEROS
                val normalized = clean.replace(Regex("^0+(?=\\d)"), "")

                if (clean.startsWith(".")) {
                    editText.removeTextChangedListener(this)
                    editText.setText("0$clean")
                    editText.setSelection(editText.text.length)
                    editText.addTextChangedListener(this)
                    return
                }

                val parts = normalized.split(".", limit = 2)

                val integerPart = parts[0]
                val decimalPart = if (parts.size > 1) parts[1] else null

                val formattedInteger = integerPart.reversed()
                    .chunked(3)
                    .joinToString(" ") { it.reversed() }
                    .reversed()

                val formattedDecimal = decimalPart
                    ?.chunked(3)
                    ?.joinToString(" ")

                val formatted = if (decimalPart != null) {
                    "$formattedInteger.$formattedDecimal"
                } else {
                    formattedInteger
                }

                if (original != formatted) {

                    editText.removeTextChangedListener(this)

                    editText.setText(formatted)

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

                    editText.setSelection(
                        newCursor.coerceAtMost(formatted.length)
                    )

                    editText.addTextChangedListener(this)
                }
            }
        })
    }
}
