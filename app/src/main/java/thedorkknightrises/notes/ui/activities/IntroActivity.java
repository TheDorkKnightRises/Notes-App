package thedorkknightrises.notes.ui.activities;

import android.os.Bundle;

import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import thedorkknightrises.notes.R;

public class IntroActivity extends com.heinrichreimersoftware.materialintro.app.IntroActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /* Enable/disable skip button */
        setButtonBackVisible(true);
        setButtonBackFunction(BUTTON_BACK_FUNCTION_SKIP);

        /* Enable/disable finish button */
        setFinishEnabled(true);
        setButtonNextVisible(true);
        setButtonNextFunction(BUTTON_NEXT_FUNCTION_NEXT_FINISH);


        addSlide(new SimpleSlide.Builder()
                .title(R.string.app_name)
                .description(R.string.welcome)
                .image(R.drawable.ic_launcher)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .description(R.string.intro1)
                .image(R.drawable.ic_add_white_24dp)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .description(R.string.intro2)
                .image(R.drawable.ic_archive_white_24dp)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .description(R.string.intro3)
                .image(R.drawable.ic_share_white_24dp)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .description(R.string.intro4)
                .image(R.drawable.ic_notifications_active_white_24dp)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .description(R.string.intro5)
                .image(R.drawable.ic_search_white_24dp)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

        addSlide(new SimpleSlide.Builder()
                .description(R.string.intro6)
                .image(R.drawable.ic_launcher)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .build());

    }

}