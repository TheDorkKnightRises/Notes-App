package thedorkknightrises.notes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class BackupDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "Backup.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NotesDb.Note.TABLE_NAME + " (" +
                    NotesDb.Note._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_SUBTITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_CONTENT + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_CREATED_AT + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_ARCHIVED + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_NOTIFIED + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_COLOR + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_ENCRYPTED + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_PINNED + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_TAG + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_REMINDER + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_CHECKLIST + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_DELETED + " INTEGER" + " ) ";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NotesDb.Note.TABLE_NAME;

    private static final String SQL_CREATE_ENTRIES_CHECKLIST =
            "CREATE TABLE " + NotesDb.Checklist.TABLE_NAME + " (" +
                    NotesDb.Checklist._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    NotesDb.Checklist.COLUMN_NAME_NOTE_ID + " INTEGER " + COMMA_SEP +
                    NotesDb.Checklist.COLUMN_NAME_ITEM + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Checklist.COLUMN_NAME_CHECKED + " INTEGER ) ";
    private static final String SQL_DELETE_ENTRIES_CHECKLIST =
            "DROP TABLE IF EXISTS " + NotesDb.Checklist.TABLE_NAME;

    public BackupDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES_CHECKLIST);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 4:
                db.execSQL("ALTER TABLE " + NotesDb.Note.TABLE_NAME + " ADD COLUMN " + NotesDb.Note.COLUMN_NAME_CHECKLIST + " INTEGER DEFAULT 0;" +
                        "UPDATE " + NotesDb.Note.TABLE_NAME + " SET " + NotesDb.Note.COLUMN_NAME_CHECKLIST + " = 0");
                Log.d(getClass().getName(), "Database updated successfully to version 5 (added checklist column)");
            case 5:
                db.execSQL(SQL_CREATE_ENTRIES_CHECKLIST);
                Log.d(getClass().getName(), "Database updated successfully to version 6 (created checklist table)");
            case 6:
                db.execSQL("ALTER TABLE " + NotesDb.Note.TABLE_NAME + " ADD COLUMN " + NotesDb.Note.COLUMN_NAME_DELETED + " INTEGER DEFAULT 0;" +
                        "UPDATE " + NotesDb.Note.TABLE_NAME + " SET " + NotesDb.Note.COLUMN_NAME_DELETED + " = 0");
                Log.d(getClass().getName(), "Database updated successfully to version 7 (added deleted column)");
                break;
            default:
                db.execSQL(SQL_DELETE_ENTRIES);
                db.execSQL(SQL_DELETE_ENTRIES_CHECKLIST);
                onCreate(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void merge(Context context) {
        SQLiteDatabase db = this.getReadableDatabase();
        NotesDbHelper notesDbHelper = new NotesDbHelper(context);

        int orig_id;
        int new_id;

        String[] projection = {
                NotesDb.Note._ID,
                NotesDb.Note.COLUMN_NAME_TITLE,
                NotesDb.Note.COLUMN_NAME_SUBTITLE,
                NotesDb.Note.COLUMN_NAME_CONTENT,
                NotesDb.Note.COLUMN_NAME_TIME,
                NotesDb.Note.COLUMN_NAME_CREATED_AT,
                NotesDb.Note.COLUMN_NAME_ARCHIVED,
                NotesDb.Note.COLUMN_NAME_NOTIFIED,
                NotesDb.Note.COLUMN_NAME_COLOR,
                NotesDb.Note.COLUMN_NAME_ENCRYPTED,
                NotesDb.Note.COLUMN_NAME_PINNED,
                NotesDb.Note.COLUMN_NAME_TAG,
                NotesDb.Note.COLUMN_NAME_REMINDER,
                NotesDb.Note.COLUMN_NAME_CHECKLIST
        };

        Cursor cursor = db.query(NotesDb.Note.TABLE_NAME, projection, null, null, null, null, NotesDb.Note._ID);
        if (cursor.moveToFirst()) {
            do {
                new_id = notesDbHelper.addOrUpdateNote(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getInt(10),
                        cursor.getInt(11),
                        cursor.getString(12),
                        cursor.getInt(13),
                        0);

                orig_id = cursor.getInt(0);

                String[] projection1 = {
                        NotesDb.Checklist.COLUMN_NAME_NOTE_ID,
                        NotesDb.Checklist.COLUMN_NAME_ITEM,
                        NotesDb.Checklist.COLUMN_NAME_CHECKED
                };

                SQLiteDatabase db1 = notesDbHelper.getWritableDatabase();

                Cursor checklistCursor = db.query(NotesDb.Checklist.TABLE_NAME, projection1,
                        NotesDb.Checklist.COLUMN_NAME_NOTE_ID + " = ? ", new String[]{String.valueOf(orig_id)},
                        null, null, NotesDb.Checklist.COLUMN_NAME_NOTE_ID);

                if (checklistCursor.moveToFirst()) {
                    do {
                        db1.delete(NotesDb.Checklist.TABLE_NAME,
                                NotesDb.Checklist.COLUMN_NAME_NOTE_ID + " = ? AND "
                                        + NotesDb.Checklist.COLUMN_NAME_ITEM + " = '" + checklistCursor.getString(1) + "'", new String[]{String.valueOf(new_id)});
                        ContentValues checklistValues = new ContentValues();
                        checklistValues.put(NotesDb.Checklist.COLUMN_NAME_NOTE_ID, new_id);
                        checklistValues.put(NotesDb.Checklist.COLUMN_NAME_ITEM, checklistCursor.getString(1));
                        checklistValues.put(NotesDb.Checklist.COLUMN_NAME_CHECKED, cursor.getInt(2));
                        db1.insertWithOnConflict(NotesDb.Checklist.TABLE_NAME, null, checklistValues, SQLiteDatabase.CONFLICT_REPLACE);
                    } while (checklistCursor.moveToNext());
                }
                checklistCursor.close();
                db1.close();

            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
    }

}