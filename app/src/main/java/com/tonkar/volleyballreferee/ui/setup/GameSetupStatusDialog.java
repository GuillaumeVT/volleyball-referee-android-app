package com.tonkar.volleyballreferee.ui.setup;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import com.google.android.material.textview.MaterialTextView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import java.util.Locale;

public class GameSetupStatusDialog {

    private final Activity mActivity;
    private final IGame    mGame;

    public GameSetupStatusDialog(Activity activity, IGame game) {
        mActivity = activity;
        mGame = game;
    }

    public void show(UsageType usageType) {
        View gameSetupStatusView = mActivity.getLayoutInflater().inflate(R.layout.game_setup_status_dialog, null);

        MaterialTextView homeTeamNameStatusView = gameSetupStatusView.findViewById(R.id.home_team_name_status);
        MaterialTextView homeTeamCaptainStatusView = gameSetupStatusView.findViewById(R.id.home_team_captain_status);

        MaterialTextView guestTeamNameStatusView = gameSetupStatusView.findViewById(R.id.guest_team_name_status);
        MaterialTextView guestTeamCaptainStatusView = gameSetupStatusView.findViewById(R.id.guest_team_captain_status);

        MaterialTextView rulesNameStatusView = gameSetupStatusView.findViewById(R.id.rules_name_status);

        MaterialTextView miscStatusView = gameSetupStatusView.findViewById(R.id.misc_status);
        MaterialTextView leagueNameStatusView = gameSetupStatusView.findViewById(R.id.league_name_status);
        MaterialTextView divisionNameStatusView = gameSetupStatusView.findViewById(R.id.division_name_status);

        setTextViewStatus(homeTeamNameStatusView, mGame.getTeamName(TeamType.HOME).length() >= 2);


        setTextViewStatus(guestTeamNameStatusView, mGame.getTeamName(TeamType.GUEST).length() >= 2);


        if (UsageType.NORMAL.equals(mGame.getUsage())) {
            MaterialTextView homeTeamPlayersStatusView = gameSetupStatusView.findViewById(R.id.home_team_players_status);
            MaterialTextView guestTeamPlayersStatusView = gameSetupStatusView.findViewById(R.id.guest_team_players_status);

            setTextViewStatus(homeTeamPlayersStatusView, mGame.getNumberOfPlayers(TeamType.HOME) >= mGame.getExpectedNumberOfPlayersOnCourt());
            homeTeamPlayersStatusView.setText(String.format(Locale.getDefault(), "%d / %d", mGame.getNumberOfPlayers(TeamType.HOME), mGame.getExpectedNumberOfPlayersOnCourt()));
            setTextViewStatus(homeTeamCaptainStatusView, mGame.getCaptain(TeamType.HOME) >= 0);

            setTextViewStatus(guestTeamPlayersStatusView, mGame.getNumberOfPlayers(TeamType.GUEST) >= mGame.getExpectedNumberOfPlayersOnCourt());
            guestTeamPlayersStatusView.setText(String.format(Locale.getDefault(), "%d / %d", mGame.getNumberOfPlayers(TeamType.GUEST), mGame.getExpectedNumberOfPlayersOnCourt()));
            setTextViewStatus(guestTeamCaptainStatusView, mGame.getCaptain(TeamType.GUEST) >= 0);
        } else {
            View homeTeamPlayersStatusLayout = gameSetupStatusView.findViewById(R.id.home_team_players_status_layout);
            View guestTeamPlayersStatusLayout = gameSetupStatusView.findViewById(R.id.guest_team_players_status_layout);

            homeTeamPlayersStatusLayout.setVisibility(View.GONE);
            homeTeamCaptainStatusView.setVisibility(View.GONE);
            guestTeamPlayersStatusLayout.setVisibility(View.GONE);
            guestTeamCaptainStatusView.setVisibility(View.GONE);
        }

        setTextViewStatus(rulesNameStatusView, mGame.getRules().getName().length() >= 2);

        if (mGame.getLeague().getName().length() > 0) {
            miscStatusView.setVisibility(View.VISIBLE);
            leagueNameStatusView.setVisibility(View.VISIBLE);
            divisionNameStatusView.setVisibility(View.VISIBLE);

            setTextViewStatus(leagueNameStatusView, mGame.getLeague().getName().length() > 0);
            setTextViewStatus(divisionNameStatusView, mGame.getLeague().getDivision().length() > 2);
        } else {
            miscStatusView.setVisibility(View.GONE);
            leagueNameStatusView.setVisibility(View.GONE);
            divisionNameStatusView.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(mActivity, R.style.AppTheme_Dialog)
                .setView(gameSetupStatusView)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setTextViewStatus(MaterialTextView statusView, boolean success) {
        statusView.setCompoundDrawablesWithIntrinsicBounds(0, 0, success ? R.drawable.ic_check : R.drawable.ic_close, 0);
        TextViewCompat.setCompoundDrawableTintList(statusView, ColorStateList.valueOf(ContextCompat.getColor(mActivity, success ? R.color.colorSuccess : R.color.colorError)));
    }
}
