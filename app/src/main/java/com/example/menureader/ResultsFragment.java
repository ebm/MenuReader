package com.example.menureader;

import android.app.Dialog;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import org.jetbrains.annotations.NotNull;

public class ResultsFragment extends Fragment {
    private Menu menu;
    private SharedViewModel svm;
    private boolean saveButtonHit;
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        LogHandler.m("View set to Results");
        View view = inflater.inflate(R.layout.results_fragment, container, false);

        svm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        handleResultMode(view);

        return view;
    }
    private void handleButtons(String mode, View view) {
        saveButtonHit = false;
        Button saveAndExitButton = view.findViewById(R.id.back_button);
        saveAndExitButton.setOnClickListener(v -> {
            LogHandler.m("Save button clicked with mode: " + mode);
            if (mode.equals("Photo")) {
                if (menu != null) {
                    svm.addMenu(menu);
                    NavHostFragment.findNavController(this).navigate(R.id.action_results_to_menulist);
                } else {
                    saveButtonHit = true;
                }
            } else if (mode.equals("MenuList")) {
                NavHostFragment.findNavController(this).navigate(R.id.action_results_to_menulist);
            }
        });

        Button discardButton = view.findViewById(R.id.discard_menu_button);
        discardButton.setOnClickListener(v -> {
            LogHandler.m("Discard button clicked with mode: " + mode);
            NavHostFragment.findNavController(this).navigate(R.id.action_results_to_menulist);
        });

        Button retakePhotoButton = view.findViewById(R.id.retake_photo_button);
        retakePhotoButton.setOnClickListener(v -> {
            LogHandler.m("Photo button clicked with mode: " + mode);
            NavHostFragment.findNavController(this).navigate(R.id.action_results_to_camera);
        });

        if (mode.equals("Photo")) {
            retakePhotoButton.setText("Retake Photo");
            discardButton.setText("Discard");
        } else if (mode.equals("MenuList")) {
            retakePhotoButton.setText("Photo");
            discardButton.setText("Back");
        }
    }
    private void handleResultMode(View view) {
        if (getArguments() == null) {
            LogHandler.m("Argument string is null. Should not be");
            return;
        }
        String mode = getArguments().getString("mode");
        if (mode.equals("Photo")) {
            initializePhoto(view);
        } else if (mode.equals("MenuList")){
            initializeMenu(view);
        } else {
            LogHandler.m("Mode set to something not handled");
            return;
        }
        handleButtons(mode, view);
    }
    private void initializeHyperlinks(ImageView image, Menu menu) {
        Bitmap bm = menu.getImageBitmap().copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);

        for (MenuLine ml : menu.getMenuList()) {
            Rect bounds = ml.getLineBounds();
            if (bounds != null) {
                canvas.drawRect(bounds, paint);
            } else {
                LogHandler.m("bounds is null for input: " + ml.getText());
            }
        }
        image.setImageBitmap(bm);
        image.setOnTouchListener((v, event) -> {
            imageOnTouchListener(v, event, menu, image);
            return true;
        });
        this.menu = menu;
    }
    private void initializeMenu(View view) {
        ImageView image = view.findViewById(R.id.menuImage);
        menu = null;
        if (svm.getMenu() == null) {
            LogHandler.m("Menu has not been populated");
            return;
        }
        initializeHyperlinks(image, svm.getMenu());
    }
    private void initializePhoto(View view) {
        ImageView image = view.findViewById(R.id.menuImage);
        menu = null;
        new Menu(svm.getBitmap(), new Menu.OnMenuReadyListener() {
            @Override
            public void onMenuReady(Menu menu) {
                LogHandler.m("Menu received from camera: "/* + menu.getText()*/);
                if (!isAdded()) {
                    LogHandler.m("Menu is ready but results_fragment was destroyed. Returning early");
                    return;
                }
                if (saveButtonHit) {
                    svm.addMenu(menu);
                    NavHostFragment.findNavController(ResultsFragment.this).navigate(R.id.action_results_to_menulist);
                }
                initializeHyperlinks(image, menu);
            }

            @Override
            public void onMenuFailed(Exception e) {
                LogHandler.m("Camera: Menu failed", e);
            }
        });
    }
    private void searchImage(String text) {
        ImageDeliver.searchFood(text, requireActivity(), new ImageDeliver.OnImageResultListener() {
            @Override
            public void onImageSuccess(Bitmap bitmap) {
                Dialog dialog = new Dialog(requireContext());

                ImageView imageView = new ImageView(requireContext());
                imageView.setImageBitmap(bitmap);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                dialog.setContentView(imageView);
                dialog.setTitle(text);
                dialog.show();
            }

            @Override
            public void onImageError(Exception e) {
                LogHandler.m("Error loading image", e);
            }
        });
    }
    private void imageOnTouchListener(View v, MotionEvent event, Menu menu, ImageView image) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            LogHandler.m("Touch Down");
            float tapX = event.getX();
            float tapY = event.getY();

            float[] imageCoords = getImageCoordinates(image, tapX, tapY);

            // Linear search is okay on regularly sized menus (50-100 items)
            for (MenuLine ml : menu.getMenuList()) {
                Rect bounds = ml.getLineBounds();
                if (bounds != null && bounds.contains((int) imageCoords[0], (int) imageCoords[1])) {
                    LogHandler.m("Touched down on " + ml.getText());
                    searchImage(ml.getText());
                    break;
                }
            }
        }
    }
    private float[] getImageCoordinates(ImageView imageView, float tapX, float tapY) {
        Drawable drawable = imageView.getDrawable();
        if (drawable == null) return new float[]{0, 0};

        // Get the image matrix that maps image coords to view coords
        Matrix inverse = new Matrix();
        imageView.getImageMatrix().invert(inverse);

        // Apply the inverse to get image coordinates from tap coordinates
        float[] point = new float[]{tapX, tapY};
        inverse.mapPoints(point);
        return point;
    }
}
