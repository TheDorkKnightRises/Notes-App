package thedorkknightrises.notes.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;

import thedorkknightrises.notes.R;

/**
 * Created by Samriddha Basu on 6/22/2016.
 */
public class SettingsActivity extends AppCompatActivity {
    private SharedPreferences pref;
    private SwitchCompat theme_switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences("Prefs", MODE_PRIVATE);
        if (pref.getBoolean("lightTheme", false))
            setTheme(R.style.AppTheme_Light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        theme_switch = (SwitchCompat) findViewById(R.id.theme_switch);

        pref= getSharedPreferences("Prefs", MODE_PRIVATE);

        theme_switch.setChecked(pref.getBoolean("lightTheme", false));
    }

    public void onCheckedChange(View v)
    {
        if (v.equals(findViewById(R.id.theme_switch)) || v.equals(findViewById(R.id.theme_switch_row))) {
            Boolean b = pref.getBoolean("lightTheme", false);
            SharedPreferences.Editor e = pref.edit();
            e.putBoolean("lightTheme", !b);
            e.commit();
            if (!v.equals(theme_switch))
                theme_switch.toggle();
            MainActivity.themeChanged = true;
            recreate();
        }
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

}
