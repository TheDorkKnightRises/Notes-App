package thedorkknightrises.notes.data;

import android.provider.BaseColumns;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class NotesDb {

    public NotesDb() {
    }

    /* Inner class that defines the table contents */
    public static abstract class Note implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_TIME = "time";
    }

    /* Inner class that defines the table contents */
    public static abstract class Archive implements BaseColumns {
        public static final String TABLE_NAME = "archive";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_TIME = "time";
    }
}
