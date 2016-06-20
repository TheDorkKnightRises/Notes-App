package thedorkknightrises.notes.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Notes.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NotesDb.Note.TABLE_NAME + " (" +
                    NotesDb.Note.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_SUBTITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_CONTENT + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_TIME + TEXT_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NotesDb.Note.TABLE_NAME;

    public NotesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addNote(String title, String subtitle, String content, String time) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotesDb.Note.COLUMN_NAME_TITLE, title);
        values.put(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(NotesDb.Note.COLUMN_NAME_CONTENT, content);
        values.put(NotesDb.Note.COLUMN_NAME_TIME, time);

        db.insert(NotesDb.Note.TABLE_NAME, null, values);
        Log.d("DB", "Added");
        db.close();
    }


    public void deleteNote(String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NotesDb.Note.TABLE_NAME, NotesDb.Note.COLUMN_NAME_TIME + " = ?",
                new String[]{time});
        Log.d("DB", "Deleted");
        db.close();
    }

    public ArrayList<NoteObj> getAllNotes() {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String selectQuery = "SELECT  * FROM " + NotesDb.Note.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                NoteObj noteObj = new NoteObj("", "", "", "");
                noteObj.setTitle(cursor.getString(0));
                noteObj.setSubtitle(cursor.getString(1));
                noteObj.setContent(cursor.getString(2));
                noteObj.setTime(cursor.getString(3));

                mList.add(noteObj);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mList;
    }

    public int deleteAllNotes() {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(NotesDb.Note.TABLE_NAME, null, null);

        db.close();
        if (result == 1)
            Log.d("DB", "All notes deleted");
        return result;
    }

    public int getCount() {

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.query(NotesDb.Note.TABLE_NAME, new String[]{NotesDb.Note._ID}, null, null, null, null, null);
        int count = cursor.getCount();

        cursor.close();
        db.close();
        return count;
    }
}