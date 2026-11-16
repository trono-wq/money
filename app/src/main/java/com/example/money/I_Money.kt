
package com.example.money

import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.text.InputType
import android.view.View
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.widget.LinearLayout
import android.app.DatePickerDialog
import java.util.Calendar
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat

class Money : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.i_money)

        // DATE BUTTON - Interact
        val dateButton = findViewById<Button>(R.id.m_b_date)
        val dateInput = findViewById<EditText>(R.id.m_i_date)

        dateButton.setOnClickListener {
            val today = java.text.SimpleDateFormat(
                "dd / MM / yyyy (EEEE)",
                Locale.ENGLISH
            ).format(java.util.Date())

            dateInput.setText(today)
        }

        val date = findViewById<EditText>(R.id.m_i_date)

        // FORMAT NUMBER INPUT
        formatNumberInput(findViewById(R.id.m_i_allowance))
        formatNumberInput(findViewById(R.id.m_i_debt))
        formatNumberInput(findViewById(R.id.m_i_credit))
        formatNumberInput(findViewById(R.id.m_i_spent))

        // DATE PICKER - Interact
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
                            }.getDisplayName(
                                Calendar.DAY_OF_WEEK,
                                Calendar.LONG,
                                Locale.US
                            )
                        )
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // DYNAMIC ENTRIES - Layout
        val enterBar = findViewById<LinearLayout>(R.id.enter_bar)
        val inputBar = findViewById<LinearLayout>(R.id.input_bar)

        // DEBT - Position Anchors
        val debtLabelAnchor = enterBar.getChildAt(4)
        val debtInputAnchor = findViewById<EditText>(R.id.m_i_credit)

        // CREDIT - Position Anchors
        val creditLabelAnchor = enterBar.getChildAt(6)
        val creditInputAnchor = findViewById<EditText>(R.id.m_i_spent)

        // DEBT - Initial Inputs
        var latestDebtAmount = findViewById<EditText>(R.id.m_i_debt)
        var latestDebtName = findViewById<EditText>(R.id.m_i_debt_n)

        // CREDIT - Initial Inputs
        var latestCreditAmount = findViewById<EditText>(R.id.m_i_credit)
        var latestCreditName = findViewById<EditText>(R.id.m_i_credit_n)

        // DEBT BUTTON - Interact
        findViewById<Button>(R.id.m_b_debt).setOnClickListener {

            if (
                latestDebtAmount.text.toString().isBlank() ||
                latestDebtName.text.toString().isBlank()
            ) return@setOnClickListener

            val newEntry = addDynamicEntry(
                enterBar,
                inputBar,
                debtLabelAnchor,
                debtInputAnchor,
                R.string.m_lb_debt,
                R.string.m_lb_debt_n,
                R.string.m_i_debt,
                R.string.m_i_debt_n,
                Color.rgb(102, 102, 102)
            )

            formatNumberInput(newEntry.first)

            latestDebtAmount = newEntry.first
            latestDebtName = newEntry.second
        }

        // CREDIT BUTTON - Interact
        findViewById<Button>(R.id.m_b_credit).setOnClickListener {

            if (
                latestCreditAmount.text.toString().isBlank() ||
                latestCreditName.text.toString().isBlank()
            ) return@setOnClickListener

            val newEntry = addDynamicEntry(
                enterBar,
                inputBar,
                creditLabelAnchor,
                creditInputAnchor,
                R.string.m_lb_credit,
                R.string.m_lb_credit_n,
                R.string.m_i_credit,
                R.string.m_i_credit_n,
                Color.rgb(102, 102, 102)
            )

            formatNumberInput(newEntry.first)

            latestCreditAmount = newEntry.first
            latestCreditName = newEntry.second
        }

        // SYSTEM BARS - Styling
        supportActionBar?.hide()

        WindowCompat.setDecorFitsSystemWindows(window, true)

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

        // NAVIGATION - Interact
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

    // DYNAMIC ENTRY - Create
    private fun addDynamicEntry(
        labelContainer: LinearLayout,
        inputContainer: LinearLayout,
        labelAnchor: View,
        inputAnchor: View,
        amountLabelRes: Int,
        nameLabelRes: Int,
        amountHintRes: Int,
        nameHintRes: Int,
        nameTextColor: Int
    ): Pair<EditText, EditText> {

        val padding = (5 * resources.displayMetrics.density).toInt()

        // AMOUNT LABEL - Layout, Styling
        val amountLabel = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            text = getString(amountLabelRes)
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(padding, padding, padding, padding)
        }

        // NAME LABEL - Layout, Styling
        val nameLabel = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            text = getString(nameLabelRes)
            setTextColor(nameTextColor)
            textSize = 15f
            setPadding(padding, padding, padding, padding)
        }

        // AMOUNT INPUT - Layout, Styling
        val amountInput = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            hint = getString(amountHintRes)
            setHintTextColor(Color.rgb(102, 102, 102))
            setTextColor(Color.WHITE)
            textSize = 15f
            setPadding(padding, padding, padding, padding)
            background = null
            inputType = InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        // NAME INPUT - Layout, Styling
        val nameInput = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            hint = getString(nameHintRes)
            setHintTextColor(Color.rgb(102, 102, 102))
            setTextColor(nameTextColor)
            textSize = 15f
            setPadding(padding, padding, padding, padding)
            background = null
            inputType = InputType.TYPE_CLASS_TEXT
        }

        // INSERT LABELS - Position
        labelContainer.addView(
            amountLabel,
            labelContainer.indexOfChild(labelAnchor)
        )

        labelContainer.addView(
            nameLabel,
            labelContainer.indexOfChild(labelAnchor)
        )

        // INSERT INPUTS - Position
        inputContainer.addView(
            amountInput,
            inputContainer.indexOfChild(inputAnchor)
        )

        inputContainer.addView(
            nameInput,
            inputContainer.indexOfChild(inputAnchor)
        )

        return Pair(amountInput, nameInput)
    }

    // NUMBER FORMATTING - Interact
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
                val normalized = clean.replace(
                    Regex("^0+(?=\\d)"),
                    ""
                )

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
