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

class Money : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.i_money)

        val date = findViewById<EditText>(R.id.input_date)

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
}
