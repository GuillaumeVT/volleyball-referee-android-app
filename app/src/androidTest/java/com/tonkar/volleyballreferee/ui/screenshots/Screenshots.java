package com.tonkar.volleyballreferee.ui.screenshots;

import android.app.LocaleManager;
import android.app.UiAutomation;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.LocaleList;
import android.util.Log;

import androidx.test.core.app.DeviceCapture;
import androidx.test.espresso.Espresso;

import com.tonkar.volleyballreferee.engine.service.StoredGamesService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;

public abstract class Screenshots {

    protected Context            mContext;
    protected UiAutomation       mUiAutomation;
    protected StoredGamesService mStoredGamesService;
    protected File               mScreenshotsDirectory;

    protected final List<Locale> mLocales = List.of(
            Locale.forLanguageTag("ar"),
            Locale.forLanguageTag("bg"),
            Locale.forLanguageTag("ca"),
            Locale.GERMAN,
            Locale.forLanguageTag("el"),
            Locale.forLanguageTag("es"),
            Locale.forLanguageTag("et"),
            Locale.FRENCH,
            Locale.forLanguageTag("hu"),
            Locale.ITALIAN,
            Locale.forLanguageTag("nl"),
            Locale.forLanguageTag("pl"),
            Locale.forLanguageTag("pt"),
            Locale.forLanguageTag("pt-BR"),
            Locale.forLanguageTag("ru"),
            Locale.ENGLISH
    );

    protected void setAppLanguage(Locale locale) {
        mContext.getSystemService(LocaleManager.class).setApplicationLocales(new LocaleList(locale));
    }

    protected void takeScreenshot(String filename, long minSize, long maxSize) throws IOException {
        Bitmap bitmap = DeviceCapture.takeScreenshot();

        Locale locale = mContext.getResources().getConfiguration().getLocales().get(0);

        File screenshot = new File(mScreenshotsDirectory, String.format("%s_%s.png", locale, filename));

        try (FileOutputStream fileOutputStream = new FileOutputStream(screenshot)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        }

        Log.i("V-Screenshots", String.format("%s: file size %d < %d < %d", screenshot, minSize, Files.size(screenshot.toPath()), maxSize));

        if (Files.size(screenshot.toPath()) < minSize || Files.size(screenshot.toPath()) > maxSize) {
            Espresso.onIdle();
            takeScreenshot(filename, minSize, maxSize);
        }
    }
}
