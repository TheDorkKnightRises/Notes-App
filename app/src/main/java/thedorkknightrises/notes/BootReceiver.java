package thedorkknightrises.notes;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;

import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.data.NotesDbHelper;
import thedorkknightrises.notes.ui.NoteActivity;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Samriddha Basu on 21-06-2017.
 */

public class BootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        ArrayList<NoteObj> list = new NotesDbHelper(context).getNotifications();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        for (int i = 0; i < list.size(); i++) {
            NoteObj note = list.get(i);
            int id = note.getId();
            String title = note.getTitle();
            String subtitle = note.getSubtitle();
            String content = note.getContent();
            String time = note.getTime();
            int archived = note.getArchived();
            int notified = note.getNotified();
            String info;
            if (!subtitle.equals("")) info = subtitle;
            else info = time;
            NotificationCompat.Builder notif =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(content)
                            .setSubText(info)
                            .setShowWhen(false)
                            .setColor(Color.argb(255, 32, 128, 200));
            notif.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setSummaryText(time));
            // Sets an ID for the notification
            Log.d("NOTIFICATION ID", String.valueOf(id));
            Intent resultIntent = new Intent(context, NoteActivity.class);
            resultIntent.putExtra(NotesDb.Note._ID, id);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TITLE, title);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_CONTENT, content);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TIME, time);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_CREATED_AT, note.getCreated_at());
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_COLOR, note.getColor());
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_ENCRYPTED, note.getEncrypted());
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_PINNED, note.getPinned());
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TAG, note.getTag());
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_REMINDER, note.getReminder());
            resultIntent.setAction("ACTION_NOTE_" + id);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(NoteActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            notif.setContentIntent(resultPendingIntent);
            notif.setOngoing(true);

            // Builds the notification and issues it.
            mNotifyMgr.notify(id, notif.build());
        }

        if (context.getSharedPreferences(Constants.PREFS, MODE_PRIVATE).getBoolean(Constants.QUICK_NOTIFY, false)) {
            NotificationCompat.Builder notif =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentText(context.getString(R.string.tap_create_note))
                            .setShowWhen(false)
                            .setPriority(NotificationCompat.PRIORITY_MIN)
                            .setColor(Color.argb(255, 32, 128, 200));
            Intent resultIntent = new Intent(context, NoteActivity.class);
            resultIntent.setAction("ACTION_NOTE_" + 0);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(NoteActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            notif.setContentIntent(resultPendingIntent);
            notif.setOngoing(true);

            // Builds the notification and issues it.
            mNotifyMgr.notify(0, notif.build());
        }
    }
}