package com.tonkar.volleyballreferee.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.gson.JsonParseException;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.*;
import com.tonkar.volleyballreferee.engine.api.*;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.ui.setup.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

import okhttp3.*;

public class HomeFragment extends Fragment {

    private StoredGamesService mStoredGamesService;

    public HomeFragment() {}

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        mStoredGamesService = new StoredGamesManager(requireContext());

        View startIndoor6x6Card = fragmentView.findViewById(R.id.start_indoor_6x6_card);
        startIndoor6x6Card.setOnClickListener(this::startIndoorGame);

        View startBeachCard = fragmentView.findViewById(R.id.start_beach_card);
        startBeachCard.setOnClickListener(this::startBeachGame);

        View startScoreBoardCard = fragmentView.findViewById(R.id.start_scoreboard_card);
        startScoreBoardCard.setOnClickListener(this::startScoreBasedGame);

        View startSnowCard = fragmentView.findViewById(R.id.start_snow_card);
        startSnowCard.setOnClickListener(this::startSnowGame);

        View startIndoor4x4Card = fragmentView.findViewById(R.id.start_indoor_4x4_card);
        startIndoor4x4Card.setOnClickListener(this::startIndoor4x4Game);

        if (mStoredGamesService.hasCurrentGame()) {
            View resumeGameCard = fragmentView.findViewById(R.id.resume_game_card);
            resumeGameCard.setVisibility(View.VISIBLE);
            resumeGameCard.setOnClickListener(this::resumeCurrentGame);
        }

        if (PrefUtils.hasServerUrl(requireContext()) && !PrefUtils.isSignedIn(requireContext())) {
            View goToSignCard = fragmentView.findViewById(R.id.goto_sign_in_card);
            goToSignCard.setVisibility(View.VISIBLE);
            goToSignCard.setOnClickListener(this::goToSignIn);
        }

        fetchFriendRequests(fragmentView);
        fetchAvailableGames(fragmentView);

        if (mStoredGamesService.hasCurrentGame()) {
            try {
                mStoredGamesService.loadCurrentGame();
            } catch (JsonParseException e) {
                Log.e(Tags.STORED_GAMES, "Failed to read the recorded game because the JSON format was invalid", e);
                mStoredGamesService.deleteCurrentGame();
            }
        }
        if (mStoredGamesService.hasSetupGame()) {
            mStoredGamesService.deleteSetupGame();
        }

