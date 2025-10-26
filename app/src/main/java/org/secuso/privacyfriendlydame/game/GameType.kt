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

package org.secuso.privacyfriendlydame.game;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.LinkedList;

import org.secuso.privacyfriendlydame.R;

/**
 * This enum is used to identify the gametype of a game. Currently, two gametypes are supported;
 * playing against a human and playing against a bot.
 */
public enum GameType implements Parcelable, Serializable{
    Bot(R.string.game_type_bot,R.drawable.icon_bot),
    Human(R.string.game_type_human,R.drawable.icon_human);

    int resIDString;
    int resIDImage;

    GameType(int resIDString, int resIDImage) {
        this.resIDImage = resIDImage;
        this.resIDString = resIDString;
    }

    public static LinkedList<GameType> getValidGameTypes() {
        LinkedList<GameType> result = new LinkedList<>();
        result.add(Human);
        result.add(Bot);
        return result;
    }

    public int getResIDImage(){return resIDImage;   }

    public int getStringResID() {
        return resIDString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
        dest.writeInt(resIDString);
        dest.writeInt(resIDImage);
    }

    public static final Parcelable.Creator<GameType> CREATOR = new Parcelable.Creator<GameType>() {
        public GameType createFromParcel(Parcel in) {
            GameType g = GameType.values()[in.readInt()];
            g.resIDString = in.readInt();
            g.resIDImage = in.readInt();
            return g;
        }

        public GameType[] newArray(int size) {
            return new GameType[size];
        }
    };
}
