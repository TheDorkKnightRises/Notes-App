package thedorkknightrises.notes.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

import thedorkknightrises.notes.NoteObj;
import thedorkknightrises.notes.R;
import thedorkknightrises.notes.db.NotesDbHelper;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class NoteActivity extends AppCompatActivity {
    private Menu menu;
    protected Boolean editMode;
    protected CoordinatorLayout coordinatorLayout;
    protected NotesDbHelper dbHelper;
    protected EditText titleText;
    protected EditText subtitleText;
    protected EditText contentText;
    FloatingActionButton fab;
    String title;
    String subtitle;
    String content;
    String time;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        getSupportActionBar().setHomeActionContentDescription(R.string.discard);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        titleText = (EditText) findViewById(R.id.title);
        subtitleText = (EditText) findViewById(R.id.subtitle);
        contentText = (EditText) findViewById(R.id.content);
        fab = (FloatingActionButton) findViewById(R.id.fab_note);

        dbHelper = new NotesDbHelper(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
        {
            editMode = false;

            titleText.setText(bundle.getString("title"));
            titleText.setEnabled(false);
            titleText.setTextColor(getResources().getColor(R.color.white));

            if (bundle.getString("subtitle").equals(""))
                subtitleText.setVisibility(View.GONE);
            else {
                subtitleText.setText(bundle.getString("subtitle"));
                subtitleText.setEnabled(false);
                subtitleText.setTextColor(getResources().getColor(R.color.white));
            }

            contentText.setText(bundle.getString("content"));
            contentText.setEnabled(false);
            contentText.setTextColor(getResources().getColor(R.color.light_gray));
            time = bundle.getString("time");
        }
        else {
            editMode = true;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setupWindowAnimations();
            }
        }

        if (!editMode) {
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(300);
        getWindow().setEnterTransition(slide);
        getWindow().setReenterTransition(slide);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem deletebtn = menu.findItem(R.id.delete);
        if (!editMode) {
            deletebtn.setVisible(true);
        } else deletebtn.setVisible(false);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.note_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        else if (id == R.id.delete) {
            dbHelper.deleteNote(time);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        if (editMode) {
            title = titleText.getText().toString();
            subtitle = subtitleText.getText().toString();
            content = contentText.getText().toString();
            if (title.equals("") || content.equals(""))
                Snackbar.make(coordinatorLayout, R.string.incomplete, Snackbar.LENGTH_LONG).show();
            else {
                if (time != null)
                    dbHelper.deleteNote(time);
                Calendar c = Calendar.getInstance();
                time = c.getTime().toString();
                dbHelper.addNote(title, subtitle, content, time);
                editMode = false;
                titleText.setEnabled(false);
                titleText.setTextColor(getResources().getColor(R.color.white));
                subtitleText.setEnabled(false);
                subtitleText.setTextColor(getResources().getColor(R.color.white));
                contentText.setEnabled(false);
                contentText.setTextColor(getResources().getColor(R.color.white));
                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));
                onPrepareOptionsMenu(menu);
            }
        } else {
            editMode = true;
            titleText.setEnabled(true);
            subtitleText.setEnabled(true);
            contentText.setEnabled(true);
            contentText.requestFocusFromTouch();
            contentText.setSelection(contentText.getText().length());
            subtitleText.setVisibility(View.VISIBLE);
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_done_white_24dp));
            onPrepareOptionsMenu(menu);
        }

    }
}