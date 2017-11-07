package com.tonkar.volleyballreferee.business.history;

import android.content.Context;
import android.util.Log;

import com.tonkar.volleyballreferee.business.game.Game;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

class SerializedGameWriter {

    static void writeGame(Context context, String fileName, Game game) {
        Log.d("VBR-History", String.format("Write serialized game to %s", fileName));
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream objOutputStream = new ObjectOutputStream(outputStream);
            objOutputStream.writeObject(game);
            objOutputStream.close();
            outputStream.close();
        } catch (IOException e) {
            Log.e("VBR-History", "Exception while writing serialized game", e);
        }
    }
}
