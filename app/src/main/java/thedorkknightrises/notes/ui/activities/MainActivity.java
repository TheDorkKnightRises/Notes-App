package thedorkknightrises.notes.ui.activities;

import android.app.LoaderManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.NativeExpressAdView;

import java.util.ArrayList;

import thedorkknightrises.notes.BootReceiver;
import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.NoteObj;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.data.NotesDbHelper;
import thedorkknightrises.notes.data.NotesProvider;
import thedorkknightrises.notes.ui.adapters.NotesAdapter;
import thedorkknightrises.notes.widget.NotesWidget;

import static thedorkknightrises.notes.Constants.NUM_COLUMNS;
import static thedorkknightrises.notes.Constants.OLDEST_FIRST;

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
    StaggeredGridLayoutManager layoutManager;
    TextView blankText;
    FloatingActionButton fab;
    SharedPreferences pref;
    NativeExpressAdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = pref.getBoolean(Constants.FIRST_START, true);
                //  If the activity has never started before...
                if (isFirstStart) {
                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);
                    //  Make a new preferences editor
                    SharedPreferences.Editor e = pref.edit();
                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean(Constants.FIRST_START, false);
                    //  Apply changes
                    e.apply();
                }
                new BootReceiver().onReceive(MainActivity.this, null);
            }
        });
        // Start the thread
        t.start();

        lightTheme = pref.getBoolean(Constants.LIGHT_THEME, false);
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

        noteObjArrayList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        MobileAds.initialize(this, getString(R.string.admob_app_id));
        adView = new NativeExpressAdView(this);
        if (lightTheme)
            adView.setAdUnitId(getString(R.string.small_banner_ad_unit_id_light));
        else
            adView.setAdUnitId(getString(R.string.small_banner_ad_unit_id));
        adView.setAdSize(new AdSize(AdSize.FULL_WIDTH, 100));
        ((LinearLayout) findViewById(R.id.linearLayout)).addView(adView, 0);

        AdRequest request = new AdRequest.Builder().build();
        adView.loadAd(request);
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

        if (lightTheme != pref.getBoolean(Constants.LIGHT_THEME, false)) {
            if (lightTheme)
                setTheme(R.style.AppTheme_NoActionBar);
            else setTheme(R.style.AppTheme_Light_NoActionBar);
            lightTheme = !lightTheme;
            recreate();
        }

        if (!pref.getBoolean(Constants.ADS_ENABLED, false)) {
            adView.setVisibility(View.GONE);
        } else {
            adView.setVisibility(View.VISIBLE);
        }

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

        if (lightTheme) {
            Drawable drawable = menu.getItem(0).getIcon();
            drawable.mutate();
            drawable.setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_IN);
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.one_column) {
            pref.edit().putInt(Constants.NUM_COLUMNS, 1).apply();
            getLoaderManager().restartLoader(0, null, this);
        } else if (id == R.id.two_column) {
            pref.edit().putInt(Constants.NUM_COLUMNS, 2).apply();
            getLoaderManager().restartLoader(0, null, this);
        } else if (id == R.id.newer) {
            pref.edit().putBoolean(Constants.OLDEST_FIRST, false).apply();
            getLoaderManager().restartLoader(0, null, this);
        } else if (id == R.id.older) {
            pref.edit().putBoolean(Constants.OLDEST_FIRST, true).apply();
            getLoaderManager().restartLoader(0, null, this);
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
            fab.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_archive) {
            archive = 1;
            getSupportActionBar().setTitle(R.string.archive);
            blankText.setText(R.string.blank_archive);
            getLoaderManager().restartLoader(0, null, this);
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
                NotesDb.Note.COLUMN_NAME_CREATED_AT,
                NotesDb.Note.COLUMN_NAME_ARCHIVED,
                NotesDb.Note.COLUMN_NAME_NOTIFIED,
                NotesDb.Note.COLUMN_NAME_COLOR,
                NotesDb.Note.COLUMN_NAME_ENCRYPTED,
                NotesDb.Note.COLUMN_NAME_PINNED,
                NotesDb.Note.COLUMN_NAME_TAG,
                NotesDb.Note.COLUMN_NAME_REMINDER
        };

        String sort;
        if (pref.getBoolean(OLDEST_FIRST, false))
            sort = " ASC";
        else
            sort = " DESC";

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, baseUri,
                projection, NotesDb.Note.COLUMN_NAME_ARCHIVED + " LIKE " + archive, null,
                NotesDb.Note.COLUMN_NAME_TIME + sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) Log.d("onLoadFinished", "Cursor is null!");
        else {
            noteObjArrayList.clear();
            if (cursor.moveToFirst()) {
                do {
                    NoteObj noteObj = new NoteObj(cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getInt(6),
                            cursor.getInt(7),
                            cursor.getString(8),
                            cursor.getInt(9),
                            cursor.getInt(10),
                            cursor.getInt(11),
                            cursor.getString(12));
                    if (noteObj.getArchived() == archive) noteObjArrayList.add(noteObj);
                } while (cursor.moveToNext());
            }

            mAdapter = new NotesAdapter(this, this, cursor);
            layoutManager = new StaggeredGridLayoutManager(pref.getInt(NUM_COLUMNS, 1), StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
            recyclerView.setLayoutManager(layoutManager);
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
