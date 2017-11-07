package com.tonkar.volleyballreferee.business.history;

import android.content.Context;
import android.util.Log;

import com.tonkar.volleyballreferee.business.game.Game;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

class SerializedGameReader {

    static Game readGame(Context context, String fileName) {
        Log.i("VBR-History", String.format("Read serialized game from %s", fileName));
        Game game = null;

        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            game = (Game)objInputStream.readObject();
            objInputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("VBR-History", String.format("%s serialized game file does not exist", fileName));
        } catch (IOException | ClassNotFoundException e) {
            Log.e("VBR-History", "Exception while reading serialized game", e);
        }

        return game;
    }
}
