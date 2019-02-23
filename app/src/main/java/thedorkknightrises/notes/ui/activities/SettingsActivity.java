package thedorkknightrises.notes.ui.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.text.DecimalFormat;
import java.util.Calendar;

import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.BackupDbHelper;
import thedorkknightrises.notes.data.NotesDbHelper;
import thedorkknightrises.notes.receivers.BootReceiver;
import thedorkknightrises.notes.util.NetworkUtils;

/**
 * Created by Samriddha Basu on 6/22/2016.
 */
public class SettingsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CODE_RESOLUTION = 3;
    private static final int STORAGE_PERMISSION_REQUEST_CODE_BACKUP = 101;
    private static final int STORAGE_PERMISSION_REQUEST_CODE_RESTORE = 102;
    GoogleApiClient mGoogleApiClient;
    NotificationManager mNotifyMgr;
    ProgressDialog progress;
    private SharedPreferences pref;
    private SwitchCompat theme_switch, notif_switch, reminder_sound_switch, reminder_vibrate_switch, reminder_led_switch, share_info_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        if (pref.getBoolean(Constants.LIGHT_THEME, false))
            setTheme(R.style.AppTheme_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
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

        theme_switch = findViewById(R.id.theme_switch);
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

        notif_switch = findViewById(R.id.quicknotify_switch);
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

        reminder_vibrate_switch = findViewById(R.id.reminder_vibrate_switch);
        reminder_vibrate_switch.setChecked(pref.getBoolean(Constants.REMINDER_VIBRATE, true));
        reminder_vibrate_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.REMINDER_VIBRATE, isChecked);
                e.apply();
            }
        });

        reminder_sound_switch = findViewById(R.id.reminder_sound_switch);
        reminder_sound_switch.setChecked(pref.getBoolean(Constants.REMINDER_SOUND, true));
        reminder_sound_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.REMINDER_SOUND, isChecked);
                e.apply();
            }
        });

        reminder_led_switch = findViewById(R.id.reminder_led_switch);
        reminder_led_switch.setChecked(pref.getBoolean(Constants.REMINDER_LED, true));
        reminder_led_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.REMINDER_LED, isChecked);
                e.apply();
            }
        });

        share_info_switch = findViewById(R.id.share_info_switch);
        share_info_switch.setChecked(pref.getBoolean(Constants.SHARE_INFO, true));
        share_info_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor e = pref.edit();
                e.putBoolean(Constants.SHARE_INFO, isChecked);
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
        } else if (v.equals(findViewById(R.id.reminder_sound_switch_row))) {
            reminder_sound_switch.toggle();
        } else if (v.equals(findViewById(R.id.reminder_vibrate_switch_row))) {
            reminder_vibrate_switch.toggle();
        } else if (v.equals(findViewById(R.id.reminder_led_switch_row))) {
            reminder_led_switch.toggle();
        } else if (v.equals(findViewById(R.id.share_info_row))) {
            share_info_switch.toggle();
        }
    }

    private void createQuickNotification() {
        new BootReceiver().onReceive(this, null);
    }

    public void driveBackup(View v) {
        if (!NetworkUtils.isNetworkConnected(this)) {
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
        if (!NetworkUtils.isNetworkConnected(this)) {
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
                        onListChanged();
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
                        onListChanged();
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
                        onListChanged();
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

    private void onListChanged() {
        Intent intent = new Intent("note-list-changed");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void localBackup(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE_BACKUP);
            return;
        }

        File dbFile = this.getDatabasePath(NotesDbHelper.DATABASE_NAME);
        Calendar calendar = Calendar.getInstance();
        DecimalFormat decimalFormat = new DecimalFormat("00");
        String dateTimeString = String.valueOf(calendar.get(Calendar.YEAR)) +
                "-" + decimalFormat.format(calendar.get(Calendar.MONTH) + 1) +
                "-" + decimalFormat.format(calendar.get(Calendar.DATE)) +
                "_" + decimalFormat.format(calendar.get(Calendar.HOUR)) +
                "-" + decimalFormat.format(calendar.get(Calendar.MINUTE));
        File file = new File(Environment.getExternalStoragePublicDirectory("NotePal").getAbsolutePath()
                + File.separator + "Backups" + File.separator + "Backup_" + dateTimeString);

        progress.setMessage(getString(R.string.backing_up));
        progress.show();
        if (copyFile(dbFile, file))
            Toast.makeText(this, getString(R.string.backup_success) + ": " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
        progress.dismiss();
    }

    public void localRestore(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE_RESTORE);
            return;
        }

        File dir = new File(Environment.getExternalStoragePublicDirectory("NotePal").getAbsolutePath()
                + File.separator + "Backups");
        dir.mkdirs();
        Log.d(getLocalClassName(), "Path: " + dir.getAbsolutePath());
        File[] files = dir.listFiles();
        Log.d(getLocalClassName(), "Number of backups: " + files.length);

        if (files.length > 0) {
            final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, (pref.getBoolean(Constants.LIGHT_THEME, false)) ? R.style.BottomSheet_Light : R.style.BottomSheet_Dark);
            bottomSheetDialog.setContentView(R.layout.backups_list_bottom_sheet_layout);
            LinearLayout linearLayout = bottomSheetDialog.findViewById(R.id.linearLayout);
            for (final File file : files) {
                TextView textView = new TextView(this);
                textView.setPadding(16, 16, 16, 16);
                textView.setMinHeight(144);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setText(file.getName());
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File backupDbFile = getDatabasePath(BackupDbHelper.DATABASE_NAME);
                        progress.setMessage(getString(R.string.restoring));
                        progress.show();
                        if (copyFile(file, backupDbFile)) {
                            BackupDbHelper backupDbHelper = new BackupDbHelper(SettingsActivity.this);
                            backupDbHelper.merge(getApplicationContext());
                            Toast.makeText(SettingsActivity.this, getString(R.string.restored), Toast.LENGTH_SHORT).show();

                            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            mNotifyMgr.cancelAll();
                            new BootReceiver().onReceive(SettingsActivity.this, null);
                            onListChanged();
                        } else {
                            Toast.makeText(SettingsActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                        }
                        progress.dismiss();
                        bottomSheetDialog.dismiss();
                    }
                });
                linearLayout.addView(textView);
            }
            bottomSheetDialog.show();
        } else {
            Toast.makeText(this, R.string.no_backups, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case STORAGE_PERMISSION_REQUEST_CODE_BACKUP:
                    localBackup(null);
                    break;
                case STORAGE_PERMISSION_REQUEST_CODE_RESTORE:
                    localRestore(null);
                    break;
            }
        } else if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, R.string.storage_permission_rationale, Toast.LENGTH_LONG).show();
        }
    }

    public boolean copyFile(File src, File dest) {
        try {
            dest.getParentFile().mkdirs();
            dest.createNewFile();
            InputStream is = new FileInputStream(src);
            OutputStream os = new FileOutputStream(dest);
            byte[] buff = new byte[1024];
            int len;
            while ((len = is.read(buff)) > 0) {
                os.write(buff, 0, len);
            }
            is.close();
            os.close();
        } catch (IOException e) {
            Log.d(getLocalClassName(), e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
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
                onListChanged();
            } else
                Toast.makeText(context, getString(R.string.error_restore), Toast.LENGTH_SHORT).show();
            progress.dismiss();
        }
    }
}
