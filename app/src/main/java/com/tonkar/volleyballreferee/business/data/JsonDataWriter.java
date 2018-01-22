package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.util.JsonWriter;
import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.Timeout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Set;

public class JsonDataWriter {

    static void writeRecordedGames(Context context, String fileName, List<RecordedGame> recordedGames) {
        Log.i("VBR-Data", String.format("Write recorded games into %s", fileName));
        try {
            FileOutputStream outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            writeRecordedGamesStream(outputStream, recordedGames);
            outputStream.close();
        } catch (IOException e) {
            Log.e("VBR-Data", "Exception while writing game", e);
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
        Log.i("VBR-Data", "Write a recorded game");
        writer.beginObject();
        writer.name("kind").value(recordedGame.getGameType().toString());
        writer.name("date").value(recordedGame.getGameDate());
        writer.name("gender").value(recordedGame.getGenderType().toString());
        writer.name("usage").value(recordedGame.getUsageType().toString());
        writer.name("live").value(!recordedGame.isMatchCompleted());
        writer.name("league").value(recordedGame.getLeagueName());
        writer.name("hTeam");
        writeTeam(writer, recordedGame.getTeam(TeamType.HOME));
        writer.name("gTeam");
        writeTeam(writer, recordedGame.getTeam(TeamType.GUEST));
        writer.name("hSets").value(recordedGame.getSets(TeamType.HOME));
        writer.name("gSets").value(recordedGame.getSets(TeamType.GUEST));
        writer.name("sets");
        writeSetArray(writer, recordedGame.getSets());
        writer.endObject();
    }

    private static void writeTeam(JsonWriter writer, RecordedTeam recordedTeam) throws IOException {
        writer.beginObject();
        writer.name("name").value(recordedTeam.getName());
        writer.name("color").value(String.format("#%06X", (0xFFFFFF & recordedTeam.getColor())).toLowerCase());
        writer.name("liberoColor").value(String.format("#%06X", (0xFFFFFF & recordedTeam.getLiberoColor())).toLowerCase());
        writer.name("players");
        writePlayers(writer, recordedTeam.getPlayers());
        writer.name("liberos");
        writePlayers(writer, recordedTeam.getLiberos());
        writer.name("captain").value(recordedTeam.getCaptain());
        writer.name("gender").value(recordedTeam.getGenderType().toString());
        writer.endObject();
    }

    private static void writePlayers(JsonWriter writer, Set<Integer> players) throws IOException {
        writer.beginArray();
        for (Integer player : players) {
            writer.value(player);
        }
        writer.endArray();
    }


    private static void writeSetArray(JsonWriter writer, List<RecordedSet> recordedSets) throws IOException {
        writer.beginArray();
        for (RecordedSet recordedSet : recordedSets) {
            writeSet(writer, recordedSet);
        }
        writer.endArray();
    }

    private static void writeSet(JsonWriter writer, RecordedSet recordedSet) throws IOException {
        writer.beginObject();
        writer.name("duration").value(recordedSet.getDuration());
        writer.name("hPoints").value(recordedSet.getPoints(TeamType.HOME));
        writer.name("gPoints").value(recordedSet.getPoints(TeamType.GUEST));
        writer.name("hTimeouts").value(recordedSet.getTimeouts(TeamType.HOME));
        writer.name("gTimeouts").value(recordedSet.getTimeouts(TeamType.GUEST));
        writer.name("ladder");
        writeLadder(writer, recordedSet.getPointsLadder());
        writer.name("serving").value(TeamType.toLetter(recordedSet.getServingTeam()));
        writer.name("hCurrentPlayers");
        writePlayers(writer, recordedSet.getCurrentPlayers(TeamType.HOME));
        writer.name("gCurrentPlayers");
        writePlayers(writer, recordedSet.getCurrentPlayers(TeamType.GUEST));
        writer.name("hStartingPlayers");
        writePlayers(writer, recordedSet.getStartingPlayers(TeamType.HOME));
        writer.name("gStartingPlayers");
        writePlayers(writer, recordedSet.getStartingPlayers(TeamType.GUEST));
        writer.name("hSubstitutions");
        writeSubstitutions(writer, recordedSet.getSubstitutions(TeamType.HOME));
        writer.name("gSubstitutions");
        writeSubstitutions(writer, recordedSet.getSubstitutions(TeamType.GUEST));
        writer.name("hCaptain").value(recordedSet.getActingCaptain(TeamType.HOME));
        writer.name("gCaptain").value(recordedSet.getActingCaptain(TeamType.GUEST));
        writer.name("hCalledTimeouts");
        writeTimeouts(writer, recordedSet.getCalledTimeouts(TeamType.HOME));
        writer.name("gCalledTimeouts");
        writeTimeouts(writer, recordedSet.getCalledTimeouts(TeamType.GUEST));
        writer.name("rTime").value(recordedSet.getRemainingTime());
        writer.endObject();
    }

    private static void writeLadder(JsonWriter writer, List<TeamType> pointsLadder) throws IOException {
        writer.beginArray();
        for (TeamType teamType : pointsLadder) {
            writer.value(TeamType.toLetter(teamType));
        }
        writer.endArray();
    }

    private static void writePlayers(JsonWriter writer, List<RecordedPlayer> players) throws IOException {
        writer.beginArray();
        for (RecordedPlayer player : players) {
            writer.beginObject();
            writer.name("num").value(player.getNumber());
            writer.name("pos").value(PositionType.toInt(player.getPositionType()));
            writer.endObject();
        }
        writer.endArray();
    }

    private static void writeSubstitutions(JsonWriter writer, List<Substitution> substitutions) throws IOException {
        writer.beginArray();
        for (Substitution substitution : substitutions) {
            writer.beginObject();
            writer.name("pIn").value(substitution.getPlayerIn());
            writer.name("pOut").value(substitution.getPlayerOut());
            writer.name("hPoints").value(substitution.getHomeTeamPoints());
            writer.name("gPoints").value(substitution.getGuestTeamPoints());
            writer.endObject();
        }
        writer.endArray();
    }

    private static void writeTimeouts(JsonWriter writer, List<Timeout> timeouts) throws IOException {
        writer.beginArray();
        for (Timeout timeout : timeouts) {
            writer.beginObject();
            writer.name("hPoints").value(timeout.getHomeTeamPoints());
            writer.name("gPoints").value(timeout.getGuestTeamPoints());
            writer.endObject();
        }
        writer.endArray();
    }

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

}
