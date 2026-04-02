package com.example.menureader;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private String text;

    private List<MenuCategory> categories;
    private Bitmap imageBitmap;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public interface OnMenuReadyListener {
        void onMenuReady(Menu menu);
        void onMenuFailed(Exception e);
    }

    public Menu(Bitmap imageBitmap, OnMenuReadyListener listener) {
        LogHandler.m("Menu initializer called");
        this.imageBitmap = imageBitmap;
        this.categories = new ArrayList<>();
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        processImage(image, listener);
    }
    public void processImage(InputImage image, OnMenuReadyListener listener) {
        TextRecognizer rec = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        rec.process(image).addOnSuccessListener(text -> {
            this.text = text.getText();
            LogHandler.m("Image processing success");

            // Gets all the categories on the menu
            for (Text.TextBlock block : text.getTextBlocks()) {
                String categoryText = block.getText();
                Rect categoryBoundingBox = block.getBoundingBox();
                List<MenuLine> lines = new ArrayList<>();
                // Gets all the menu items
                for (Text.Line line : block.getLines()) {
                    MenuLine ml = new MenuLine(line.getText(), line.getBoundingBox());
                    lines.add(ml);
                }
                categories.add(new MenuCategory(categoryText, lines, categoryBoundingBox));
            }
            listener.onMenuReady(this);
        }).addOnFailureListener(e -> {
            LogHandler.m("Failed to process image", e);
            listener.onMenuFailed(e);
        });
    }
    public List<MenuCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<MenuCategory> categories) {
        this.categories = categories;
    }
}
