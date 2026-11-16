
package com.example.money

class Credit : PeopleActivity() {

    override val isDebt = false

    override val layoutRes = R.layout.iii_credit

    override val editButtonId = R.id.cr_b_edit
}
