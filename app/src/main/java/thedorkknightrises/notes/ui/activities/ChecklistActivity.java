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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
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
import thedorkknightrises.notes.receivers.AlarmReceiver;

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
    ArrayList<ChecklistData> checklistDatas = new ArrayList<>(), oDatas;
    View toolbar_note, toolbar, bottom_bar;
    float radius;
    // Counters to work around Android Date and Time Picker bugs on lower versions
    int noOfTimesCalledDate, noOfTimesCalledTime;
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

            oTitle = savedInstanceState.getString("oTitle");
            oSubtitle = savedInstanceState.getString("oSubtitle");
            oDatas = savedInstanceState.getParcelableArrayList("oDatas");

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
        } else {
            if (title == null || title.isEmpty()) {
                title = "";
                titleText.setVisibility(View.INVISIBLE);
            }
            if (subtitle == null || subtitle.isEmpty()) {
                subtitle = "";
                subtitleText.setVisibility(View.INVISIBLE);
            }

            // save original values in case there are assignments later
            oTitle = title.trim();
            oSubtitle = subtitle.trim();
            oDatas = checklistDatas;
        }

        if (bundle == null && savedInstanceState == null) {
            Calendar c = Calendar.getInstance();
            //get date and time, specifically in 24-hr format suitable for sorting
            created_at = sdf.format(c.getTime());
        } else {
            titleText.setText(title);
            subtitleText.setText(subtitle);
            for (ChecklistData checklistData : checklistDatas) {
                Log.d(getLocalClassName(), checklistData.toString());
            }
            checklistView.setChecklistData(checklistDatas);
        }

        // Toast.makeText(this, checklistDatas.get(0).getText(), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toolbar.setVisibility(View.VISIBLE);
                revealToolbar();
                if (title == null || title.isEmpty())
                    titleText.setVisibility(View.VISIBLE);
                if (subtitle == null || subtitle.isEmpty())
                    subtitleText.setVisibility(View.VISIBLE);
            }
        }, 350);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            setupWindowAnimations();
        }

        if (archived == 1) {
            ((ImageButton) findViewById(R.id.archive_button)).setImageDrawable(getResources().getDrawable(R.drawable.ic_unarchive_white_24dp));
        }

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

        bundle.putString("oTitle", oTitle);
        bundle.putString("oSubtitle", oSubtitle);
        bundle.putParcelableArrayList("oDatas", oDatas);

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
        saveData();
    }

    public void notifBtn(View v) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheet_Dark);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_layout);
        final TextView timeView = bottomSheetDialog.findViewById(R.id.reminder_time);
        Switch notifSwitch = bottomSheetDialog.findViewById(R.id.notification_switch);
        if (notified == 1) notifSwitch.setChecked(true);
        final Switch reminderSwitch = bottomSheetDialog.findViewById(R.id.reminder_switch);
        if (!reminder.equals(Constants.REMINDER_NONE)) {
            reminderSwitch.setChecked(true);
            timeView.setText(reminder);
            timeView.setVisibility(View.VISIBLE);
        }

        title = titleText.getText().toString();
        subtitle = subtitleText.getText().toString();
        time = sdf.format(Calendar.getInstance().getTime());
        final StringBuffer content = new StringBuffer();
        checklistDatas = checklistView.getChecklistData();
        for (ChecklistData data : checklistDatas) {
            if (data.isChecked()) {
                content.append("[\u2713] ").append(data.getText()).append("\n");
            } else {
                content.append("[    ] ").append(data.getText()).append("\n");
            }
        }

        notifSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                notified = 1 - notified;
                saveData();
                notif(notified);
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
                    saveData();
                    toggleReminder(false);
                    timeView.setVisibility(View.GONE);
                } else {
                    noOfTimesCalledDate = 0;
                    noOfTimesCalledTime = 0;
                    final Calendar calendar = Calendar.getInstance();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(ChecklistActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, final int year, final int month, final int day) {
                                    if (noOfTimesCalledDate % 2 == 0) {
                                        new TimePickerDialog(ChecklistActivity.this,
                                                new TimePickerDialog.OnTimeSetListener() {
                                                    @Override
                                                    public void onTimeSet(TimePicker timePicker, int hour, int min) {
                                                        if (noOfTimesCalledTime % 2 == 0) {
                                                            calendar.set(year, month, day, hour, min);
                                                            calendar.set(Calendar.SECOND, 0);
                                                            if (calendar.compareTo(Calendar.getInstance()) <= 0) {
                                                                Toast.makeText(ChecklistActivity.this, R.string.invalid_time, Toast.LENGTH_SHORT).show();
                                                                return;
                                                            }
                                                            reminder = readableDateFormat.format(calendar.getTime());
                                                            saveData();
                                                            toggleReminder(true);
                                                            reminderSwitch.setTag(Boolean.TRUE);
                                                            reminderSwitch.setChecked(true);
                                                            reminderSwitch.setTag(null);
                                                            timeView.setText(reminder);
                                                            timeView.setVisibility(View.VISIBLE);
                                                            noOfTimesCalledTime++;
                                                        }
                                                    }
                                                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(ChecklistActivity.this)).show();
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

            title = titleText.getText().toString();
            subtitle = subtitleText.getText().toString();
            time = sdf.format(Calendar.getInstance().getTime());
            final StringBuffer content = new StringBuffer();
            checklistDatas = checklistView.getChecklistData();
            for (ChecklistData data : checklistDatas) {
                if (data.isChecked()) {
                    content.append("[\u2713] ").append(data.getText()).append("\n");
                } else {
                    content.append("[    ] ").append(data.getText()).append("\n");
                }
            }

            if (!subtitle.equals("")) info = subtitle;
            else info = time;
            NotificationCompat.Builder notif =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_stat_notepal)
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
            Intent resultIntent = new Intent(this, ChecklistActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(NotesDb.Note._ID, id);
            bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
            bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
            bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, content.toString());
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
            stackBuilder.addParentStack(ChecklistActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

            notif.setContentIntent(resultPendingIntent);
            notif.setOngoing(true);
            notif.setChannelId(Constants.CHANNEL_ID_NOTE);

            Log.d("Note:", "id: " + id + " created_at: " + created_at);
            // Builds the notification and issues it.
            mNotifyMgr.notify(id, notif.build());

        }
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
        notif(0);
        toggleReminder(false);
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

        oTitle = title = titleText.getText().toString();
        oSubtitle = subtitle = subtitleText.getText().toString();
        time = sdf.format(Calendar.getInstance().getTime());
        StringBuffer content = new StringBuffer();
        oDatas = checklistDatas = checklistView.getChecklistData();
        for (ChecklistData data : checklistDatas) {
            if (data.isChecked()) {
                content.append("[\u2713] ").append(data.getText()).append("\n");
            } else {
                content.append("[    ] ").append(data.getText()).append("\n");
            }
        }
        id = dbHelper.saveChecklist(id, title, subtitle, content.toString(), checklistDatas, time, created_at, archived, notified, color, encrypted, pinned, tag, reminder);
        notif(notified);
        onListChanged();
        return true;
    }

    private void onListChanged() {
        Intent intent = new Intent("note-list-changed");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}