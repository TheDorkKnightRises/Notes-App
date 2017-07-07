package thedorkknightrises.notes.ui.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.firebase.crash.FirebaseCrash;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import thedorkknightrises.notes.BootReceiver;
import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.BackupDbHelper;
import thedorkknightrises.notes.data.NotesDbHelper;
import thedorkknightrises.notes.util.NetworkUtil;

/**
 * Created by Samriddha Basu on 6/22/2016.
 */
public class SettingsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CODE_RESOLUTION = 3;
    GoogleApiClient mGoogleApiClient;
    NotificationManager mNotifyMgr;
    ProgressDialog progress;
    private SharedPreferences pref;
    private SwitchCompat theme_switch, notif_switch, ad_switch, reminder_sound_switch, reminder_vibrate_switch, reminder_led_switch;
    private int type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        if (pref.getBoolean(Constants.LIGHT_THEME, false))
            setTheme(R.style.AppTheme_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        progress = new ProgressDialog(this);

        theme_switch = (SwitchCompat) findViewById(R.id.theme_switch);
        theme_switch.setChecked(pref.getBoolean(Constants.LIGHT_THEME, false));
        theme_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.LIGHT_THEME, isChecked);
                e.apply();
                recreate();
            }
        });

        notif_switch = (SwitchCompat) findViewById(R.id.quicknotify_switch);
        notif_switch.setChecked(pref.getBoolean(Constants.QUICK_NOTIFY, false));
        notif_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.QUICK_NOTIFY, isChecked);
                e.apply();
                if (!isChecked)
                    mNotifyMgr.cancel(0);
                else
                    createQuickNotification();
            }
        });

        ad_switch = (SwitchCompat) findViewById(R.id.ads_switch);
        ad_switch.setChecked(pref.getBoolean(Constants.ADS_ENABLED, true));
        ad_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.ADS_ENABLED, isChecked);
                e.apply();
            }
        });

        reminder_vibrate_switch = (SwitchCompat) findViewById(R.id.reminder_vibrate_switch);
        reminder_vibrate_switch.setChecked(pref.getBoolean(Constants.REMINDER_VIBRATE, true));
        reminder_vibrate_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.REMINDER_VIBRATE, isChecked);
                e.apply();
            }
        });

        reminder_sound_switch = (SwitchCompat) findViewById(R.id.reminder_sound_switch);
        reminder_sound_switch.setChecked(pref.getBoolean(Constants.REMINDER_SOUND, true));
        reminder_sound_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.REMINDER_SOUND, isChecked);
                e.apply();
            }
        });

        reminder_led_switch = (SwitchCompat) findViewById(R.id.reminder_led_switch);
        reminder_led_switch.setChecked(pref.getBoolean(Constants.REMINDER_LED, true));
        reminder_led_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.REMINDER_LED, isChecked);
                e.apply();
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                FirebaseCrash.report(e);
                Snackbar.make(findViewById(R.id.rootview), getText(R.string.drive_error), Snackbar.LENGTH_SHORT).show();
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    public void onCheckedChange(View v) {
        if (v.equals(findViewById(R.id.theme_switch_row))) {
            theme_switch.toggle();
        } else if (v.equals(findViewById(R.id.notification_switch_row))) {
            notif_switch.toggle();
        } else if (v.equals(findViewById(R.id.ads_switch_row))) {
            ad_switch.toggle();
        } else if (v.equals(findViewById(R.id.reminder_sound_switch_row))) {
            reminder_sound_switch.toggle();
        } else if (v.equals(findViewById(R.id.reminder_vibrate_switch_row))) {
            reminder_vibrate_switch.toggle();
        } else if (v.equals(findViewById(R.id.reminder_led_switch_row))) {
            reminder_led_switch.toggle();
        }
    }

    private void createQuickNotification() {
        NotificationCompat.Builder notif =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.new_note))
                        .setContentText(getString(R.string.tap_create_note))
                        .setShowWhen(false)
                        .setGroup(Constants.QUICK_NOTIFY)
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setColor(Color.argb(255, 32, 128, 200));
        Intent resultIntent = new Intent(this, NoteActivity.class);
        resultIntent.setAction("ACTION_NOTE_" + 0);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(NoteActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        notif.addAction(R.drawable.ic_note_white_24dp, getString(R.string.new_note), resultPendingIntent);
        // TODO: Uncomment once checklists are implemented
        // resultIntent.putExtra(NotesDb.Note.COLUMN_NAME_CHECKLIST, true);
        // resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        // notif.addAction(R.drawable.ic_list_white_24dp, getString(R.string.new_checklist), resultPendingIntent);

        notif.setContentIntent(resultPendingIntent);
        notif.setOngoing(true);

        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        TaskStackBuilder newStackBuilder = TaskStackBuilder.create(this);
        newStackBuilder.addParentStack(NoteActivity.class);
        newStackBuilder.addNextIntent(settingsIntent);
        resultPendingIntent =
                newStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.addAction(R.drawable.ic_settings_white_24dp, getString(R.string.action_settings), resultPendingIntent);

        // Builds the notification and issues it.
        mNotifyMgr.notify(0, notif.build());
    }

    public void driveBackup(View v) {
        if (!NetworkUtil.isNetworkConnected(this)) {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        progress.setMessage(getString(R.string.connecting));
        progress.setCancelable(false);
        progress.show();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        progress.setMessage(getString(R.string.backing_up));
        final File file = this.getDatabasePath(NotesDbHelper.DATABASE_NAME);
        final Context context = this;
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e("DRIVE_BACKUP", "Error while trying to create new file contents");
                            return;
                        }
                        final DriveContents driveContents = result.getDriveContents();

                        // Perform I/O off the UI thread.
                        new BackupFileTask(context, driveContents, file).execute();
                    }
                });
    }

    public void driveRestore(View v) {
        if (!NetworkUtil.isNetworkConnected(this)) {
            Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
            return;
        }
        final Context context = this;
        progress.setMessage(getString(R.string.connecting));
        progress.setCancelable(false);
        progress.show();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        progress.setMessage(getString(R.string.restoring));
        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.TITLE, NotesDbHelper.DATABASE_NAME)))
                .build();
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    Toast.makeText(SettingsActivity.this, getText(R.string.error_restore), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                    result.release();
                    return;
                }
                Log.e("Drive results: ", String.valueOf(result.getMetadataBuffer().getCount()));
                if (result.getMetadataBuffer().getCount() != 0) {
                    DriveFile file = result.getMetadataBuffer().get(0).getDriveId().asDriveFile();
                    file.open(mGoogleApiClient, DriveFile.MODE_READ_ONLY, null)
                            .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                                @Override
                                public void onResult(@NonNull DriveApi.DriveContentsResult result) {
                                    if (!result.getStatus().isSuccess()) {
                                        progress.dismiss();
                                        return;
                                    }

                                    // DriveContents object contains pointers
                                    // to the actual byte stream
                                    DriveContents contents = result.getDriveContents();
                                    File file = getDatabasePath(BackupDbHelper.DATABASE_NAME);

                                    new FetchFileTask(context, contents, file, progress).execute();
                                }
                            });
                } else {
                    progress.dismiss();
                    Toast.makeText(context, getString(R.string.no_backups), Toast.LENGTH_SHORT).show();
                }
                result.release();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void resetNotes(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AppTheme_PopupOverlay);
        dialog.setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NotesDbHelper dbHelper = new NotesDbHelper(SettingsActivity.this);
                        dbHelper.deleteAllNotes();
                        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNotifyMgr.cancelAll();
                        new BootReceiver().onReceive(SettingsActivity.this, null);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void clearAllNotifications(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AppTheme_PopupOverlay);
        dialog.setMessage(R.string.confirm_clear_notifications)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NotesDbHelper dbHelper = new NotesDbHelper(SettingsActivity.this);
                        dbHelper.clearAllNotifications();
                        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        mNotifyMgr.cancelAll();
                        new BootReceiver().onReceive(SettingsActivity.this, null);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void clearAllReminders(View view) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AppTheme_PopupOverlay);
        dialog.setMessage(R.string.confirm_clear_reminders)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NotesDbHelper dbHelper = new NotesDbHelper(SettingsActivity.this);
                        dbHelper.clearAllReminders();
                        new BootReceiver().onReceive(SettingsActivity.this, null);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public class BackupFileTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private DriveContents driveContents;
        private File file;

        public BackupFileTask(Context context, DriveContents driveContents, File file) {
            this.context = context;
            this.driveContents = driveContents;
            this.file = file;
        }

        @Override
        protected Void doInBackground(Void... objects) {
            // write content to DriveContents
            OutputStream outputStream = driveContents.getOutputStream();

            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                FirebaseCrash.log("FileNotFoundException while performing Drive backup");
                e.printStackTrace();
            }

            byte[] buf = new byte[1024];
            int bytesRead;
            try {
                if (inputStream != null) {
                    while ((bytesRead = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, bytesRead);
                    }
                }
            } catch (IOException e) {
                FirebaseCrash.log("Exception while performing Drive backup");
                e.printStackTrace();
            }

            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                    .setMimeType(MimeTypeMap.getSingleton().getMimeTypeFromExtension("db"))
                    .setTitle(NotesDbHelper.DATABASE_NAME)
                    .build();

            // create a file in selected folder
            Drive.DriveApi.getAppFolder(mGoogleApiClient)
                    .createFile(mGoogleApiClient, metadataChangeSet, driveContents)
                    .setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {
                        @Override
                        public void onResult(@NonNull DriveFolder.DriveFileResult result) {
                            if (!result.getStatus().isSuccess()) {
                                Log.d("DRIVE_BACKUP", "Error while trying to create the file");
                                return;
                            }
                            Toast.makeText(context, getText(R.string.backup_success), Toast.LENGTH_SHORT).show();
                            progress.dismiss();
                        }
                    });
            return null;
        }
    }

    public class FetchFileTask extends AsyncTask<Void, Void, Boolean> {
        private Context context;
        private DriveContents driveContents;
        private File file;
        private ProgressDialog progress;

        public FetchFileTask(Context context, DriveContents driveContents, File file, ProgressDialog progressDialog) {
            this.context = context;
            this.driveContents = driveContents;
            this.file = file;
            this.progress = progressDialog;
        }

        @Override
        protected Boolean doInBackground(Void... objects) {
            InputStream input = driveContents.getInputStream();
            try {
                OutputStream output = new FileOutputStream(file);
                try {
                    byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;

                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                    input.close();
                } catch (Exception e) {
                    FirebaseCrash.log("Exception while restoring Drive backup");
                    e.printStackTrace();
                    return false;
                }
            } catch (FileNotFoundException e) {
                FirebaseCrash.log("FileNotFoundException while restoring Drive backup");
                e.printStackTrace();
                return false;
            }
            BackupDbHelper backupDbHelper = new BackupDbHelper(context);
            backupDbHelper.merge(getApplicationContext());

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            file.delete();
            if (result) {
                Toast.makeText(context, getString(R.string.restored), Toast.LENGTH_SHORT).show();
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.cancelAll();
                new BootReceiver().onReceive(context, null);
            } else
                Toast.makeText(context, getString(R.string.error_restore), Toast.LENGTH_SHORT).show();
            progress.dismiss();
        }
    }
}
