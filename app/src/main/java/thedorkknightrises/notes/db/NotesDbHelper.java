package thedorkknightrises.notes.db;

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
                    NotesDb.Note.COLUMN_NAME_TIME + TEXT_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NotesDb.Note.TABLE_NAME;
    private static final String SQL_CREATE_ENTRIES_ARCHIVE =
            "CREATE TABLE " + NotesDb.Archive.TABLE_NAME + " (" +
                    NotesDb.Archive._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    NotesDb.Archive.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Archive.COLUMN_NAME_SUBTITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Archive.COLUMN_NAME_CONTENT + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Archive.COLUMN_NAME_TIME + TEXT_TYPE + " )";
    private static final String SQL_DELETE_ENTRIES_ARCHIVE =
            "DROP TABLE IF EXISTS " + NotesDb.Archive.TABLE_NAME;

    public NotesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES_ARCHIVE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_ENTRIES_ARCHIVE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addNote(int id, String title, String subtitle, String content, String time) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotesDb.Note._ID, id);
        values.put(NotesDb.Note.COLUMN_NAME_TITLE, title);
        values.put(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(NotesDb.Note.COLUMN_NAME_CONTENT, content);
        values.put(NotesDb.Note.COLUMN_NAME_TIME, time);

        db.insert(NotesDb.Note.TABLE_NAME, null, values);
        Log.d("DB", "Added");
        db.close();
    }

    public void addNoteToArchive(int id, String title, String subtitle, String content, String time) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotesDb.Archive._ID, id);
        values.put(NotesDb.Archive.COLUMN_NAME_TITLE, title);
        values.put(NotesDb.Archive.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(NotesDb.Archive.COLUMN_NAME_CONTENT, content);
        values.put(NotesDb.Archive.COLUMN_NAME_TIME, time);

        db.insert(NotesDb.Archive.TABLE_NAME, null, values);
        Log.d("DB", "Added to archive");
        db.close();
    }


    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NotesDb.Note.TABLE_NAME, NotesDb.Note._ID + " = ?",
                new String[]{Integer.toString(id)});
        Log.d("DB", "Deleted");
        db.close();
    }

    public void deleteNoteFromArchive(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NotesDb.Archive.TABLE_NAME, NotesDb.Archive._ID + " = ?",
                new String[]{Integer.toString(id)});
        Log.d("DB", "Deleted from archive");
        db.close();
    }

    public ArrayList<NoteObj> getAllNotes() {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String selectQuery = "SELECT  * FROM " + NotesDb.Note.TABLE_NAME;
        String[] projection = {
                NotesDb.Note._ID,
                NotesDb.Note.COLUMN_NAME_TITLE,
                NotesDb.Note.COLUMN_NAME_SUBTITLE,
                NotesDb.Note.COLUMN_NAME_CONTENT,
                NotesDb.Note.COLUMN_NAME_TIME
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDb.Note.TABLE_NAME, projection, null, null, null, null, NotesDb.Note._ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                NoteObj noteObj = new NoteObj(0, "", "", "", "");
                noteObj.setId(cursor.getInt(0));
                noteObj.setTitle(cursor.getString(1));
                noteObj.setSubtitle(cursor.getString(2));
                noteObj.setContent(cursor.getString(3));
                noteObj.setTime(cursor.getString(4));

                mList.add(noteObj);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mList;
    }

    public ArrayList<NoteObj> getAllNotesFromArchive() {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String[] projection = {
                NotesDb.Archive._ID,
                NotesDb.Archive.COLUMN_NAME_TITLE,
                NotesDb.Archive.COLUMN_NAME_SUBTITLE,
                NotesDb.Archive.COLUMN_NAME_CONTENT,
                NotesDb.Archive.COLUMN_NAME_TIME
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDb.Archive.TABLE_NAME, projection, null, null, null, null, NotesDb.Archive._ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                NoteObj noteObj = new NoteObj(0, "", "", "", "");
                noteObj.setId(cursor.getInt(0));
                noteObj.setTitle(cursor.getString(1));
                noteObj.setSubtitle(cursor.getString(2));
                noteObj.setContent(cursor.getString(3));
                noteObj.setTime(cursor.getString(4));

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

    public int deleteAllNotesFromArchive() {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(NotesDb.Archive.TABLE_NAME, null, null);

        db.close();
        if (result == 1)
            Log.d("DB", "All notes deleted from archive");
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