package de.tu_darmstadt.informatik.secuso.privacyfriendlydame.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.tu_darmstadt.informatik.secuso.privacyfriendlydame.R;
import de.tu_darmstadt.informatik.secuso.privacyfriendlydame.controller.GameController;
import de.tu_darmstadt.informatik.secuso.privacyfriendlydame.ui.view.DameBoardLayout;

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
