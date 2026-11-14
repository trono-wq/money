
package com.example.money

import android.os.Bundle
import android.content.Intent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat

class History : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.ii_history)

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

        findViewById<TextView>(R.id.ab_money).setOnClickListener {finish()}

        findViewById<TextView>(R.id.ab_week).setOnClickListener {
            startActivity(Intent(this, Week::class.java))
        }

        findViewById<TextView>(R.id.ab_wm).setOnClickListener {
            startActivity(Intent(this, Wm::class.java))
        }

        findViewById<TextView>(R.id.ab_debt).setOnClickListener {
            startActivity(Intent(this, Debt::class.java))
        }

        findViewById<TextView>(R.id.ab_credit).setOnClickListener {
            startActivity(Intent(this, Credit::class.java))
        }
    }
}
