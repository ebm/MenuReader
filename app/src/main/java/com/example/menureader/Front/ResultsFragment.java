package com.example.menureader.Front;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.menureader.*;
import com.example.menureader.Handling.ImageDeliver;
import com.example.menureader.Handling.Menu;
import com.example.menureader.Handling.MenuLine;
import org.jetbrains.annotations.NotNull;

public class ResultsFragment extends Fragment {
    private static final float TAP_SLOP_SQ = 16f * 16f; // pixels²

    private Menu menu;
    private SharedViewModel svm;
    private boolean saveButtonHit;

    /**
     * Function gets called before fragment loads
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return
     */
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

    /**
     * Buttons do different things based off mode. Function initializes buttons and their
     * uses depending on the mode.
     *
     * @param mode String with the value "Photo" or "MenuList"
     * @param view
     */
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

    /**
     * Handles mode argument from initialization
     *
     * @param view
     */
    private void handleResultMode(View view) {
        if (getArguments() == null) {
            LogHandler.m("Argument string is null. Should not be");
            return;
        }
        String mode = getArguments().getString("mode");
        assert mode != null;
        if (mode.equals("Photo")) {
            initializePhoto(view);
        } else if (mode.equals("MenuList")) {
            initializeMenu(view);
        } else {
            LogHandler.m("Mode set to something not handled");
            return;
        }
        handleButtons(mode, view);
    }

    /**
     * Creates outlines for all text OCR captured and sets up pinch-zoom/pan touch handling.
     */
    private void initializeHyperlinks(ImageView image, Menu menu) {
        image.setImageBitmap(drawBoundingBoxes(menu));
        this.menu = menu;

        Matrix imageMatrix = setupFitCenterMatrix(image);
        ScaleGestureDetector scaleDetector = buildScaleDetector(image, imageMatrix);
        setupTouchListener(image, imageMatrix, scaleDetector, menu);
    }

    /** Returns a copy of the menu's image with red bounding boxes drawn around each menu item. */
    private Bitmap drawBoundingBoxes(Menu menu) {
        Bitmap bm = menu.getImageBitmap().copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        for (MenuLine ml : menu.getMenuList()) {
            Rect bounds = ml.getLineBounds();
            if (bounds != null) canvas.drawRect(bounds, paint);
            else LogHandler.m("bounds is null for input: " + ml.getText());
        }
        return bm;
    }

    /**
     * Switches the ImageView to MATRIX scale type and initializes the matrix to fitCenter
     * once the view has been laid out. Returns the matrix for subsequent manipulation.
     */
    private Matrix setupFitCenterMatrix(ImageView image) {
        image.setScaleType(ImageView.ScaleType.MATRIX);
        Matrix imageMatrix = new Matrix();
        image.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                image.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (image.getDrawable() == null) return;
                float iW = image.getDrawable().getIntrinsicWidth();
                float iH = image.getDrawable().getIntrinsicHeight();
                float vW = image.getWidth();
                float vH = image.getHeight();
                float scale = Math.min(vW / iW, vH / iH);
                imageMatrix.setScale(scale, scale);
                imageMatrix.postTranslate((vW - iW * scale) / 2f, (vH - iH * scale) / 2f);
                image.setImageMatrix(imageMatrix);
            }
        });
        return imageMatrix;
    }

    /** Creates a ScaleGestureDetector that applies pinch-zoom to the given matrix. */
    private ScaleGestureDetector buildScaleDetector(ImageView image, Matrix matrix) {
        return new ScaleGestureDetector(requireContext(),
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector d) {
                        matrix.postScale(d.getScaleFactor(), d.getScaleFactor(),
                                d.getFocusX(), d.getFocusY());
                        image.setImageMatrix(matrix);
                        return true;
                    }
                });
    }

    /** Attaches a touch listener that handles pinch-zoom, pan, and tap-to-search. */
    private void setupTouchListener(ImageView image, Matrix matrix,
                                    ScaleGestureDetector scaleDetector, Menu menu) {
        float[] lastTouch = new float[2];
        float[] downTouch = new float[2];
        boolean[] wasMultiTouch = {false};

        image.setOnTouchListener((v, event) -> {
            scaleDetector.onTouchEvent(event);
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouch[0] = downTouch[0] = event.getX();
                    lastTouch[1] = downTouch[1] = event.getY();
                    wasMultiTouch[0] = false;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    wasMultiTouch[0] = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (!wasMultiTouch[0] && event.getPointerCount() == 1) {
                        matrix.postTranslate(event.getX() - lastTouch[0], event.getY() - lastTouch[1]);
                        image.setImageMatrix(matrix);
                    }
                    lastTouch[0] = event.getX();
                    lastTouch[1] = event.getY();
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    // Prevent a position jump when lifting one finger after a pinch.
                    int remaining = event.getActionIndex() == 0 ? 1 : 0;
                    lastTouch[0] = event.getX(remaining);
                    lastTouch[1] = event.getY(remaining);
                    break;
                case MotionEvent.ACTION_UP:
                    float dx = event.getX() - downTouch[0];
                    float dy = event.getY() - downTouch[1];
                    if (!wasMultiTouch[0] && dx * dx + dy * dy < TAP_SLOP_SQ) {
                        imageOnTouchListener(v, event, menu, image);
                    }
                    break;
            }
            return true;
        });
    }

    /**
     * Initializes menu. Mode should be "MenuList" at this point and svm.getMenu()
     * should have a menu added to the shared variable.
     *
     * @param view
     */
    private void initializeMenu(View view) {
        assert (svm.getMenu() != null);
        ImageView image = view.findViewById(R.id.menuImage);
        menu = null;
        initializeHyperlinks(image, svm.getMenu());
    }

    /**
     * Initialize menu. Mode should be "Photo" at this point and svm.getBitmap()
     * should not be null
     *
     * @param view
     */
    private void initializePhoto(View view) {
        assert (svm.getBitmap() != null);
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

    /**
     * Searches an image given some string text. Opens a swipeable gallery bottom sheet.
     *
     * @param text query
     */
    private void searchImage(String text) {
        ImageViewerDialog dialog = ImageViewerDialog.newInstance(text);

        // Create ImageDeliver before show() so cache-hit images (delivered synchronously)
        // are queued in the dialog before onCreateView runs.
        new ImageDeliver(text, requireActivity(), new ImageDeliver.OnImageResultListener() {
            @Override
            public void onImageSuccess(Bitmap bitmap) {
                dialog.addImage(bitmap);
            }

            @Override
            public void onImageError(Exception e) {
                LogHandler.m("Error loading image", e);
            }
        });

        dialog.show(getParentFragmentManager(), "image_viewer");
    }

    /**
     * Handles a screen tap. If tap is in the bounds of a hyperlink, open the link.
     * TODO: Remove rectangles. Do checks by coordinates. Would need to update how MenuLine is created
     *
     * @param v
     * @param event
     * @param menu
     * @param image
     */
    private void imageOnTouchListener(View v, MotionEvent event, Menu menu, ImageView image) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            LogHandler.m("Touch Up");
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

    /**
     * Image does not take up entire screen. Need Matrix calculations to convert coordinates of
     * tap to coordinates on image.
     *
     * @param imageView
     * @param tapX
     * @param tapY
     * @return
     */
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
