package thedorkknightrises.notes.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.LibsBuilder;

import thedorkknightrises.notes.R;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class AboutActivity extends AppCompatActivity {
    SharedPreferences pref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences("Prefs", MODE_PRIVATE);
        if (pref.getBoolean("lightTheme", false))
            setTheme(R.style.AppTheme_Light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home)
            onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public void onLinkClick(View v) {
        String text = (String) ((Button) v).getText();
        String uri;
        if (text.equals(getString(R.string.github))) {
            uri = "https://github.com/TheDorkKnightRises";
        } else if (text.equals(getString(R.string.website))) {
            uri = "https://samriddhabasu.github.io";
        } else if (text.equals(getString(R.string.source))) {
            uri = "https://github.com/TheDorkKnightRises/Notes-App";
        } else {
            Toast.makeText(getApplicationContext(), getText(R.string.play_store_prompt), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(i);
    }

    public void onNoticeClick(View v) {
        switch (v.getId()) {
            case R.id.legal:
                new LibsBuilder()
                        //start the activity
                        .start(this);
                break;
            case R.id.copyright:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setMessage(getString(R.string.copyright))
                        .show();
                break;
        }
    }

    public void showIntro(View v) {
        Intent i = new Intent(this, IntroActivity.class);
        startActivity(i);
    }


}
