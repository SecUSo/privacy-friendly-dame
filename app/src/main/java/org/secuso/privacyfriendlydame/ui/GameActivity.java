package org.secuso.privacyfriendlydame.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.secuso.privacyfriendlydame.R;
import org.secuso.privacyfriendlydame.controller.GameController;
import org.secuso.privacyfriendlydame.ui.view.DameBoardLayout;

public class GameActivity extends AppCompatActivity {

    private DameBoardLayout boardLayout;
    private GameController gameController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        boardLayout = (DameBoardLayout) findViewById(R.id.board);
        gameController = new GameController();
        boardLayout.setGameController(gameController);
    }
}

/*
public class GameActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {

    }
}
*/
