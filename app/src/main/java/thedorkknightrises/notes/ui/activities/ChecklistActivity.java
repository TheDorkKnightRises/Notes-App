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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import thedorkknightrises.checklistview.views.ChecklistView;
import thedorkknightrises.notes.AlarmReceiver;
import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.data.NotesDbHelper;

/**
 * Created by Samriddha on 23-07-2017.
 */

public class ChecklistActivity extends AppCompatActivity {
    protected Boolean editMode;
    protected CoordinatorLayout coordinatorLayout;
    protected NotesDbHelper dbHelper;
    protected EditText titleText;
    protected EditText subtitleText;
    protected EditText contentText;
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
        setContentView(R.layout.activity_checklist);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        ChecklistView checklistView = (ChecklistView) findViewById(R.id.checklist_view);
        checklistView.getDragLinearLayout().setContainerScrollView((ScrollView) findViewById(R.id.scrollView));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
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
        backPressFlag = true;
        onBackPressed();
    }

    public void delete(View v) {
        finish();
    }

    public void onClick(View v) {


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
            Intent resultIntent = new Intent(this, thedorkknightrises.notes.ui.activities.NoteActivity.class);
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
            stackBuilder.addParentStack(thedorkknightrises.notes.ui.activities.NoteActivity.class);
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
        if (!backPressFlag) {
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