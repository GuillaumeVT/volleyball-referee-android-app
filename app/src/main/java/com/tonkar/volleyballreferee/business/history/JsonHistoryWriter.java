package com.tonkar.volleyballreferee.business.history;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public class JsonHistoryWriter {

    static void writeRecordedGames(Context context, String fileName, List<RecordedGame> recordedGames) {
        Log.i("VBR-History", String.format("Write recorded games history into %s", fileName));
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writeRecordedGamesStream(outputStream, recordedGames);
            outputStream.close();
        } catch (IOException e) {
            Log.e("VBR-History", "Exception while writing game", e);
        }
    }

    public static void writeRecordedGamesStream(OutputStream outputStream, List<RecordedGame> recordedGames) throws IOException {
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        writeGameArray(writer, recordedGames);
        writer.close();
    }

    private static void writeGameArray(JsonWriter writer, List<RecordedGame> recordedGames) throws IOException {
        writer.beginArray();
        for (RecordedGame recordedGame : recordedGames) {
            writeGame(writer, recordedGame);
        }
        writer.endArray();
    }

    private static void writeGame(JsonWriter writer, RecordedGame recordedGame) throws IOException {
        Log.i("VBR-History", "Write a recorded game");
        writer.beginObject();
        writer.name("game_type").value(recordedGame.getGameType().toString());
        writer.name("game_date").value(recordedGame.getGameDate());
        writeHomeTeam(writer, recordedGame.getHomeTeam());
        writeGuestTeam(writer, recordedGame.getGuestTeam());
        writeSetArray(writer, recordedGame.getSets());
        writer.endObject();
    }

    private static void writeHomeTeam(JsonWriter writer, RecordedTeam recordedTeam) throws IOException {
        writer.name("home_team");
        writer.beginObject();
        writer.name("team_name").value(recordedTeam.getName());
        writer.name("team_color").value(recordedTeam.getColorId());
        writer.endObject();
    }

    private static void writeGuestTeam(JsonWriter writer, RecordedTeam recordedTeam) throws IOException {
        writer.name("guest_team");
        writer.beginObject();
        writer.name("team_name").value(recordedTeam.getName());
        writer.name("team_color").value(recordedTeam.getColorId());
        writer.endObject();
    }

    private static void writeSetArray(JsonWriter writer, List<RecordedSet> recordedSets) throws IOException {
        writer.name("set_list");
        writer.beginArray();
        for (RecordedSet recordedSet : recordedSets) {
            writeSet(writer, recordedSet);
        }
        writer.endArray();
    }

    private static void writeSet(JsonWriter writer, RecordedSet recordedSet) throws IOException {
        writer.beginObject();
        writer.name("set_duration").value(recordedSet.getDuration());
        writeLadder(writer, recordedSet.getPointsLadder());
        writer.endObject();
    }

    private static void writeLadder(JsonWriter writer, List<TeamType> pointsLadder) throws IOException {
        writer.name("set_ladder");
        writer.beginArray();
        for (TeamType teamType : pointsLadder) {
            writer.value(teamType.toString());
        }
        writer.endArray();
    }

}
