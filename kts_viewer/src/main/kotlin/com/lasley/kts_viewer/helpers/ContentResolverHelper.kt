package com.lasley.kts_viewer.helpers

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import com.lasley.kts_viewer.data.UriToken
import com.lasley.kts_viewer.data.toMap

/**
 * Converter class for java/ kotlin code to android code.
 *
 * Purpose: separate android-only items from classes, for easier unit testing without android mocks
 */
class ContentResolverHelper(
    private val contentResolver: ContentResolver
) {
    // todo; replace "Cursor" with non-cursor object?
    fun query(
        path: String,
        projection: List<String>? = null,
        selection: String? = null,
        selectionArgs: List<String>? = null,
        sortOrder: String? = null,
        cancelRequest: (CancelAction) -> Unit = {}
    ): Cursor? {
        val pathUri = Uri.parse(path)
        val cancelSignal = CancellationSignal()

        CancelAction.builder {
            cancelCalled = { cancelSignal.cancel() }
            cancelRequest(this)
            cancelSignal.setOnCancelListener { cancelListener?.onCancel() }
        }

        return contentResolver.query(
            pathUri,
            projection?.toTypedArray(),
            selection,
            selectionArgs?.toTypedArray(),
            sortOrder,
            cancelSignal
        )
    }

    /**
     * Inserts a row into a table at the given URL.
     *
     * If the content provider supports transactions the insertion will be atomic.
     *
     * @param path The URL of the table to insert into.
     * @param content The initial values for the newly inserted row. The key is the column name for
     *               the field. Passing an empty ContentValues will create an empty row.
     * @return The URL (as a [Map] of the newly created row.
     * May return [null] if the underlying content provider returns [null].
     */
    fun insert(
        path: String,
        content: Map<String, Any?>? = null
    ): Map<UriToken, Any?>? {
        val pathUri = Uri.parse(path)
        val values = mapToValues(content)
        return tryOrNull { contentResolver.insert(pathUri, values)?.toMap }
    }

    /**
     * Deletes row(s) specified by a content URI.
     *
     * If the content provider supports transactions, the deletion will be atomic.
     *
     * @param path The URL of the row to delete.
     * @param where A filter to apply to rows before deleting, formatted as an SQL WHERE clause
    (excluding the WHERE itself).
     * @return The number of rows deleted.
     */
    fun delete(
        path: String,
        where: String? = null,
        vararg args: String
    ): Int {
        val pathUri = Uri.parse(path)
        return contentResolver.delete(pathUri, where, args)
    }

    /**
     * Update row(s) in a content URI.
     *
     * If the content provider supports transactions the update will be atomic.
     *
     * @param path The URI to modify.
     * @param content The new field values. The key is the column name for the field.
     *   A null value will remove an existing field value.
     * @param where A filter to apply to rows before updating, formatted as an SQL WHERE clause
     *   (excluding the WHERE itself).
     * @return the number of rows updated, or null if unable to complete the transaction.
     */
    fun update(
        path: String,
        content: Map<String, Any?>? = null,
        where: String? = null,
        vararg selectionArgs: String
    ): Int? {
        val pathUri = Uri.parse(path)
        val values = mapToValues(content)
        return tryOrNull { contentResolver.update(pathUri, values, where, selectionArgs) }
    }

    private fun mapToValues(data: Map<String, Any?>?): ContentValues {
        return ContentValues().apply {
            data?.forEach { (key, value) ->
                if (value == null)
                    putNull(key)
                else
                    when (value) {
                        is String -> put(key, value)
                        is Byte -> put(key, value)
                        is Short -> put(key, value)
                        is Int -> put(key, value)
                        is Long -> put(key, value)
                        is Float -> put(key, value)
                        is Double -> put(key, value)
                        is Boolean -> put(key, value)
                        is ByteArray -> put(key, value)
                    }
            }
        }
    }
}

/**
 * Provides [CancellationSignal] to non-android code
 */
class CancelAction {
    var cancelCalled: () -> Unit = {}
        internal set
    var cancelListener: OnCancelListener? = null
        private set

    fun cancel() {
        cancelCalled
    }

    fun setCancelListener(listener: OnCancelListener) {
        cancelListener = listener
    }

    companion object {
        fun builder(config: CancelAction.() -> Unit) = CancelAction().apply(config)
    }


    interface OnCancelListener {
        /**
         * Called when [CancellationSignal.cancel] is invoked.
         */
        fun onCancel()
    }

}