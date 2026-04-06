package com.example.menureader.Handling;

import android.app.Activity;
import android.graphics.Bitmap;

public class ImageObject {


    public interface OnImageObjectSuccess {
         public void onImageCreation(ImageObject imageObject);
         public void onImageFailure(Exception e);
    }
    private String imageURL;
    private Bitmap bitmap;
    public ImageObject(String imageURL, Bitmap bitmap) {
        this.imageURL = imageURL;
        this.bitmap = bitmap;
    }
    public ImageObject(String imageURL, Activity activity, OnImageObjectSuccess listener) {
        ImageDeliver.getBitmapFromUrlThread(imageURL, activity, new ImageDeliver.OnImageResultListener() {
            @Override
            public void onImageSuccess(Bitmap bitmap) {
                ImageObject.this.setBitmap(bitmap);
                listener.onImageCreation(ImageObject.this);
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
}
