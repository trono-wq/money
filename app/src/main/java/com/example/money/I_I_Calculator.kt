
package com.example.money

import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat
import org.json.JSONArray
import java.math.BigDecimal
import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.MotionEvent
import android.widget.ScrollView

class Calculator : AppCompatActivity() {

    private lateinit var display: EditText

    private var cursorPosition = 0
    private var rawToDisplay = listOf(0)
    private var displayToRaw = listOf(0)

    private lateinit var historyPanel: LinearLayout
    private lateinit var historyItems: LinearLayout
    private lateinit var historyButton: AppCompatButton
    private lateinit var clearButton: AppCompatButton

    private lateinit var preferences: SharedPreferences

    private lateinit var historyScroll: ScrollView

    private var expression = ""
    private var historyVisible = false

    private val history = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContentView(R.layout.i_i_calculator)

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

        display = findViewById(R.id.calculator)

// DISPLAY - Cursor
        display.setOnFocusChangeListener { _, hasFocus ->
            display.isCursorVisible = hasFocus
        }

        display.showSoftInputOnFocus = false
        display.isSingleLine = true
        display.setHorizontallyScrolling(true)
        display.isFocusable = true
        display.isFocusableInTouchMode = true

// DISPLAY - Initial Focus
        display.requestFocus()
        display.isCursorVisible = true


        historyPanel = findViewById(R.id.history_panel)
        historyScroll = findViewById(R.id.history_scroll)
        historyItems = findViewById(R.id.history_items)

        historyButton = findViewById(R.id.c_h)
        clearButton = findViewById(R.id.c_c)

        preferences = getSharedPreferences(
            "calculator_history",
            MODE_PRIVATE
        )

        loadHistory()

        setupButtons()
        setupNavigation()

