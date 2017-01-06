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
import android.text.TextUtils;

import static android.app.SearchManager.SUGGEST_URI_PATH_QUERY;

public class NotesProvider extends ContentProvider {
    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        /*
         * The calls to addURI() go here, for all of the content URI patterns that the provider
         * should recognize. For this snippet, only the calls for table 3 are shown.
         */

        /*
         * Sets the integer value for multiple rows in table to 1. Notice that no wildcard is used
         * in the path
         */
        sUriMatcher.addURI("thedorkknightrises.notes.provider", NotesDb.Note.TABLE_NAME, 1);
        /*
         * Sets the code for a single row to 2. In this case, the "#" wildcard is
         * used
         */
        sUriMatcher.addURI("thedorkknightrises.notes.provider", NotesDb.Note.TABLE_NAME + "/#", 2);
        sUriMatcher.addURI("thedorkknightrises.notes.provider", SUGGEST_URI_PATH_QUERY + "/*", 5);
    }

    NotesDbHelper mHelper;

    public NotesProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
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
            default:
                // no support for deleting photos or entities -
                // photos are deleted by a trigger when the item is deleted
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
                builder.setTables(NotesDb.Note.TABLE_NAME);
                break;
            case 2:
                builder.setTables(NotesDb.Note.TABLE_NAME);
                // limit query to one row at most:
                builder.appendWhere(NotesDb.Note._ID + " = "
                        + uri.getLastPathSegment());
                break;
            case 5:
                String query = Uri.decode(uri.getLastPathSegment());
                selection = NotesDb.Note.COLUMN_NAME_TITLE + " LIKE" + "'%" + query + "%' OR " + NotesDb.Note.COLUMN_NAME_SUBTITLE + " LIKE" + "'%" + query + "%' OR " + NotesDb.Note.COLUMN_NAME_CONTENT + " LIKE" + "'%" + query + "%'";
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
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
