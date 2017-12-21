package com.tonkar.volleyballreferee.business.history;

import android.content.Context;
import android.graphics.Color;
import android.util.JsonReader;
import android.util.Log;

import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class JsonHistoryReader {

    static List<RecordedGame> readRecordedGames(Context context, String fileName) {
        Log.i("VBR-History", String.format("Read recorded games history from %s", fileName));
        List<RecordedGame> games = new ArrayList<>();

        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            games = readRecordedGamesStream(inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i("VBR-History", String.format("%s game history file does not yet exist", fileName));
        } catch (IOException e) {
            Log.e("VBR-History", "Exception while reading games", e);
        }

        return games;
    }

    public static List<RecordedGame> readRecordedGamesStream(InputStream inputStream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return readGameArray(reader);
        } finally {
            reader.close();
        }
    }

    private static List<RecordedGame> readGameArray(JsonReader reader) throws IOException {
        List<RecordedGame> games = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            games.add(readGame(reader));
        }
        reader.endArray();

        return games;
    }

    private static RecordedGame readGame(JsonReader reader) throws IOException {
        RecordedGame recordedGame = new RecordedGame();

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "kind":
                    recordedGame.setGameType(GameType.valueOf(reader.nextString()));
                    break;
                case "date":
                    recordedGame.setGameDate(reader.nextLong());
                    break;
                case "gender":
                    recordedGame.setGenderType(GenderType.valueOf(reader.nextString()));
                    break;
                case "live":
                    recordedGame.setMatchCompleted(!reader.nextBoolean());
                    break;
                case "hTeam":
                    readTeam(reader, recordedGame.getTeam(TeamType.HOME));
                    break;
                case "gTeam":
                    readTeam(reader, recordedGame.getTeam(TeamType.GUEST));
                    break;
                case "hSets":
                    recordedGame.setSets(TeamType.HOME, reader.nextInt());
                    break;
                case "gSets":
                    recordedGame.setSets(TeamType.GUEST, reader.nextInt());
                    break;
                case "sets":
                    readSetArray(reader, recordedGame.getSets());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return recordedGame;
    }

    private static void readTeam(JsonReader reader, RecordedTeam recordedTeam) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "name":
                    recordedTeam.setName(reader.nextString());
                    break;
                case "color":
                    recordedTeam.setColor(Color.parseColor(reader.nextString()));
                    break;
                case "liberoColor":
                    recordedTeam.setLiberoColor(Color.parseColor(reader.nextString()));
                    break;
                case "players":
                    readPlayerArray(reader, recordedTeam.getPlayers());
                    break;
                case "liberos":
                    readPlayerArray(reader, recordedTeam.getLiberos());
                    break;
                case "captain":
                    recordedTeam.setCaptain(reader.nextInt());
                    break;
                case "gender":
                    recordedTeam.setGenderType(GenderType.valueOf(reader.nextString()));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private static void readPlayerArray(JsonReader reader, Set<Integer> players) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            players.add(reader.nextInt());
        }
        reader.endArray();
    }

    private static void readSetArray(JsonReader reader, List<RecordedSet> recordedSets) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            RecordedSet recordedSet = new RecordedSet();
            recordedSets.add(recordedSet);
            readSet(reader, recordedSet);
        }
        reader.endArray();
    }

    private static void readSet(JsonReader reader, RecordedSet recordedSet) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "duration":
                    recordedSet.setDuration(reader.nextLong());
                    break;
                case "hPoints":
                    recordedSet.setPoints(TeamType.HOME, reader.nextInt());
                    break;
                case "gPoints":
                    recordedSet.setPoints(TeamType.GUEST, reader.nextInt());
                    break;
                case "hTimeouts":
                    recordedSet.setTimeouts(TeamType.HOME, reader.nextInt());
                    break;
                case "gTimeouts":
                    recordedSet.setTimeouts(TeamType.GUEST, reader.nextInt());
                    break;
                case "ladder":
                    readLadder(reader, recordedSet.getPointsLadder());
                    break;
                case "serving":
                    recordedSet.setServingTeam(TeamType.fromLetter(reader.nextString()));
                    break;
                case "hCurrentPlayers":
                    readPlayerObjectArray(reader, recordedSet.getCurrentPlayers(TeamType.HOME));
                    break;
                case "gCurrentPlayers":
                    readPlayerObjectArray(reader, recordedSet.getCurrentPlayers(TeamType.GUEST));
                    break;
                case "hStartingPlayers":
                    readPlayerObjectArray(reader, recordedSet.getStartingPlayers(TeamType.HOME));
                    break;
                case "gStartingPlayers":
                    readPlayerObjectArray(reader, recordedSet.getStartingPlayers(TeamType.GUEST));
                    break;
                case "hSubstitutions":
                    readSubstitutionArray(reader, recordedSet.getSubstitutions(TeamType.HOME));
                    break;
                case "gSubstitutions":
                    readSubstitutionArray(reader, recordedSet.getSubstitutions(TeamType.GUEST));
                    break;
                case "hCaptain":
                    recordedSet.setActingCaptain(TeamType.HOME, reader.nextInt());
                    break;
                case "gCaptain":
                    recordedSet.setActingCaptain(TeamType.GUEST, reader.nextInt());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
    }

    private static void readLadder(JsonReader reader, List<TeamType> pointsLadder) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            pointsLadder.add(TeamType.fromLetter(reader.nextString()));
        }
        reader.endArray();
    }

    private static void readPlayerObjectArray(JsonReader reader, List<RecordedPlayer> players) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            RecordedPlayer player = new RecordedPlayer();
            players.add(player);
            readPlayerObject(reader, player);
        }
        reader.endArray();
    }

    private static void readPlayerObject(JsonReader reader, RecordedPlayer player) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "num":
                    player.setNumber(reader.nextInt());
                    break;
                case "pos":
                    player.setPositionType(PositionType.fromInt(reader.nextInt()));
                    break;
            }
        }
        reader.endObject();
    }

    private static void readSubstitutionArray(JsonReader reader, List<Substitution> substitutions) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            Substitution substitution = new Substitution();
            substitutions.add(substitution);
            readSubstitution(reader, substitution);
        }
        reader.endArray();
    }

    private static void readSubstitution(JsonReader reader, Substitution substitution) throws IOException {
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "pIn":
                case "in":
                    substitution.setPlayerIn(reader.nextInt());
                    break;
                case "pOut":
                case "out":
                    substitution.setPlayerOut(reader.nextInt());
                    break;
                case "hPoints":
                    substitution.setHomeTeamPoints(reader.nextInt());
                    break;
                case "gPoints":
                    substitution.setGuestTeamPoints(reader.nextInt());
                    break;
            }
        }
        reader.endObject();
    }

    static List<SavedTeam> readSavedTeams(Context context, String fileName) {
        Log.i("VBR-SavedTeams", String.format("Read saved teams from %s", fileName));
        List<SavedTeam> teams = new ArrayList<>();

        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            teams = readSavedTeamsStream(inputStream);
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.i("VBR-SavedTeams", String.format("%s saved teams file does not yet exist", fileName));
        } catch (IOException e) {
            Log.e("VBR-SavedTeams", "Exception while reading teams", e);
        }

        return teams;
    }

    public static List<SavedTeam> readSavedTeamsStream(InputStream inputStream) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        try {
            return readTeamArray(reader);
        } finally {
            reader.close();
        }
    }

    private static List<SavedTeam> readTeamArray(JsonReader reader) throws IOException {
        List<SavedTeam> teams = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            teams.add(readTeam(reader));
        }
        reader.endArray();

        return teams;
    }

    private static SavedTeam readTeam(JsonReader reader) throws IOException {
        IndoorTeamDefinition teamDefinition = new IndoorTeamDefinition(TeamType.HOME);
        Set<Integer> players = new TreeSet<>();
        Set<Integer> liberos = new TreeSet<>();

        reader.beginObject();

        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "name":
                    teamDefinition.setName(reader.nextString());
                    break;
                case "color":
                    teamDefinition.setColor(Color.parseColor(reader.nextString()));
                    break;
                case "liberoColor":
                    teamDefinition.setLiberoColor(Color.parseColor(reader.nextString()));
                    break;
                case "players":
                    readPlayerArray(reader, players);
                    for (Integer number: players) {
                        teamDefinition.addPlayer(number);
                    }
                    break;
                case "liberos":
                    readPlayerArray(reader, liberos);
                    for (Integer number: liberos) {
                        teamDefinition.addLibero(number);
                    }
                    break;
                case "captain":
                    teamDefinition.setCaptain(reader.nextInt());
                    break;
                case "gender":
                    teamDefinition.setGenderType(GenderType.valueOf(reader.nextString()));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new SavedTeam(teamDefinition);
    }
}
