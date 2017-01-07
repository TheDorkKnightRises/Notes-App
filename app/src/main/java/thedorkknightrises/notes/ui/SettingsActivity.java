package thedorkknightrises.notes.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
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
import com.google.android.gms.drive.DriveId;
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

import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.BackupDbHelper;
import thedorkknightrises.notes.data.NotesDbHelper;

/**
 * Created by Samriddha Basu on 6/22/2016.
 */
public class SettingsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int REQUEST_CODE_RESOLUTION = 3;
    GoogleApiClient mGoogleApiClient;
    private SharedPreferences pref;
    private SwitchCompat theme_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences("Prefs", MODE_PRIVATE);
        if (pref.getBoolean("lightTheme", false))
            setTheme(R.style.AppTheme_Light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
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

    @Override
    protected void onResume() {
        super.onResume();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        theme_switch = (SwitchCompat) findViewById(R.id.theme_switch);

        pref = getSharedPreferences("Prefs", MODE_PRIVATE);

        theme_switch.setChecked(pref.getBoolean("lightTheme", false));

        theme_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedChange(theme_switch);
            }
        });
    }

    public void onCheckedChange(View v) {
        if (v.equals(theme_switch) || v.equals(findViewById(R.id.theme_switch_row))) {
            Boolean b = pref.getBoolean("lightTheme", false);
            SharedPreferences.Editor e = pref.edit();
            e.putBoolean("lightTheme", !b);
            e.apply();
            recreate();
        }
    }

    public void driveBackup(View v) {
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
        final Context context = this;
        final ProgressDialog progress = new ProgressDialog(context);
        progress.setMessage(getString(R.string.restoring));
        progress.setCancelable(false);
        progress.show();
        Query query = new Query.Builder().addFilter(Filters.and(
                Filters.eq(SearchableField.TITLE, NotesDbHelper.DATABASE_NAME)))
                .build();
        Drive.DriveApi.query(mGoogleApiClient, query).setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {
            @Override
            public void onResult(@NonNull DriveApi.MetadataBufferResult result) {
                if (!result.getStatus().isSuccess()) {
                    Toast.makeText(SettingsActivity.this, getText(R.string.error_restore), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                    return;
                }
                if (result.getMetadataBuffer().iterator().hasNext()) {
                    DriveFile file = result.getMetadataBuffer().iterator().next().getDriveId().asDriveFile();
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
                }
            }
        });

    }

    public void fetchFile(DriveId driveId) {

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
                MainActivity.changed = true;
                Toast.makeText(context, getString(R.string.restored), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(context, getString(R.string.error_restore), Toast.LENGTH_SHORT).show();
            progress.dismiss();
        }
    }
}
