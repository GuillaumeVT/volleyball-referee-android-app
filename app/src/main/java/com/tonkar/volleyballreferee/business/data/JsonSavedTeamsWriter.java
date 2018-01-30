package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;

public class JsonSavedTeamsWriter {

    static void writeSavedTeams(Context context, String fileName, List<SavedTeam> savedTeams) {
        Log.i("VBR-SavedTeams", String.format("Write saved teams into %s", fileName));
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writeSavedTeamsStream(outputStream, savedTeams);
            outputStream.close();
        } catch (IOException e) {
            Log.e("VBR-SavedTeams", "Exception while writing team", e);
        }
    }

    public static void writeSavedTeamsStream(OutputStream outputStream, List<SavedTeam> savedTeams) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        writeTeamArray(writer, savedTeams);
        writer.close();
    }

    private static void writeTeamArray(JsonWriter writer, List<SavedTeam> savedTeams) throws IOException {
        writer.beginArray();
        for (SavedTeam savedTeam : savedTeams) {
            writeTeam(writer, savedTeam);
        }
        writer.endArray();
    }

    private static void writeTeam(JsonWriter writer, SavedTeam savedTeam) throws IOException {
        writer.beginObject();
        writer.name("name").value(savedTeam.getTeamName(null));
        writer.name("color").value(String.format("#%06X", (0xFFFFFF & savedTeam.getTeamColor(null))).toLowerCase());
        writer.name("liberoColor").value(String.format("#%06X", (0xFFFFFF & savedTeam.getLiberoColor(null))).toLowerCase());
        writer.name("players");
        writePlayers(writer, savedTeam.getPlayers(null));
        writer.name("liberos");
        writePlayers(writer, savedTeam.getLiberos(null));
        writer.name("captain").value(savedTeam.getCaptain(null));
        writer.name("gender").value(savedTeam.getGenderType().toString());
        writer.endObject();
    }

    private static void writePlayers(JsonWriter writer, Set<Integer> players) throws IOException {
        writer.beginArray();
        for (Integer player : players) {
            writer.value(player);
        }
        writer.endArray();
    }
}
