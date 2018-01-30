package com.tonkar.volleyballreferee.business.data;

import android.content.Context;
import android.graphics.Color;
import android.util.JsonReader;
import android.util.Log;

import com.tonkar.volleyballreferee.business.team.IndoorTeamDefinition;
import com.tonkar.volleyballreferee.interfaces.GenderType;
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

public class JsonSavedTeamsReader {

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

    private static void readPlayerArray(JsonReader reader, Set<Integer> players) throws IOException {
        reader.beginArray();
        while (reader.hasNext()) {
            players.add(reader.nextInt());
        }
        reader.endArray();
    }

}
