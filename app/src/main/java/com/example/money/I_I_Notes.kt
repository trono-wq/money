
package com.example.money

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat

class Notes : AppCompatActivity() {
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

        findViewById<TextView>(R.id.ab_money).setOnClickListener {
            val intent = Intent(this, Money::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)
        }

        findViewById<TextView>(R.id.ab_status).setOnClickListener {
            startActivity(Intent(this, Status::class.java))
        }

        findViewById<TextView>(R.id.ab_calculator).setOnClickListener {
            startActivity(Intent(this, Calculator::class.java))
        }
    }
}
