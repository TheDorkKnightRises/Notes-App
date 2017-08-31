package thedorkknightrises.notes.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
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
    ArrayList<ChecklistData> checklistDatas, oDatas;
    View toolbar_note, toolbar, bottom_bar;
    float radius;
    private ChecklistView checklistView;
    private NotesDbHelper dbHelper;
    private int cx, cy;
    private int id = -1, archived = 0, notified = 0, encrypted = 0, pinned = 0, tag = 0, checklist = 1;
    private String title, subtitle, time, created_at, color = Constants.COLOR_NONE, reminder = Constants.REMINDER_NONE;
    private String oTitle, oSubtitle;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        lightTheme = pref.getBoolean(Constants.LIGHT_THEME, false);
        if (lightTheme)
            setTheme(R.style.NoteLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        dbHelper = new NotesDbHelper(this);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        toolbar = findViewById(R.id.toolbar);
        toolbar_note = findViewById(R.id.toolbar_note);
        bottom_bar = findViewById(R.id.bottom_bar);

        titleText = findViewById(R.id.title);
        subtitleText = findViewById(R.id.subtitle);
        timeText = findViewById(R.id.note_date);

        checklistView = findViewById(R.id.checklist_view);
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

        if (title == null || title.isEmpty()) {
            title = "";
            titleText.setVisibility(View.INVISIBLE);
        }
        if (subtitle == null || subtitle.isEmpty()) {
            subtitle = "";
            subtitleText.setVisibility(View.INVISIBLE);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toolbar.setVisibility(View.VISIBLE);
                if (savedInstanceState == null) revealToolbar();
                if (title == null || title.isEmpty())
                    titleText.setVisibility(View.VISIBLE);
                if (subtitle == null || subtitle.isEmpty())
                    subtitleText.setVisibility(View.VISIBLE);
            }
        }, 350);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setupWindowAnimations();
        }

        // save original values in case there are assignments later
        oTitle = title.trim();
        oSubtitle = subtitle.trim();
        oDatas = checklistDatas;

        if (lightTheme)
            titleText.setTextColor(getResources().getColor(R.color.black));
        else titleText.setTextColor(getResources().getColor(R.color.white));

        if (lightTheme)
            subtitleText.setTextColor(getResources().getColor(R.color.dark_gray));
        else subtitleText.setTextColor(getResources().getColor(R.color.light_gray));

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
        slide.addTarget(R.id.note_content);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
        getWindow().setReenterTransition(slide);
    }

    private void revealToolbar() {
        if (Build.VERSION.SDK_INT >= 21) {
            cx = bottom_bar.getWidth() / 2;
            cy = bottom_bar.getHeight() / 2;
            radius = (float) Math.hypot(cx, cy);

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(bottom_bar, cx, cy, 0, radius);

            // make the view visible and start the animation
            bottom_bar.setVisibility(View.VISIBLE);
            anim.start();
        } else bottom_bar.setVisibility(View.VISIBLE);
    }

    private void hideToolbar() {
        if (Build.VERSION.SDK_INT >= 21) {
            cx = bottom_bar.getWidth() / 2;
            cy = bottom_bar.getHeight() / 2;
            radius = (float) Math.hypot(cx, cy);

            // create the animation (the final radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(bottom_bar, cx, cy, radius, 0);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    bottom_bar.setVisibility(View.INVISIBLE);
                }
            });

            // start the animation
            anim.start();
        } else bottom_bar.setVisibility(View.INVISIBLE);
    }

    public void onClick(View view) {
        oTitle = titleText.getText().toString().trim();
        oSubtitle = subtitleText.getText().toString().trim();
        oDatas = checklistView.getChecklistData();
        saveData();
    }

    public void share(View v) {
        StringBuffer shareText = new StringBuffer();
        if (!titleText.getText().toString().trim().equals("")) {
            shareText.append(titleText.getText().toString().trim()).append("\n");
        }
        if (!subtitleText.getText().toString().trim().equals("")) {
            shareText.append("(").append(subtitleText.getText().toString().trim()).append(")\n");
        }
        checklistDatas = checklistView.getChecklistData();
        for (ChecklistData data : checklistDatas) {
            if (data.isChecked()) {
                shareText.append("[\u2713] ").append(data.getText()).append("\n");
            } else {
                shareText.append("[    ] ").append(data.getText()).append("\n");
            }
        }
        if (pref.getBoolean(Constants.SHARE_INFO, true))
            shareText.append("\n").append(getString(R.string.shared_using)).append(" | https://goo.gl/o4Sr7n");
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        share.setType("text/plain");
        if (share.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(share, getResources().getString(R.string.share_via)));
        } else {
            Toast.makeText(this, R.string.no_share_app_found, Toast.LENGTH_SHORT).show();
        }
    }

    public void close(View v) {
        onBackPressed();
    }

    public void delete(View v) {
        dbHelper.deleteNote(created_at);
        dbHelper.deleteChecklistData(id);
        onListChanged();
        finish();
    }

    public void archive(View v) {
        archived = 1 - archived;
        saveData();
        exit();
    }

    @Override
    public void onBackPressed() {
        if (!titleText.getText().toString().trim().equals(oTitle) || !subtitleText.getText().toString().trim().equals(oSubtitle) || !checklistView.getChecklistData().equals(oDatas)) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_discard)
                    .setPositiveButton(R.string.save_exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (saveData())
                                exit();
                        }
                    })
                    .setNeutralButton(R.string.cancel, null)
                    .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            exit();
                        }
                    })
                    .show();
        } else {
            exit();
        }
    }

    protected void exit() {
        hideToolbar();
        toolbar.setVisibility(View.INVISIBLE);
        if (oTitle.isEmpty())
            titleText.setVisibility(View.INVISIBLE);
        if (oSubtitle.isEmpty())
            subtitleText.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }

    private boolean saveData() {
        if (checklistView.getChecklistData().size() == 0) {
            Snackbar.make(coordinatorLayout, R.string.incomplete, Snackbar.LENGTH_LONG).show();
            return false;
        }

        title = titleText.getText().toString();
        subtitle = subtitleText.getText().toString();
        time = sdf.format(Calendar.getInstance().getTime());
        dbHelper.saveChecklist(id, title, subtitle, checklistView.getChecklistData(), time, created_at, archived, notified, color, encrypted, pinned, tag, reminder);
        onListChanged();
        return true;
    }

    private void onListChanged() {
        Intent intent = new Intent("note-list-changed");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}