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

    public Menu(Bitmap imageBitmap) {
        LogHandler.m("Menu", "Menu initializer called");
        this.imageBitmap = imageBitmap;
        this.categories = new ArrayList<>();
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        processImage(image);
    }
    public void processImage(InputImage image) {
        TextRecognizer rec = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        rec.process(image).addOnSuccessListener(text -> {
            this.text = text.getText();
            LogHandler.m("Menu OCR Handler", "Image processing success");

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
        }).addOnFailureListener(e -> {
            LogHandler.m("Menu OCR Handler", "Failed to process image", e);
        });
    }
}
