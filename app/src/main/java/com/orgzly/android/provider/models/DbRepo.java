package com.orgzly.android.provider.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.orgzly.android.provider.DatabaseUtils;
import com.orgzly.android.provider.Provider;
import com.orgzly.android.provider.ProviderContract;

/**
 * User-configured repositories.
 */
public class DbRepo {
    public static final String TABLE = "repos";

    public static final String[] CREATE_SQL = new String[] {
            "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
            BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Columns.REPO_URL + " TEXT NOT NULL, " +
            Columns.IS_REPO_ACTIVE + " INTEGER DEFAULT 1, " +
            "UNIQUE (" + Columns.REPO_URL + "))"
    };

    public static final String DROP_SQL = "DROP TABLE IF EXISTS " + TABLE;

    /**
     * Inserts new URL or updates existing marking it as active.
     */
    public static long insert(Context context, SQLiteDatabase db, String url) {
        ContentValues values = new ContentValues();
        values.put(Column.REPO_URL, url);
        values.put(Column.IS_REPO_ACTIVE, 1);

        long id = DatabaseUtils.getId(
                db,
                TABLE,
                Column.REPO_URL + "=?",
                new String[] { url });

        if (id > 0) {
            db.update(TABLE, values, Column._ID + "=" + id, null);
        } else {
            id = db.insertOrThrow(TABLE, null, values);
        }

        notify(context);

        return id;
    }

    /**
     * Delete repos by marking them as inactive.
     */
    public static int delete(Context context, SQLiteDatabase db, String selection, String[] selectionArgs) {
        ContentValues values = new ContentValues();
        values.put(Column.IS_REPO_ACTIVE, 0);

        int result = db.update(TABLE, values, selection, selectionArgs);

        notify(context);

        return result;
    }

    public static int update(Context context, SQLiteDatabase db, ContentValues contentValues, String selection, String[] selectionArgs) {
        int result = db.update(TABLE, contentValues, selection, selectionArgs);
        notify(context);
        return result;
    }

    /*
     * TODO: Try doing notifyChange in all models, instead of Provider
     * It's easy to search for it and find all table uses and notifications are at one place.
     */
    private static void notify(Context context) {
        Provider.notifyChange(context, ProviderContract.Repos.ContentUri.repos());

        /* Books view is using repo table. */
        Provider.notifyChange(context, ProviderContract.Books.ContentUri.books());
    }

    public interface Columns {
        String REPO_URL = "repo_url";
        String IS_REPO_ACTIVE = "is_repo_active";
    }

    public static class Column implements Columns, BaseColumns {}
}
