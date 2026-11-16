package com.example.money

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat

abstract class PeopleActivity : AppCompatActivity() {

    protected abstract val isDebt: Boolean
    protected abstract val layoutRes: Int
    protected abstract val editButtonId: Int

    private lateinit var enterBar: LinearLayout
    private lateinit var inputBar: LinearLayout
    private lateinit var editButton: Button

    private var editing = false

    private var entries = mutableListOf<PersonEntry>()

    private val expanded = mutableSetOf<String>()

    private val nameFields = mutableMapOf<String, EditText>()

    private val dateFields =
        mutableMapOf<String, Pair<EditText, EditText>>()

    private val white = Color.WHITE
    private val gray = Color.rgb(102, 102, 102)
    private val gold = Color.rgb(211, 154, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(layoutRes)

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

        enterBar = findViewById(R.id.enter_bar)
        inputBar = findViewById(R.id.input_bar)
        editButton = findViewById(editButtonId)

        entries = MoneyData.getPeople(this, isDebt)

        findViewById<TextView>(R.id.b_ab_history)
            .setOnClickListener {
                finish()
            }

        editButton.setOnClickListener {
            if (editing) {
                saveChanges()
            } else {
                editing = true
                renderEntries()
            }
        }

        renderEntries()
    }

    // GROUP ENTRIES BY PERSON

    private fun groupedEntries(): Map<String, List<PersonEntry>> {
        return entries.groupBy {
            it.name.trim().lowercase()
        }
    }

    private fun displayName(group: List<PersonEntry>): String {
        return group.firstOrNull()?.name ?: ""
    }

    // RENDER

    private fun renderEntries() {

        enterBar.removeAllViews()
        inputBar.removeAllViews()

        nameFields.clear()
        dateFields.clear()

        editButton.text = if (editing) "Save" else "Edit"

        editButton.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(gray)
        )

        // TOTAL

        val total = entries.sumOf { it.amount }

        addLeftText("TOTAL", bold = true)
        addRightValue(
            MoneyData.formatAmount(total),
            bold = true,
            clearAction = if (editing) {
                {
                    entries.clear()
                    renderEntries()
                }
            } else null
        )

        // PEOPLE

        val groups = groupedEntries()

        groups.forEach { (key, group) ->

            val name = displayName(group)
            val personTotal = group.sumOf { it.amount }

            addPersonHeader(
                key,
                name,
                personTotal
            )

            if (key in expanded) {

                group.forEach { entry ->
                    addDateRow(entry)
                }
            }
        }
    }

    // LEFT TEXT ROW

    private fun addLeftText(
        value: String,
        bold: Boolean = false,
        color: Int = white
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val text = TextView(this).apply {
            this.text = value
            setTextColor(color)
            textSize = 15f
            setPadding(dp(5), dp(5), dp(5), dp(5))

            if (bold) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        row.addView(text)
        enterBar.addView(row)
    }

    // RIGHT VALUE ROW

    private fun addRightValue(
        value: String,
        bold: Boolean = false,
        clearAction: (() -> Unit)? = null
    ) {
        val row = createRightRow()

        val text = TextView(this).apply {
            this.text = value
            setTextColor(white)
            textSize = 15f
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(dp(5), dp(5), dp(5), dp(5))

            if (bold) {
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        row.addView(text)

        if (clearAction != null) {
            row.addView(createClearButton(clearAction))
        }

        inputBar.addView(row)
    }

    // PERSON HEADER

    private fun addPersonHeader(
        key: String,
        name: String,
        total: Double
    ) {

        // LEFT SIDE

        val leftRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        if (editing) {

            val nameInput = createEditText(name, false)

            nameFields[key] = nameInput

            leftRow.addView(
                nameInput,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

        } else {

            val nameText = TextView(this).apply {
                text = name
                setTextColor(white)
                textSize = 15f
                setPadding(dp(5), dp(5), dp(5), dp(5))

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            leftRow.addView(nameText)
        }

        val arrow = Button(this).apply {
            text = if (key in expanded) "^" else "v"
            textSize = 10f
            setTextColor(white)
            setPadding(0, 0, 0, 0)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(gray)

            layoutParams = LinearLayout.LayoutParams(
                dp(25),
                dp(30)
            )

            setOnClickListener {
                if (key in expanded) {
                    expanded.remove(key)
                } else {
                    expanded.add(key)
                }

                renderEntries()
            }
        }

        leftRow.addView(arrow)
        enterBar.addView(leftRow)

        // RIGHT SIDE

        val rightRow = createRightRow()

        val amountText = TextView(this).apply {
            text = MoneyData.formatAmount(total)
            setTextColor(white)
            textSize = 15f
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(dp(5), dp(5), dp(5), dp(5))

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        rightRow.addView(amountText)

        if (editing) {
            rightRow.addView(
                createClearButton {
                    entries.removeAll {
                        it.name.trim().lowercase() == key
                    }

                    expanded.remove(key)
                    renderEntries()
                }
            )
        }

        inputBar.addView(rightRow)
    }

    // DATE ROW

    private fun addDateRow(entry: PersonEntry) {

        // LEFT DATE

        if (editing) {

            val dateInput = createEditText(entry.date, false)

            enterBar.addView(dateInput)

            // RIGHT AMOUNT

            val amountInput = createEditText(
                MoneyData.formatAmount(entry.amount),
                true
            )

            val rightRow = createRightRow()

            rightRow.addView(
                amountInput,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            rightRow.addView(
                createClearButton {
                    entries.removeAll { it.id == entry.id }
                    renderEntries()
                }
            )

            inputBar.addView(rightRow)

            dateFields[entry.id] =
                Pair(dateInput, amountInput)

        } else {

            addLeftText(
                "    ${entry.date}",
                color = gray
            )

            addRightValue(
                MoneyData.formatAmount(entry.amount)
            )
        }
    }

    // CREATE ROWS

    private fun createRightRow(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun createClearButton(
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = "C"
            textSize = 10f
            setTextColor(white)
            setPadding(0, 0, 0, 0)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(gray)

            layoutParams = LinearLayout.LayoutParams(
                dp(25),
                dp(30)
            )

            setOnClickListener {
                action()
            }
        }
    }

    private fun createEditText(
        value: String,
        number: Boolean
    ): EditText {
        return EditText(this).apply {
            setText(value)
            setTextColor(white)
            setHintTextColor(gray)
            textSize = 15f
            setPadding(dp(5), dp(5), dp(5), dp(5))
            background = null

            inputType = if (number) {
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
            } else {
                InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
            }

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    // SAVE CHANGES

    private fun saveChanges() {

        val updated = mutableListOf<PersonEntry>()

        val groups = groupedEntries()

        groups.forEach { (key, group) ->

            val newName = nameFields[key]
                ?.text
                ?.toString()
                ?.trim()
                ?: displayName(group)

            if (newName.isBlank()) return@forEach

            group.forEach { entry ->

                val fields = dateFields[entry.id]

                val newDate = fields?.first
                    ?.text
                    ?.toString()
                    ?.trim()
                    ?: entry.date

                val amountText = fields?.second
                    ?.text
                    ?.toString()
                    ?: MoneyData.formatAmount(entry.amount)

                val newAmount = MoneyData.parseAmount(amountText)

                if (newDate.isNotBlank() && amountText.isNotBlank()) {
                    updated.add(
                        entry.copy(
                            name = newName,
                            date = newDate,
                            amount = newAmount
                        )
                    )
                }
            }
        }

        MoneyData.replacePeople(
            this,
            isDebt,
            updated
        )

        entries = updated
        editing = false

        renderEntries()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