        return fragmentView;
    }

    private void resumeCurrentGame(View view) {
        Log.i(Tags.GAME_UI, "Resume game");
        resumeCurrentGame();
    }

    private void goToSignIn(View view) {
        Log.i(Tags.USER_UI, "User sign in");
        navigateToFragment(R.id.user_fragment);
    }

    private void startIndoorGame(View view) {
        Log.i(Tags.GAME_UI, "Start an indoor game");
        UserSummaryDto user = PrefUtils.getUser(requireContext());
        IndoorGame game = GameFactory.createIndoorGame(UUID.randomUUID().toString(),
                                                       Optional.ofNullable(user).map(UserSummaryDto::getId).orElse(null),
                                                       Optional.ofNullable(user).map(UserSummaryDto::getPseudo).orElse(null),
                                                       System.currentTimeMillis(), 0L, Rules.officialIndoorRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game");
        final Intent intent = new Intent(requireContext(), GameSetupActivity.class);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, "gameKindToToolbar").toBundle());
    }

    private void startBeachGame(View view) {
        Log.i(Tags.GAME_UI, "Start a beach game");
        UserSummaryDto user = PrefUtils.getUser(requireContext());
        BeachGame game = GameFactory.createBeachGame(UUID.randomUUID().toString(),
                                                     Optional.ofNullable(user).map(UserSummaryDto::getId).orElse(null),
                                                     Optional.ofNullable(user).map(UserSummaryDto::getPseudo).orElse(null),
                                                     System.currentTimeMillis(), 0L, Rules.officialBeachRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(requireContext(), QuickGameSetupActivity.class);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, "gameKindToToolbar").toBundle());
    }

    private void startSnowGame(View view) {
        Log.i(Tags.GAME_UI, "Start a snow game");
        UserSummaryDto user = PrefUtils.getUser(requireContext());
        SnowGame game = GameFactory.createSnowGame(UUID.randomUUID().toString(),
                                                   Optional.ofNullable(user).map(UserSummaryDto::getId).orElse(null),
                                                   Optional.ofNullable(user).map(UserSummaryDto::getPseudo).orElse(null),
                                                   System.currentTimeMillis(), 0L, Rules.officialSnowRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game");
        final Intent intent = new Intent(requireContext(), GameSetupActivity.class);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, "gameKindToToolbar").toBundle());
    }

    private void startIndoor4x4Game(View view) {
        Log.i(Tags.GAME_UI, "Start a 4x4 indoor game");
        UserSummaryDto user = PrefUtils.getUser(requireContext());
        Indoor4x4Game game = GameFactory.createIndoor4x4Game(UUID.randomUUID().toString(),
                                                             Optional.ofNullable(user).map(UserSummaryDto::getId).orElse(null),
                                                             Optional.ofNullable(user).map(UserSummaryDto::getPseudo).orElse(null),
                                                             System.currentTimeMillis(), 0L, Rules.defaultIndoor4x4Rules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game");
        final Intent intent = new Intent(requireContext(), GameSetupActivity.class);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, "gameKindToToolbar").toBundle());
    }

    private void startScoreBasedGame(View view) {
        Log.i(Tags.GAME_UI, "Start a score-based game");
        UserSummaryDto user = PrefUtils.getUser(requireContext());
        IndoorGame game = GameFactory.createPointBasedGame(UUID.randomUUID().toString(),
                                                           Optional.ofNullable(user).map(UserSummaryDto::getId).orElse(null),
                                                           Optional.ofNullable(user).map(UserSummaryDto::getPseudo).orElse(null),
                                                           System.currentTimeMillis(), 0L, Rules.officialIndoorRules());
        mStoredGamesService.saveSetupGame(game);

        Log.i(Tags.GAME_UI, "Start activity to setup game quickly");
        final Intent intent = new Intent(requireContext(), QuickGameSetupActivity.class);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, "gameKindToToolbar").toBundle());
    }

    private void goToAvailableGames(View view) {
        Log.i(Tags.SCHEDULE_UI, "Scheduled games");
        navigateToFragment(R.id.scheduled_games_list_fragment);
    }

    private void goToColleagues(View view) {
        Log.i(Tags.USER_UI, "User colleagues");
        navigateToFragment(R.id.colleagues_list_fragment);
    }

    private void resumeCurrentGame() {
        Log.i(Tags.GAME_UI, "Start game activity and resume current game");
        IGame game = mStoredGamesService.loadCurrentGame();

        if (game == null) {
            UiUtils.makeErrorText(requireContext(), getString(R.string.resume_game_error), Toast.LENGTH_LONG).show();
        } else {
            final Intent gameIntent = new Intent(requireContext(), GameActivity.class);
            startActivity(gameIntent);
            UiUtils.animateCreate(requireActivity());
        }
    }

    private void navigateToFragment(@IdRes int fragmentId) {
        NavHostFragment navigationHostFragment = (NavHostFragment) requireActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.main_container_view);
        if (navigationHostFragment != null) {
            NavController navigationController = navigationHostFragment.getNavController();
            navigationController.navigate(fragmentId);
        }
    }

    private void fetchFriendRequests(View view) {
        if (PrefUtils.canSync(requireContext())) {
            VbrApi.getInstance(requireContext()).countFriendRequests(requireContext(), new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    initFriendRequestsButton(view, new CountDto(0L));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            CountDto count = JsonConverters.GSON.fromJson(body.string(), CountDto.class);
                            initFriendRequestsButton(view, count);
                        }
                    } else {
                        initFriendRequestsButton(view, new CountDto(0L));
                    }
                }
            });
        }
    }

    private void initFriendRequestsButton(View view, CountDto count) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                if (count.getCount() > 0) {
                    TextView gotoColleaguesText = view.findViewById(R.id.goto_colleagues_text);
                    gotoColleaguesText.setText(
                            String.format(Locale.getDefault(), "%s: %d", gotoColleaguesText.getText(), count.getCount()));

                    View gotoColleaguesCard = view.findViewById(R.id.goto_colleagues_card);
                    gotoColleaguesCard.setVisibility(View.VISIBLE);
                    gotoColleaguesCard.setOnClickListener(this::goToColleagues);
                }
            });
        }
    }

    private void fetchAvailableGames(View view) {
        if (PrefUtils.canSync(requireContext())) {
            VbrApi.getInstance(requireContext()).countAvailableGames(requireContext(), new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    call.cancel();
                    initAvailableGamesButton(view, new CountDto(0L));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.code() == HttpURLConnection.HTTP_OK) {
                        try (ResponseBody body = response.body()) {
                            CountDto count = JsonConverters.GSON.fromJson(body.string(), CountDto.class);
                            initAvailableGamesButton(view, count);
                        }
                    } else {
                        initAvailableGamesButton(view, new CountDto(0L));
                    }
                }
            });
        }
    }

    private void initAvailableGamesButton(View view, CountDto count) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                if (count.getCount() > 0) {
                    TextView gotoAvailableGamesText = view.findViewById(R.id.goto_available_games_text);
                    gotoAvailableGamesText.setText(
                            String.format(Locale.getDefault(), "%s: %d", gotoAvailableGamesText.getText(), count.getCount()));

                    View gotoAvailableGamesCard = view.findViewById(R.id.goto_available_games_card);
                    gotoAvailableGamesCard.setVisibility(View.VISIBLE);
                    gotoAvailableGamesCard.setOnClickListener(this::goToAvailableGames);
                }
            });
        }
    }
}