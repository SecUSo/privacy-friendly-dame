package org.secuso.privacyfriendlydame

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.map
import org.secuso.pfacore.application.PFData
import org.secuso.pfacore.model.Theme
import org.secuso.pfacore.model.about.About
import org.secuso.pfacore.model.preferences.Preferable
import org.secuso.pfacore.ui.help.Help
import org.secuso.pfacore.ui.preferences.appPreferences
import org.secuso.pfacore.ui.preferences.settings.appearance
import org.secuso.pfacore.ui.preferences.settings.general
import org.secuso.pfacore.ui.preferences.settings.preferenceFirstTimeLaunch
import org.secuso.pfacore.ui.preferences.settings.preferenceFirstTimePFACoreLaunch
import org.secuso.pfacore.ui.preferences.settings.settingDeviceInformationOnErrorReport
import org.secuso.pfacore.ui.preferences.settings.settingThemeSelector
import org.secuso.pfacore.ui.preferences.settings.switch
import org.secuso.pfacore.ui.tutorial.buildTutorial

class PFApplicationData private constructor(context: Context) {

    // Preferences
    lateinit var theme: Preferable<String>
        private set
    lateinit var firstTimeLaunch: Preferable<Boolean>
        private set
    lateinit var includeDeviceDataInReport: Preferable<Boolean>
        private set
    lateinit var firstTimePFACore: Preferable<Boolean>
        private set
    lateinit var ruleFlyingKing: Preferable<Boolean>
        private set
    lateinit var ruleWhiteBegins: Preferable<Boolean>
        private set
    lateinit var lastChosenPage: Preferable<Int>
        private set
    lateinit var lastChosenDifficulty: Preferable<Int>
        private set

    private val preferences = appPreferences(context) {
        preferences {
            firstTimeLaunch = preferenceFirstTimeLaunch
            firstTimePFACore = preferenceFirstTimePFACoreLaunch
            lastChosenPage = preference {
                key = "lastChosenPage"
                default = 0
                backup = true
            }
            lastChosenDifficulty = preference {
                key = "lastChosenDifficulty"
                default = 0
                backup = true
            }
        }
        settings {
            appearance {
                theme = settingThemeSelector
            }
            category(R.string.title_game_rules) {
                ruleFlyingKing = switch {
                    key = "pref_rule_flying_king"
                    default = false
                    backup = true
                    title { resource( R.string.pref_title_rule_flying_king) }
                    summary { resource( R.string.pref_desc_rule_flying_king) }
                }
                ruleWhiteBegins = switch {
                    key = "pref_rule_white_begins"
                    default = false
                    backup = true
                    title { resource(R.string.pref_title_rule_white_begins) }
                    summary { resource(R.string.pref_desc_rule_white_begins) }
                }
            }
            general {
                includeDeviceDataInReport = settingDeviceInformationOnErrorReport
            }
        }
        onFinish {
            if (firstTimePFACore.value) {
                // We migrate the old preferences to the new ones
                val preferenceManager = context.getSharedPreferences("privacy_friendly_apps", 0)
                firstTimeLaunch.value = preferenceManager.getBoolean("IsFirstTimeLaunch", true)

                firstTimePFACore.value = false
            }
        }
    }

    private val help = Help.build(context) {
        listOf(
            R.string.help_whatis to listOf(R.string.help_whatis_answer),
            R.string.help_what_modes_are_there to listOf(R.string.sMode1, R.string.sMode1Summary, R.string.sMode2, R.string.sMode2Summary),
            R.string.help_how_can_you_move to listOf(R.string.sHelpMoveTap),
            R.string.help_game_rules_title to listOf(R.string.help_game_setup_description, R.string.help_captures_descripting, R.string.help_dame_description, R.string.sHelpWinSummary),
            R.string.help_privacy to listOf(R.string.help_privacy_answer),
            R.string.help_permission to listOf(R.string.sHelpPermissionsDescription)
        ).map {
            item {
                title { resource(it.first) }
                descriptions(context, it.second)
            }
        }
    }

    private val about = About(
        name = context.resources.getString(R.string.app_name),
        version = BuildConfig.VERSION_NAME,
        authors = context.resources.getString(R.string.about_author_names),
        repo = context.resources.getString(org.secuso.pfacore.R.string.about_github)
    )

    private val tutorial = buildTutorial {
        stage {
            title = ContextCompat.getString(context, R.string.slide1_heading)
            description = ContextCompat.getString(context, R.string.slide1_text)
            images = single(R.mipmap.splash)
        }
        stage {
            title = ContextCompat.getString(context, R.string.slide2_heading)
            description = ContextCompat.getString(context, R.string.slide2_text)
            images = multiple(R.drawable.ic_keyboard_arrow_left_black_24dp, R.drawable.icon_bot, R.drawable.ic_keyboard_arrow_right_black_24dp) {
                columns = 3
            }
        }
    }

    val data = PFData(
        about = about,
        help = help,
        preferences = preferences,
        tutorial = tutorial,
        theme = theme.state.map { Theme.valueOf(it) },
        firstLaunch = firstTimeLaunch,
        includeDeviceDataInReport = includeDeviceDataInReport,
    )

    companion object {
        private var _instance: PFApplicationData? = null
        fun instance(context: Context): PFApplicationData {
            if (_instance == null) {
                _instance = PFApplicationData(context)
            }
            return _instance!!
        }
    }
}