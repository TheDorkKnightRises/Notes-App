package thedorkknightrises.notes.ui.activities;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;

import thedorkknightrises.checklistview.views.ChecklistView;
import thedorkknightrises.notes.Constants;
import thedorkknightrises.notes.R;

/**
 * Created by Samriddha on 23-07-2017.
 */

public class ChecklistActivity extends AppCompatActivity {
    protected CoordinatorLayout coordinatorLayout;
    SharedPreferences pref;
    boolean lightTheme;
    private boolean backPressFlag = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        lightTheme = pref.getBoolean(Constants.LIGHT_THEME, false);
        if (lightTheme)
            setTheme(R.style.NoteLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        ChecklistView checklistView = (ChecklistView) findViewById(R.id.checklist_view);
        checklistView.getDragLinearLayout().setContainerScrollView((ScrollView) findViewById(R.id.scrollView));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Slide slide = new Slide(Gravity.TOP);
        slide.addTarget(R.id.note_card);
        slide.addTarget(R.id.note_title);
        slide.addTarget(R.id.note_subtitle);
        slide.addTarget(R.id.note_content);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
        getWindow().setReenterTransition(slide);
    }

    public void close(View v) {
        backPressFlag = true;
        onBackPressed();
    }

    public void delete(View v) {
        finish();
    }

    public void onClick(View v) {


    }

    @Override
    public void onBackPressed() {
        if (!backPressFlag) {
            Toast.makeText(getApplicationContext(), getText(R.string.back_press), Toast.LENGTH_SHORT).show();
            backPressFlag = true;

            // Thread to change backPressedFlag to false after 3000ms
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        backPressFlag = false;
                    }
                }
            }).start();
            return;
        }
        super.onBackPressed();
    }
}