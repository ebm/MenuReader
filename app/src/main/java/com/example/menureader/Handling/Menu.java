package com.example.menureader.Handling;

import android.graphics.Bitmap;
import com.example.menureader.LogHandler;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private String text;

    private List<MenuLine> menuList;
    private final Bitmap imageBitmap;

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public interface OnMenuReadyListener {
        void onMenuReady(Menu menu);

        void onMenuFailed(Exception e);
    }

    public Menu(Bitmap imageBitmap, OnMenuReadyListener listener) {
        LogHandler.m("Menu initializer called");
        this.imageBitmap = imageBitmap;
        this.menuList = new ArrayList<>();
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        processImage(image, listener);
    }

    /**
     * Processes an image with OCR
     *
     * @param image
     * @param listener
     */
    public void processImage(InputImage image, OnMenuReadyListener listener) {
        TextRecognizer rec = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        rec.process(image).addOnSuccessListener(text -> {
            this.text = text.getText();
            LogHandler.m("Image processing success");

            // Gets all the categories on the menu
            for (Text.TextBlock block : text.getTextBlocks()) {
                // Gets all the menu items
                for (Text.Line line : block.getLines()) {
                    MenuLine ml = new MenuLine(line.getText(), line.getBoundingBox());
                    menuList.add(ml);
                }
            }
            listener.onMenuReady(this);
        }).addOnFailureListener(e -> {
            LogHandler.m("Failed to process image", e);
            listener.onMenuFailed(e);
        });
    }
    public String getText() {
        return text;
    }
    public List<MenuLine> getMenuList() {
        return menuList;
    }
}
