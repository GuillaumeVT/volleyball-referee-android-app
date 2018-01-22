package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.GameService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

class SerializedGameReader {

    static GameService readGame(Context context, String fileName) {
        Log.i("VBR-Data", String.format("Read serialized game from %s", fileName));
        GameService game = null;

        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            game = (GameService)objInputStream.readObject();
            objInputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("VBR-Data", String.format("%s serialized game file does not exist", fileName));
        } catch (IOException | ClassNotFoundException e) {
            Log.e("VBR-Data", "Exception while reading serialized game", e);
        }

        return game;
    }
}
