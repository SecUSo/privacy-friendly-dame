package org.secuso.privacyfriendlydame.game

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

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

/**
 * This class contains and bundles all implemented rules for checkers.
 *
 * @author Patrick Schneider
 * @version 20230115
 */
class GameRules : Parcelable, Serializable {
    val flyingDame: Boolean
    val whiteBegins: Boolean

    constructor(flying_dame: Boolean, white_begins: Boolean) {
        this.flyingDame = flying_dame
        this.whiteBegins = white_begins
    }

    private constructor(parcel: Parcel) {
        this.flyingDame = parcel.readInt() != 0
        this.whiteBegins = parcel.readInt() != 0
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeInt(if (this.flyingDame) 1 else 0)
        parcel.writeInt(if (this.whiteBegins) 1 else 0)
    }

    companion object {
        val CREATOR
                : Parcelable.Creator<GameRules?> = object : Parcelable.Creator<GameRules?> {
            override fun createFromParcel(parcel: Parcel): GameRules {
                return GameRules(parcel)
            }

            override fun newArray(i: Int): Array<GameRules?> {
                return arrayOfNulls<GameRules>(i)
            }
        }
    }
}
