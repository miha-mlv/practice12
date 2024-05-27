package com.example.practice12;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.Objects;

public class BookProvider extends ContentProvider {

    private static final String AUTHORITY = "com.example.practice12.provider";
    private static final String BASE_PATH = "users";
    public static final Uri CONTENT_URI = Uri.parse("content://com.example.practice12.provider/users");

    private static final int PEOPLE = 100;
    private static final int PEOPLE_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, PEOPLE);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PEOPLE_ID);
    }

    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        DatabaseHelper dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();
        return database != null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(DatabaseHelper.TABLE_NAME);

        int uriType = uriMatcher.match(uri);
        switch (uriType) {
            case PEOPLE_ID:
                queryBuilder.appendWhere(DatabaseHelper.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case PEOPLE:
                // no filter
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        long id = database.insert(DatabaseHelper.TABLE_NAME, "", values);

        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(newUri, null);
            return newUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        int rowsDeleted;
        switch (uriType) {
            case PEOPLE:
                rowsDeleted = database.delete(DatabaseHelper.TABLE_NAME, selection, selectionArgs);
                break;
            case PEOPLE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = database.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_ID + "=" + id + " AND (" + selection + ")", selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = uriMatcher.match(uri);
        int rowsUpdated = 0;
        switch (uriType) {
            case PEOPLE:
                rowsUpdated = database.update(DatabaseHelper.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PEOPLE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = database.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = database.update(DatabaseHelper.TABLE_NAME, values, DatabaseHelper.COLUMN_ID + "=" + id + " AND (" + selection + ")", selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "users.db";
        private static final int DATABASE_VERSION = 1;
        private static final String TABLE_NAME = "users";
        private static final String COLUMN_ID = "_id";
        private static final String COLUMN_NAME = "name";
        private static final String COLUMN_ADDRESS = "surname";
        private static final String COLUMN_AGE = "age";
        private static final String COLUMN_EMAIL = "email";

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT NOT NULL, " +
                    COLUMN_ADDRESS + " TEXT NOT NULL, " +
                    COLUMN_AGE + " INTEGER NOT NULL, " +
                    COLUMN_EMAIL + " TEXT NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}