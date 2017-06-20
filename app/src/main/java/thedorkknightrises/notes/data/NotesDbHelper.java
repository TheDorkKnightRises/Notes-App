package thedorkknightrises.notes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import thedorkknightrises.notes.NoteObj;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class NotesDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "Notes.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NotesDb.Note.TABLE_NAME + " (" +
                    NotesDb.Note._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_SUBTITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_CONTENT + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_TIME + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_ARCHIVED + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_NOTIFIED + " INTEGER)";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NotesDb.Note.TABLE_NAME;

    public NotesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addNote(String title, String subtitle, String content, String time, int archived, int notified) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotesDb.Note.COLUMN_NAME_TITLE, title);
        values.put(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(NotesDb.Note.COLUMN_NAME_CONTENT, content);
        values.put(NotesDb.Note.COLUMN_NAME_TIME, time);
        values.put(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
        values.put(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);

        db.insertWithOnConflict(NotesDb.Note.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        Log.d("DB", "Added");
        db.close();
    }

    public void deleteNote(String title, String subtitle, String content, String time, int archived) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NotesDb.Note.TABLE_NAME,
                NotesDb.Note.COLUMN_NAME_TITLE + " = ? AND "
                        + NotesDb.Note.COLUMN_NAME_SUBTITLE + " = ? AND "
                        + NotesDb.Note.COLUMN_NAME_CONTENT + " = ? AND "
                        + NotesDb.Note.COLUMN_NAME_TIME + " = ? AND "
                        + NotesDb.Note.COLUMN_NAME_ARCHIVED + " = ? ",
                new String[]{title, subtitle, content, time, Integer.toString(archived)});
        Log.d("DB", "Deleted");
        db.close();
    }

    public ArrayList<NoteObj> getAllNotes(int archive) {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String[] projection = {
                NotesDb.Note._ID,
                NotesDb.Note.COLUMN_NAME_TITLE,
                NotesDb.Note.COLUMN_NAME_SUBTITLE,
                NotesDb.Note.COLUMN_NAME_CONTENT,
                NotesDb.Note.COLUMN_NAME_TIME,
                NotesDb.Note.COLUMN_NAME_ARCHIVED,
                NotesDb.Note.COLUMN_NAME_NOTIFIED
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDb.Note.TABLE_NAME, projection, NotesDb.Note.COLUMN_NAME_ARCHIVED + " LIKE " + archive, null, null, null, NotesDb.Note.COLUMN_NAME_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                NoteObj noteObj = new NoteObj(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getInt(6));
                mList.add(noteObj);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mList;
    }

    public int deleteAllNotes(int archive) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(NotesDb.Note.TABLE_NAME, NotesDb.Note.COLUMN_NAME_ARCHIVED + " LIKE " + archive, null);

        db.close();
        if (result == 1)
            Log.d("DB", "All notes deleted");
        return result;
    }
}