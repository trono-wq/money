
package com.example.money

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.ViewCompat

class Status : AppCompatActivity() {

    // STATE

    private var editing = false

    private var clearLeftover = false
    private var clearSpent = false
    private var clearAllowance = false
    private var clearDebt = false
    private var clearCredit = false

    private var canSave = false

    // VIEWS

    private lateinit var editButton: Button

    private lateinit var leftoverView: EditText
    private lateinit var spentView: EditText
    private lateinit var allowanceView: EditText
    private lateinit var debtView: EditText
    private lateinit var creditView: EditText

    private lateinit var leftoverClearButton: Button
    private lateinit var spentClearButton: Button
    private lateinit var allowanceClearButton: Button
    private lateinit var debtClearButton: Button
    private lateinit var creditClearButton: Button

    // COLORS

    private val gray = Color.rgb(102, 102, 102)
    private val gold = Color.rgb(211, 154, 0)

    // LAYOUT

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

        // VIEWS

        editButton = findViewById(R.id.s_b_edit)

        leftoverView = findViewById(R.id.s_s_leftover)
        spentView = findViewById(R.id.s_s_spent)
        allowanceView = findViewById(R.id.s_s_allowance)
        debtView = findViewById(R.id.s_s_debt)
        creditView = findViewById(R.id.s_s_credit)

        leftoverClearButton = findViewById(R.id.s_b_leftover)
        spentClearButton = findViewById(R.id.s_b_spent)
        allowanceClearButton = findViewById(R.id.s_b_allowance)
        debtClearButton = findViewById(R.id.s_b_debt)
        creditClearButton = findViewById(R.id.s_b_credit)

        // READ-ONLY VALUES

        setReadOnly(leftoverView)
        setReadOnly(spentView)
        setReadOnly(allowanceView)
        setReadOnly(debtView)
        setReadOnly(creditView)

        // CLEAR BUTTONS

        leftoverClearButton.setOnClickListener {

            if (!editing) return@setOnClickListener

            clearLeftover = true

            updateSaveButton()
            updateDisplayedTotals()
        }

        spentClearButton.setOnClickListener {

            if (!editing) return@setOnClickListener

            clearSpent = true

            updateSaveButton()
            updateDisplayedTotals()
        }

        allowanceClearButton.setOnClickListener {

            if (!editing) return@setOnClickListener

            clearAllowance = true

            updateSaveButton()
            updateDisplayedTotals()
        }

        debtClearButton.setOnClickListener {

            if (!editing) return@setOnClickListener

            clearDebt = true

            updateSaveButton()
            updateDisplayedTotals()
        }

        creditClearButton.setOnClickListener {

            if (!editing) return@setOnClickListener

            clearCredit = true

            updateSaveButton()
            updateDisplayedTotals()
        }

        // EDIT / SAVE

        editButton.setOnClickListener {

            if (!editing) {

                editing = true

                resetPendingChanges()

                updateSaveButton()
                updateDisplayedTotals()

            } else if (canSave) {

                saveChanges()

            } else {

                cancelEditing()
            }
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

            startActivity(Intent(this, Flight::class.java))
        }

        // INITIAL STATE

        updateSaveButton()
        updateDisplayedTotals()
    }

    // READ-ONLY

    private fun setReadOnly(view: EditText) {

        view.isFocusable = false
        view.isFocusableInTouchMode = false
        view.isCursorVisible = false
        view.keyListener = null
    }

    // EDIT STATE

    private fun resetPendingChanges() {

        clearLeftover = false
        clearSpent = false
        clearAllowance = false
        clearDebt = false
        clearCredit = false
    }

    private fun hasChanges(): Boolean {

        return clearLeftover ||
                clearSpent ||
                clearAllowance ||
                clearDebt ||
                clearCredit
    }

    private fun updateSaveButton() {

        if (!editing) {

            canSave = false

            editButton.text = "Edit"
            editButton.isEnabled = true

            editButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(gray)
            )

            setClearButtonsVisible(false)

            return
        }

        canSave = hasChanges()

        editButton.text = "Save"
        editButton.isEnabled = true

        editButton.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(
                if (canSave) gold else gray
            )
        )

        setClearButtonsVisible(true)
    }

    private fun setClearButtonsVisible(visible: Boolean) {

        val visibility =
            if (visible) android.view.View.VISIBLE
            else android.view.View.GONE

        leftoverClearButton.visibility = visibility
        spentClearButton.visibility = visibility
        allowanceClearButton.visibility = visibility
        debtClearButton.visibility = visibility
        creditClearButton.visibility = visibility
    }

    // DISPLAY

    private fun updateDisplayedTotals() {

        val leftover =
            if (clearLeftover) {
                0.0
            } else {
                (if (clearAllowance) 0.0
                else MoneyData.getTotalAllowance(this)) -
                        (if (clearSpent) 0.0
                        else MoneyData.getTotalSpent(this))
            }

        val spent =
            if (clearLeftover || clearSpent) 0.0
            else MoneyData.getTotalSpent(this)

        val allowance =
            if (clearLeftover || clearAllowance) 0.0
            else MoneyData.getTotalAllowance(this)

        val debt =
            if (clearDebt) 0.0
            else MoneyData.getTotalDebt(this)

        val credit =
            if (clearCredit) 0.0
            else MoneyData.getTotalCredit(this)

        leftoverView.setText(MoneyData.formatAmount(leftover))
        spentView.setText(MoneyData.formatAmount(spent))
        allowanceView.setText(MoneyData.formatAmount(allowance))
        debtView.setText(MoneyData.formatAmount(debt))
        creditView.setText(MoneyData.formatAmount(credit))
    }

    // SAVE

    private fun saveChanges() {

        if (!editing || !canSave) return

        // LEFTOVER

        if (clearLeftover) {

            MoneyData.replaceMoneyEntries(
                this,
                MoneyData.getMoneyEntries(this).map { entry ->
                    entry.copy(
                        allowance = 0.0,
                        spent = 0.0
                    )
                }
            )

        } else {

            // SPENT

            if (clearSpent) {

                MoneyData.replaceMoneyEntries(
                    this,
                    MoneyData.getMoneyEntries(this).map { entry ->
                        entry.copy(spent = 0.0)
                    }
                )
            }

            // ALLOWANCE

            if (clearAllowance) {

                MoneyData.replaceMoneyEntries(
                    this,
                    MoneyData.getMoneyEntries(this).map { entry ->
                        entry.copy(allowance = 0.0)
                    }
                )
            }
        }

        // DEBT

        if (clearDebt) {

            MoneyData.replacePeople(
                this,
                true,
                emptyList()
            )
        }

        // CREDIT

        if (clearCredit) {

            MoneyData.replacePeople(
                this,
                false,
                emptyList()
            )
        }

        editing = false

        resetPendingChanges()

        updateSaveButton()
        updateDisplayedTotals()
    }

    // CANCEL

    private fun cancelEditing() {

        editing = false

        resetPendingChanges()

        updateSaveButton()
        updateDisplayedTotals()
    }

    // REFRESH

    override fun onResume() {

        super.onResume()

        if (!editing) {
            updateDisplayedTotals()
        }
    }
}
