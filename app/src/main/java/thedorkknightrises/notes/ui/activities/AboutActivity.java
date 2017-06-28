package thedorkknightrises.notes.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.aboutlibraries.LibsBuilder;

import thedorkknightrises.notes.BuildConfig;
import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.R;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class AboutActivity extends AppCompatActivity {
    SharedPreferences pref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        if (pref.getBoolean(Constants.LIGHT_THEME, false))
            setTheme(R.style.AppTheme_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_new);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView versionButton = ((TextView) findViewById(R.id.versionText));
        versionButton.setText(getString(R.string.version_name) + BuildConfig.VERSION_NAME);
        versionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AboutActivity.this);
                alert.setTitle(getString(R.string.app_name))
                        .setMessage(getString(R.string.package_name) + BuildConfig.APPLICATION_ID + "\n"
                                + getString(R.string.version_name) + BuildConfig.VERSION_NAME + "\n"
                                + getString(R.string.version_code) + BuildConfig.VERSION_CODE)
                        .show();
            }
        });

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
        String text = ((TextView) v).getText().toString();
        String uri = "";
        if (text.equals(getString(R.string.github))) {
            uri = "https://github.com/TheDorkKnightRises";
        } else if (text.equals(getString(R.string.website))) {
            uri = "https://samriddhabasu.github.io";
        } else if (text.equals(getString(R.string.play_store))) {
            uri = "https://play.google.com/store/apps/details?id=thedorkknightrises.notes";
        } else if (text.equals(getString(R.string.source))) {
            uri = "https://github.com/TheDorkKnightRises/Notes-App";
        } else if (text.equals(getString(R.string.dev_play_store))) {
            uri = "https://play.google.com/store/apps/dev?id=7533730446729607250";
        }
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(i);
    }

    public void onNoticeClick(View v) {
        switch (v.getId()) {
            case R.id.legal:
                LibsBuilder libsBuilder =
                        new LibsBuilder()
                                .withAboutIconShown(true)
                                .withAboutDescription(getString(R.string.license_icon));

                if (pref.getBoolean(Constants.LIGHT_THEME, false)) {
                    libsBuilder.withActivityTheme(R.style.AboutLibrariesTheme_Light)
                            .start(this);
                } else {
                    libsBuilder.start(this);
                }
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
