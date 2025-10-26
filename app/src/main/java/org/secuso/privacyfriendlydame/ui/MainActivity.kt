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
package org.secuso.privacyfriendlydame.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import org.secuso.pfacore.model.DrawerElement
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlydame.PFApplicationData
import org.secuso.privacyfriendlydame.R
import org.secuso.privacyfriendlydame.game.CheckersGame
import org.secuso.privacyfriendlydame.game.GameType
import java.io.FileInputStream
import java.io.IOException
import java.io.ObjectInputStream

/**
 * @author Christopher Beckmann, Karola Marky
 * @version 20251024
 */
class MainActivity : BaseActivity() {
    private val mViewPager: ViewPager by lazy { findViewById(R.id.scroller) }
    private var gameContinuable: Boolean = false
    private var currentGame: CheckersGame? = null

    private val lastChosenPage by lazy { PFApplicationData.instance(this).lastChosenPage }
    private val lastChosenDifficulty by lazy { PFApplicationData.instance(this).lastChosenDifficulty }

    private val overwriteGameDialog by lazy {
        AbortElseDialog.build(this) {
            title = { ContextCompat.getString(context, R.string.OverwriteResumableGameTitle) }
            content = { ContextCompat.getString(context, R.string.OverwriteResumableGame) }
            acceptLabel = ContextCompat.getString(context, R.string.yes)
            onElse = {
                deleteFile(SAVE_FILE)
                startGame()
            }
        }
    }

    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.ic_menu_home

    override fun onStart() {
        super.onStart()
        currentGame = loadFile()
        findViewById<Button>(R.id.continueButton).apply {
            if (currentGame?.isGameFinished != false) {
                // no saved game available
                gameContinuable = false
                isEnabled = false
                setBackgroundResource(R.drawable.button_disabled)
            } else {
                gameContinuable = true
                isEnabled = true
                setBackgroundResource(R.drawable.standalone_button)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mSectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)
        mViewPager.setAdapter(mSectionsPagerAdapter)
        mViewPager.setCurrentItem(lastChosenPage.value)

        val mArrowLeft: ImageView = findViewById(R.id.arrow_left)
        val mArrowRight: ImageView = findViewById(R.id.arrow_right)

        //care for initial postiton of the ViewPager
        mArrowLeft.visibility = if (lastChosenPage.value == 0) View.INVISIBLE else View.VISIBLE
        mArrowRight.visibility = if (lastChosenPage.value == mSectionsPagerAdapter.count - 1) View.INVISIBLE else View.VISIBLE

        mArrowLeft.setOnClickListener { mViewPager.arrowScroll(View.FOCUS_LEFT) }
        mArrowRight.setOnClickListener { mViewPager.arrowScroll(View.FOCUS_RIGHT) }

        //Update ViewPager on change
        mViewPager.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                mArrowLeft.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                mArrowRight.visibility = if (position == mSectionsPagerAdapter.count - 1) View.INVISIBLE else View.VISIBLE

                lastChosenPage.value = position
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        currentGame = loadFile()

        findViewById<Button>(R.id.continueButton).apply {
            if (currentGame?.isGameFinished != false) {
                // no saved game available
                gameContinuable = false
                isEnabled = false
                setBackgroundResource(R.drawable.button_disabled)
            } else {
                gameContinuable = true
                isEnabled = true
                setBackgroundResource(R.drawable.button_normal)
            }
            setOnClickListener {
                if (gameContinuable) {
                    Intent(this@MainActivity, GameActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(this)
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.no_resumable_game),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        findViewById<Button>(R.id.play_button).setOnClickListener {
            if (gameContinuable) {
                overwriteGameDialog.show()
            } else {
                startGame()
            }
        }
    }

    private fun loadFile(): CheckersGame? {
        var ois: ObjectInputStream? = null
        var fis: FileInputStream? = null
        try {
            fis = this.openFileInput(SAVE_FILE)
            ois = ObjectInputStream(fis)
            currentGame = ois.readObject() as CheckersGame?
            return currentGame
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } finally {
            if (ois != null) try {
                ois.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (fis != null) try {
                fis.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun startGame() {
        val gameType = GameType.getValidGameTypes()[mViewPager.currentItem]

        val intent = Intent(this@MainActivity, GameActivity::class.java).apply {
            putExtra("gameType", gameType.name)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            if (gameType == GameType.Bot) {
                lastChosenDifficulty.value = findViewById<SeekBar>(R.id.difficultyBar).progress
                putExtra("level", lastChosenDifficulty.value)
            }
        }

        startActivity(intent)
    }

    class SectionsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PageFragment (defined as a static inner class below).
            return GameTypeFragment.newInstance(position)
        }

        override fun getCount(): Int {
            return GameType.getValidGameTypes().size
        }
    }

    class GameTypeFragment : Fragment() {
        lateinit var levelText: TextView
        lateinit var diffBar: SeekBar

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val rootView = inflater.inflate(R.layout.fragment_main_menu, container, false)

            val gameType = GameType.getValidGameTypes()[requireArguments().getInt(ARG_SECTION_NUMBER)]

            val textView = rootView.findViewById<TextView>(R.id.section_label)
            val imageView = rootView.findViewById<ImageView>(R.id.gameTypeImage)
            diffBar = rootView.findViewById(R.id.difficultyBar)
            levelText = rootView.findViewById(R.id.levelText)

            imageView.setImageResource(gameType.resIDImage)

            val difficulty = PFApplicationData.instance(requireContext()).lastChosenDifficulty.value

            diffBar.setOnSeekBarChangeListener(seekBarChangeListener)
            diffBar.progress = difficulty

            if (gameType == GameType.Human) {
                diffBar.visibility = View.INVISIBLE
                levelText.visibility = View.INVISIBLE
            } else {
                diffBar.visibility = View.VISIBLE
                levelText.visibility = View.VISIBLE
            }

            textView.setText(gameType.stringResID)
            return rootView
        }

        var seekBarChangeListener: SeekBar.OnSeekBarChangeListener =
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    levelText.text = resources.getStringArray(R.array.levels)[progress]
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            }

        companion object {
            /**
             * The fragment argument representing the section number for this
             * fragment.
             */
            private const val ARG_SECTION_NUMBER = "section_number"

            /**
             * Returns a new instance of this fragment for the given section
             * number.
             */
            fun newInstance(sectionNumber: Int): GameTypeFragment {
                val fragment = GameTypeFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.setArguments(args)
                return fragment
            }
        }
    }

    companion object {
        const val SAVE_FILE: String = "savedata"
    }
}
