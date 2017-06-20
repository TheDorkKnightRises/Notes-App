package thedorkknightrises.notes.ui;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.data.NotesDbHelper;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class NoteActivity extends AppCompatActivity {
    protected Boolean editMode;
    protected CoordinatorLayout coordinatorLayout;
    protected NotesDbHelper dbHelper;
    protected EditText titleText;
    protected EditText subtitleText;
    protected EditText contentText;
    protected TextView timeText;
    FloatingActionButton fab;
    View toolbar;
    View archive_hint;
    SharedPreferences pref;
    boolean lightTheme;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Menu menu;
    private  int id = -1;
    private String title, oldTitle;
    private String subtitle, oldSubtitle;
    private String content, oldContent;
    private String time, oldTime;
    private int archived = 0, oldArchived;
    private int notified = 0;
    private boolean backPressFlag = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences("Prefs", MODE_PRIVATE);
        lightTheme = pref.getBoolean("lightTheme", false);
        if (lightTheme)
            setTheme(R.style.NoteLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        titleText = (EditText) findViewById(R.id.title);
        subtitleText = (EditText) findViewById(R.id.subtitle);
        contentText = (EditText) findViewById(R.id.content);
        timeText = (TextView) findViewById(R.id.note_date);
        fab = (FloatingActionButton) findViewById(R.id.fab_note);
        toolbar = findViewById(R.id.toolbar);
        archive_hint = findViewById(R.id.archive_hint);

        dbHelper = new NotesDbHelper(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            editMode = false;
            id = bundle.getInt(NotesDb.Note._ID);
            title = bundle.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = bundle.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            content = bundle.getString(NotesDb.Note.COLUMN_NAME_CONTENT);
            time = bundle.getString(NotesDb.Note.COLUMN_NAME_TIME);
            archived = bundle.getInt(NotesDb.Note.COLUMN_NAME_ARCHIVED);
            notified = bundle.getInt(NotesDb.Note.COLUMN_NAME_NOTIFIED);
        }
        else {
            editMode = true;
        }

        if (savedInstanceState != null) {
            editMode = savedInstanceState.getBoolean("editMode");
            id = savedInstanceState.getInt(NotesDb.Note._ID);
            title = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            content = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_CONTENT);
            time = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_TIME);
            archived = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_ARCHIVED);
            notified = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_NOTIFIED);
        }

        if (!editMode) {

            titleText.setText(title);
            titleText.setEnabled(false);
            if (lightTheme)
                titleText.setTextColor(getResources().getColor(R.color.black));
            else titleText.setTextColor(getResources().getColor(R.color.white));

            if (subtitle.equals(""))
                subtitleText.setVisibility(View.GONE);
            else {
                subtitleText.setText(subtitle);
                subtitleText.setEnabled(false);
                if (lightTheme)
                    subtitleText.setTextColor(getResources().getColor(R.color.dark_gray));
                else subtitleText.setTextColor(getResources().getColor(R.color.light_gray));
            }

            contentText.setText(content);
            contentText.setEnabled(false);
            if (lightTheme)
                contentText.setTextColor(getResources().getColor(R.color.black));
            else contentText.setTextColor(getResources().getColor(R.color.white));

            timeText.setText(time);

            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setupWindowAnimations();
            }
            toolbar.setVisibility(View.GONE);
            timeText.setText("");
        }

        if (archived == 1) {
            ((ImageButton) findViewById(R.id.archive_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_unarchive_white_24dp));
            archive_hint.setVisibility(View.VISIBLE);
        }

        if (notified == 1) {
            ((ImageButton) findViewById(R.id.notif_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_notifications_off_white_24dp));
        }
    }

    @Override
    protected void onResume() {
        View deleteBtn = findViewById(R.id.delete);
        if (!editMode) {
            deleteBtn.setVisibility(View.VISIBLE);
        } else deleteBtn.setVisibility(View.GONE);
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle)   {
        bundle.putBoolean("editMode", editMode);
        bundle.putInt(NotesDb.Note._ID, id);
        bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
        bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, content);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, time);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
        super.onSaveInstanceState(bundle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Slide slide = new Slide(Gravity.TOP);
        slide.addTarget(R.id.note_card);
        slide.addTarget(R.id.note_title);
        slide.addTarget(R.id.note_subtitle);
        slide.addTarget(R.id.note_content);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
        getWindow().setReenterTransition(slide);
    }

    public void close(View v) {
        if (subtitleText.getText().toString().isEmpty()) subtitleText.setVisibility(View.INVISIBLE);
        backPressFlag = true;
        onBackPressed();
    }

    public void delete(View v) {
        dbHelper.deleteNote(title, subtitle, content, time, archived);
        MainActivity.changed = true;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(id);
        finish();
    }

    public void onClick(View v) {
        if (editMode) {
            title = titleText.getText().toString().trim();
            subtitle = subtitleText.getText().toString().trim();
            content = contentText.getText().toString().trim();
            if (title.equals("") || content.equals(""))
                Snackbar.make(coordinatorLayout, R.string.incomplete, Snackbar.LENGTH_LONG).show();
            else {
                Calendar c = Calendar.getInstance();
                //get date and time, specifically in 24-hr format suitable for sorting
                time = sdf.format(c.getTime());
                Log.d("TIME", time);
                archived = 0;
                dbHelper.deleteNote(oldTitle, oldSubtitle, oldContent, oldTime, oldArchived);
                id = dbHelper.addNote(title, subtitle, content, time, archived, notified);
                editMode = false;
                MainActivity.changed = true;
                titleText.setEnabled(false);
                if (lightTheme)
                    titleText.setTextColor(getResources().getColor(R.color.black));
                else titleText.setTextColor(getResources().getColor(R.color.white));
                if (!subtitle.equals("")) {
                    subtitleText.setEnabled(false);
                    if (lightTheme)
                        subtitleText.setTextColor(getResources().getColor(R.color.dark_gray));
                    else subtitleText.setTextColor(getResources().getColor(R.color.light_gray));
                } else subtitleText.setVisibility(View.GONE);
                contentText.setEnabled(false);
                Linkify.addLinks(contentText, Linkify.ALL);
                if (lightTheme)
                    contentText.setTextColor(getResources().getColor(R.color.black));
                else contentText.setTextColor(getResources().getColor(R.color.white));

                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));
                onPrepareOptionsMenu(menu);

                archive_hint.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                findViewById(R.id.note_update).setVisibility(View.VISIBLE);
                timeText.setText(time);
                editMode = false;

                if (MainActivity.archive == 1) {
                    MainActivity.archive = 0;
                }

                notif();
            }
        } else {
            saveOldData();
            titleText.setEnabled(true);
            subtitleText.setEnabled(true);
            contentText.setEnabled(true);
            contentText.setSelection(contentText.getText().length());
            subtitleText.setVisibility(View.VISIBLE);
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_done_white_24dp));
            onPrepareOptionsMenu(menu);
            toolbar.setVisibility(View.GONE);
            findViewById(R.id.note_update).setVisibility(View.GONE);
            timeText.setText("");
            editMode = true;
            contentText.requestFocusFromTouch();
        }
        onResume();

    }

    private void saveOldData() {
        oldTitle = title;
        oldSubtitle = subtitle;
        oldContent = content;
        oldTime = time;
        oldArchived = archived;
    }

    public void share(View v)   {
        Intent share = new Intent(Intent.ACTION_SEND);
        if (subtitle.equals(""))
            share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.title)+": "+title+"\n"+content);
        else share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.title)+": "+title+"\n"+getResources().getString(R.string.description)+": "+subtitle+"\n\n"+content);
        share.setType("text/plain");
        startActivity(Intent.createChooser(share, getResources().getString(R.string.share_title)));
    }

    public void notifBtn(View v) {
        dbHelper.deleteNote(title, subtitle, content, time, archived);
        if (notified == 1) {
            notified = 0;
            dbHelper.addNote(title, subtitle, content, time, archived, notified);
            MainActivity.changed = true;
            notif();
        } else {
            notified = 1;
            dbHelper.addNote(title, subtitle, content, time, archived, notified);
            MainActivity.changed = true;
            notif();
        }
    }

    public void notif() {
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notified == 0) {
            mNotifyMgr.cancel(id);
            ((ImageButton) findViewById(R.id.notif_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_notifications_active_white_24dp));
        } else {
            ((ImageButton) findViewById(R.id.notif_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_notifications_off_white_24dp));
            String info;
            if (!subtitle.equals("")) info = subtitle;
            else info = time;
            NotificationCompat.Builder notif =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(content)
                            .setSubText(info)
                            .setColor(Color.argb(255, 32, 128, 200));
            notif.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setSummaryText(time));
            // Sets an ID for the notification
            Log.d("NOTIFICATION ID", String.valueOf(id));
            Intent resultIntent = new Intent(this, NoteActivity.class);
            resultIntent.putExtra(NotesDb.Note._ID, id);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TITLE, title);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_CONTENT, content);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TIME, time);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
            resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
            resultIntent.setAction("ACTION_NOTE_" + id);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
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
    }

    public void archive(View v) {
        dbHelper.deleteNote(title, subtitle, content, time, archived);
        if (archived == 1) {
            Toast.makeText(this, R.string.removed_archive, Toast.LENGTH_SHORT).show();
            archived = 0;
            dbHelper.addNote(title, subtitle, content, time, archived, notified);
            MainActivity.changed = true;
            notif();
            finish();
        } else {
            Toast.makeText(this, R.string.added_archive, Toast.LENGTH_SHORT).show();
            archived = 1;
            dbHelper.addNote(title, subtitle, content, time, archived, notified);
            MainActivity.changed = true;
            notif();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (editMode && !backPressFlag) {
            Toast.makeText(getApplicationContext(), getText(R.string.back_press), Toast.LENGTH_SHORT).show();
            backPressFlag = true;

            // Thread to change backPressedFlag to false after 3000ms
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        backPressFlag = false;
                    }
                }
            }).start();
            return;
        }
        super.onBackPressed();
    }
}