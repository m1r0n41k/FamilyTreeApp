package co.familytreeapp.util

import android.app.Activity
import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import co.familytreeapp.R
import co.familytreeapp.ui.NavigationParameters

/**
 * Type definition for an action to be preformed when a view in the list has been clicked.
 *
 * This is a function type with its parameters as the view that was clicked and the
 * [layout position][RecyclerView.ViewHolder.getLayoutPosition] of the ViewHolder. The function does
 * not return anything.
 */
typealias OnItemClick = (view: View, position: Int) -> Unit

/**
 * Converts a dip value into pixels.
 */
fun Context.dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()
fun View.dpToPx(dp: Int) = (dp * resources.displayMetrics.density).toInt()

/**
 * Returns a layout with an added navigation drawer (using the template `activity_navigation.xml`).
 *
 * @param layoutRes the layout resource of the activity
 * @return the inflated layout resource inside the content frame of the navigation layout
 */
fun Context.withNavigation(@LayoutRes layoutRes: Int): View {
    val layoutInflater = LayoutInflater.from(this)

    val navigationLayout = layoutInflater.inflate(R.layout.activity_navigation, null)
    val activityLayout = layoutInflater.inflate(layoutRes, null)

    navigationLayout.findViewById<FrameLayout>(R.id.content_frame).apply {
        removeAllViews()
        addView(activityLayout)
    }

    return navigationLayout
}

/**
 * Helper function for returning a [NavigationParameters] using values from the default navigation
 * layout (from template `activity_navigation.xml`).
 *
 * @param navigationItem    an integer representing the navigation item in the menu
 * @param toolbar           the [Toolbar] being used in the layout of the activity (subclass of
 *                          [NavigationDrawerActivity])
 * @see NavigationParameters
 */
fun Activity.standardNavigationParams(navigationItem: Int, toolbar: Toolbar) = NavigationParameters(
        navigationItem,
        findViewById(R.id.drawerLayout),
        findViewById(R.id.navigationView),
        toolbar
)