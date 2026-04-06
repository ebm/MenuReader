package com.example.menureader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.menureader.Handling.Menu;
import com.example.menureader.Handling.MenuLine;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Tests OCR functionality
 */
@RunWith(AndroidJUnit4.class)
public class OCRTest {
    @Test
    public void testMenuRecognition() throws Exception {
        Context appContext = InstrumentationRegistry.getInstrumentation().getContext();

        LogHandler.m("In test framework");
        String[] files = appContext.getAssets().list("");
        for (String f : files) {
            LogHandler.m("Asset: " + f);
        }

        InputStream is = appContext.getAssets().open("McDonalds_Menu.jpg");
        Bitmap bitmap = BitmapFactory.decodeStream(is);

        CountDownLatch latch = new CountDownLatch(1);

        // Atomic Reference is not necessary because await() guarantees exception and menu are set before continuing
        final Exception[] exception = new Exception[1];
        Menu menu = new Menu(bitmap, new Menu.OnMenuReadyListener() {
            @Override
            public void onMenuReady(Menu menu) {
                exception[0] = null;
                latch.countDown();
            }

            @Override
            public void onMenuFailed(Exception e) {
                exception[0] = e;
                latch.countDown();
            }
        });
        long timeout = 10;
        LogHandler.m("Waiting for ocr...");
        long start = System.nanoTime();
        assertTrue("OCR took longer than " + timeout + " seconds", latch.await(timeout, TimeUnit.SECONDS));
        long end = System.nanoTime() - start;
        assertNull("OCR failed with exception" + exception[0], exception[0]);
        LogHandler.m("testMenuRecognition passed. Initialization took " + ((double) end / 1_000_000_000) + " seconds.");

        LogHandler.m("Menu:");
        for (MenuLine ml : menu.getMenuList()) {
            LogHandler.m("Line: " + ml.getText());
        }
    }

}