package com.tonkar.volleyballreferee.ui.screenshots;

import android.app.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.*;
import android.util.Log;

import androidx.test.core.app.DeviceCapture;
import androidx.test.espresso.Espresso;
import androidx.test.platform.app.InstrumentationRegistry;

import com.tonkar.volleyballreferee.engine.service.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public abstract class Screenshots {

    protected Context            mContext;
    protected UiAutomation       mUiAutomation;
    protected StoredGamesService mStoredGamesService;
    protected File               mScreenshotsDirectory;

    protected final List<Locale> mLocales = List.of(Locale.forLanguageTag("ar"), Locale.forLanguageTag("bg"), Locale.forLanguageTag("ca"),
                                                    Locale.GERMAN, Locale.forLanguageTag("el"), Locale.forLanguageTag("es"),
                                                    Locale.forLanguageTag("et"), Locale.FRENCH, Locale.forLanguageTag("hu"), Locale.ITALIAN,
                                                    Locale.forLanguageTag("nl"), Locale.forLanguageTag("pl"), Locale.forLanguageTag("pt"),
                                                    Locale.forLanguageTag("pt-BR"), Locale.forLanguageTag("ru"), Locale.ENGLISH);

    protected void init(String screenshotDirectory) {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mUiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        mStoredGamesService = new StoredGamesManager(mContext);
        mScreenshotsDirectory = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_SCREENSHOTS), screenshotDirectory);

        if (!mScreenshotsDirectory.exists()) {
            mScreenshotsDirectory.mkdirs();
        }
    }

    protected void setAppLanguage(Locale locale) {
        mContext.getSystemService(LocaleManager.class).setApplicationLocales(new LocaleList(locale));
    }

    protected void takeScreenshot(String filename, long minSize, long maxSize) throws IOException {
        Bitmap bitmap = DeviceCapture.takeScreenshot();

        Locale locale = mContext.getResources().getConfiguration().getLocales().get(0);

        File localeDirectory = new File(mScreenshotsDirectory, locale.toString());

        if (!localeDirectory.exists()) {
            localeDirectory.mkdirs();
        }

        File screenshot = new File(localeDirectory, String.format("%s.png", filename));

        try (FileOutputStream fileOutputStream = new FileOutputStream(screenshot)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        }

        long screenshotSize = Files.size(screenshot.toPath());

        Log.i("V-Screenshots", String.format("%s: file size %d < %d < %d", screenshot, minSize, screenshotSize, maxSize));

        if (screenshotSize < minSize || screenshotSize > maxSize) {
            Espresso.onIdle();
            takeScreenshot(filename, minSize, maxSize);
        }
    }
}
