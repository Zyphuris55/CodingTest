package com.lasley.kts_provider;

import com.lasley.kts_provider.database.ContentDatabase;

import org.junit.AfterClass;
import org.junit.BeforeClass;


public class AbstractDatabaseTest {
    static ContentDatabase database;

    @BeforeClass
    public static void setup() {
    }

    @AfterClass
    public static void teardown() {
        if (database != null) {
            database.close();
            database = null;
        }
    }
}
