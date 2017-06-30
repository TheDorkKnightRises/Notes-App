package thedorkknightrises.notes.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.LibsBuilder;

import thedorkknightrises.notes.BuildConfig;
import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.R;

/**
 * Created by Samriddha Basu on 6/20/2016.
 */
public class AboutActivity extends AppCompatActivity {
    SharedPreferences pref;
    String versionInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        if (pref.getBoolean(Constants.LIGHT_THEME, false))
            setTheme(R.style.AppTheme_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        versionInfo = getString(R.string.package_name) + BuildConfig.APPLICATION_ID + "\n"
                + getString(R.string.version_name) + BuildConfig.VERSION_NAME + "\n"
                + getString(R.string.version_code) + BuildConfig.VERSION_CODE;

        TextView versionButton = ((TextView) findViewById(R.id.versionText));
        versionButton.setText(getString(R.string.version_name) + BuildConfig.VERSION_NAME);
        versionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(AboutActivity.this);
                alert.setTitle(getString(R.string.app_name))
                        .setMessage(versionInfo)
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
            uri = getString(R.string.link_github);
        } else if (text.equals(getString(R.string.website))) {
            uri = getString(R.string.link_site_dev);
        } else if (text.equals(getString(R.string.rate_app))) {
            uri = getString(R.string.link_play_store_app);
        } else if (text.equals(getString(R.string.source))) {
            uri = getString(R.string.link_github_app);
        } else if (text.equals(getString(R.string.play_store))) {
            uri = getString(R.string.link_play_store_dev);
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


    public void shareApp(View view) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.check_out) + " \"" + getString(R.string.app_name) + "\" " + getString(R.string.on_play_store) + ":\n" + getString(R.string.link_play_store_app));
        share.setType("text/plain");
        if (share.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(share, getResources().getString(R.string.share_via)));
        } else {
            Toast.makeText(this, R.string.no_share_app_found, Toast.LENGTH_SHORT).show();
        }
    }

    public void feedback(View view) {
        Intent mail = new Intent(Intent.ACTION_SENDTO);
        mail.setData(Uri.parse("mailto:"));
        mail.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.dev_email)});
        mail.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback) + " : " + getString(R.string.app_name));
        mail.putExtra(Intent.EXTRA_TEXT, versionInfo + "\n"
                + getString(R.string.android_version) + Build.VERSION.SDK_INT + "\n"
                + getString(R.string.device) + Build.BRAND + "\u0020" + Build.DEVICE + "\n\n\n");
        if (mail.resolveActivity(getPackageManager()) != null) {
            startActivity(mail);
        } else {
            Toast.makeText(this, R.string.no_mail_app, Toast.LENGTH_SHORT).show();
        }
    }
}
