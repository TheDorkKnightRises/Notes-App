package thedorkknightrises.notes.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.actions.NoteIntents;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import thedorkknightrises.notes.AlarmReceiver;
import thedorkknightrises.notes.Constants;
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
    protected EditText titleText, subtitleText, contentText;
    protected TextView timeText;
    FloatingActionButton fab;
    View toolbar_note, toolbar, bottom_bar;
    View archive_hint;
    SharedPreferences pref;
    boolean lightTheme;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), readableDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
    float radius;
    // Counters to work around Android Date and Time Picker bugs on lower versions
    int noOfTimesCalledDate, noOfTimesCalledTime;
    private int cx, cy;
    private int id = -1, archived = 0, notified = 0, encrypted = 0, pinned = 0, tag = 0, checklist = 0;
    private String title, subtitle, content, time, created_at, color = Constants.COLOR_NONE, reminder = Constants.REMINDER_NONE;
    private boolean backPressFlag = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        lightTheme = pref.getBoolean(Constants.LIGHT_THEME, false);
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
        toolbar_note = findViewById(R.id.toolbar_note);
        toolbar = findViewById(R.id.toolbar);
        bottom_bar = findViewById(R.id.bottom_bar);
        archive_hint = findViewById(R.id.archive_hint);

        dbHelper = new NotesDbHelper(this);

        Bundle bundle = getIntent().getBundleExtra(Constants.NOTE_DETAILS_BUNDLE);
        if (bundle != null) {
            editMode = false;
            id = bundle.getInt(NotesDb.Note._ID);
            title = bundle.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = bundle.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            content = bundle.getString(NotesDb.Note.COLUMN_NAME_CONTENT);
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
        } else {
            editMode = true;
        }

        if (savedInstanceState != null) {
            editMode = savedInstanceState.getBoolean("editMode");
            id = savedInstanceState.getInt(NotesDb.Note._ID);
            title = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            content = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_CONTENT);
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
            bottom_bar.setVisibility(View.VISIBLE);
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
        }

        if (!editMode) {
            if (TextUtils.isEmpty(title))
                titleText.setVisibility(View.GONE);
            else {
                titleText.setText(title);
                titleText.setEnabled(false);
                if (lightTheme)
                    titleText.setTextColor(getResources().getColor(R.color.black));
                else titleText.setTextColor(getResources().getColor(R.color.white));
            }
            if (TextUtils.isEmpty(subtitle))
                subtitleText.setVisibility(View.GONE);
            else {
                subtitleText.setText(subtitle);
                subtitleText.setEnabled(false);
                if (lightTheme)
                    subtitleText.setTextColor(getResources().getColor(R.color.dark_gray));
                else subtitleText.setTextColor(getResources().getColor(R.color.light_gray));
            }

            contentText.setText(content);
            edit(contentText, false);
            if (lightTheme)
                contentText.setTextColor(getResources().getColor(R.color.black));
            else contentText.setTextColor(getResources().getColor(R.color.white));

            timeText.setText(time);

            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setupWindowAnimations();
            }
            bottom_bar.setVisibility(View.GONE);
            timeText.setText("");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toolbar.setVisibility(View.VISIBLE);
                if (savedInstanceState == null && !editMode) revealToolbar();
            }
        }, 350);

        if (archived == 1) {
            archive_hint.setVisibility(View.VISIBLE);
            ((ImageButton) findViewById(R.id.archive_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_unarchive_white_24dp));
        }

        Log.d("Note:", "id: " + id + " created_at: " + created_at);
        Intent intent = getIntent();
        if (NoteIntents.ACTION_CREATE_NOTE.equals(intent.getAction()) || Intent.ACTION_SEND.equals(intent.getAction())) {
            if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                content = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
                if (!TextUtils.isEmpty(content)) contentText.setText(content);
            }
            if (intent.hasExtra(Intent.EXTRA_SUBJECT)) {
                title = getIntent().getExtras().getString(Intent.EXTRA_SUBJECT);
                if (!TextUtils.isEmpty(title)) titleText.setText(title);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolbar.setVisibility(View.VISIBLE);
                    if (savedInstanceState == null && !editMode) revealToolbar();
                    onClick(fab);
                }
            }, 350);
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
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("editMode", editMode);
        bundle.putInt(NotesDb.Note._ID, id);
        bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
        bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, content);
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

    public void close(View v) {
        if (titleText.getText().toString().isEmpty()) titleText.setVisibility(View.INVISIBLE);
        if (subtitleText.getText().toString().isEmpty()) subtitleText.setVisibility(View.INVISIBLE);
        backPressFlag = true;
        onBackPressed();
    }

    public void delete(View v) {
        notif(0);
        toggleReminder(false);
        dbHelper.deleteNote(created_at);
        finish();
    }

    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editMode) {
            title = titleText.getText().toString().trim();
            subtitle = subtitleText.getText().toString().trim();
            content = contentText.getText().toString().trim();
            if (content.equals("")) {
                contentText.requestFocus();
                Snackbar.make(coordinatorLayout, R.string.incomplete, Snackbar.LENGTH_LONG).show();
            } else {
                Calendar c = Calendar.getInstance();
                //get date and time, specifically in 24-hr format suitable for sorting
                time = sdf.format(c.getTime());
                Log.d("TIME", time);
                archived = 0;
                notif(0);
                id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder, checklist);
                editMode = false;
                if (!title.equals("")) {
                    titleText.setEnabled(false);
                    if (lightTheme)
                        titleText.setTextColor(getResources().getColor(R.color.black));
                    else titleText.setTextColor(getResources().getColor(R.color.white));
                } else titleText.setVisibility(View.GONE);
                if (!subtitle.equals("")) {
                    subtitleText.setEnabled(false);
                    if (lightTheme)
                        subtitleText.setTextColor(getResources().getColor(R.color.dark_gray));
                    else subtitleText.setTextColor(getResources().getColor(R.color.light_gray));
                } else subtitleText.setVisibility(View.GONE);
                edit(contentText, false);
                if (lightTheme)
                    contentText.setTextColor(getResources().getColor(R.color.black));
                else contentText.setTextColor(getResources().getColor(R.color.white));

                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));

                archive_hint.setVisibility(View.GONE);
                revealToolbar();
                findViewById(R.id.note_update).setVisibility(View.VISIBLE);
                timeText.setText(time);
                editMode = false;

                if (MainActivity.archive == 1) {
                    MainActivity.archive = 0;
                }

                notif(notified);

                // Hide the keyboard
                imm.hideSoftInputFromWindow(contentText.getWindowToken(), 0);
            }
        } else {
            titleText.setEnabled(true);
            subtitleText.setEnabled(true);
            edit(contentText, true);
            contentText.setSelection(contentText.getText().length());
            titleText.setVisibility(View.VISIBLE);
            subtitleText.setVisibility(View.VISIBLE);
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_done_white_24dp));
            hideToolbar();
            if (archived == 1) {
                archive_hint.setVisibility(View.VISIBLE);
            }
            findViewById(R.id.note_update).setVisibility(View.GONE);
            timeText.setText("");
            editMode = true;
            contentText.requestFocusFromTouch();
            // Show the keyboard
            imm.showSoftInput(contentText, InputMethodManager.SHOW_IMPLICIT);
        }
        onResume();

    }

    private void edit(EditText editText, boolean enabled) {
        if (!enabled) {
            Linkify.addLinks(editText, Linkify.ALL);
            editText.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            editText.setText(editText.getText().toString());
            editText.setMovementMethod(ArrowKeyMovementMethod.getInstance());
        }
        editText.setFocusable(enabled);
        editText.setFocusableInTouchMode(enabled);
        editText.setClickable(enabled);
        editText.setLongClickable(enabled);
        editText.setLinksClickable(!enabled);
    }

    public void share(View v) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, content);
        share.setType("text/plain");
        if (share.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(share, getResources().getString(R.string.share_via)));
        } else {
            Toast.makeText(this, R.string.no_share_app_found, Toast.LENGTH_SHORT).show();
        }
    }

    public void notifBtn(View v) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheet_Dark);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);
        final TextView timeView = (TextView) bottomSheetDialog.findViewById(R.id.reminder_time);
        Switch notifSwitch = (Switch) bottomSheetDialog.findViewById(R.id.notification_switch);
        if (notified == 1) notifSwitch.setChecked(true);
        final Switch reminderSwitch = (Switch) bottomSheetDialog.findViewById(R.id.reminder_switch);
        if (!reminder.equals(Constants.REMINDER_NONE)) {
            reminderSwitch.setChecked(true);
            timeView.setText(reminder);
            timeView.setVisibility(View.VISIBLE);
        }
        notifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                notif(0);
                if (notified == 1) {
                    notified = 0;
                    id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder, checklist);
                    notif(notified);
                } else {
                    notified = 1;
                    id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder, checklist);
                    notif(notified);
                }
            }
        });

        reminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (reminderSwitch.getTag() != null) {
                    return;
                }
                if (!b) {
                    reminder = Constants.REMINDER_NONE;
                    id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder, checklist);
                    toggleReminder(false);
                    timeView.setVisibility(View.GONE);
                } else {
                    noOfTimesCalledDate = 0;
                    noOfTimesCalledTime = 0;
                    final Calendar calendar = Calendar.getInstance();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(NoteActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, final int year, final int month, final int day) {
                                    if (noOfTimesCalledDate % 2 == 0) {
                                        new TimePickerDialog(NoteActivity.this,
                                                new TimePickerDialog.OnTimeSetListener() {
                                                    @Override
                                                    public void onTimeSet(TimePicker timePicker, int hour, int min) {
                                                        if (noOfTimesCalledTime % 2 == 0) {
                                                            calendar.set(year, month, day, hour, min);
                                                            calendar.set(Calendar.SECOND, 0);
                                                            if (calendar.compareTo(Calendar.getInstance()) <= 0) {
                                                                Toast.makeText(NoteActivity.this, R.string.invalid_time, Toast.LENGTH_SHORT).show();
                                                                return;
                                                            }
                                                            reminder = readableDateFormat.format(calendar.getTime());
                                                            id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder, checklist);
                                                            toggleReminder(true);
                                                            reminderSwitch.setTag(Boolean.TRUE);
                                                            reminderSwitch.setChecked(true);
                                                            reminderSwitch.setTag(null);
                                                            timeView.setText(reminder);
                                                            timeView.setVisibility(View.VISIBLE);
                                                            noOfTimesCalledTime++;
                                                        }
                                                    }
                                                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(NoteActivity.this)).show();
                                        noOfTimesCalledDate++;
                                    }
                                }
                            },
                            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                    datePickerDialog.show();

                    reminderSwitch.setTag(Boolean.TRUE);
                    reminderSwitch.setChecked(false);
                    reminderSwitch.setTag(null);
                }
            }
        });

        bottomSheetDialog.show();
    }

    private void toggleReminder(boolean enable) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        Bundle bundle = new Bundle();
        bundle.putInt(NotesDb.Note._ID, id);
        intent.putExtra(Constants.NOTE_DETAILS_BUNDLE, bundle);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (enable) {
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(readableDateFormat.parse(reminder));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            alarmManager.cancel(alarmIntent);
        }
    }

    public void notif(int notified) {
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notified == 0) {
            mNotifyMgr.cancel(id);
        } else {
            String info;
            if (!subtitle.equals("")) info = subtitle;
            else info = time;
            NotificationCompat.Builder notif =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentText(content)
                            .setSubText(info)
                            .setShowWhen(false)
                            .setCategory(getString(R.string.notes))
                            .setColor(Color.argb(255, 32, 128, 200));

            if (!TextUtils.isEmpty(title)) {
                notif.setContentTitle(title);
            } else {
                notif.setContentTitle(getString(R.string.note));
            }

            notif.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setSummaryText(time));
            // Sets an ID for the notification
            Log.d("NOTIFICATION ID", String.valueOf(id));
            Intent resultIntent = new Intent(this, NoteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(NotesDb.Note._ID, id);
            bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
            bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
            bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, content);
            bundle.putString(NotesDb.Note.COLUMN_NAME_TIME, time);
            bundle.putString(NotesDb.Note.COLUMN_NAME_CREATED_AT, created_at);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
            bundle.putString(NotesDb.Note.COLUMN_NAME_COLOR, color);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED, encrypted);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_PINNED, pinned);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_TAG, tag);
            bundle.putString(NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_CHECKLIST, checklist);
            resultIntent.putExtra(Constants.NOTE_DETAILS_BUNDLE, bundle);
            resultIntent.setAction("ACTION_NOTE_" + id);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(NoteActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

            notif.setContentIntent(resultPendingIntent);
            notif.setOngoing(true);

            // TODO: Add actions to open and dismiss
            // notif.addAction(R.drawable.common_full_open_on_phone, getString(R.string.open_app), resultPendingIntent);

            Log.d("Note:", "id: " + id + " created_at: " + created_at);
            // Builds the notification and issues it.
            mNotifyMgr.notify(id, notif.build());

        }
    }

    public void archive(View v) {
        notif(0);
        if (archived == 1) {
            Toast.makeText(this, R.string.removed_archive, Toast.LENGTH_SHORT).show();
            archived = 0;
            id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder, checklist);
            notif(notified);
            finish();
        } else {
            Toast.makeText(this, R.string.added_archive, Toast.LENGTH_SHORT).show();
            archived = 1;
            id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder, checklist);
            notif(notified);
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
        hideToolbar();
        toolbar.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }
}