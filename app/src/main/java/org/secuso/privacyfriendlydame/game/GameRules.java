package org.secuso.privacyfriendlydame.game;

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

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * This class contains and bundles all implemented rules for checkers.
 *
 * @author Patrick Schneider
 * @version 20230115
 */
public class GameRules implements Parcelable, Serializable {

    private final boolean flying_dame;

    public GameRules(boolean flying_dame) {
        this.flying_dame = flying_dame;
    }

    private GameRules(Parcel parcel) {
        flying_dame = parcel.readInt() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(flying_dame ? 1 : 0);
    }

    public static final Parcelable.Creator<GameRules> CREATOR
        = new Parcelable.Creator<GameRules>() {

        @Override
        public GameRules createFromParcel(Parcel parcel) {
            return new GameRules(parcel);
        }

        @Override
        public GameRules[] newArray(int i) {
            return new GameRules[i];
        }
    };

    public boolean getFlyingDame() {
        return this.flying_dame;
    }
}
