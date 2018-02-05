package com.tonkar.volleyballreferee.ui.web;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.WebUtils;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.Locale;

public class WebActivity extends AppCompatActivity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        String url;

        if (savedInstanceState == null) {
            url = getIntent().getStringExtra("url");
        }
        else {
            url = savedInstanceState.getString("url");
        }

        mWebView = findViewById(R.id.web_view);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        mWebView.loadUrl(url);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", mWebView.getUrl());
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_web, menu);

        MenuItem searchGamesItem = menu.findItem(R.id.action_search_online_games);
        SearchView searchGamesView = (SearchView) searchGamesItem.getActionView();

        searchGamesView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            }
        });

        searchGamesView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchQuery) {
                String searchQueryTrim = searchQuery.trim();
                if (searchQueryTrim.length() > 2) {
                    mWebView.loadUrl(String.format(Locale.getDefault(), "%s/%s", WebUtils.SEARCH_URL, searchQueryTrim));
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                return onQueryTextSubmit(searchQuery);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_online_games:
                return true;
            case R.id.action_view_live_games:
                mWebView.loadUrl(WebUtils.LIVE_URL);
                return true;
            case R.id.action_share:
                UiUtils.shareUrl(this, mWebView.getTitle() + "\n" + mWebView.getUrl());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
