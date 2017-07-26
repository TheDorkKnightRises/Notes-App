package thedorkknightrises.notes.ui.activities;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.NoteObj;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.NotesDb;
import thedorkknightrises.notes.data.NotesProvider;
import thedorkknightrises.notes.ui.adapters.NotesAdapter;

/**
 * Created by Samriddha Basu on 1/6/2017.
 */

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, SearchView.OnQueryTextListener {
    boolean lightTheme;
    ArrayList<NoteObj> noteObjArrayList;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    TextView blankText;
    NotesAdapter mAdapter;
    String query = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        lightTheme = getSharedPreferences(Constants.PREFS, MODE_PRIVATE).getBoolean(Constants.LIGHT_THEME, false);
        if (lightTheme)
            setTheme(R.style.AppTheme_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getLoaderManager().initLoader(0, null, this);

        recyclerView = (RecyclerView) this.findViewById(R.id.gridview);
        layoutManager = new LinearLayoutManager(this);
        noteObjArrayList = new ArrayList<>();

        recyclerView.setLayoutManager(layoutManager);
        blankText = (TextView) findViewById(R.id.blankTextView);


        if (savedInstanceState == null)
            handleIntent(getIntent());
        else {
            noteObjArrayList = (ArrayList<NoteObj>) savedInstanceState.getSerializable("results");
            //mAdapter = new NotesAdapter(noteObjArrayList, getApplicationContext(), this);
            //recyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            //NotesDbHelper mHelper = new NotesDbHelper(this);
            //noteObjArrayList = mHelper.searchDB(query, MainActivity.archive);
            //mAdapter = new NotesAdapter(noteObjArrayList, this, this);
            //recyclerView.setAdapter(mAdapter);
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("results", noteObjArrayList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        android.support.v7.widget.SearchView searchView =
                (android.support.v7.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        if (MainActivity.archive == 1) {
            searchView.setQueryHint(getText(R.string.search_archive));
            blankText.setText(getText(R.string.search_no_results_archive));
        }
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (query.trim().equals("")) return new Loader<>(this);
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
                NotesDb.Note.COLUMN_NAME_REMINDER,
                NotesDb.Note.COLUMN_NAME_CHECKLIST
        };

        String selection = NotesDb.Note.COLUMN_NAME_ARCHIVED + " LIKE " + MainActivity.archive
                + " AND ( " + NotesDb.Note.COLUMN_NAME_TITLE + " LIKE " + "'%" + query
                + "%' OR " + NotesDb.Note.COLUMN_NAME_SUBTITLE + " LIKE " + "'%" + query
                + "%' OR " + NotesDb.Note.COLUMN_NAME_CONTENT + " LIKE " + "'%" + query + "%')";

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(this, baseUri,
                projection, selection, null,
                NotesDb.Note.COLUMN_NAME_TIME + " DESC");
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
                            cursor.getString(12),
                            cursor.getInt(13));
                    if (noteObj.getArchived() == MainActivity.archive)
                        noteObjArrayList.add(noteObj);
                } while (cursor.moveToNext());
            }

            Parcelable recyclerViewState = null;
            if (layoutManager != null && mAdapter != null) {
                // Save state
                recyclerViewState = layoutManager.onSaveInstanceState();
            }
            mAdapter = new NotesAdapter(this, this, cursor);
            recyclerView.setAdapter(mAdapter);
            if (recyclerViewState != null) {
                layoutManager.onRestoreInstanceState(recyclerViewState);
                recyclerView.smoothScrollToPosition(0);
            }

        }

        if (!query.trim().equals("")) {
            if (noteObjArrayList.size() == 0)
                blankText.setVisibility(View.VISIBLE);
            else
                blankText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!newText.equals("")) {
            query = newText;
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }
        return false;
    }
}