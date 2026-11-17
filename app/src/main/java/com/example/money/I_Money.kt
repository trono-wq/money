

package com.example.money

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import java.util.Calendar
import java.util.Locale

class Money : AppCompatActivity() {

    private data class InputPair(
        val amount: EditText,
        val name: EditText,
        val amountLabel: TextView? = null,
        val nameLabel: TextView? = null
    )

    private val debtInputs = mutableListOf<InputPair>()
    private val creditInputs = mutableListOf<InputPair>()

    private var isCompactingEntries = false

    private lateinit var dateInput: EditText
    private lateinit var allowanceInput: EditText
    private lateinit var spentInput: EditText
    private lateinit var saveButton: Button
    private lateinit var clearAllButton: Button

    private lateinit var enterBar: LinearLayout
    private lateinit var inputBar: LinearLayout

    private lateinit var debtLabelAnchor: View
    private lateinit var debtInputAnchor: View
    private lateinit var creditLabelAnchor: View
    private lateinit var creditInputAnchor: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.i_money)

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

        // INPUTS

        dateInput = findViewById(R.id.m_i_date)
        allowanceInput = findViewById(R.id.m_i_allowance)
        spentInput = findViewById(R.id.m_i_spent)
        saveButton = findViewById(R.id.save)
        clearAllButton = findViewById(R.id.b_clear_all)
        clearAllButton.visibility = View.GONE

        clearAllButton.setOnClickListener {
            clearAllInputs()
        }

        enterBar = findViewById(R.id.enter_bar)
        inputBar = findViewById(R.id.input_bar)

        debtLabelAnchor = enterBar.getChildAt(4)
        debtInputAnchor = findViewById(R.id.m_i_credit)

        creditLabelAnchor = enterBar.getChildAt(6)
        creditInputAnchor = findViewById(R.id.m_i_spent)

        val firstDebt = InputPair(
            findViewById(R.id.m_i_debt),
            findViewById(R.id.m_i_debt_n)
        )

        val firstCredit = InputPair(
            findViewById(R.id.m_i_credit),
            findViewById(R.id.m_i_credit_n)
        )

        debtInputs.add(firstDebt)
        creditInputs.add(firstCredit)

        watchForEmptyEntry(firstDebt, debtInputs)
        watchForEmptyEntry(firstCredit, creditInputs)

        // DATE PICKER

        findViewById<Button>(R.id.m_b_date).setOnClickListener {
            dateInput.setText(
                java.text.SimpleDateFormat(
                    "dd / MM / yyyy (EEEE)",
                    Locale.ENGLISH
                ).format(java.util.Date())
            )
        }

        dateInput.setOnClickListener {

            val calendar = Calendar.getInstance()

            val dialog = DatePickerDialog(
                this,
                { _, year, month, day ->

                    val selected = Calendar.getInstance().apply {
                        set(year, month, day)
                    }

                    dateInput.setText(
                        java.text.SimpleDateFormat(
                            "dd / MM / yyyy (EEEE)",
                            Locale.ENGLISH
                        ).format(selected.time)
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // PREVENT FUTURE DATES

            val today = Calendar.getInstance()

            today.set(Calendar.HOUR_OF_DAY, 23)
            today.set(Calendar.MINUTE, 59)
            today.set(Calendar.SECOND, 59)
            today.set(Calendar.MILLISECOND, 999)

            dialog.datePicker.maxDate = today.timeInMillis

            dialog.show()
        }

        // NUMBER INPUT FORMATTING

        formatNumberInput(allowanceInput)
        formatNumberInput(spentInput)

        debtInputs.forEach {
            formatNumberInput(it.amount)
            capitalizeWords(it.name)
        }

        creditInputs.forEach {
            formatNumberInput(it.amount)
            capitalizeWords(it.name)
        }

        // DYNAMIC DEBT INPUT

        findViewById<Button>(R.id.m_b_debt).setOnClickListener {

            val latest = debtInputs.last()

            if (
                latest.amount.text.isBlank() ||
                latest.name.text.isBlank()
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

            formatNumberInput(newEntry.amount)
            capitalizeWords(newEntry.name)

            debtInputs.add(newEntry)

            watchForEmptyEntry(newEntry, debtInputs)

            updateSaveButton()
        }

        // DYNAMIC CREDIT INPUT

        findViewById<Button>(R.id.m_b_credit).setOnClickListener {

            val latest = creditInputs.last()

            if (
                latest.amount.text.isBlank() ||
                latest.name.text.isBlank()
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

            formatNumberInput(newEntry.amount)
            capitalizeWords(newEntry.name)

            creditInputs.add(newEntry)

            watchForEmptyEntry(newEntry, creditInputs)

            updateSaveButton()
        }

        // SAVE BUTTON

        val watcher = object : TextWatcher {

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
            ) {
                updateSaveButton()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        dateInput.addTextChangedListener(watcher)
        allowanceInput.addTextChangedListener(watcher)
        spentInput.addTextChangedListener(watcher)

        debtInputs.forEach {
            it.amount.addTextChangedListener(watcher)
            it.name.addTextChangedListener(watcher)
        }

        creditInputs.forEach {
            it.amount.addTextChangedListener(watcher)
            it.name.addTextChangedListener(watcher)
        }

        saveButton.setOnClickListener {
            saveEntries()
        }

        // NAVIGATION

        findViewById<TextView>(R.id.ab_status).setOnClickListener {
            startActivity(Intent(this, Status::class.java))
        }

        findViewById<TextView>(R.id.ab_calculator).setOnClickListener {
            startActivity(Intent(this, Calculator::class.java))
        }

        findViewById<TextView>(R.id.ab_flight).setOnClickListener {
            startActivity(Intent(this, Flight::class.java))
        }

        findViewById<TextView>(R.id.ab_notes).setOnClickListener {
            startActivity(Intent(this, Notes::class.java))
        }

        updateSaveButton()
    }

    // VALIDATION

    private fun hasValidInput(): Boolean {

        if (hasDuplicateNames(debtInputs) || hasDuplicateNames(creditInputs)) {
            return false
        }

        val hasAllowance = allowanceInput.text.isNotBlank()
        val hasSpent = spentInput.text.isNotBlank()

        val hasDebt = debtInputs.any {
            it.amount.text.isNotBlank()
        }

        val hasCredit = creditInputs.any {
            it.amount.text.isNotBlank()
        }

        val hasAnyInput =
            hasAllowance || hasSpent || hasDebt || hasCredit

        if (!hasAnyInput) return false

        if (dateInput.text.isBlank()) return false

        for (entry in debtInputs) {
            if (
                entry.amount.text.isNotBlank() &&
                entry.name.text.isBlank()
            ) return false
        }

        for (entry in creditInputs) {
            if (
                entry.amount.text.isNotBlank() &&
                entry.name.text.isBlank()
            ) return false
        }

        return true
    }

    private fun updateSaveButton() {

        val valid = hasValidInput()

        saveButton.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                if (valid) Color.rgb(211, 154, 0)
                else Color.rgb(102, 102, 102)
            )
        )

        saveButton.setTypeface(
            null,
            if (valid) android.graphics.Typeface.BOLD
            else android.graphics.Typeface.NORMAL
        )

        clearAllButton.visibility =
            if (valid) View.VISIBLE else View.GONE
    }

    // CLEAR ALL INPUTS

    private fun clearAllInputs() {

        isCompactingEntries = true

        try {
            dateInput.text.clear()
            allowanceInput.text.clear()
            spentInput.text.clear()

            debtInputs.forEach { entry ->
                entry.amount.text.clear()
                entry.name.text.clear()
            }

            creditInputs.forEach { entry ->
                entry.amount.text.clear()
                entry.name.text.clear()
            }

            removeExtraEntries(debtInputs, enterBar, inputBar)
            removeExtraEntries(creditInputs, enterBar, inputBar)

        } finally {
            isCompactingEntries = false
        }

        updateSaveButton()
    }

    // SAVE DATA

    private fun saveEntries() {

        if (!hasValidInput()) return

        val date = dateInput.text.toString()

        val allowance = MoneyData.parseAmount(
            allowanceInput.text.toString()
        )

        val spent = MoneyData.parseAmount(
            spentInput.text.toString()
        )

        val debts = debtInputs
            .filter {
                it.amount.text.isNotBlank() &&
                        it.name.text.isNotBlank()
            }
            .map {
                PersonEntry(
                    name = it.name.text.toString().trim(),
                    amount = MoneyData.parseAmount(
                        it.amount.text.toString()
                    ),
                    date = date
                )
            }

        val credits = creditInputs
            .filter {
                it.amount.text.isNotBlank() &&
                        it.name.text.isNotBlank()
            }
            .map {
                PersonEntry(
                    name = it.name.text.toString().trim(),
                    amount = MoneyData.parseAmount(
                        it.amount.text.toString()
                    ),
                    date = date
                )
            }

        // REPLACE EXISTING DATE ENTRIES

        MoneyData.replaceDateEntries(
            this,
            date,
            allowance,
            spent,
            debts,
            credits
        )

        // REMOVE EXTRA DEBT / CREDIT ROWS

        removeExtraEntries(debtInputs, enterBar, inputBar)
        removeExtraEntries(creditInputs, enterBar, inputBar)

        // CLEAR INPUTS

        dateInput.text.clear()
        allowanceInput.text.clear()
        spentInput.text.clear()

        debtInputs.forEach {
            it.amount.text.clear()
            it.name.text.clear()
        }

        creditInputs.forEach {
            it.amount.text.clear()
            it.name.text.clear()
        }

        updateSaveButton()
    }

    // REMOVE EXTRA INPUT GROUPS

    private fun removeExtraEntries(
        entries: MutableList<InputPair>,
        labelContainer: LinearLayout,
        inputContainer: LinearLayout
    ) {

        for (i in entries.lastIndex downTo 1) {

            val entry = entries[i]

            entry.amountLabel?.let {
                labelContainer.removeView(it)
            }

            entry.nameLabel?.let {
                labelContainer.removeView(it)
            }

            inputContainer.removeView(entry.amount)
            inputContainer.removeView(entry.name)

            entries.removeAt(i)
        }
    }

    // AUTOMATICALLY COMPACT EMPTY GROUPS

    private fun watchForEmptyEntry(
        entry: InputPair,
        entries: MutableList<InputPair>
    ) {

        val watcher = object : TextWatcher {

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

                if (isCompactingEntries) return

                if (
                    entry.amount.text.isBlank() &&
                    entry.name.text.isBlank()
                ) {
                    compactEntries(entries)
                }
            }
        }

        entry.amount.addTextChangedListener(watcher)
        entry.name.addTextChangedListener(watcher)
    }

    // COMPACT INPUT GROUPS

    private fun compactEntries(
        entries: MutableList<InputPair>
    ) {

        if (isCompactingEntries) return

        isCompactingEntries = true

        try {

            // COLLECT NON-EMPTY ENTRIES

            val remainingEntries = entries.mapNotNull { entry ->

                val amount = entry.amount.text.toString()
                val name = entry.name.text.toString()

                if (amount.isBlank() && name.isBlank()) {
                    null
                } else {
                    Pair(amount, name)
                }
            }

            // MOVE VALUES UPWARD

            for (i in remainingEntries.indices) {

                val entry = entries[i]
                val values = remainingEntries[i]

                entry.amount.setText(values.first)
                entry.name.setText(values.second)
            }

            // REMOVE UNUSED EXTRA ROWS

            val requiredCount = maxOf(1, remainingEntries.size)

            for (i in entries.lastIndex downTo requiredCount) {

                val entry = entries[i]

                entry.amountLabel?.let {
                    enterBar.removeView(it)
                }

                entry.nameLabel?.let {
                    enterBar.removeView(it)
                }

                inputBar.removeView(entry.amount)
                inputBar.removeView(entry.name)

                entries.removeAt(i)
            }

        } finally {
            isCompactingEntries = false
        }

        updateSaveButton()
    }

    // CREATE DYNAMIC INPUT

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
    ): InputPair {

        val padding =
            (5 * resources.displayMetrics.density).toInt()

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

            inputType =
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

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

            inputType =
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }

        // INSERT LABELS

        labelContainer.addView(
            amountLabel,
            labelContainer.indexOfChild(labelAnchor)
        )

        labelContainer.addView(
            nameLabel,
            labelContainer.indexOfChild(labelAnchor)
        )

        // INSERT INPUTS

        inputContainer.addView(
            amountInput,
            inputContainer.indexOfChild(inputAnchor)
        )

        inputContainer.addView(
            nameInput,
            inputContainer.indexOfChild(inputAnchor)
        )

        // SAVE BUTTON WATCHER

        val watcher = object : TextWatcher {

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
            ) {
                updateSaveButton()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        amountInput.addTextChangedListener(watcher)
        nameInput.addTextChangedListener(watcher)

        return InputPair(
            amountInput,
            nameInput,
            amountLabel,
            nameLabel
        )
    }

    // CAPITALIZE WORDS

    private fun capitalizeWords(editText: EditText) {
        editText.inputType =
            InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_WORDS
    }

    // NUMBER FORMATTING

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

                if (clean.startsWith(".")) {
                    editText.removeTextChangedListener(this)
                    editText.setText("0$clean")
                    editText.setSelection(editText.text.length)
                    editText.addTextChangedListener(this)
                    return
                }

                val parts = clean.split(".", limit = 2)

                val integerPart = parts[0]
                val decimalPart =
                    if (parts.size > 1) parts[1] else null

                val formattedInteger = integerPart
                    .reversed()
                    .chunked(3)
                    .joinToString(" ") { it.reversed() }
                    .split(" ")
                    .reversed()
                    .joinToString(" ")

                val formattedDecimal = decimalPart
                    ?.chunked(3)
                    ?.joinToString(" ")

                val formatted =
                    if (decimalPart != null) {
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

    private fun hasDuplicateNames(entries: List<InputPair>): Boolean {
        val names = entries
            .map { it.name.text.toString().trim().lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }

        return names.size != names.distinct().size
    }
}