package thedorkknightrises.notes.ui.activities;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import thedorkknightrises.checklistview.ChecklistData;
import thedorkknightrises.checklistview.views.ChecklistView;
import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.data.NotesDbHelper;

/**
 * Created by Samriddha on 23-07-2017.
 */

public class ChecklistActivity extends AppCompatActivity {
    protected CoordinatorLayout coordinatorLayout;
    protected EditText titleText, subtitleText;
    protected TextView timeText;
    SharedPreferences pref;
    boolean lightTheme;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), readableDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
    ArrayList<ChecklistData> checklistDatas;
    private boolean backPressFlag = false;
    private ChecklistView checklistView;
    private NotesDbHelper dbHelper;
    private int id = -1, archived = 0, notified = 0, encrypted = 0, pinned = 0, tag = 0, checklist = 1;
    private String title, subtitle, time, created_at, color = Constants.COLOR_NONE, reminder = Constants.REMINDER_NONE;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        lightTheme = pref.getBoolean(Constants.LIGHT_THEME, false);
        if (lightTheme)
            setTheme(R.style.NoteLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        dbHelper = new NotesDbHelper(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        titleText = (EditText) findViewById(R.id.title);
        subtitleText = (EditText) findViewById(R.id.subtitle);
        timeText = (TextView) findViewById(R.id.note_date);

        checklistView = (ChecklistView) findViewById(R.id.checklist_view);
        checklistView.getDragLinearLayout().setContainerScrollView((ScrollView) findViewById(R.id.scrollView));

        Bundle bundle = getIntent().getBundleExtra(Constants.NOTE_DETAILS_BUNDLE);
        if (bundle != null) {
            id = bundle.getInt(NotesDb.Note._ID);
            title = bundle.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = bundle.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            time = bundle.getString(NotesDb.Note.COLUMN_NAME_TIME);
            created_at = bundle.getString(NotesDb.Note.COLUMN_NAME_CREATED_AT);
            archived = bundle.getInt(NotesDb.Note.COLUMN_NAME_ARCHIVED);
            notified = bundle.getInt(NotesDb.Note.COLUMN_NAME_NOTIFIED);
            color = bundle.getString(NotesDb.Note.COLUMN_NAME_COLOR);
            encrypted = bundle.getInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED);
            pinned = bundle.getInt(NotesDb.Note.COLUMN_NAME_PINNED);
            tag = bundle.getInt(NotesDb.Note.COLUMN_NAME_TAG);
            reminder = bundle.getString(NotesDb.Note.COLUMN_NAME_REMINDER);
            checklist = bundle.getInt(NotesDb.Note.COLUMN_NAME_CHECKLIST);
            checklistDatas = dbHelper.getChecklistData(id);
            if (!reminder.equals(Constants.REMINDER_NONE)) {
                try {
                    Calendar c = Calendar.getInstance();
                    c.setTime(readableDateFormat.parse(reminder));
                    if (c.before(new Date(System.currentTimeMillis()))) {
                        reminder = Constants.REMINDER_NONE;
                        dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (savedInstanceState != null) {
            id = savedInstanceState.getInt(NotesDb.Note._ID);
            title = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            time = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_TIME);
            created_at = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_CREATED_AT);
            archived = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_ARCHIVED);
            notified = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_NOTIFIED);
            color = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_COLOR);
            encrypted = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED);
            pinned = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_PINNED);
            tag = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_TAG);
            reminder = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_REMINDER);
            checklist = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_CHECKLIST);
            checklistDatas = savedInstanceState.getParcelableArrayList(Constants.CHECKLIST_DATA);
            // bottom_bar.setVisibility(View.VISIBLE);
            if (!reminder.equals(Constants.REMINDER_NONE)) {
                try {
                    Calendar c = Calendar.getInstance();
                    c.setTime(readableDateFormat.parse(reminder));
                    if (c.before(new Date(System.currentTimeMillis()))) {
                        reminder = Constants.REMINDER_NONE;
                        dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (bundle == null && savedInstanceState == null) {
            Calendar c = Calendar.getInstance();
            //get date and time, specifically in 24-hr format suitable for sorting
            created_at = sdf.format(c.getTime());
        } else {
            titleText.setText(title);
            subtitleText.setText(subtitle);
            checklistView.setChecklistData(checklistDatas);
        }

        // Toast.makeText(this, checklistDatas.get(0).getText(), Toast.LENGTH_SHORT).show();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setupWindowAnimations();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putInt(NotesDb.Note._ID, id);
        bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
        bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, time);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CREATED_AT, created_at);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
        bundle.putString(NotesDb.Note.COLUMN_NAME_COLOR, color);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED, encrypted);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_PINNED, pinned);
        bundle.putString(NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_CHECKLIST, checklist);
        bundle.putParcelableArrayList(Constants.CHECKLIST_DATA, checklistView.getChecklistData());

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
        backPressFlag = true;
        onBackPressed();
    }

    public void delete(View v) {
        dbHelper.deleteNote(created_at);
        dbHelper.deleteChecklistData(id);
        finish();
    }

    public void onClick(View v) {

    }

    @Override
    public void onBackPressed() {
        if (!backPressFlag) {
            Toast.makeText(getApplicationContext(), getText(R.string.back_press_checklist), Toast.LENGTH_SHORT).show();
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
        saveData();
        super.onBackPressed();
    }

    private void saveData() {
        title = titleText.getText().toString();
        subtitle = subtitleText.getText().toString();
        time = sdf.format(Calendar.getInstance().getTime());
        dbHelper.saveChecklist(id, title, subtitle, checklistView.getChecklistData(), time, created_at, archived, notified, color, encrypted, pinned, tag, reminder);
    }
}