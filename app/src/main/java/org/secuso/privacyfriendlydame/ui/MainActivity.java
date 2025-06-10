/*
 This file is part of Privacy Friendly Dame.

 Privacy Friendly Dame is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Dame is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlydame.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlydame.GoodbyeGoogleHelperKt;
import org.secuso.privacyfriendlydame.R;
import org.secuso.privacyfriendlydame.game.CheckersGame;
import org.secuso.privacyfriendlydame.game.GameType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author Christopher Beckmann, Karola Marky
 * @version 20171016
 */

public class MainActivity extends BaseActivity {

    public final static String SAVE_FILE = "savedata";

    private ViewPager mViewPager;
    private ImageView mArrowLeft;
    private ImageView mArrowRight;
    private Boolean game_continuable;
    private CheckersGame currentGame;

    @Override
    protected void onStart() {
        super.onStart();
        currentGame = loadFile();
        Button game_continue = findViewById(R.id.continueButton);
        if (currentGame == null || currentGame.isGameFinished())
        {
            // no saved game available
            game_continuable = false;
            game_continue.setClickable(true);
            game_continue.setBackgroundResource(R.drawable.button_disabled);
        }
        else
        {
            game_continuable = true;
            game_continue.setClickable(true);
            game_continue.setBackgroundResource(R.drawable.standalone_button);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overridePendingTransition(0, 0);
        final SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.scroller);
        if(mViewPager != null) {
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        int index = mSharedPreferences.getInt("lastChosenPage", 0);

        mViewPager.setCurrentItem(index);
        mArrowLeft = findViewById(R.id.arrow_left);
        mArrowRight = findViewById(R.id.arrow_right);
        Button newGameBtn = findViewById(R.id.play_button);

        //care for initial postiton of the ViewPager
        mArrowLeft.setVisibility((index==0)?View.INVISIBLE:View.VISIBLE);
        mArrowRight.setVisibility((index==mSectionsPagerAdapter.getCount()-1)?View.INVISIBLE:View.VISIBLE);

        //Update ViewPager on change
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mArrowLeft.setVisibility((position==0)?View.INVISIBLE:View.VISIBLE);
                mArrowRight.setVisibility((position==mSectionsPagerAdapter.getCount()-1)?View.INVISIBLE:View.VISIBLE);

                //save position in settings
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("lastChosenPage", position);
                editor.apply();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        currentGame = loadFile();
        Button game_continue = findViewById(R.id.continueButton);
        if (currentGame == null || currentGame.isGameFinished())
        {
            // no saved game available
            game_continuable = false;
            game_continue.setClickable(true);
            game_continue.setBackgroundResource(R.drawable.button_disabled);
        }
        else
        {
            game_continuable = true;
            game_continue.setClickable(true);
            game_continue.setBackgroundResource(R.drawable.button_normal);
        }
        GoodbyeGoogleHelperKt.checkGoodbyeGoogle(this, getLayoutInflater());
    }

    private CheckersGame loadFile() {
        ObjectInputStream ois = null;
        FileInputStream fis = null;
        try {
            fis = this.openFileInput(SAVE_FILE);
            ois = new ObjectInputStream(fis);
            currentGame = (CheckersGame) ois.readObject();
            return currentGame;
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) try { ois.close(); } catch (IOException e) { e.printStackTrace();}
            if (fis != null) try { fis.close(); } catch (IOException e) { e.printStackTrace();}
        }
        return null;
    }

    /**
     * This method connects the Activity to the menu item
     * @return ID of the menu item it belongs to
     */
    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_example;
    }

    @Override
    protected void onResume() {
        super.onResume();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        }
    }

    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.arrow_left:
                mViewPager.arrowScroll(View.FOCUS_LEFT);
                break;
            case R.id.arrow_right:
                mViewPager.arrowScroll(View.FOCUS_RIGHT);
                break;
            case R.id.play_button:

                if (game_continuable) {
                    // show alertDialog
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    // Setting Dialog Title
                    builder.setTitle(R.string.OverwriteResumableGameTitle);
                    // Setting Dialog Message
                    builder.setMessage(R.string.OverwriteResumableGame);

                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // delete file
                            deleteFile(SAVE_FILE);
                            startGame();
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton(R.string.cancel, null);
                    if (!this.isFinishing()) {
                        builder.show();
                    }
                } else {
                    startGame();
                }

                break;
            case R.id.continueButton:
                //i = new Intent(this, GameActivity.class);
                if (game_continuable)
                {
                    Intent myintent = new Intent(MainActivity.this, GameActivity.class);
                    myintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(myintent);
                }
                else
                {
                    Toast.makeText(MainActivity.this, getString(R.string.no_resumable_game), Toast.LENGTH_LONG).show();
                }

                break;
            default:
                break;
        }
    }

    private void startGame() {
        GameType gameType = GameType.getValidGameTypes().get(mViewPager.getCurrentItem());

        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        intent.putExtra("gameType", gameType.name());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if(gameType == GameType.Bot) {
                SeekBar diffBar = findViewById(R.id.difficultyBar);
                mSharedPreferences.edit().putInt("lastChosenDifficulty", diffBar.getProgress()).apply();
                intent.putExtra("level",diffBar.getProgress());
        }

        startActivity(intent);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PageFragment (defined as a static inner class below).
            return GameTypeFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return GameType.getValidGameTypes().size();
        }
    }

    public static class GameTypeFragment extends Fragment {

        TextView levelText;
        SeekBar diffBar;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static GameTypeFragment newInstance(int sectionNumber) {
            GameTypeFragment fragment = new GameTypeFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public GameTypeFragment() {

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);

            GameType gameType = GameType.getValidGameTypes().get(getArguments().getInt(ARG_SECTION_NUMBER));

            TextView textView = rootView.findViewById(R.id.section_label);
            ImageView imageView = rootView.findViewById(R.id.gameTypeImage);
            diffBar= rootView.findViewById(R.id.difficultyBar);
            levelText=rootView.findViewById(R.id.levelText);

            imageView.setImageResource(gameType.getResIDImage());

            int difficulty = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("lastChosenDifficulty", 1);

            if(diffBar != null) {
                diffBar.setOnSeekBarChangeListener(seekBarChangeListener);
                diffBar.setProgress(difficulty);
            }

            if(gameType==GameType.Human){
                diffBar.setVisibility(View.INVISIBLE);
                levelText.setVisibility(View.INVISIBLE);
            } else {
                diffBar.setVisibility(View.VISIBLE);
                levelText.setVisibility(View.VISIBLE);
            }

            textView.setText(gameType.getStringResID());
            return rootView;
        }

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                levelText.setText(getResources().getStringArray(R.array.levels)[progress]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
    }
}
