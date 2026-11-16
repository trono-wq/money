
package com.example.money

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class MoneyEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val allowance: Double,
    val spent: Double
)

data class PersonEntry(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val date: String
)

object MoneyData {

    private const val PREFS = "money_data"
    private const val MONEY_KEY = "money_entries"
    private const val DEBT_KEY = "debt_entries"
    private const val CREDIT_KEY = "credit_entries"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun readArray(context: Context, key: String): JSONArray {
        val saved = prefs(context).getString(key, "[]") ?: "[]"

        return try {
            JSONArray(saved)
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun writeArray(
        context: Context,
        key: String,
        array: JSONArray
    ) {
        prefs(context).edit()
            .putString(key, array.toString())
            .apply()
    }

    // MONEY ENTRIES

    fun getMoneyEntries(context: Context): MutableList<MoneyEntry> {
        val array = readArray(context, MONEY_KEY)
        val result = mutableListOf<MoneyEntry>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            result.add(
                MoneyEntry(
                    id = item.optString("id"),
                    date = item.optString("date"),
                    allowance = item.optDouble("allowance"),
                    spent = item.optDouble("spent")
                )
            )
        }

        return result
    }

    fun addMoneyEntry(
        context: Context,
        date: String,
        allowance: Double,
        spent: Double
    ) {
        val entries = getMoneyEntries(context)

        entries.add(
            MoneyEntry(
                date = date,
                allowance = allowance,
                spent = spent
            )
        )

        val array = JSONArray()

        entries.forEach { entry ->
            array.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("date", entry.date)
                    put("allowance", entry.allowance)
                    put("spent", entry.spent)
                }
            )
        }

        writeArray(context, MONEY_KEY, array)
    }

    // DEBT / CREDIT ENTRIES

    fun getPeople(
        context: Context,
        isDebt: Boolean
    ): MutableList<PersonEntry> {
        val key = if (isDebt) DEBT_KEY else CREDIT_KEY
        val array = readArray(context, key)
        val result = mutableListOf<PersonEntry>()

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            result.add(
                PersonEntry(
                    id = item.optString("id"),
                    name = item.optString("name"),
                    amount = item.optDouble("amount"),
                    date = item.optString("date")
                )
            )
        }

        return result
    }

    fun addPeople(
        context: Context,
        isDebt: Boolean,
        people: List<PersonEntry>
    ) {
        val key = if (isDebt) DEBT_KEY else CREDIT_KEY
        val existing = getPeople(context, isDebt)

        existing.addAll(people)

        savePeople(context, key, existing)
    }

    fun replacePeople(
        context: Context,
        isDebt: Boolean,
        people: List<PersonEntry>
    ) {
        val key = if (isDebt) DEBT_KEY else CREDIT_KEY
        savePeople(context, key, people)
    }

    private fun savePeople(
        context: Context,
        key: String,
        people: List<PersonEntry>
    ) {
        val array = JSONArray()

        people.forEach { person ->
            array.put(
                JSONObject().apply {
                    put("id", person.id)
                    put("name", person.name)
                    put("amount", person.amount)
                    put("date", person.date)
                }
            )
        }

        writeArray(context, key, array)
    }

    // TOTALS

    fun getTotalAllowance(context: Context): Double =
        getMoneyEntries(context).sumOf { it.allowance }

    fun getTotalSpent(context: Context): Double =
        getMoneyEntries(context).sumOf { it.spent }

    fun getTotalDebt(context: Context): Double =
        getPeople(context, true).sumOf { it.amount }

    fun getTotalCredit(context: Context): Double =
        getPeople(context, false).sumOf { it.amount }

    fun getLeftover(context: Context): Double =
        getTotalAllowance(context) - getTotalSpent(context)

    // NUMBER FORMATTING

    fun parseAmount(value: String): Double {
        return value
            .replace(" ", "")
            .toDoubleOrNull() ?: 0.0
    }

    fun formatAmount(value: Double): String {
        return String.format(
            java.util.Locale.US,
            "%,.2f",
            value
        ).trimEnd('0').trimEnd('.')
    }
}
