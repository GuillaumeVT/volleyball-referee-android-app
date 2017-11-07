package com.tonkar.volleyballreferee.business.history;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
        GameType gameType = null;
        long gameDate = 0L;
        RecordedTeam homeTeam = null;
        RecordedTeam guestTeam = null;
        List<RecordedSet> sets = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "game_type":
                    gameType = GameType.valueOf(reader.nextString());
                    break;
                case "game_date":
                    gameDate = reader.nextLong();
                    break;
                case "home_team":
                    homeTeam = readTeam(reader);
                    break;
                case "guest_team":
                    guestTeam = readTeam(reader);
                    break;
                case "set_list":
                    sets = readSetArray(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new RecordedGame(gameType, gameDate, homeTeam, guestTeam, sets);
    }

    private static RecordedTeam readTeam(JsonReader reader) throws IOException {
        String teamName = null;
        int teamColorId = -1;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "team_name":
                    teamName = reader.nextString();
                    break;
                case "team_color":
                    teamColorId = reader.nextInt();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        return new RecordedTeam(teamName, teamColorId);
    }

    private static List<RecordedSet> readSetArray(JsonReader reader) throws IOException {
        List<RecordedSet> sets = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            sets.add(readSet(reader));
        }
        reader.endArray();

        return sets;
    }

    private static RecordedSet readSet(JsonReader reader) throws IOException {
        long duration = 0L;
        List<TeamType > pointsLadder = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            switch (name) {
                case "set_duration":
                    duration = reader.nextLong();
                    break;
                case "set_ladder":
                    pointsLadder = readLadder(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();

        int homeTeamPoints = 0;
        int guestTeamPoints = 0;

        if (pointsLadder != null) {
            for (TeamType teamType : pointsLadder) {
                if (TeamType.HOME.equals(teamType)) {
                    homeTeamPoints++;
                } else {
                    guestTeamPoints++;
                }
            }
        }

        return new RecordedSet(duration, homeTeamPoints, guestTeamPoints, pointsLadder);
    }

    private static List<TeamType> readLadder(JsonReader reader) throws IOException {
        List<TeamType> ladder = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            ladder.add(TeamType.valueOf(reader.nextString()));
        }
        reader.endArray();

        return ladder;
    }

}
