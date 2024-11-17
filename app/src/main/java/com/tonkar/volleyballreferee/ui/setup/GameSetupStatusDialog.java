package com.tonkar.volleyballreferee.ui.setup;

import android.app.Activity;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textview.MaterialTextView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.*;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class GameSetupStatusDialog {

    private final Activity mActivity;
    private final IGame    mGame;

    public GameSetupStatusDialog(Activity activity, IGame game) {
        mActivity = activity;
        mGame = game;
    }

    public void show(UsageType usageType) {
        boolean isSimpleSetup = UsageType.POINTS_SCOREBOARD.equals(usageType);

        boolean homeTeamNameValid = mGame.getTeamName(TeamType.HOME).length() >= IBaseTeam.TEAM_NAME_MIN_LENGTH;
        boolean homeTeamPlayersValid = mGame.getNumberOfPlayers(TeamType.HOME) >= mGame.getExpectedNumberOfPlayersOnCourt();
        boolean homeTeamCaptainValid = mGame.getCaptain(TeamType.HOME) >= 0;
        boolean homeTeamValid = homeTeamNameValid && homeTeamPlayersValid && homeTeamCaptainValid;

        boolean guestTeamNameValid = mGame.getTeamName(TeamType.GUEST).length() >= IBaseTeam.TEAM_NAME_MIN_LENGTH;
        boolean guestTeamPlayersValid = mGame.getNumberOfPlayers(TeamType.GUEST) >= mGame.getExpectedNumberOfPlayersOnCourt();
        boolean guestTeamCaptainValid = mGame.getCaptain(TeamType.GUEST) >= 0;
        boolean guestTeamValid = guestTeamNameValid && guestTeamPlayersValid && guestTeamCaptainValid;

        boolean rulesNameValid = mGame.getRules().getName().length() >= Rules.RULES_NAME_MIN_LENGTH;

        boolean leagueNameValid = mGame.getLeague().getName().length() >= 2;
        boolean divisionNameValid = mGame.getLeague().getDivision().length() >= 2;
        boolean miscValid = (leagueNameValid && divisionNameValid) || (StringUtils.isBlank(
                mGame.getLeague().getName()) && StringUtils.isBlank(mGame.getLeague().getDivision()));

        View gameSetupStatusView = mActivity.getLayoutInflater().inflate(R.layout.game_setup_status_dialog, null);

        TableRow homeTeamNameStatusRow = gameSetupStatusView.findViewById(R.id.home_team_name_status_row);
        TableRow homeTeamPlayersStatusRow = gameSetupStatusView.findViewById(R.id.home_team_players_status_row);
        MaterialTextView homeTeamPlayersStatusView = gameSetupStatusView.findViewById(R.id.home_team_players_status);
        TableRow homeTeamCaptainStatusRow = gameSetupStatusView.findViewById(R.id.home_team_captain_status_row);

        if (homeTeamValid) {
            homeTeamNameStatusRow.setVisibility(View.GONE);
            homeTeamPlayersStatusRow.setVisibility(View.GONE);
            homeTeamCaptainStatusRow.setVisibility(View.GONE);
        } else {
            ImageView homeTeamStatusIcon = gameSetupStatusView.findViewById(R.id.home_team_status_icon);
            homeTeamStatusIcon.setVisibility(View.GONE);
            homeTeamNameStatusRow.setVisibility(homeTeamNameValid ? View.GONE : View.VISIBLE);
            homeTeamPlayersStatusView.setText(String.format(Locale.getDefault(), "%d / %d", mGame.getNumberOfPlayers(TeamType.HOME),
                                                            mGame.getExpectedNumberOfPlayersOnCourt()));
            homeTeamPlayersStatusRow.setVisibility(homeTeamPlayersValid || isSimpleSetup ? View.GONE : View.VISIBLE);
            homeTeamCaptainStatusRow.setVisibility(homeTeamCaptainValid || isSimpleSetup ? View.GONE : View.VISIBLE);
        }

        TableRow guestTeamNameStatusRow = gameSetupStatusView.findViewById(R.id.guest_team_name_status_row);
        TableRow guestTeamPlayersStatusRow = gameSetupStatusView.findViewById(R.id.guest_team_players_status_row);
        MaterialTextView guestTeamPlayersStatusView = gameSetupStatusView.findViewById(R.id.guest_team_players_status);
        TableRow guestTeamCaptainStatusRow = gameSetupStatusView.findViewById(R.id.guest_team_captain_status_row);

        if (guestTeamValid) {
            guestTeamNameStatusRow.setVisibility(View.GONE);
            guestTeamPlayersStatusRow.setVisibility(View.GONE);
            guestTeamCaptainStatusRow.setVisibility(View.GONE);
        } else {
            ImageView guestTeamStatusIcon = gameSetupStatusView.findViewById(R.id.guest_team_status_icon);
            guestTeamStatusIcon.setVisibility(View.GONE);
            guestTeamNameStatusRow.setVisibility(guestTeamNameValid ? View.GONE : View.VISIBLE);
            guestTeamPlayersStatusView.setText(String.format(Locale.getDefault(), "%d / %d", mGame.getNumberOfPlayers(TeamType.GUEST),
                                                             mGame.getExpectedNumberOfPlayersOnCourt()));
            guestTeamPlayersStatusRow.setVisibility(guestTeamPlayersValid || isSimpleSetup ? View.GONE : View.VISIBLE);
            guestTeamCaptainStatusRow.setVisibility(guestTeamCaptainValid || isSimpleSetup ? View.GONE : View.VISIBLE);
        }

        if (rulesNameValid) {
            TableRow rulesNameStatusRow = gameSetupStatusView.findViewById(R.id.rules_name_status_row);
            rulesNameStatusRow.setVisibility(View.GONE);
        } else {
            ImageView rulesStatusIcon = gameSetupStatusView.findViewById(R.id.rules_status_icon);
            rulesStatusIcon.setVisibility(View.GONE);
        }

        TableRow leagueNameStatusRow = gameSetupStatusView.findViewById(R.id.league_name_status_row);
        TableRow divisionPlayersStatusRow = gameSetupStatusView.findViewById(R.id.division_name_status_row);

        if (miscValid) {
            TableRow rulesNameStatusRow = gameSetupStatusView.findViewById(R.id.rules_name_status_row);
            rulesNameStatusRow.setVisibility(View.GONE);
            leagueNameStatusRow.setVisibility(View.GONE);
            divisionPlayersStatusRow.setVisibility(View.GONE);
        } else {
            ImageView miscStatusIcon = gameSetupStatusView.findViewById(R.id.misc_status_icon);
            miscStatusIcon.setVisibility(View.GONE);
            leagueNameStatusRow.setVisibility(leagueNameValid ? View.GONE : View.VISIBLE);
            divisionPlayersStatusRow.setVisibility(divisionNameValid ? View.GONE : View.VISIBLE);
        }

        new AlertDialog.Builder(mActivity, R.style.AppTheme_Dialog)
                .setView(gameSetupStatusView)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
