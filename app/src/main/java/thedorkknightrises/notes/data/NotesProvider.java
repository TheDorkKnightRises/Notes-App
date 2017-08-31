package thedorkknightrises.notes.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import thedorkknightrises.notes.BuildConfig;

public class NotesProvider extends ContentProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".data.NotesProvider";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {

        /*
         * Sets the integer value for multiple rows in table to 1. Notice that no wildcard is used
         * in the path
         */
        sUriMatcher.addURI(AUTHORITY, NotesDb.Note.TABLE_NAME, 1);
        /*
         * Sets the code for a single row to 2. In this case, the "#" wildcard is
         * used
         */
        sUriMatcher.addURI(AUTHORITY, NotesDb.Note.TABLE_NAME + "/#", 2);

        sUriMatcher.addURI(AUTHORITY, NotesDb.Checklist.TABLE_NAME, 3);
        sUriMatcher.addURI(AUTHORITY, NotesDb.Checklist.TABLE_NAME + "/#", 4);
    }

    NotesDbHelper mHelper;

    public NotesProvider() {

    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        int delCount = 0;
        SQLiteDatabase db = mHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case 1:
                delCount = db.delete(NotesDb.Note.TABLE_NAME, selection, selectionArgs);
                break;
            case 2:
                String idStr = uri.getLastPathSegment();
                String where = NotesDb.Note._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                delCount = db.delete(NotesDb.Note.TABLE_NAME, where, selectionArgs);
                break;
            case 3:
                delCount = db.delete(NotesDb.Checklist.TABLE_NAME, selection, selectionArgs);
                break;
            case 4:
                String idStr1 = uri.getLastPathSegment();
                String where1 = NotesDb.Note._ID + " = " + idStr1;
                if (!TextUtils.isEmpty(selection)) {
                    where1 += " AND " + selection;
                }
                delCount = db.delete(NotesDb.Checklist.TABLE_NAME, where1, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes:
        if (delCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delCount;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case 1:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + NotesDb.Note.TABLE_NAME;
            case 2:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + NotesDb.Note.TABLE_NAME;
            case 3:
                return ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + NotesDb.Checklist.TABLE_NAME;
            case 4:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + NotesDb.Checklist.TABLE_NAME;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        long id;
        switch (sUriMatcher.match(uri)) {
            case 1:
                id = db.insertWithOnConflict(NotesDb.Note.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                return getUriForId(id, uri);
            case 3:
                id = db.insertWithOnConflict(NotesDb.Checklist.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                return getUriForId(id, uri);
            default:
                throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }
    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            // notify all listeners of changes and return itemUri:
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        }
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    @Override
    public boolean onCreate() {
        mHelper = new NotesDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mHelper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = NotesDb.Note.COLUMN_NAME_TIME + " DESC";
        }
        switch (sUriMatcher.match(uri)) {
            case 1:
                builder.setTables(NotesDb.Note.TABLE_NAME + " JOIN " + NotesDb.Checklist.TABLE_NAME + " ON " + NotesDb.Note.TABLE_NAME + "." + NotesDb.Note._ID + " = " + NotesDb.Checklist.TABLE_NAME + "." + NotesDb.Checklist.COLUMN_NAME_NOTE_ID);
                break;
            case 2:
                builder.setTables(NotesDb.Note.TABLE_NAME);
                // limit query to one row at most:
                builder.appendWhere(NotesDb.Note._ID + " = "
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return builder.query(db, projection, selection, selectionArgs, NotesDb.Note.TABLE_NAME + "." + NotesDb.Note._ID, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        int updateCount = 0;
        switch (sUriMatcher.match(uri)) {
            case 1:
                updateCount = db.update(NotesDb.Note.TABLE_NAME, values, selection, selectionArgs);
                break;
            case 2:
                String idStr = uri.getLastPathSegment();
                String where = NotesDb.Note._ID + " = " + idStr;
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                updateCount = db.update(NotesDb.Note.TABLE_NAME, values, where, selectionArgs);
                break;
            default:
                // no support for updating photos!
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        // notify all listeners of changes:
        if (updateCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }
}
