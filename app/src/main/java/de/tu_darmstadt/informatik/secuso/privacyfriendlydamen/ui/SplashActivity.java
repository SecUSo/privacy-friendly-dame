package de.tu_darmstadt.informatik.secuso.privacyfriendlydamen.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.tu_darmstadt.informatik.secuso.privacyfriendlydamen.ui.TutorialActivity;

/**
 * Created by yonjuni on 22.10.16.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent mainIntent = new Intent(SplashActivity.this, TutorialActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        SplashActivity.this.finish();

    }

}