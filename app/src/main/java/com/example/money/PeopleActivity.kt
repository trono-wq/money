

package com.example.money

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import java.util.Locale
import android.graphics.Typeface
import java.text.SimpleDateFormat

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

    // ORIGINAL AMOUNTS DURING EDITING
    private val draftAmounts = mutableMapOf<String, String>()

    // DISPLAYED TOTALS
    private val personTotalViews = mutableMapOf<String, TextView>()
    private lateinit var grandTotalView: TextView

    // BUTTON COLORS
    private val white = Color.WHITE
    private val gray = Color.rgb(102, 102, 102)
    private val gold = Color.rgb(211, 154, 0)

    // ROW DIMENSIONS
    private val rowHeight = 40
    private val clearButtonWidth = 25

    // SAVE STATE
    private var canSave = false

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

            if (!editing) {

                // ENTER EDIT MODE

                editing = true

                draftAmounts.clear()

                entries.forEach { entry ->
                    draftAmounts[entry.id] =
                        MoneyData.formatAmount(entry.amount)
                }

                renderEntries()

            } else {

                if (canSave) {

                    // SAVE VALID CHANGES
                    saveChanges()

                } else {

                    // GRAY SAVE: EXIT WITHOUT SAVING
                    cancelEditing()
                }
            }
        }

        renderEntries()
    }

    // GROUP ENTRIES BY PERSON AND SORT BY DATE
    private fun groupedEntries(): Map<String, List<PersonEntry>> {

        val dateFormat = SimpleDateFormat(
            "MM / dd / yyyy (EEEE)",
            Locale.ENGLISH
        ).apply {
            isLenient = false
        }

        return entries
            .groupBy {
                it.name.trim().lowercase(Locale.ROOT)
            }
            .mapValues { (_, group) ->

                group.sortedBy { entry ->
                    runCatching {
                        dateFormat.parse(entry.date)?.time
                    }.getOrNull() ?: Long.MAX_VALUE
                }
            }
    }

    private fun displayName(group: List<PersonEntry>): String {
        return group.firstOrNull()?.name ?: ""
    }

    // AMOUNT HELPERS

    private fun parseEditableAmount(value: String): Double? {
        return value
            .replace(",", "")
            .replace(" ", "")
            .toDoubleOrNull()
    }

    private fun currentAmount(entry: PersonEntry): Double {
        if (!editing) return entry.amount

        return parseEditableAmount(
            draftAmounts[entry.id] ?: ""
        ) ?: entry.amount
    }

    // SAVE BUTTON STATE

    private fun updateSaveButton() {

        if (!editing) {
            canSave = false

            editButton.text = "Edit"
            editButton.isEnabled = entries.sumOf { it.amount } > 0.0
            editButton.backgroundTintList =
                ColorStateList.valueOf(gray)

            return
        }

        var valid = true
        var changed = false

        entries.forEach { entry ->

            val amount = parseEditableAmount(
                draftAmounts[entry.id] ?: ""
            )

            if (
                amount == null ||
                amount < 0.0 ||
                amount > entry.amount
            ) {
                valid = false
            } else if (amount != entry.amount) {
                changed = true
            }
        }

        canSave = valid && changed

        editButton.text = "Save"
        editButton.isEnabled = true

        editButton.backgroundTintList =
            ColorStateList.valueOf(
                if (canSave) gold else gray
            )
    }

    // UPDATE DISPLAYED TOTALS

    private fun updateDisplayedTotals() {

        if (!editing) return

        val total = entries.sumOf {
            currentAmount(it)
        }

        grandTotalView.text =
            MoneyData.formatAmount(total)

        groupedEntries().forEach { (key, group) ->

            val personTotal = group.sumOf {
                currentAmount(it)
            }

            personTotalViews[key]?.text =
                MoneyData.formatAmount(personTotal)
        }
    }

    // RENDER

    private fun renderEntries() {

        enterBar.removeAllViews()
        inputBar.removeAllViews()

        personTotalViews.clear()

        updateSaveButton()

        // TOTAL

        val total = entries.sumOf {
            currentAmount(it)
        }

        addLeftText("TOTAL", bold = true)

        grandTotalView = addRightValue(
            MoneyData.formatAmount(total),
            bold = true,
            clearAction = if (editing) {
                {
                    entries.forEach { entry ->
                        draftAmounts[entry.id] = "0"
                    }

                    renderEntries()
                }
            } else {
                null
            }
        )

        // PEOPLE

        val groups = groupedEntries()

        groups.forEach { (key, group) ->

            val name = displayName(group)

            val personTotal = group.sumOf {
                currentAmount(it)
            }

            addPersonHeader(
                key,
                name,
                personTotal,
                group
            )

            if (key in expanded) {

                group.forEach { entry ->
                    addDateRow(entry)
                }
            }
        }

        updateSaveButton()
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
                dp(rowHeight)
            )
        }

        val text = TextView(this).apply {
            this.text = value
            setTextColor(color)
            textSize = 15f

            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(5),
                0,
                dp(5),
                0
            )

            if (bold) {
                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
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
        textColor: Int = white,
        clearAction: (() -> Unit)? = null
    ): TextView {

        val row = createRightRow()

        val text = TextView(this).apply {
            this.text = value
            setTextColor(textColor)
            textSize = 16f

            gravity = Gravity.END or Gravity.CENTER_VERTICAL

            setPadding(
                dp(5),
                0,
                dp(5),
                0
            )

            if (bold) {
                setTypeface(
                    null,
                    android.graphics.Typeface.BOLD
                )
            }

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        }

        row.addView(text)

        // ALWAYS RESERVE CLEAR BUTTON SPACE

        if (clearAction != null) {
            row.addView(createClearButton(clearAction))
        } else {
            row.addView(createClearButtonSpace())
        }

        inputBar.addView(row)

        return text
    }

    // PERSON HEADER

    private fun addPersonHeader(
        key: String,
        name: String,
        total: Double,
        group: List<PersonEntry>
    ) {

        // LEFT SIDE

        val leftRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(rowHeight)
            )
        }

        // NAME IS ALWAYS READ-ONLY

        val nameText = TextView(this).apply {
            text = name
            setTextColor(white)
            textSize = 15f

            gravity = Gravity.CENTER_VERTICAL

            setPadding(
                dp(5),
                0,
                dp(5),
                0
            )

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        }

        leftRow.addView(nameText)

        // EXPAND / COLLAPSE BUTTON

        val arrow = Button(this).apply {
            text = if (key in expanded) "∧" else "v"
            textSize = 10f
            setTextColor(white)
            setPadding(0, 0, 0, 0)

            setTypeface(null, if (key in expanded) Typeface.BOLD else Typeface.NORMAL)

            backgroundTintList =
                ColorStateList.valueOf(gray)

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

            setPadding(
                dp(5),
                0,
                dp(5),
                0
            )

            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        }

        personTotalViews[key] = amountText

        rightRow.addView(amountText)

        // PERSON CLEAR BUTTON

        if (editing) {

            rightRow.addView(
                createClearButton {
                    group.forEach { entry ->
                        draftAmounts[entry.id] = "0"
                    }

                    renderEntries()
                }
            )

        } else {

            rightRow.addView(createClearButtonSpace())
        }

        inputBar.addView(rightRow)
    }

    // DATE ROW

    private fun addDateRow(entry: PersonEntry) {

        // LEFT DATE - ALWAYS READ-ONLY

        addLeftText(
            "    ${entry.date}",
            color = gray
        )

        // RIGHT AMOUNT

        if (editing) {

            val amountInput = createAmountEditText(
                draftAmounts[entry.id]
                    ?: MoneyData.formatAmount(entry.amount)
            )

            amountInput.addTextChangedListener(
                object : TextWatcher {

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                        draftAmounts[entry.id] =
                            s?.toString() ?: ""

                        updateSaveButton()
                        updateDisplayedTotals()
                    }

                    override fun afterTextChanged(
                        s: Editable?
                    ) {
                    }
                }
            )

            val rightRow = createRightRow()

            rightRow.addView(
                amountInput,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1f
                )
            )

            // DATE CLEAR BUTTON

            rightRow.addView(
                createClearButton {
                    amountInput.setText("0")
                }
            )

            inputBar.addView(rightRow)

        } else {

            addRightValue(
                MoneyData.formatAmount(entry.amount),
                textColor = gray
            )
        }
    }

    // CREATE RIGHT ROW

    private fun createRightRow(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(rowHeight)
            )
        }
    }

    // INVISIBLE CLEAR BUTTON SPACE

    private fun createClearButtonSpace(): View {

        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                dp(clearButtonWidth),
                dp(30)
            )

            visibility = View.INVISIBLE
        }
    }

    // CLEAR BUTTON

    private fun createClearButton(
        action: () -> Unit
    ): Button {

        return Button(this).apply {
            text = "C"
            textSize = 10f
            setTextColor(white)
            setPadding(0, 0, 0, 0)

            backgroundTintList =
                ColorStateList.valueOf(gray)

            layoutParams = LinearLayout.LayoutParams(
                dp(clearButtonWidth),
                dp(30)
            )

            setOnClickListener {
                action()
                updateSaveButton()
                updateDisplayedTotals()
            }
        }
    }

    // AMOUNT EDIT FIELD

    private fun createAmountEditText(
        value: String
    ): EditText {

        return EditText(this).apply {
            setText(value)

            setTextColor(gray)
            setHintTextColor(gray)

            textSize = 15f

            setPadding(
                dp(5),
                0,
                dp(5),
                0
            )

            background = null

            inputType =
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL

            gravity = Gravity.END or Gravity.CENTER_VERTICAL

            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // SAVE CHANGES

    private fun saveChanges() {

        if (!editing || !canSave) return

        val updated = mutableListOf<PersonEntry>()
        var totalDecrease = 0.0

        // VALIDATE AND UPDATE EACH ORIGINAL ENTRY

        entries.forEach { entry ->

            val amountText =
                draftAmounts[entry.id] ?: ""

            val newAmount =
                parseEditableAmount(amountText)

            if (
                newAmount == null ||
                newAmount < 0.0 ||
                newAmount > entry.amount
            ) {
                updateSaveButton()
                return
            }

            totalDecrease += entry.amount - newAmount

            updated.add(
                entry.copy(
                    amount = newAmount
                )
            )
        }

        // REMOVE PEOPLE WHOSE COMBINED TOTAL IS ZERO.
        // THIS HAPPENS ONLY AFTER SAVE.

        val remaining = updated
            .groupBy {
                it.name.trim().lowercase(Locale.ROOT)
            }
            .values
            .filter { group ->
                group.sumOf { it.amount } > 0.0
            }
            .flatten()
            .toMutableList()

        // SAVE THE UPDATED HISTORY FIRST.

        MoneyData.replacePeople(
            this,
            isDebt,
            remaining
        )

        // RETURN THE REDUCED AMOUNT TO THE CORRESPONDING MONEY TOTAL.
        // APPLY THIS ONLY AFTER THE HISTORY SAVE IS COMMITTED.

        if (totalDecrease > 0.0) {
            if (isDebt) {
                MoneyData.addToSpent(this, totalDecrease)
            } else {
                MoneyData.addToAllowance(this, totalDecrease)
            }
        }

        entries = remaining

        editing = false
        draftAmounts.clear()

        renderEntries()
    }

    // CANCEL EDITING

    private fun cancelEditing() {

        editing = false
        draftAmounts.clear()

        renderEntries()
    }

    // DP CONVERSION

    private fun dp(value: Int): Int {
        return (
                value * resources.displayMetrics.density
                ).toInt()
    }
}
