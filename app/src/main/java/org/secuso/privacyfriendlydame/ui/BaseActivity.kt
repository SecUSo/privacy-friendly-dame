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

import androidx.core.content.ContextCompat
import org.secuso.pfacore.model.DrawerMenu
import org.secuso.pfacore.ui.activities.DrawerActivity
import org.secuso.privacyfriendlydame.R
/**
 * @author Christopher Beckmann, Karola Marky
 * @version 20171017
 * This class is a parent class of all activities that can be accessed from the
 * Navigation Drawer (example see MainActivity.java)
 */
abstract class BaseActivity : DrawerActivity() {

    override fun drawer() = DrawerMenu.build {
        name = getString(R.string.app_name)
        icon = R.mipmap.ic_drawer
        section {
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_main)
                icon = R.drawable.ic_menu_home
                clazz = MainActivity::class.java
            }
        }
        defaultDrawerSection(this)
    }

    companion object {

        // fade in and fade out durations for the main content when switching between
        // different Activities of the app through the Nav Drawer
        const val MAIN_CONTENT_FADEOUT_DURATION: Int = 150
        const val MAIN_CONTENT_FADEIN_DURATION: Int = 250
    }
}
