package com.example.menureader;

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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.jetbrains.annotations.NotNull;

public class ResultsFragment extends Fragment {
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater,
                             @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
                             @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        LogHandler.m("View set to Results");
        View view = inflater.inflate(R.layout.results_fragment, container, false);
        //applyOffset(view);

        ImageView image = view.findViewById(R.id.menuImage);
        SharedViewModel svm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        new Menu(svm.getBitmap(), new Menu.OnMenuReadyListener() {
            @Override
            public void onMenuReady(Menu menu) {
                LogHandler.m("Menu received from camera: " + menu.getText());
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
            }

            @Override
            public void onMenuFailed(Exception e) {
                LogHandler.m("Camera: Menu failed", e);
            }
        });

        Button backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_results_to_menulist);
        });
        return view;
    }
    public void imageOnTouchListener(View v, MotionEvent event, Menu menu, ImageView image) {
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
//    private void applyOffset(View v) {
//        ViewCompat.setOnApplyWindowInsetsListener(v.findViewById(R.id.back_button), (view, insets) -> {
//            int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
//            params.bottomMargin = navBarHeight - 50;  // nav bar height + your desired padding
//            view.setLayoutParams(params);
//            return insets;
//        });
//    }
}
