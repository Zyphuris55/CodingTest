package com.lasley.kts_provider.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.lasley.kts_provider.data.HistoryStamp;


public class DatabaseHistory {

    private static ContentDatabase database;

    public enum HistoryToken {
        Query, Insert, Delete, Update
    }

    public static void init(Context context) {
        database = ContentDatabase.getInstance(context);
    }

    @VisibleForTesting
    public static void init(ContentDatabase testDatabase) {
        database = testDatabase;
    }

    private static void addStep(HistoryStamp step) {
        if (database == null) {
            System.out.println("DatabaseHistory: missing");
            return;
        }
        System.out.println(step);
        database.dataDao().appendHistory(step);
    }

    public static void query(HistoryActionToken result) {
        HistoryStamp step = new HistoryStamp();
        step.actionType = HistoryToken.Query.name();
        step.actionResult = result.name();
        step.timestamp = System.currentTimeMillis();
        addStep(step);
    }

    public static void query(
        HistoryActionToken result,
        String comments
    ) {
        HistoryStamp step = new HistoryStamp();
        step.actionType = HistoryToken.Query.name();
        step.actionResult = result.name();
        step.comments = comments;
        step.timestamp = System.currentTimeMillis();
        addStep(step);
    }

    public static void insert(
        HistoryActionToken result,
        String id
    ) {
        HistoryStamp step = new HistoryStamp();
        step.actionType = HistoryToken.Insert.name();
        step.actionResult = result.name();
        step.itemID = id;
        step.timestamp = System.currentTimeMillis();
        addStep(step);
    }

    public static void insert(
        HistoryActionToken result,
        String id,
        String comment
    ) {
        HistoryStamp step = new HistoryStamp();
        step.actionType = HistoryToken.Insert.name();
        step.actionResult = result.name();
        step.itemID = id;
        step.comments = comment;
        step.timestamp = System.currentTimeMillis();
        addStep(step);
    }

    public static void delete(
        HistoryActionToken result,
        String id
    ) {
        HistoryStamp step = new HistoryStamp();
        step.actionType = HistoryToken.Delete.name();
        step.actionResult = result.name();
        step.itemID = id;
        step.timestamp = System.currentTimeMillis();
        addStep(step);
    }

    public static void update(
        HistoryActionToken result,
        String id
    ) {
        HistoryStamp step = new HistoryStamp();
        step.actionType = HistoryToken.Update.name();
        step.actionResult = result.name();
        step.itemID = id;
        step.timestamp = System.currentTimeMillis();
        addStep(step);
    }

    public static void update(
        HistoryActionToken result,
        String id,
        String comment
    ) {
        HistoryStamp step = new HistoryStamp();
        step.actionType = HistoryToken.Update.name();
        step.actionResult = result.name();
        step.itemID = id;
        step.comments = comment;
        step.timestamp = System.currentTimeMillis();
        addStep(step);
    }
}

enum HistoryActionToken {
    OK, Failed,
    Missing_ID, Unknown_ID, Item_Missing,
    Unknown_URI, Unknown_Type,
    Missing_Content, Parse_Failed
}
