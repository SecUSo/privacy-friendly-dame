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
package org.secuso.privacyfriendlydame.game

import android.os.Parcel
import android.os.Parcelable
import org.secuso.privacyfriendlydame.R
import java.io.Serializable
import java.util.LinkedList

/**
 * This enum is used to identify the gametype of a game. Currently, two gametypes are supported;
 * playing against a human and playing against a bot.
 */
enum class GameType(var stringResID: Int, var resIDImage: Int) : Parcelable, Serializable {
    Bot(R.string.game_type_bot, R.drawable.icon_bot),
    Human(R.string.game_type_human, R.drawable.icon_human);

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ordinal)
        dest.writeInt(this.stringResID)
        dest.writeInt(resIDImage)
    }

    companion object {
        val validGameTypes: LinkedList<GameType?>
            get() {
                val result = LinkedList<GameType?>()
                result.add(GameType.Human)
                result.add(GameType.Bot)
                return result
            }

        val CREATOR: Parcelable.Creator<GameType?> = object : Parcelable.Creator<GameType?> {
            override fun createFromParcel(`in`: Parcel): GameType {
                val g = entries[`in`.readInt()]
                g.stringResID = `in`.readInt()
                g.resIDImage = `in`.readInt()
                return g
            }

            override fun newArray(size: Int): Array<GameType?> {
                return arrayOfNulls<GameType>(size)
            }
        }
    }
}
