package com.lasley.kts_provider.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.lasley.kts_provider.database.DatabaseHistory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Entity(tableName = "_historyActions")
public class HistoryStamp {
    @PrimaryKey
    public long timestamp = System.currentTimeMillis();

    /**
     * High-level, what happened?
     * - Query
     * - Insert
     * - Update
     * - Delete
     * - Bulk insert
     */
    @NonNull
    public String actionType = "None";

    /**
     * What was the result of the action:
     * - Success
     * - Not found
     * - Fail (why is in the comments)
     * - Locked
     */
    @Nullable
    public String actionResult;

    /**
     * What content in question was involved in the history action
     */
    @ColumnInfo(name = "item_id", index = true)
    @Nullable
    public String itemID;

    /**
     * More details about the [actionResult]
     */
    @Nullable
    public String comments;


    public String time() {
        Date date = new Date(timestamp);
        DateFormat formatter = new SimpleDateFormat("YYYY-MM-DD @ HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    public HistoryStamp() {
    }

    @NonNull
    @Override
    public String toString() {
        String result = "Action: " + actionType +
            " @{ " + time() + " }";

        if (actionResult != null && !actionResult.isEmpty())
            result += ", Result: " + actionResult;

        if (itemID != null && !itemID.isEmpty())
            result += ", ID: " + itemID;

        if (comments != null && !comments.isEmpty())
            result += ", Comments: " + comments;

        return result;
    }
}
