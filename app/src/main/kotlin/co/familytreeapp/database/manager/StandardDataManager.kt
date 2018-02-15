package co.familytreeapp.database.manager

import android.content.Context
import android.util.Log
import co.familytreeapp.database.DatabaseHelper
import co.familytreeapp.model.StandardData

/**
 * An abstract data manager class which includes methods that all "standard" data managers should
 * inherit/implement.
 *
 * @see DataManager
 */
abstract class StandardDataManager<T : StandardData>(
        private val context: Context
): DataManager<T>(context) {

    companion object {
        private const val LOG_TAG = "StandardDataManager"
    }

    /**
     * The name of the unique (primary key) ID column of the table the subclass is managing.
     */
    abstract val idColumn: String

    /**
     * Updates an item of type [T] with [oldItemId], replacing it with the new [item].
     *
     * @see add
     * @see delete
     */
    fun update(oldItemId: Int, item: T) {
        delete(oldItemId)
        add(item)
    }

    /**
     * Deletes an item of type [T] with specified [id] from the table named [tableName].
     *
     * @see deleteWithReferences
     */
    fun delete(id: Int) {
        val db = DatabaseHelper.getInstance(context).writableDatabase
        db.delete(tableName, "$idColumn=?", arrayOf(id.toString()))
        Log.d(LOG_TAG, "Deleted item (id: $id)")
    }

    /**
     * Deletes an item of type [T] with specified [id] and any other references to it.
     * This function should be overridden by subclasses of [StandardDataManager] to specify which references
     * should be deleted.
     *
     * @see delete
     */
    open fun deleteWithReferences(id: Int) {
        delete(id)
    }

}