        updateDisplay()
    }

    // SCREEN - Hide Cursor When Tapping Outside
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        if (ev.action == MotionEvent.ACTION_DOWN && ::display.isInitialized) {

            val x = ev.rawX.toInt()
            val y = ev.rawY.toInt()

            val displayBounds = Rect()
            display.getGlobalVisibleRect(displayBounds)

            val buttonIds = listOf(
                R.id.c_h,
                R.id.c_c,
                R.id.c_backspace,
                R.id.c_divide,
                R.id.c_0,
                R.id.c_1,
                R.id.c_2,
                R.id.c_3,
                R.id.c_4,
                R.id.c_5,
                R.id.c_6,
                R.id.c_7,
                R.id.c_8,
                R.id.c_9,
                R.id.c_add,
                R.id.c_subtract,
                R.id.c_multiply,
                R.id.c_decimal,
                R.id.c_parenthesis,
                R.id.c_equal
            )

            val tappedButton = buttonIds.any { id ->
                val button = findViewById<View>(id)
                val bounds = Rect()
                button.getGlobalVisibleRect(bounds)
                bounds.contains(x, y)
            }

            if (!displayBounds.contains(x, y) && !tappedButton) {
                display.clearFocus()
                display.isCursorVisible = false
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    // BUTTONS - Interact

    private fun setupButtons() {

        val numberButtons = mapOf(
            R.id.c_0 to "0",
            R.id.c_1 to "1",
            R.id.c_2 to "2",
            R.id.c_3 to "3",
            R.id.c_4 to "4",
            R.id.c_5 to "5",
            R.id.c_6 to "6",
            R.id.c_7 to "7",
            R.id.c_8 to "8",
            R.id.c_9 to "9"
        )

        numberButtons.forEach { (id, value) ->
            findViewById<AppCompatButton>(id).setOnClickListener {
                addInput(value)
            }
        }

        findViewById<AppCompatButton>(R.id.c_add)
            .setOnClickListener { addInput("+") }

        findViewById<AppCompatButton>(R.id.c_subtract)
            .setOnClickListener { addInput("-") }

        findViewById<AppCompatButton>(R.id.c_multiply)
            .setOnClickListener { addInput("*") }

        findViewById<AppCompatButton>(R.id.c_divide)
            .setOnClickListener { addInput("/") }

        findViewById<AppCompatButton>(R.id.c_decimal)
            .setOnClickListener { addInput(".") }

        findViewById<AppCompatButton>(R.id.c_parenthesis)
            .setOnClickListener {
                addParenthesis()
            }

        findViewById<AppCompatButton>(R.id.c_equal)
            .setOnClickListener {
                calculate()
            }

        findViewById<AppCompatButton>(R.id.c_backspace)
            .setOnClickListener {
                backspace()
            }

        historyButton.setOnClickListener {
            toggleHistory()
        }

        clearButton.setOnClickListener {
            if (historyVisible) {
                clearHistory()
            } else {
                clearCalculator()
            }
        }
    }

    // INPUT - Cursor Position

    private fun getCursorPosition(): Int {

        val selection = display.selectionStart
            .coerceIn(0, displayToRaw.lastIndex)

        return displayToRaw[selection]
            .coerceIn(0, expression.length)
    }

    // INPUT - Calculator

    private fun addInput(value: String) {

        if (expression == "Error") {
            expression = ""
            cursorPosition = 0
        }

        cursorPosition = getCursorPosition()

        // INPUT - Prevent Zero as First Digit
        if (value == "0") {

            val previous = expression.getOrNull(cursorPosition - 1)

            if (
                cursorPosition == 0 ||
                previous in listOf('+', '-', '*', '/', '(')
            ) {
                return
            }
        }

        // INPUT - Decimal
        if (value == ".") {

            var start = cursorPosition

            while (
                start > 0 &&
                (expression[start - 1].isDigit() || expression[start - 1] == '.')
            ) {
                start--
            }

            var end = cursorPosition

            while (
                end < expression.length &&
                (expression[end].isDigit() || expression[end] == '.')
            ) {
                end++
            }

            if (expression.substring(start, end).contains(".")) return

            if (start == cursorPosition) {
                expression = expression.substring(0, cursorPosition) +
                        "0" +
                        expression.substring(cursorPosition)
                cursorPosition++
            }
        }

        // INPUT - Operators
        if (value in listOf("+", "-", "*", "/")) {

            val previous = expression.getOrNull(cursorPosition - 1)
            val next = expression.getOrNull(cursorPosition)
            val beforePrevious = expression.getOrNull(cursorPosition - 2)

            // INPUT - Negative Number
            if (value == "-") {

                // INPUT - Negative Number at Beginning
                if (cursorPosition == 0) {

                    if (expression.isNotEmpty()) return

                    expression = "(-)"
                    cursorPosition = 2

                    updateDisplay()
                    return
                }

                // Prevent Duplicate Unary Minus
                if (
                    previous == '-' &&
                    (
                            cursorPosition == 1 ||
                                    beforePrevious == '(' ||
                                    beforePrevious in listOf('+', '-', '*', '/')
                            )
                ) {
                    return
                }

                // INPUT - Negative Operand After Operator
                if (previous in listOf('+', '-', '*', '/')) {

                    if (next == '(' || next == '-') return

                    expression = expression.substring(0, cursorPosition) +
                            "(-)" +
                            expression.substring(cursorPosition)

                    cursorPosition += 2

                    updateDisplay()
                    return
                }
            }

            // INPUT - Positive Operators
            if (value in listOf("+", "*", "/")) {

                if (cursorPosition == 0) return

                if (previous in listOf('+', '-', '*', '/')) {

                    // Do Not Replace Unary Minus
                    if (
                        previous == '-' &&
                        (
                                cursorPosition == 1 ||
                                        beforePrevious == '(' ||
                                        beforePrevious in listOf('+', '-', '*', '/')
                                )
                    ) {
                        return
                    }

                    expression = expression.removeRange(
                        cursorPosition - 1,
                        cursorPosition
                    )

                    cursorPosition--
                }
            }
        }

        // INPUT - Automatic Multiplication After )
        if (
            value.firstOrNull()?.isDigit() == true &&
            expression.getOrNull(cursorPosition - 1) == ')'
        ) {
            expression = expression.substring(0, cursorPosition) +
                    "*" +
                    expression.substring(cursorPosition)

            cursorPosition++
        }

        // INPUT - Insert
        expression = expression.substring(0, cursorPosition) +
                value +
                expression.substring(cursorPosition)

        cursorPosition += value.length

        updateDisplay()
    }





    private fun addParenthesis() {

        cursorPosition = getCursorPosition()

        val openCount = expression.count { it == '(' }
        val closeCount = expression.count { it == ')' }

        val previous = expression.getOrNull(cursorPosition - 1)

        val insertion = if (openCount == closeCount) {

            if (previous?.isDigit() == true || previous == ')') {
                "*("
            } else {
                "("
            }

        } else {

            if (previous?.isDigit() == true || previous == ')') {
                ")"
            } else {
                "("
            }
        }

        expression = expression.substring(0, cursorPosition) +
                insertion +
                expression.substring(cursorPosition)

        cursorPosition += insertion.length

        updateDisplay()
    }

    private fun backspace() {

        cursorPosition = getCursorPosition()

        if (cursorPosition > 0 && expression.isNotEmpty()) {

            expression = expression.removeRange(
                cursorPosition - 1,
                cursorPosition
            )

            cursorPosition--

            updateDisplay()
        }
    }

    private fun clearCalculator() {

        expression = ""
        cursorPosition = 0

        updateDisplay()
    }

    // DISPLAY - Formatting

    private fun updateDisplay() {

        val output = StringBuilder()

        val newRawToDisplay = MutableList(expression.length + 1) { 0 }
        val newDisplayToRaw = mutableListOf(0)

        fun appendText(text: String, rawPosition: Int) {

            text.forEach { character ->
                output.append(character)
                newDisplayToRaw.add(rawPosition)
            }
        }

        var index = 0

        while (index < expression.length) {

            val character = expression[index]

            if (character.isDigit()) {

                var integerEnd = index

                while (
                    integerEnd < expression.length &&
                    expression[integerEnd].isDigit()
                ) {
                    integerEnd++
                }

                val integerLength = integerEnd - index
                var digitIndex = 0

                while (index < integerEnd) {

                    if (
                        digitIndex > 0 &&
                        (integerLength - digitIndex) % 3 == 0
                    ) {
                        appendText(" ", index)
                    }

                    newRawToDisplay[index] = output.length

                    appendText(
                        expression[index].toString(),
                        index + 1
                    )

                    index++
                    digitIndex++
                }

                continue
            }

            if (character == '.') {

                newRawToDisplay[index] = output.length

                appendText(".", index + 1)

                index++
                continue
            }

            if (character in listOf('+', '-', '*', '/')) {

                if (output.isNotEmpty() && output.last() != ' ') {
                    appendText(" ", index)
                }

                newRawToDisplay[index] = output.length

                val symbol = when (character) {
                    '*' -> "×"
                    '/' -> "÷"
                    else -> character.toString()
                }

                appendText(symbol, index + 1)
                appendText(" ", index + 1)

                index++
                continue
            }

            newRawToDisplay[index] = output.length

            appendText(character.toString(), index + 1)

            index++
        }

        newRawToDisplay[expression.length] = output.length

        rawToDisplay = newRawToDisplay
        displayToRaw = newDisplayToRaw

        val finalText = output.toString().trimEnd()

        display.setText(finalText)

        display.typeface = if (expression == "Error") {
            Typeface.DEFAULT_BOLD
        } else {
            Typeface.DEFAULT
        }

        cursorPosition = cursorPosition.coerceIn(0, expression.length)

        val displayCursor = rawToDisplay[cursorPosition]
            .coerceIn(0, display.length())

        display.setSelection(displayCursor)
    }

    // CALCULATION - Logic

    private fun calculate() {

        if (expression.isBlank()) return

        try {

            // CALCULATION - Complete Missing Parentheses
            val openCount = expression.count { it == '(' }
            val closeCount = expression.count { it == ')' }

            if (openCount > closeCount) {
                expression += ")".repeat(openCount - closeCount)
            }

            val originalExpression = expression
            val result = evaluateExpression(expression)

            val formattedResult = BigDecimal.valueOf(result)
                .stripTrailingZeros()
                .toPlainString()

            val historyEntry = "$originalExpression = $formattedResult"

            if (result != 0.0) {
                history.add(0, historyEntry)
            }

            while (history.size > 5) {
                history.removeAt(history.lastIndex)
            }

            saveHistory()

            expression = formattedResult
            cursorPosition = expression.length

            updateDisplay()

        } catch (e: Exception) {

            expression = "Error"
            cursorPosition = expression.length

            updateDisplay()
        }

        if (historyVisible) {
            displayHistory()
        }
    }

    // EXPRESSION EVALUATOR - Logic

    private fun evaluateExpression(input: String): Double {

        val tokens = mutableListOf<String>()
        var number = ""

        for (char in input) {

            when {

                char.isDigit() || char == '.' -> number += char

                char in listOf('+', '-', '*', '/', '(', ')') -> {

                    if (number.isNotEmpty()) {
                        tokens.add(number)
                        number = ""
                    }

                    tokens.add(char.toString())
                }

                else -> throw IllegalArgumentException()
            }
        }

        if (number.isNotEmpty()) {
            tokens.add(number)
        }

        class Parser {

            var position = 0

            fun parseExpression(): Double {

                var result = parseTerm()

                while (position < tokens.size) {

                    when (tokens[position]) {

                        "+" -> {
                            position++
                            result += parseTerm()
                        }

                        "-" -> {
                            position++
                            result -= parseTerm()
                        }

                        else -> return result
                    }
                }

                return result
            }

            fun parseTerm(): Double {

                var result = parseFactor()

                while (position < tokens.size) {

                    when (tokens[position]) {

                        "*" -> {
                            position++
                            result *= parseFactor()
                        }

                        "/" -> {

                            position++

                            val divisor = parseFactor()

                            if (divisor == 0.0) {
                                throw ArithmeticException()
                            }

                            result /= divisor
                        }

                        else -> return result
                    }
                }

                return result
            }

            fun parseFactor(): Double {

                if (position >= tokens.size) {
                    throw IllegalArgumentException()
                }

                val token = tokens[position++]

                return when (token) {

                    "(" -> {

                        val result = parseExpression()

                        if (
                            position >= tokens.size ||
                            tokens[position] != ")"
                        ) {
                            throw IllegalArgumentException()
                        }

                        position++

                        result
                    }

                    "-" -> -parseFactor()

                    "+" -> parseFactor()

                    else -> token.toDouble()
                }
            }
        }

        val parser = Parser()
        val result = parser.parseExpression()

        if (parser.position != tokens.size || !result.isFinite()) {
            throw IllegalArgumentException()
        }

        return result
    }

    // HISTORY - Display

    private fun toggleHistory() {

        if (historyVisible) {
            closeHistory()
        } else {
            openHistory()
        }
    }

    private fun openHistory() {

        historyVisible = true

        historyButton.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#999999"))

        clearButton.backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#999999"))

        historyButton.setTextColor(Color.BLACK)
        clearButton.setTextColor(Color.BLACK)

        displayHistory()

        historyPanel.visibility = View.VISIBLE
    }

    private fun closeHistory() {

        historyButton.backgroundTintList = null
        clearButton.backgroundTintList = null

        historyButton.setTextColor(Color.WHITE)
        clearButton.setTextColor(Color.WHITE)

        historyVisible = false

        historyPanel.visibility = View.GONE
    }


// HISTORY - Display

    private fun displayHistory() {

        historyItems.removeAllViews()

        if (history.isEmpty()) {

            val emptyText = TextView(this)

            emptyText.text = "No history"
            emptyText.textSize = 14f
            emptyText.setTypeface(null, Typeface.BOLD)
            emptyText.setTextColor(Color.parseColor("#666666"))
            emptyText.gravity = Gravity.CENTER

            historyItems.addView(emptyText)

        } else {

            history.forEach { entry ->

                val parts = entry.split(" = ", limit = 2)

                // HISTORY ROW - Layout
                val row = LinearLayout(this)

                row.orientation = LinearLayout.HORIZONTAL
                row.gravity = Gravity.TOP
                row.clipChildren = false
                row.clipToPadding = false

                // HISTORY ROW - Expression
                val expressionText = TextView(this)

                expressionText.text = Regex("\\d+(\\.\\d+)?").replace(parts[0]) { match ->
                    formatNumber(match.value)
                }
                    .replace(Regex("(?<=[0-9)])\\+"), " + ")
                    .replace(Regex("(?<=[0-9)])-"), " - ")
                    .replace("*", " × ")
                    .replace("/", " ÷ ")

                expressionText.textSize = 16f
                expressionText.setTextColor(Color.BLACK)
                expressionText.gravity = Gravity.START or Gravity.TOP
                expressionText.setPadding(5, 8, 5, 8)

                // HISTORY ROW - Result
                val resultText = TextView(this)

                resultText.text = formatNumber(
                    parts.getOrElse(1) { "" }
                )

                resultText.textSize = 16f
                resultText.setTextColor(Color.BLACK)
                resultText.typeface = Typeface.DEFAULT_BOLD
                resultText.gravity = Gravity.END or Gravity.TOP
                resultText.setPadding(5, 8, 5, 8)

                // HISTORY ROW - Width
                row.addView(
                    expressionText,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.65f
                    )
                )

                row.addView(
                    resultText,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0.35f
                    )
                )

                historyItems.addView(row)

                // HISTORY ROW - Vertical Alignment
                row.post {

                    val resultOffset =
                        (expressionText.lineCount - 1) *
                                expressionText.lineHeight

                    resultText.translationY = resultOffset.toFloat()

                    val requiredHeight = maxOf(
                        expressionText.height,
                        resultOffset + resultText.height
                    )

                    row.layoutParams = row.layoutParams.apply {
                        height = requiredHeight
                    }
                }
            }
        }

        // HISTORY PANEL - Maximum Height
        historyItems.post {

            val maxHeight =
                (200 * resources.displayMetrics.density).toInt()

            val contentHeight = historyItems.height

            val params = historyScroll.layoutParams

            params.height = minOf(contentHeight, maxHeight)

            historyScroll.layoutParams = params
        }
    }


    // HISTORY - Storage

    private fun loadHistory() {

        val savedHistory = preferences.getString(
            "history",
            "[]"
        ) ?: "[]"

        val jsonArray = JSONArray(savedHistory)

        history.clear()

        for (i in 0 until jsonArray.length()) {
            history.add(jsonArray.getString(i))
        }
    }

    private fun saveHistory() {

        val jsonArray = JSONArray()

        history.forEach {
            jsonArray.put(it)
        }

        preferences.edit()
            .putString("history", jsonArray.toString())
            .apply()
    }

    private fun clearHistory() {

        history.clear()

        saveHistory()
        displayHistory()
    }

    // NAVIGATION - Interact

    private fun setupNavigation() {

        findViewById<TextView>(R.id.ab_money)
            .setOnClickListener {

                val intent = Intent(this, Money::class.java)

                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP

                startActivity(intent)
            }

        findViewById<TextView>(R.id.ab_notes)
            .setOnClickListener {
                startActivity(Intent(this, Notes::class.java))
            }

        findViewById<TextView>(R.id.ab_status)
            .setOnClickListener {
                startActivity(Intent(this, Status::class.java))
            }
    }

    // NUMBER - Formatting
    private fun formatNumber(value: String): String {

        val parts = value.split(".", limit = 2)

        val integerPart = parts[0]

        val decimalPart = if (parts.size > 1) {
            "." + parts[1].chunked(3).joinToString(" ")
        } else {
            ""
        }

        val sign = if (integerPart.startsWith("-")) "-" else ""

        val digits = integerPart.removePrefix("-")

        val formattedInteger = digits.reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()

        return sign + formattedInteger + decimalPart
    }
}
