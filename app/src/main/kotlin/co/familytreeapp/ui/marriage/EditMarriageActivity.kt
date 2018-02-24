package co.familytreeapp.ui.marriage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import co.familytreeapp.R
import co.familytreeapp.database.manager.MarriagesManager
import co.familytreeapp.database.manager.PersonManager
import co.familytreeapp.model.Marriage
import co.familytreeapp.model.Person
import co.familytreeapp.ui.UiHelper
import co.familytreeapp.ui.Validator
import co.familytreeapp.ui.person.EditPersonActivity
import co.familytreeapp.ui.widget.DateViewHelper
import co.familytreeapp.ui.widget.PersonSelectorHelper
import co.familytreeapp.util.toTitleCase

/**
 * Activity to edit a [Marriage].
 */
class EditMarriageActivity : AppCompatActivity() {

    companion object {

        private const val LOG_TAG = "EditMarriageActivity"

        /**
         * Intent extra key for supplying a [Marriage] to this activity.
         */
        const val EXTRA_MARRIAGE = "extra_marriage"

        /**
         * Request code for starting [EditPersonActivity] for result, to create a new [Person] to be
         * used as the first person of the marriage.
         */
        private const val REQUEST_CREATE_PERSON_1 = 6

        /**
         * Request code for starting [EditPersonActivity] for result, to create a new [Person] to be
         * used as the second person of the marriage.
         */
        private const val REQUEST_CREATE_PERSON_2 = 7
    }

    private lateinit var person1Selector: PersonSelectorHelper
    private lateinit var person2Selector: PersonSelectorHelper

    private lateinit var startDateHelper: DateViewHelper
    private lateinit var placeInput: EditText
    private lateinit var isMarriedCheckBox: CheckBox
    private lateinit var endDateHelper: DateViewHelper

    /**
     * The [Marriage] received via intent extra from the previous activity. If a new marriage is
     * being created (hence no intent extra), then this will be null.
     *
     * This [Marriage] will not be affected by changes made in this activity.
     */
    private var marriage: Marriage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_marriage)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener { sendCancelledResult() }

        marriage = intent.extras?.getParcelable(EXTRA_MARRIAGE)

        if (marriage == null) {
            Log.v(LOG_TAG, "Editing a new marriage")
        } else {
            Log.v(LOG_TAG, "Editing an existing marriage: $marriage")
        }

        assignUiComponents()
        setupLayout()
    }

    /**
     * Assigns the variables for our UI components in the layout.
     */
    private fun assignUiComponents() {
        person1Selector = PersonSelectorHelper(this, findViewById(R.id.editText_person1)).apply {
            setOnCreateNewPerson { _, _ ->
                val intent = Intent(this@EditMarriageActivity, EditPersonActivity::class.java)
                startActivityForResult(intent, REQUEST_CREATE_PERSON_1)
            }
        }
        person2Selector = PersonSelectorHelper(this, findViewById(R.id.editText_person2)).apply {
            setOnCreateNewPerson { _, _ ->
                val intent = Intent(this@EditMarriageActivity, EditPersonActivity::class.java)
                startActivityForResult(intent, REQUEST_CREATE_PERSON_2)
            }
        }

        startDateHelper = DateViewHelper(this, findViewById(R.id.editText_startDate))
        placeInput = findViewById(R.id.editText_placeOfMarriage)

        isMarriedCheckBox = findViewById(R.id.checkbox_married)

        endDateHelper = DateViewHelper(this, findViewById(R.id.editText_endDate))
    }

    private fun setupLayout() {
        UiHelper.setDateRangePickerConstraints(startDateHelper, endDateHelper)

        if (marriage == null) {
            Log.i(LOG_TAG, "Marriage is null - setting up the default layout")
            setupDefaultLayout()
            return
        }

        marriage?.let {
            val personManager = PersonManager(this)
            person1Selector.person = personManager.get(it.person1Id)
            person2Selector.person = personManager.get(it.person2Id)

            setMarriageOngoing(it.isOngoing())
        }
    }

    private fun setupDefaultLayout() {
        setMarriageOngoing(true)
    }

    /**
     * Toggles the checkbox and other UI components for whether or not a marriage [isOngoing].
     * This should be used instead of toggling the checkbox manually.
     */
    private fun setMarriageOngoing(isOngoing: Boolean) {
        isMarriedCheckBox.isChecked = isOngoing
        findViewById<LinearLayout>(R.id.group_endInfo).visibility =
                if (isOngoing) View.VISIBLE else View.GONE
    }

    /**
     * Validates the user input and writes it to the database.
     */
    private fun saveData() {
        // Don't continue with db write if inputs invalid
        val newMarriage = validateMarriage() ?: return

        val marriagesManager = MarriagesManager(this)

        if (marriage == null) {
            marriagesManager.add(newMarriage)
            sendSuccessfulResult(newMarriage)
            return
        }

        if (marriage!! == newMarriage) {
            // Nothing changed, so avoid all db write (nothing will change in result activity)
            sendCancelledResult()
        } else {
            marriagesManager.update(marriage!!.getIds(), newMarriage)
            sendSuccessfulResult(newMarriage)
        }
    }

    /**
     * Validates the user inputs and constructs a [Marriage] object from it.
     *
     * @return  the constructed [Marriage] object if user inputs are valid. If one or more user
     *          inputs are invalid, then this will return null.
     */
    private fun validateMarriage(): Marriage? {
        val validator = Validator(findViewById<CoordinatorLayout>(R.id.coordinatorLayout))

        val person1 = person1Selector.person
        val person2 = person2Selector.person
        if (!validator.checkMarriagePeople(person1, person2)) return null

        // Dates should be ok from dialog constraint, but best to double-check before db write
        val startDate = startDateHelper.date
        val endDate = if (isMarriedCheckBox.isChecked) endDateHelper.date else null
        if (!validator.checkDates(startDate, endDate)) return null

        return Marriage(
                person1!!.id,
                person2!!.id,
                startDate!!,
                endDate,
                placeInput.text.toString().trim().toTitleCase()
        )
    }

    /**
     * Sends an "ok" result back to where this activity was invoked from.
     *
     * @param result    the new/updated/deleted [Marriage]. If deleted this must be null.
     * @see android.app.Activity.RESULT_OK
     */
    private fun sendSuccessfulResult(result: Marriage?) {
        Log.d(LOG_TAG, "Sending successful result: $result")
        val returnIntent = Intent().putExtra(EXTRA_MARRIAGE, result)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    /**
     * Sends a "cancelled" result back to where this activity was invoked from.
     *
     * @see android.app.Activity.RESULT_CANCELED
     */
    private fun sendCancelledResult() {
        Log.d(LOG_TAG, "Sending cancelled result")
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_done -> saveData()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = sendCancelledResult()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CREATE_PERSON_1 -> if (resultCode == Activity.RESULT_OK) {
                val newPerson1 = data!!.getParcelableExtra<Person>(EditPersonActivity.EXTRA_PERSON)
                person1Selector.person = newPerson1
            }
            REQUEST_CREATE_PERSON_2 -> if (resultCode == Activity.RESULT_OK) {
                val newPerson2 = data!!.getParcelableExtra<Person>(EditPersonActivity.EXTRA_PERSON)
                person2Selector.person = newPerson2
            }
        }
    }

}