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
    public ImageObject(String imageURL, Bitmap bitmap) {
        this.imageURL = imageURL;
        this.bitmap = bitmap;
        size = Controller.getStringSize(imageURL) + bitmap.getByteCount();
    }

    /**
     * Creates an ImageObject given a string url of an image. Needs a listener to handle
     * the callback when ImageObject has succeeded/failed
     *
     * @param imageURL
     * @param activity
     * @param listener
     */
    public ImageObject(String imageURL, Activity activity, OnImageObjectSuccess listener) {
        this.size = Controller.getStringSize(imageURL);
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
