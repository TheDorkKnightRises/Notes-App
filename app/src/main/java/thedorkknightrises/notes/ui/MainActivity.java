package thedorkknightrises.notes.ui;

import android.app.LoaderManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import thedorkknightrises.notes.NoteObj;
import thedorkknightrises.notes.NotesAdapter;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.data.NotesDbHelper;
import thedorkknightrises.notes.data.NotesProvider;
import thedorkknightrises.notes.widget.NotesWidget;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static boolean added = false;
    static boolean changed = true;
    static boolean lightTheme;
    static int archive = 0;
    public NotesAdapter mAdapter;
    protected NotesDbHelper dbHelper;
    ArrayList<NoteObj> noteObjArrayList;
    RecyclerView recyclerView;
    TextView blankText;
    FloatingActionButton fab;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences("Prefs", MODE_PRIVATE);

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = pref.getBoolean("firstStart", true);
                //  If the activity has never started before...
                if (isFirstStart) {
                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);
                    //  Make a new preferences editor
                    SharedPreferences.Editor e = pref.edit();
                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);
                    //  Apply changes
                    e.apply();
                }
            }
        });
        // Start the thread
        t.start();

        lightTheme = pref.getBoolean("lightTheme", false);
        if (lightTheme)
            setTheme(R.style.AppTheme_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getLoaderManager().initLoader(0, null, this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getBaseContext(), NoteActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
                    startActivity(i, options.toBundle());
                } else startActivity(i);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        dbHelper = new NotesDbHelper(this);

        blankText = (TextView) findViewById(R.id.blankTextView);

        if (archive == 1) {
            getSupportActionBar().setTitle(R.string.archive);
            blankText.setText(R.string.blank_archive);
            fab.setVisibility(View.GONE);
        }

        //noteObjArrayList = dbHelper.getAllNotes(archive);
        noteObjArrayList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        //mAdapter = new NotesAdapter(noteObjArrayList, this, MainActivity.this);
        //recyclerView.setAdapter(mAdapter);

    }


    @Override
    public void onResume() {
        if (changed) {
            getLoaderManager().restartLoader(0, null, this);
            changed = false;
            added = false;
            updateWidgets();
        }

        if (archive == 1)
            getSupportActionBar().setTitle(R.string.archive);
        else getSupportActionBar().setTitle(R.string.notes);

        if (lightTheme != pref.getBoolean("lightTheme", false)) {
            if (lightTheme)
                setTheme(R.style.AppTheme_NoActionBar);
            else setTheme(R.style.AppTheme_Light_NoActionBar);
            lightTheme = !lightTheme;
            recreate();
        }
        super.onPostResume();

        super.onResume();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.delete_all) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AppTheme_PopupOverlay);
            dialog.setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHelper.deleteAllNotes(archive);
                            noteObjArrayList.clear();
                            recyclerView.removeAllViewsInLayout();
                            blankText.setVisibility(View.VISIBLE);
                            changed = false;
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else if (id == R.id.search) {
            startActivity(new Intent(this, SearchActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            archive = 0;
            getSupportActionBar().setTitle(R.string.notes);
            blankText.setText(R.string.blank);
            getLoaderManager().restartLoader(0, null, this);
            //noteObjArrayList = dbHelper.getAllNotes(archive);
            //mAdapter = new NotesAdapter(noteObjArrayList, this, MainActivity.this);
            //recyclerView.setAdapter(mAdapter);

            //if (noteObjArrayList.size() == 0)
            //    blankText.setVisibility(View.VISIBLE);
            //else
            //    blankText.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_archive) {
            archive = 1;
            getSupportActionBar().setTitle(R.string.archive);
            blankText.setText(R.string.blank_archive);
            getLoaderManager().restartLoader(0, null, this);
            //noteObjArrayList = dbHelper.getAllNotes(archive);
            //mAdapter = new NotesAdapter(noteObjArrayList, this, MainActivity.this);
            //recyclerView.setAdapter(mAdapter);

            //if (noteObjArrayList.size() == 0)
            //    blankText.setVisibility(View.VISIBLE);
            //else
            //    blankText.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        } else if (id == R.id.nav_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateWidgets() {
        Intent intent = new Intent();
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        AppWidgetManager man = AppWidgetManager.getInstance(this);
        int[] ids = man.getAppWidgetIds(
                new ComponentName(this, NotesWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.
        Uri.Builder builder = NotesProvider.BASE_URI.buildUpon().appendPath(NotesDb.Note.TABLE_NAME);
        Uri baseUri = builder.build();

        String[] projection = {
                NotesDb.Note._ID,
                NotesDb.Note.COLUMN_NAME_TITLE,
                NotesDb.Note.COLUMN_NAME_SUBTITLE,
                NotesDb.Note.COLUMN_NAME_CONTENT,
                NotesDb.Note.COLUMN_NAME_TIME,
                NotesDb.Note.COLUMN_NAME_ARCHIVED,
                NotesDb.Note.COLUMN_NAME_NOTIFIED
        };

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, baseUri,
                projection, NotesDb.Note.COLUMN_NAME_ARCHIVED + " LIKE " + archive, null,
                NotesDb.Note.COLUMN_NAME_TIME + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) Log.d("onLoadFinished", "Cursor is null!");
        else {
            noteObjArrayList.clear();
            if (cursor.moveToFirst()) {
                do {
                    NoteObj noteObj = new NoteObj(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getInt(5), cursor.getInt(6));
                    if (noteObj.getArchived() == archive) noteObjArrayList.add(noteObj);
                } while (cursor.moveToNext());
            }

            mAdapter = new NotesAdapter(this, this, cursor);
            recyclerView.setAdapter(mAdapter);

        }

        if (noteObjArrayList.size() == 0)
            blankText.setVisibility(View.VISIBLE);
        else
            blankText.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
