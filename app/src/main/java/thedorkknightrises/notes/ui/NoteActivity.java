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
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import thedorkknightrises.notes.R;
import thedorkknightrises.notes.db.NotesDb;
import thedorkknightrises.notes.db.NotesDbHelper;

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
    SharedPreferences pref;
    boolean lightTheme;
    private Menu menu;
    private  int id = -1;
    private String title;
    private String subtitle;
    private String content;
    private String time;

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

        dbHelper = new NotesDbHelper(this);

        if (savedInstanceState != null) {
            editMode = savedInstanceState.getBoolean("editMode");
            id = savedInstanceState.getInt("id");
            title = savedInstanceState.getString("title");
            subtitle = savedInstanceState.getString("subtitle");
            content = savedInstanceState.getString("content");
            time = savedInstanceState.getString("time");
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            editMode = false;
            id = bundle.getInt("id");
            title = bundle.getString("title");
            subtitle = bundle.getString("subtitle");
            content = bundle.getString("content");
            time = bundle.getString("time");
        }
        else {
            editMode = true;
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

        if (MainActivity.archive)
            ((ImageButton)findViewById(R.id.archive_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_unarchive_white_24dp));

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
        bundle.putInt("id", id);
        bundle.putString("title", title);
        bundle.putString("subtitle", subtitle);
        bundle.putString("content", content);
        bundle.putString("time", time);
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
        if (MainActivity.added) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(NotesDb.Note._ID, id + 1);
            editor.commit();
        }
        onBackPressed();
    }

    public void delete(View v) {
        if (MainActivity.archive)
            dbHelper.deleteNoteFromArchive(id);
        else dbHelper.deleteNote(id);
        MainActivity.changed = true;
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
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
                if (id == -1) {
                    id = pref.getInt(NotesDb.Note._ID, 1);
                    MainActivity.added = true;
                }   else dbHelper.deleteNote(id);
                time = c.getTime().toString().substring(0, 19);
                dbHelper.addNote(id, title, subtitle, content, time+":"+c.get(Calendar.SECOND));
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

                toolbar.setVisibility(View.VISIBLE);
                findViewById(R.id.note_update).setVisibility(View.VISIBLE);
                timeText.setText(time);
                editMode = false;
            }
        } else {
            titleText.setEnabled(true);
            subtitleText.setEnabled(true);
            contentText.setEnabled(true);
            contentText.requestFocus();
            contentText.setSelection(contentText.getText().length());
            subtitleText.setVisibility(View.VISIBLE);
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_done_white_24dp));
            onPrepareOptionsMenu(menu);
            toolbar.setVisibility(View.GONE);
            findViewById(R.id.note_update).setVisibility(View.GONE);
            timeText.setText("");
            editMode = true;
        }
        onResume();

    }

    public void share(View v)   {
        Intent share = new Intent(Intent.ACTION_SEND);
        if (subtitle.equals(""))
            share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.title)+": "+title+"\n"+content);
        else share.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.title)+": "+title+"\n"+getResources().getString(R.string.description)+": "+subtitle+"\n\n"+content);
        share.setType("text/plain");
        startActivity(Intent.createChooser(share, getResources().getString(R.string.share_title)));
    }

    public void notif(View v)   {
        String info;
        if (!subtitle.equals("")) info = subtitle;
        else info = time;
        NotificationCompat.Builder notif =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setContentInfo(info)
                        .setColor(Color.argb(255, 32, 128, 200));
        notif.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setSummaryText(time));
        // Sets an ID for the notification
        int mNotificationId = id;
        Intent resultIntent = new Intent(this, NoteActivity.class);
        resultIntent.putExtra(NotesDb.Note._ID, id);
        resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TITLE, title);
        resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_CONTENT, content);
        resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_TIME, time.substring(0, 19));

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notif.setContentIntent(resultPendingIntent);

        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notif.build());
    }

    public void archive(View v) {
        if(MainActivity.archive) {
            Toast.makeText(this, R.string.removed_archive, Toast.LENGTH_SHORT).show();
            dbHelper.addNote(id, title, subtitle, content, time);
            dbHelper.deleteNoteFromArchive(id);
            MainActivity.changed = true;
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.cancel(id);
            finish();
        } else {
            Toast.makeText(this, R.string.added_archive, Toast.LENGTH_SHORT).show();
            dbHelper.addNoteToArchive(id, title, subtitle, content, time);
            dbHelper.deleteNote(id);
            MainActivity.changed = true;
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.cancel(id);
            finish();
        }
    }
}