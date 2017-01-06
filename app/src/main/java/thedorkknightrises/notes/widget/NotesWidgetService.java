package thedorkknightrises.notes.widget;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import thedorkknightrises.notes.NoteObj;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.data.NotesDbHelper;

/**
 * Created by Samriddha Basu on 10/28/2016.
 */

public class NotesWidgetService extends RemoteViewsService {
    private ArrayList<NoteObj> notes;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private final Context context;

        public ListRemoteViewsFactory(Context applicationContext, Intent intent) {
            context = applicationContext;
        }

        @Override
        public void onCreate() {
            NotesDbHelper notesDbHelper = new NotesDbHelper(context);
            notes = notesDbHelper.getAllNotes();
        }

        @Override
        public void onDataSetChanged() {
            NotesDbHelper notesDbHelper = new NotesDbHelper(context);
            notes = notesDbHelper.getAllNotes();
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            if (notes != null)
                return notes.size();
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(this.context.getPackageName(), R.layout.note_layout_widget);
            NoteObj noteObj = notes.get(position);
            if (noteObj != null) {
                remoteViews.setTextViewText(R.id.note_title, noteObj.getTitle());
                if ("".equals(noteObj.getSubtitle()))
                    remoteViews.setViewVisibility(R.id.note_subtitle, View.GONE);
                else remoteViews.setTextViewText(R.id.note_subtitle, noteObj.getSubtitle());
                remoteViews.setTextViewText(R.id.note_content, noteObj.getContent());
                remoteViews.setTextViewText(R.id.note_date, noteObj.getTime());

                Bundle extras = new Bundle();
                extras.putInt(NotesDb.Note._ID, noteObj.getId());
                extras.putString(NotesDb.Note.COLUMN_NAME_TITLE, noteObj.getTitle());
                extras.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, noteObj.getSubtitle());
                extras.putString(NotesDb.Note.COLUMN_NAME_CONTENT, noteObj.getContent());
                extras.putString(NotesDb.Note.COLUMN_NAME_TIME, noteObj.getTime());
                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                // Make it possible to distinguish the individual on-click
                // action of a given item
                remoteViews.setOnClickFillInIntent(R.id.note_card_widget, fillInIntent);
            }

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public long getItemId(int position) {
            NoteObj noteObj = notes.get(position);
            if (noteObj != null) {
                return noteObj.getId();
            }
            return 0;
        }

    }

}