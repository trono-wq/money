
package com.example.money

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat

class Status : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.i_i_status)

        window.setBackgroundDrawableResource(android.R.color.black)
        window.statusBarColor = Color.BLACK
        window.navigationBarColor = Color.BLACK

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(android.R.id.content)
        ) { view, insets ->

            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                bars.left + 20,
                bars.top + 50,
                bars.right + 20,
                bars.bottom + 50
            )

            insets
        }

        // NAVIGATION

        findViewById<TextView>(R.id.ab_money).setOnClickListener {
            val intent = Intent(this, Money::class.java)

            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP

            startActivity(intent)
        }

        findViewById<TextView>(R.id.ab_calculator).setOnClickListener {
            startActivity(Intent(this, Calculator::class.java))
        }

        findViewById<TextView>(R.id.ab_notes).setOnClickListener {
            startActivity(Intent(this, Notes::class.java))
        }

        findViewById<TextView>(R.id.ab_flight).setOnClickListener {
            startActivity(Intent(this, Notes::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateTotals()
    }

    private fun updateTotals() {

        findViewById<TextView>(R.id.s_s_spent).text =
            MoneyData.formatAmount(MoneyData.getTotalSpent(this))

        findViewById<TextView>(R.id.s_s_debt).text =
            MoneyData.formatAmount(MoneyData.getTotalDebt(this))

        findViewById<TextView>(R.id.s_s_credit).text =
            MoneyData.formatAmount(MoneyData.getTotalCredit(this))

        findViewById<TextView>(R.id.s_s_allowance).text =
            MoneyData.formatAmount(MoneyData.getTotalAllowance(this))

        findViewById<TextView>(R.id.s_s_leftover).text =
            MoneyData.formatAmount(MoneyData.getLeftover(this))
    }
}
