package com.example.menureader.Handling;

import android.app.Activity;
import android.graphics.Bitmap;

public class ImageObject {
    public interface OnImageObjectSuccess {
         void onImageCreation(ImageObject imageObject);
         void onImageFailure(Exception e);
    }
    private String imageURL;
    private Bitmap bitmap;
    private int size;
    // No easy way to get exact string lengths in Java. A Java string has 38 bytes of
    // overhead. sizeOfHeaderForStringByes is set to 50 bytes for safety.
    private int sizeOfHeaderForStringBytes = 50;
    public ImageObject(String imageURL, Bitmap bitmap) {
        this.imageURL = imageURL;
        this.bitmap = bitmap;
        // Java uses UTF_16 so each character is 2 bytes.
        size = imageURL.length() * 2 + sizeOfHeaderForStringBytes + bitmap.getByteCount();
    }

    /**
     * Creates an ImageObject given a string url of an image. Needs a listener to handle
     * the callback when ImageObject has succeeded/failed
     *
     * @param imageURL
     * @param activity
     * @param listener
     */
    public ImageObject(String imageURL, Activity activity, OnImageObjectSuccess listener, int size) {
        size = imageURL.length() * 2 + sizeOfHeaderForStringBytes;
        ImageDeliver.getBitmapFromUrlThread(imageURL, activity, new ImageDeliver.OnImageResultListener() {
            @Override
            public void onImageSuccess(Bitmap bitmap) {
                ImageObject.this.setBitmap(bitmap);
                listener.onImageCreation(ImageObject.this);

                ImageObject.this.size += bitmap.getByteCount();
            }

            @Override
            public void onImageError(Exception e) {
                listener.onImageFailure(e);
            }
        });
    }
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
    public int getSize() {
        return size;
    }

}
