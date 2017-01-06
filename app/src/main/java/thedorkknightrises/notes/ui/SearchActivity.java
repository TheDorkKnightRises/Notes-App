package thedorkknightrises.notes.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

import java.util.ArrayList;

import thedorkknightrises.notes.NoteObj;
import thedorkknightrises.notes.NotesAdapter;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.data.NotesDbHelper;

/**
 * Created by Samriddha Basu on 1/6/2017.
 */

public class SearchActivity extends AppCompatActivity {
    ArrayList<NoteObj> noteObjArrayList;
    RecyclerView recyclerView;
    NotesAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) this.findViewById(R.id.gridview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (savedInstanceState == null)
            handleIntent(getIntent());
        else {
            noteObjArrayList = (ArrayList<NoteObj>) savedInstanceState.getSerializable("results");
            mAdapter = new NotesAdapter(noteObjArrayList, getApplicationContext(), this);
            recyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            NotesDbHelper mHelper = new NotesDbHelper(this);
            noteObjArrayList = mHelper.searchDB(query, MainActivity.archive);
            mAdapter = new NotesAdapter(noteObjArrayList, this, this);
            recyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("results", noteObjArrayList);
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
        if (MainActivity.archive) searchView.setQueryHint(getText(R.string.search_archive));
        searchView.onActionViewExpanded();
        searchView.setIconified(false);

        return true;
    }
}